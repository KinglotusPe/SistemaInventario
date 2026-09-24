package com.tienda.inventario.repository;

import com.tienda.inventario.entity.DetalleOrdenCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleOrdenCompraRepository extends JpaRepository<DetalleOrdenCompra, Integer> {
    List<DetalleOrdenCompra> findByOrdenCompra_IdOrden(Integer idOrden);
}

