package com.tienda.inventario.repository;

import com.tienda.inventario.entity.MovimientoInventario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Integer> {

    // Historial paginado ordenado cronológicamente descendente
    Page<MovimientoInventario> findAllByOrderByFechaMovimientoDesc(Pageable pageable);

    Optional<MovimientoInventario> findByNumeroMovimiento(String numeroMovimiento);

    List<MovimientoInventario> findByAlmacenOrigen_IdAlmacen(Integer idAlmacen);

    List<MovimientoInventario> findByUsuario_IdUsuario(Integer idUsuario);

    // Consulta JPQL para reportes de movimientos entre rangos de fechas
    @Query("SELECT m FROM MovimientoInventario m " +
           "WHERE m.fechaMovimiento BETWEEN :fechaInicio AND :fechaFin " +
           "ORDER BY m.fechaMovimiento DESC")
    List<MovimientoInventario> findMovimientosPorRangoFechas(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    @Query("SELECT m FROM MovimientoInventario m LEFT JOIN FETCH m.detalles d LEFT JOIN FETCH d.producto WHERE m.idMovimiento = :idMovimiento")
    Optional<MovimientoInventario> findByIdWithDetalles(@Param("idMovimiento") Integer idMovimiento);

    @Query("SELECT MAX(m.numeroMovimiento) FROM MovimientoInventario m")
    String findMaxNumeroMovimiento();
}

