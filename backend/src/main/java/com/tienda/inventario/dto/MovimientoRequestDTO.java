package com.tienda.inventario.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MovimientoRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "El almacén de origen es obligatorio")
    @JsonAlias({"almacenId", "id_almacen_origen", "almacen_id", "idAlmacen"})
    private Integer idAlmacenOrigen;

    @JsonAlias({"almacenDestinoId", "id_almacen_destino", "almacen_destino_id", "idAlmacenDestino"})
    private Integer idAlmacenDestino; // Requerido si es TRASLADO

    @NotBlank(message = "El código de tipo de movimiento es obligatorio (ENTRADA_COMPRA, SALIDA_VENTA, AJUSTE_POSITIVO, AJUSTE_NEGATIVO, TRASLADO)")
    @JsonAlias({"tipo", "tipoMovimiento", "tipo_movimiento", "codigo_tipo"})
    private String codigoTipo;

    @NotBlank(message = "El motivo del movimiento es obligatorio")
    private String motivo;

    // Soporte para múltiples ítems en un solo movimiento
    private List<ItemMovimientoDTO> items = new ArrayList<>();

    // Campos de conveniencia para movimientos de un solo producto
    @JsonAlias({"productoId", "id_producto", "producto_id"})
    private Integer idProducto;
    private Integer cantidad;

    public MovimientoRequestDTO() {}

    public MovimientoRequestDTO(Integer idAlmacenOrigen, Integer idAlmacenDestino, String codigoTipo, String motivo) {
        this.idAlmacenOrigen = idAlmacenOrigen;
        this.idAlmacenDestino = idAlmacenDestino;
        this.codigoTipo = codigoTipo;
        this.motivo = motivo;
    }

    public Integer getIdAlmacenOrigen() {
        return idAlmacenOrigen;
    }

    public void setIdAlmacenOrigen(Integer idAlmacenOrigen) {
        this.idAlmacenOrigen = idAlmacenOrigen;
    }

    public Integer getIdAlmacenDestino() {
        return idAlmacenDestino;
    }

    public void setIdAlmacenDestino(Integer idAlmacenDestino) {
        this.idAlmacenDestino = idAlmacenDestino;
    }

    public String getCodigoTipo() {
        return codigoTipo;
    }

    public void setCodigoTipo(String codigoTipo) {
        this.codigoTipo = codigoTipo;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public List<ItemMovimientoDTO> getItems() {
        return items;
    }

    public void setItems(List<ItemMovimientoDTO> items) {
        this.items = items;
    }

    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}
