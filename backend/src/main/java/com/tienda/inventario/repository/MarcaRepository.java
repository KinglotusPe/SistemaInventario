package com.tienda.inventario.repository;

import com.tienda.inventario.entity.Marca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarcaRepository extends JpaRepository<Marca, Integer> {
    List<Marca> findByEstadoTrue();
}

