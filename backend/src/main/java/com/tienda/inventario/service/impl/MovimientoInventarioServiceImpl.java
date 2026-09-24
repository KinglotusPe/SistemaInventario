package com.tienda.inventario.service.impl;

import com.tienda.inventario.dto.ItemMovimientoDTO;
import com.tienda.inventario.dto.MovimientoRequestDTO;
import com.tienda.inventario.exception.ResourceNotFoundException;
import com.tienda.inventario.exception.StockInsuficienteException;
import com.tienda.inventario.entity.*;
import com.tienda.inventario.repository.*;
import com.tienda.inventario.service.IMovimientoInventarioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class MovimientoInventarioServiceImpl implements IMovimientoInventarioService {

    private final MovimientoInventarioRepository movimientoRepository;
    private final DetalleMovimientoRepository detalleMovimientoRepository;
    private final StockAlmacenRepository stockAlmacenRepository;
    private final ProductoRepository productoRepository;
    private final AlmacenRepository almacenRepository;
    private final TipoMovimientoRepository tipoMovimientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaSistemaRepository auditoriaRepository;

    public MovimientoInventarioServiceImpl(MovimientoInventarioRepository movimientoRepository,
                                          DetalleMovimientoRepository detalleMovimientoRepository,
                                          StockAlmacenRepository stockAlmacenRepository,
                                          ProductoRepository productoRepository,
                                          AlmacenRepository almacenRepository,
                                          TipoMovimientoRepository tipoMovimientoRepository,
                                          UsuarioRepository usuarioRepository,
                                          AuditoriaSistemaRepository auditoriaRepository) {
        this.movimientoRepository = movimientoRepository;
        this.detalleMovimientoRepository = detalleMovimientoRepository;
        this.stockAlmacenRepository = stockAlmacenRepository;
        this.productoRepository = productoRepository;
        this.almacenRepository = almacenRepository;
        this.tipoMovimientoRepository = tipoMovimientoRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaRepository = auditoriaRepository;
    }

    /**
     * Registra un movimiento de almacén garantizando atomicidad (ACID) y prevención
     * de condiciones de carrera (Race Conditions) mediante Bloqueo Pesimista (PESSIMISTIC_WRITE).
     */
    @Override
    @Transactional(
        propagation = Propagation.REQUIRED,
        isolation = Isolation.READ_COMMITTED,
        rollbackFor = Exception.class
    )
    public MovimientoInventario registrarMovimiento(MovimientoRequestDTO request) {
        // 1. Obtener usuario autenticado del contexto de seguridad
        Usuario usuario = getUsuarioAutenticado();

        // 2. Validar privilegios RBAC según la naturaleza de la operación
        String codigoTipo = request.getCodigoTipo().trim().toUpperCase();
        validarPrivilegiosPorTipo(codigoTipo);

        // 3. Validar tipo de movimiento en la base de datos
        TipoMovimiento tipo = tipoMovimientoRepository.findByCodigo(codigoTipo)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de movimiento no registrado en el sistema: " + codigoTipo));

        // 4. Validar almacén de origen
        Almacen almacenOrigen = almacenRepository.findById(request.getIdAlmacenOrigen())
                .orElseThrow(() -> new ResourceNotFoundException("Almacén de origen no encontrado con ID: " + request.getIdAlmacenOrigen()));

        // 5. Validar almacén de destino (obligatorio si es TRASLADO)
        Almacen almacenDestino = null;
        if ("TRASLADO".equalsIgnoreCase(codigoTipo)) {
            if (request.getIdAlmacenDestino() == null) {
                throw new IllegalArgumentException("Para movimientos de TRASLADO es obligatorio indicar el almacén de destino.");
            }
            if (request.getIdAlmacenOrigen().equals(request.getIdAlmacenDestino())) {
                throw new IllegalArgumentException("El almacén de destino no puede ser idéntico al almacén de origen.");
            }
            almacenDestino = almacenRepository.findById(request.getIdAlmacenDestino())
                    .orElseThrow(() -> new ResourceNotFoundException("Almacén de destino no encontrado con ID: " + request.getIdAlmacenDestino()));
        }
        final Almacen destinoFinal = almacenDestino;

        // 6. Normalizar partidas de ítems (soporta llamada por lista o llamada singular)
        List<ItemMovimientoDTO> items = new ArrayList<>();
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            items.addAll(request.getItems());
        } else if (request.getIdProducto() != null && request.getCantidad() != null) {
            items.add(new ItemMovimientoDTO(request.getIdProducto(), request.getCantidad(), null));
        } else {
            throw new IllegalArgumentException("Debe especificar al menos un producto y cantidad para el movimiento.");
        }

        // 7. Instanciar la cabecera del movimiento de inventario
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setNumeroMovimiento(generarCorrelativoMovimiento());
        movimiento.setTipoMovimiento(tipo);
        movimiento.setAlmacenOrigen(almacenOrigen);
        movimiento.setAlmacenDestino(almacenDestino);
        movimiento.setUsuario(usuario);
        movimiento.setMotivo(request.getMotivo().trim());

        // 8. Procesar cada ítem con Control de Concurrencia Pesimista en StockAlmacen
        for (ItemMovimientoDTO itemDto : items) {
            Producto producto = productoRepository.findById(itemDto.getIdProducto())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + itemDto.getIdProducto()));

            if (!Boolean.TRUE.equals(producto.getEstado())) {
                throw new IllegalArgumentException("El producto '" + producto.getNombre() + "' (SKU: " + producto.getCodigoSku() + ") está inactivo.");
            }

            int cantidad = itemDto.getCantidad();
            if (cantidad <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a 0 para el producto: " + producto.getNombre());
            }

            BigDecimal costoUnitario = itemDto.getCostoUnitario() != null ? itemDto.getCostoUnitario() : producto.getPrecioCosto();
            BigDecimal subtotal = costoUnitario.multiply(BigDecimal.valueOf(cantidad));

            // Bloquear la fila de stock en almacén origen
            StockAlmacen stockOrigen = stockAlmacenRepository.findByProductoAndAlmacenWithLock(producto.getIdProducto(), almacenOrigen.getIdAlmacen())
                    .orElseGet(() -> {
                        StockAlmacen nuevoStock = new StockAlmacen(producto, almacenOrigen, 0);
                        return stockAlmacenRepository.save(nuevoStock);
                    });

            int stockActualOrigen = stockOrigen.getStockActual();

            switch (codigoTipo) {
                case "ENTRADA_COMPRA":
                case "AJUSTE_POSITIVO":
                    stockOrigen.setStockActual(stockActualOrigen + cantidad);
                    stockAlmacenRepository.save(stockOrigen);
                    break;

                case "SALIDA_VENTA":
                case "AJUSTE_NEGATIVO":
                    if (stockActualOrigen < cantidad) {
                        throw new StockInsuficienteException(
                                String.format("Stock insuficiente en '%s' para producto '%s' (SKU: %s). Disponible: %d, Solicitado: %d.",
                                        almacenOrigen.getNombre(), producto.getNombre(), producto.getCodigoSku(), stockActualOrigen, cantidad)
                        );
                    }
                    stockOrigen.setStockActual(stockActualOrigen - cantidad);
                    stockAlmacenRepository.save(stockOrigen);
                    break;

                case "TRASLADO":
                    if (stockActualOrigen < cantidad) {
                        throw new StockInsuficienteException(
                                String.format("Traslado denegado: Stock insuficiente en almacén origen '%s' para '%s'. Disponible: %d, Requerido: %d.",
                                        almacenOrigen.getNombre(), producto.getNombre(), stockActualOrigen, cantidad)
                        );
                    }
                    // Decrementar origen
                    stockOrigen.setStockActual(stockActualOrigen - cantidad);
                    stockAlmacenRepository.save(stockOrigen);

                    // Bloquear e incrementar destino
                    StockAlmacen stockDestino = stockAlmacenRepository.findByProductoAndAlmacenWithLock(producto.getIdProducto(), destinoFinal.getIdAlmacen())
                            .orElseGet(() -> {
                                StockAlmacen nuevoDestino = new StockAlmacen(producto, destinoFinal, 0);
                                return stockAlmacenRepository.save(nuevoDestino);
                            });
                    stockDestino.setStockActual(stockDestino.getStockActual() + cantidad);
                    stockAlmacenRepository.save(stockDestino);
                    break;

                default:
                    throw new IllegalArgumentException("Código de tipo de movimiento no soportado: " + codigoTipo);
            }

            // Crear y asociar detalle
            DetalleMovimiento detalle = new DetalleMovimiento(producto, cantidad, costoUnitario, subtotal);
            movimiento.agregarDetalle(detalle);
        }

        // 9. Persistir cabecera y detalles en cascada
        MovimientoInventario guardado = movimientoRepository.save(movimiento);

        // 10. Registrar auditoría del sistema
        auditoriaRepository.save(new AuditoriaSistema(
                usuario.getIdUsuario(),
                usuario.getUsername(),
                "MOVIMIENTO_" + codigoTipo,
                "movimientos_inventario",
                guardado.getIdMovimiento(),
                "Registro de movimiento " + guardado.getNumeroMovimiento() + " en almacén " + almacenOrigen.getNombre(),
                "127.0.0.1"
        ));

        return guardado;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovimientoInventario> listarMovimientos(Pageable pageable) {
        return movimientoRepository.findAllByOrderByFechaMovimientoDesc(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public MovimientoInventario obtenerPorId(Integer idMovimiento) {
        return movimientoRepository.findByIdWithDetalles(idMovimiento)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado con ID: " + idMovimiento));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoInventario> listarPorRangoFechas(LocalDateTime inicio, LocalDateTime fin) {
        return movimientoRepository.findMovimientosPorRangoFechas(inicio, fin);
    }

    /**
     * Valida permisos RBAC granulares contra el usuario autenticado.
     */
    private void validarPrivilegiosPorTipo(String codigoTipo) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return; // Permite flujos batch internos si los hubiera
        }

        boolean esAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));
        if (esAdmin) {
            return; // Administrador tiene bypass de privilegios
        }

        switch (codigoTipo) {
            case "AJUSTE_POSITIVO":
            case "AJUSTE_NEGATIVO":
                if (auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("INVENTARIO_AJUSTAR"))) {
                    throw new AccessDeniedException("Acceso Denegado (403): Se requiere el privilegio 'INVENTARIO_AJUSTAR' para ejecutar ajustes físicos.");
                }
                break;

            case "TRASLADO":
                if (auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("INVENTARIO_TRASLADAR"))) {
                    throw new AccessDeniedException("Acceso Denegado (403): Se requiere el privilegio 'INVENTARIO_TRASLADAR' para realizar transferencias entre almacenes.");
                }
                break;

            case "ENTRADA_COMPRA":
                if (auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("INVENTARIO_ENTRADA"))) {
                    throw new AccessDeniedException("Acceso Denegado (403): Se requiere el privilegio 'INVENTARIO_ENTRADA' para ingresar mercancía.");
                }
                break;

            case "SALIDA_VENTA":
                if (auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("INVENTARIO_SALIDA"))) {
                    throw new AccessDeniedException("Acceso Denegado (403): Se requiere el privilegio 'INVENTARIO_SALIDA' para despachar mercancía.");
                }
                break;
        }
    }

    private Usuario getUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null && !"anonymousUser".equals(auth.getName())) {
            return usuarioRepository.findByUsername(auth.getName())
                    .orElseGet(() -> usuarioRepository.findByUsername("admin").orElse(null));
        }
        return usuarioRepository.findByUsername("admin").orElse(null);
    }

    private synchronized String generarCorrelativoMovimiento() {
        String maxNumero = movimientoRepository.findMaxNumeroMovimiento();
        int siguiente = 1;
        if (maxNumero != null && maxNumero.startsWith("MOV-")) {
            try {
                String numStr = maxNumero.replace("MOV-", "");
                siguiente = Integer.parseInt(numStr) + 1;
            } catch (NumberFormatException ignored) {}
        }
        return String.format("MOV-%06d", siguiente);
    }
}

