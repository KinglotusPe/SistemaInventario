package com.tienda.inventario.service;

import com.tienda.inventario.dto.VentaRequestDTO;
import com.tienda.inventario.entity.Venta;

import java.util.List;

public interface IVentaService {

    Venta registrarVenta(VentaRequestDTO request);

    Venta anularVenta(Integer idVenta, String motivo);

    List<Venta> listarVentas();

    Venta obtenerPorId(Integer idVenta);
}

