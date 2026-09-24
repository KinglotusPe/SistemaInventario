package com.tienda.inventario.service;

import com.tienda.inventario.dto.MovimientoRequestDTO;
import com.tienda.inventario.entity.MovimientoInventario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface IMovimientoInventarioService {

    MovimientoInventario registrarMovimiento(MovimientoRequestDTO request);

    Page<MovimientoInventario> listarMovimientos(Pageable pageable);

    MovimientoInventario obtenerPorId(Integer idMovimiento);

    List<MovimientoInventario> listarPorRangoFechas(LocalDateTime inicio, LocalDateTime fin);
}

