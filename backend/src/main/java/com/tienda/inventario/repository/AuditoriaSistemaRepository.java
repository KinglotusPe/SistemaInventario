package com.tienda.inventario.repository;

import com.tienda.inventario.entity.AuditoriaSistema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditoriaSistemaRepository extends JpaRepository<AuditoriaSistema, Integer> {
    Page<AuditoriaSistema> findAllByOrderByFechaHoraDesc(Pageable pageable);
    List<AuditoriaSistema> findByUsername(String username);
    List<AuditoriaSistema> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);
}

