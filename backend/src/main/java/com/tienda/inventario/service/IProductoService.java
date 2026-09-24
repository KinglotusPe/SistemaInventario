package com.tienda.inventario.service;

import com.tienda.inventario.dto.ProductoDTO;
import com.tienda.inventario.dto.StockCriticoDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IProductoService {

    List<ProductoDTO> listarTodos();

    Page<ProductoDTO> listarPaginado(Pageable pageable);

    Page<ProductoDTO> buscarPorFiltro(String filtro, Pageable pageable);

    ProductoDTO buscarPorId(Integer id);

    ProductoDTO guardar(ProductoDTO dto);

    ProductoDTO actualizar(Integer id, ProductoDTO dto);

    void eliminarLogico(Integer id);

    List<StockCriticoDTO> obtenerProductosStockCritico();
    
    List<ProductoDTO> importarProductosJson(List<ProductoDTO> lista);
}
