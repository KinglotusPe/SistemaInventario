package com.tienda.inventario.repository;

import com.tienda.inventario.entity.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoMovimientoRepository extends JpaRepository<TipoMovimiento, Integer> {
    Optional<TipoMovimiento> findByCodigo(String codigo);
}

