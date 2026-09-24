package com.tienda.inventario.repository;

import com.tienda.inventario.entity.DetalleMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleMovimientoRepository extends JpaRepository<DetalleMovimiento, Integer> {
    List<DetalleMovimiento> findByMovimiento_IdMovimiento(Integer idMovimiento);
}

