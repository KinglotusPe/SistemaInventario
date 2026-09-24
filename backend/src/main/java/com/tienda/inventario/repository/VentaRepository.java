package com.tienda.inventario.repository;

import com.tienda.inventario.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Integer> {

    Optional<Venta> findBySerieAndNumeroCorrelativo(String serie, String numeroCorrelativo);

    List<Venta> findByUsuario_IdUsuario(Integer idUsuario);

    List<Venta> findByAlmacen_IdAlmacen(Integer idAlmacen);

    List<Venta> findByFechaVentaBetween(LocalDateTime inicio, LocalDateTime fin);

    @Query("SELECT v FROM Venta v LEFT JOIN FETCH v.detalles d LEFT JOIN FETCH d.producto WHERE v.idVenta = :idVenta")
    Optional<Venta> findByIdWithDetalles(@Param("idVenta") Integer idVenta);

    @Query("SELECT MAX(v.numeroCorrelativo) FROM Venta v WHERE v.serie = :serie")
    String findMaxCorrelativoBySerie(@Param("serie") String serie);
}

