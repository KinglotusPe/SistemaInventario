package com.tienda.inventario.repository;

import com.tienda.inventario.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    // Consulta derivada para SKU único
    Optional<Producto> findByCodigoSku(String codigoSku);

    // Listado paginado de productos activos
    Page<Producto> findByEstadoTrue(Pageable pageable);

    // Consulta JPQL personalizada: Filtrado por Categoría con paginación
    @Query("SELECT p FROM Producto p WHERE p.categoria.idCategoria = :idCategoria AND p.estado = true")
    Page<Producto> findByCategoriaId(@Param("idCategoria") Integer idCategoria, Pageable pageable);

    // Consulta JPQL personalizada: Búsqueda flexible por Nombre o Código SKU (case-insensitive)
    @Query("SELECT p FROM Producto p WHERE p.estado = true AND " +
           "(LOWER(p.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(p.codigoSku) LIKE LOWER(CONCAT('%', :filtro, '%')))")
    Page<Producto> buscarPorNombreOSku(@Param("filtro") String filtro, Pageable pageable);

    // Consulta JPQL personalizada con JOIN para detectar productos en Stock Crítico
    @Query("SELECT DISTINCT p FROM Producto p " +
           "JOIN StockAlmacen s ON s.producto.idProducto = p.idProducto " +
           "WHERE p.estado = true AND s.stockActual <= p.stockMinimo " +
           "ORDER BY p.nombre ASC")
    List<Producto> findProductosConStockCritico();
}

