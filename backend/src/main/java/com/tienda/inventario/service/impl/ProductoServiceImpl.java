package com.tienda.inventario.service.impl;

import com.tienda.inventario.dto.ProductoDTO;
import com.tienda.inventario.dto.StockCriticoDTO;
import com.tienda.inventario.exception.ResourceNotFoundException;
import com.tienda.inventario.entity.*;
import com.tienda.inventario.repository.*;
import com.tienda.inventario.service.IProductoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductoServiceImpl implements IProductoService {

    private final ProductoRepository productoRepository;
    private final StockAlmacenRepository stockAlmacenRepository;
    private final CategoriaRepository categoriaRepository;
    private final MarcaRepository marcaRepository;
    private final UnidadMedidaRepository unidadMedidaRepository;
    private final AlmacenRepository almacenRepository;

    public ProductoServiceImpl(ProductoRepository productoRepository,
                               StockAlmacenRepository stockAlmacenRepository,
                               CategoriaRepository categoriaRepository,
                               MarcaRepository marcaRepository,
                               UnidadMedidaRepository unidadMedidaRepository,
                               AlmacenRepository almacenRepository) {
        this.productoRepository = productoRepository;
        this.stockAlmacenRepository = stockAlmacenRepository;
        this.categoriaRepository = categoriaRepository;
        this.marcaRepository = marcaRepository;
        this.unidadMedidaRepository = unidadMedidaRepository;
        this.almacenRepository = almacenRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> listarTodos() {
        return productoRepository.findAll().stream()
                .filter(Producto::getEstado)
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoDTO> listarPaginado(Pageable pageable) {
        return productoRepository.findByEstadoTrue(pageable)
                .map(this::convertirADto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoDTO> buscarPorFiltro(String filtro, Pageable pageable) {
        if (filtro == null || filtro.trim().isEmpty()) {
            return listarPaginado(pageable);
        }
        return productoRepository.buscarPorNombreOSku(filtro.trim(), pageable)
                .map(this::convertirADto);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoDTO buscarPorId(Integer id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
        return convertirADto(producto);
    }

    @Override
    @Transactional
    public ProductoDTO guardar(ProductoDTO dto) {
        if (productoRepository.findByCodigoSku(dto.getCodigoSku()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un producto con el SKU: " + dto.getCodigoSku());
        }

        Categoria categoria = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + dto.getIdCategoria()));

        int idMarca = (dto.getIdMarca() != null && dto.getIdMarca() > 0) ? dto.getIdMarca() : 1;
        Marca marca = marcaRepository.findById(idMarca)
                .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada con ID: " + idMarca));

        int idUnidad = (dto.getIdUnidad() != null && dto.getIdUnidad() > 0) ? dto.getIdUnidad() : 1;
        UnidadMedida unidad = unidadMedidaRepository.findById(idUnidad)
                .orElseThrow(() -> new ResourceNotFoundException("Unidad de medida no encontrada con ID: " + idUnidad));

        Producto producto = new Producto();
        producto.setCodigoSku(dto.getCodigoSku().trim().toUpperCase());
        producto.setCodigoBarras(dto.getCodigoBarras());
        producto.setNombre(dto.getNombre().trim());
        producto.setDescripcion(dto.getDescripcion());
        producto.setCategoria(categoria);
        producto.setMarca(marca);
        producto.setUnidadMedida(unidad);
        producto.setPrecioCosto(dto.getPrecioCosto() != null ? dto.getPrecioCosto() : BigDecimal.ZERO);
        producto.setPrecioVenta(dto.getPrecioVenta() != null ? dto.getPrecioVenta() : dto.getPrecioUnitario());
        producto.setStockMinimo(dto.getStockMinimo() != null ? dto.getStockMinimo() : 5);
        producto.setEstado(true);

        Producto guardado = productoRepository.save(producto);

        // Inicializar stock en el almacén principal (ID 1)
        almacenRepository.findById(1).ifPresent(almacen -> {
            int stockInicial = (dto.getStockActual() != null && dto.getStockActual() >= 0) ? dto.getStockActual() : 0;
            StockAlmacen stockAlmacen = new StockAlmacen(guardado, almacen, stockInicial);
            stockAlmacenRepository.save(stockAlmacen);
        });

        return convertirADto(guardado);
    }

    @Override
    @Transactional
    public ProductoDTO actualizar(Integer id, ProductoDTO dto) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        Categoria categoria = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + dto.getIdCategoria()));

        if (dto.getIdMarca() != null) {
            Marca marca = marcaRepository.findById(dto.getIdMarca())
                    .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada con ID: " + dto.getIdMarca()));
            producto.setMarca(marca);
        }

        if (dto.getIdUnidad() != null) {
            UnidadMedida unidad = unidadMedidaRepository.findById(dto.getIdUnidad())
                    .orElseThrow(() -> new ResourceNotFoundException("Unidad de medida no encontrada con ID: " + dto.getIdUnidad()));
            producto.setUnidadMedida(unidad);
        }

        producto.setNombre(dto.getNombre().trim());
        producto.setDescripcion(dto.getDescripcion());
        producto.setCodigoBarras(dto.getCodigoBarras());
        producto.setCategoria(categoria);
        if (dto.getPrecioCosto() != null) {
            producto.setPrecioCosto(dto.getPrecioCosto());
        }
        if (dto.getPrecioVenta() != null) {
            producto.setPrecioVenta(dto.getPrecioVenta());
        } else if (dto.getPrecioUnitario() != null) {
            producto.setPrecioVenta(dto.getPrecioUnitario());
        }
        producto.setStockMinimo(dto.getStockMinimo());

        Producto actualizado = productoRepository.save(producto);
        return convertirADto(actualizado);
    }

    @Override
    @Transactional
    public void eliminarLogico(Integer id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
        producto.setEstado(false);
        productoRepository.save(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockCriticoDTO> obtenerProductosStockCritico() {
        return stockAlmacenRepository.findStockCritico().stream()
                .map(s -> new StockCriticoDTO(
                        s.getProducto().getIdProducto(),
                        s.getProducto().getCodigoSku(),
                        s.getProducto().getNombre(),
                        s.getProducto().getCategoria().getNombre(),
                        s.getStockActual(),
                        s.getProducto().getStockMinimo(),
                        s.getProducto().getPrecioVenta()
                ))
                .collect(Collectors.toList());
    }

    private ProductoDTO convertirADto(Producto entity) {
        ProductoDTO dto = new ProductoDTO();
        dto.setIdProducto(entity.getIdProducto());
        dto.setCodigoSku(entity.getCodigoSku());
        dto.setCodigoBarras(entity.getCodigoBarras());
        dto.setNombre(entity.getNombre());
        dto.setDescripcion(entity.getDescripcion());
        dto.setIdCategoria(entity.getCategoria().getIdCategoria());
        dto.setNombreCategoria(entity.getCategoria().getNombre());
        if (entity.getMarca() != null) {
            dto.setIdMarca(entity.getMarca().getIdMarca());
            dto.setNombreMarca(entity.getMarca().getNombre());
        }
        if (entity.getUnidadMedida() != null) {
            dto.setIdUnidad(entity.getUnidadMedida().getIdUnidad());
            dto.setNombreUnidad(entity.getUnidadMedida().getNombre());
        }
        dto.setPrecioCosto(entity.getPrecioCosto());
        dto.setPrecioVenta(entity.getPrecioVenta());
        dto.setStockMinimo(entity.getStockMinimo());
        dto.setEstado(entity.getEstado());

        // Sumar existencias en todos los almacenes
        int totalStock = stockAlmacenRepository.findByProducto_IdProducto(entity.getIdProducto())
                .stream()
                .mapToInt(StockAlmacen::getStockActual)
                .sum();
        dto.setStockActual(totalStock);

        return dto;
    }
}

