package com.tienda.inventario.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

public class VentaRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "El tipo de comprobante es obligatorio (BOLETA, FACTURA, NOTA_VENTA)")
    private String tipoComprobante;

    @NotNull(message = "El ID del cliente es obligatorio")
    private Integer idCliente;

    @NotNull(message = "El almacén de despacho es obligatorio")
    private Integer idAlmacen;

    @NotBlank(message = "El método de pago es obligatorio (EFECTIVO, TARJETA, TRANSFERENCIA, YAPE_PLIN)")
    private String metodoPago;

    @NotEmpty(message = "Debe incluir al menos un ítem en la venta")
    @Valid
    private List<ItemVentaDTO> detalles;

    public VentaRequestDTO() {}

    public VentaRequestDTO(String tipoComprobante, Integer idCliente, Integer idAlmacen, String metodoPago, List<ItemVentaDTO> detalles) {
        this.tipoComprobante = tipoComprobante;
        this.idCliente = idCliente;
        this.idAlmacen = idAlmacen;
        this.metodoPago = metodoPago;
        this.detalles = detalles;
    }

    public String getTipoComprobante() {
        return tipoComprobante;
    }

    public void setTipoComprobante(String tipoComprobante) {
        this.tipoComprobante = tipoComprobante;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public Integer getIdAlmacen() {
        return idAlmacen;
    }

    public void setIdAlmacen(Integer idAlmacen) {
        this.idAlmacen = idAlmacen;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public List<ItemVentaDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<ItemVentaDTO> detalles) {
        this.detalles = detalles;
    }
}
