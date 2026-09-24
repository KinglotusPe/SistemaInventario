package com.tienda.inventario.repository;

import com.tienda.inventario.entity.StockAlmacen;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockAlmacenRepository extends JpaRepository<StockAlmacen, Integer> {

    Optional<StockAlmacen> findByProducto_IdProductoAndAlmacen_IdAlmacen(Integer idProducto, Integer idAlmacen);

    List<StockAlmacen> findByAlmacen_IdAlmacen(Integer idAlmacen);

    List<StockAlmacen> findByProducto_IdProducto(Integer idProducto);

    // Búsqueda con bloqueo pesimista (PESSIMISTIC_WRITE) para evitar condiciones de carrera concurrentes
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM StockAlmacen s WHERE s.producto.idProducto = :idProducto AND s.almacen.idAlmacen = :idAlmacen")
    Optional<StockAlmacen> findByProductoAndAlmacenWithLock(@Param("idProducto") Integer idProducto, @Param("idAlmacen") Integer idAlmacen);

    // Incremento atómico en base de datos
    @Modifying
    @Query("UPDATE StockAlmacen s SET s.stockActual = s.stockActual + :cantidad WHERE s.producto.idProducto = :idProducto AND s.almacen.idAlmacen = :idAlmacen")
    int incrementarStock(@Param("idProducto") Integer idProducto, @Param("idAlmacen") Integer idAlmacen, @Param("cantidad") Integer cantidad);

    // Decremento atómico validando que no quede en negativo
    @Modifying
    @Query("UPDATE StockAlmacen s SET s.stockActual = s.stockActual - :cantidad WHERE s.producto.idProducto = :idProducto AND s.almacen.idAlmacen = :idAlmacen AND s.stockActual >= :cantidad")
    int decrementarStock(@Param("idProducto") Integer idProducto, @Param("idAlmacen") Integer idAlmacen, @Param("cantidad") Integer cantidad);

    // Consulta de productos en stock crítico o agotados
    @Query("SELECT s FROM StockAlmacen s JOIN FETCH s.producto p JOIN FETCH s.almacen a WHERE s.stockActual <= p.stockMinimo ORDER BY s.stockActual ASC")
    List<StockAlmacen> findStockCritico();

    // Consulta de stock crítico filtrado por almacén
    @Query("SELECT s FROM StockAlmacen s JOIN FETCH s.producto p JOIN FETCH s.almacen a WHERE s.almacen.idAlmacen = :idAlmacen AND s.stockActual <= p.stockMinimo ORDER BY s.stockActual ASC")
    List<StockAlmacen> findStockCriticoPorAlmacen(@Param("idAlmacen") Integer idAlmacen);
}

