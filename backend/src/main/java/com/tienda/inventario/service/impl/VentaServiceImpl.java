package com.tienda.inventario.service.impl;

import com.tienda.inventario.dto.ItemVentaDTO;
import com.tienda.inventario.dto.VentaRequestDTO;
import com.tienda.inventario.exception.ResourceNotFoundException;
import com.tienda.inventario.exception.StockInsuficienteException;
import com.tienda.inventario.entity.*;
import com.tienda.inventario.repository.*;
import com.tienda.inventario.service.IVentaService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class VentaServiceImpl implements IVentaService {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final ClienteRepository clienteRepository;
    private final AlmacenRepository almacenRepository;
    private final ProductoRepository productoRepository;
    private final StockAlmacenRepository stockAlmacenRepository;
    private final UsuarioRepository usuarioRepository;
    private final TipoMovimientoRepository tipoMovimientoRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final AuditoriaSistemaRepository auditoriaRepository;

    public VentaServiceImpl(VentaRepository ventaRepository,
                            DetalleVentaRepository detalleVentaRepository,
                            ClienteRepository clienteRepository,
                            AlmacenRepository almacenRepository,
                            ProductoRepository productoRepository,
                            StockAlmacenRepository stockAlmacenRepository,
                            UsuarioRepository usuarioRepository,
                            TipoMovimientoRepository tipoMovimientoRepository,
                            MovimientoInventarioRepository movimientoRepository,
                            AuditoriaSistemaRepository auditoriaRepository) {
        this.ventaRepository = ventaRepository;
        this.detalleVentaRepository = detalleVentaRepository;
        this.clienteRepository = clienteRepository;
        this.almacenRepository = almacenRepository;
        this.productoRepository = productoRepository;
        this.stockAlmacenRepository = stockAlmacenRepository;
        this.usuarioRepository = usuarioRepository;
        this.tipoMovimientoRepository = tipoMovimientoRepository;
        this.movimientoRepository = movimientoRepository;
        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    @Transactional(
        propagation = Propagation.REQUIRED,
        isolation = Isolation.READ_COMMITTED,
        rollbackFor = Exception.class
    )
    public Venta registrarVenta(VentaRequestDTO request) {
        Usuario usuario = getUsuarioAutenticado();

        Cliente cliente = clienteRepository.findById(request.getIdCliente())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + request.getIdCliente()));

        Almacen almacen = almacenRepository.findById(request.getIdAlmacen())
                .orElseThrow(() -> new ResourceNotFoundException("Almacén no encontrado con ID: " + request.getIdAlmacen()));

        String serie = switch (request.getTipoComprobante().toUpperCase()) {
            case "FACTURA" -> "F001";
            case "BOLETA" -> "B001";
            default -> "NV01";
        };

        String correlativo = generarSiguienteCorrelativo(serie);

        Venta venta = new Venta();
        venta.setTipoComprobante(request.getTipoComprobante().toUpperCase());
        venta.setSerie(serie);
        venta.setNumeroCorrelativo(correlativo);
        venta.setCliente(cliente);
        venta.setUsuario(usuario);
        venta.setAlmacen(almacen);
        venta.setMetodoPago(request.getMetodoPago().toUpperCase());
        venta.setEstado("EMITIDA");

        BigDecimal subtotalAcumulado = BigDecimal.ZERO;

        for (ItemVentaDTO item : request.getDetalles()) {
            Producto producto = productoRepository.findById(item.getIdProducto())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + item.getIdProducto()));

            if (!Boolean.TRUE.equals(producto.getEstado())) {
                throw new IllegalArgumentException("No se puede vender un producto inactivo: " + producto.getNombre());
            }

            // Bloqueo pesimista para evitar carreras de concurrencia
            StockAlmacen stock = stockAlmacenRepository.findByProductoAndAlmacenWithLock(producto.getIdProducto(), almacen.getIdAlmacen())
                    .orElseThrow(() -> new StockInsuficienteException("No existen registros de stock para el producto " + producto.getNombre() + " en almacén " + almacen.getNombre()));

            if (stock.getStockActual() < item.getCantidad()) {
                throw new StockInsuficienteException(
                        String.format("Stock insuficiente para venta de '%s' (SKU: %s). Disponible: %d, Solicitado: %d.",
                                producto.getNombre(), producto.getCodigoSku(), stock.getStockActual(), item.getCantidad())
                );
            }

            // Actualizar stock atómicamente
            stock.setStockActual(stock.getStockActual() - item.getCantidad());
            stockAlmacenRepository.save(stock);

            BigDecimal precioUnitario = producto.getPrecioVenta();
            BigDecimal descuento = item.getDescuento() != null ? item.getDescuento() : BigDecimal.ZERO;
            BigDecimal precioConDescuento = precioUnitario.subtract(descuento);
            BigDecimal itemSubtotal = precioConDescuento.multiply(BigDecimal.valueOf(item.getCantidad()));

            subtotalAcumulado = subtotalAcumulado.add(itemSubtotal);

            DetalleVenta detalle = new DetalleVenta(producto, item.getCantidad(), precioUnitario, descuento, itemSubtotal);
            venta.agregarDetalle(detalle);
        }

        BigDecimal subtotalGravado = subtotalAcumulado.divide(BigDecimal.valueOf(1.18), 2, RoundingMode.HALF_UP);
        BigDecimal igv = subtotalAcumulado.subtract(subtotalGravado);

        venta.setSubtotal(subtotalGravado);
        venta.setIgv(igv);
        venta.setTotal(subtotalAcumulado);

        Venta ventaGuardada = ventaRepository.save(venta);

        // Registro de auditoría
        auditoriaRepository.save(new AuditoriaSistema(
                usuario.getIdUsuario(),
                usuario.getUsername(),
                "VENTA_EMITIDA",
                "ventas",
                ventaGuardada.getIdVenta(),
                "Emisión de comprobante " + serie + "-" + correlativo + " Total: S/ " + ventaGuardada.getTotal(),
                "127.0.0.1"
        ));

        return ventaGuardada;
    }

    @Override
    @Transactional(
        propagation = Propagation.REQUIRED,
        isolation = Isolation.READ_COMMITTED,
        rollbackFor = Exception.class
    )
    public Venta anularVenta(Integer idVenta, String motivo) {
        Usuario usuario = getUsuarioAutenticado();

        Venta venta = ventaRepository.findByIdWithDetalles(idVenta)
                .orElseThrow(() -> new ResourceNotFoundException("Comprobante de venta no encontrado con ID: " + idVenta));

        if ("ANULADA".equalsIgnoreCase(venta.getEstado())) {
            throw new IllegalArgumentException("La venta " + venta.getSerie() + "-" + venta.getNumeroCorrelativo() + " ya se encuentra anulada.");
        }

        // Devolver las existencias físicas al stock del almacén
        for (DetalleVenta detalle : venta.getDetalles()) {
            Producto producto = detalle.getProducto();
            StockAlmacen stock = stockAlmacenRepository.findByProductoAndAlmacenWithLock(producto.getIdProducto(), venta.getAlmacen().getIdAlmacen())
                    .orElseGet(() -> new StockAlmacen(producto, venta.getAlmacen(), 0));

            stock.setStockActual(stock.getStockActual() + detalle.getCantidad());
            stockAlmacenRepository.save(stock);
        }

        venta.setEstado("ANULADA");
        Venta actualizada = ventaRepository.save(venta);

        auditoriaRepository.save(new AuditoriaSistema(
                usuario.getIdUsuario(),
                usuario.getUsername(),
                "VENTA_ANULADA",
                "ventas",
                actualizada.getIdVenta(),
                "Anulación de comprobante " + venta.getSerie() + "-" + venta.getNumeroCorrelativo() + " Motivo: " + motivo,
                "127.0.0.1"
        ));

        return actualizada;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Venta> listarVentas() {
        return ventaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Venta obtenerPorId(Integer idVenta) {
        return ventaRepository.findByIdWithDetalles(idVenta)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con ID: " + idVenta));
    }

    private synchronized String generarSiguienteCorrelativo(String serie) {
        String maxCorrelativo = ventaRepository.findMaxCorrelativoBySerie(serie);
        int siguiente = 1;
        if (maxCorrelativo != null && !maxCorrelativo.isBlank()) {
            try {
                siguiente = Integer.parseInt(maxCorrelativo) + 1;
            } catch (NumberFormatException ignored) {}
        }
        return String.format("%08d", siguiente);
    }

    private Usuario getUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null && !"anonymousUser".equals(auth.getName())) {
            return usuarioRepository.findByUsername(auth.getName())
                    .orElseGet(() -> usuarioRepository.findByUsername("admin").orElse(null));
        }
        return usuarioRepository.findByUsername("admin").orElse(null);
    }
}

