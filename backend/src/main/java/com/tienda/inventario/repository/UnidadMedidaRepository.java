package com.tienda.inventario.repository;

import com.tienda.inventario.entity.UnidadMedida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UnidadMedidaRepository extends JpaRepository<UnidadMedida, Integer> {
    Optional<UnidadMedida> findByCodigo(String codigo);
}

