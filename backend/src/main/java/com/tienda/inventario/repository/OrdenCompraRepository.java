package com.tienda.inventario.repository;

import com.tienda.inventario.entity.OrdenCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Integer> {

    Optional<OrdenCompra> findByNumeroOrden(String numeroOrden);

    List<OrdenCompra> findByProveedor_IdProveedor(Integer idProveedor);

    List<OrdenCompra> findByEstado(String estado);

    @Query("SELECT o FROM OrdenCompra o LEFT JOIN FETCH o.detalles d LEFT JOIN FETCH d.producto WHERE o.idOrden = :idOrden")
    Optional<OrdenCompra> findByIdWithDetalles(@Param("idOrden") Integer idOrden);

    @Query("SELECT MAX(o.numeroOrden) FROM OrdenCompra o")
    String findMaxNumeroOrden();
}

