package com.tienda.inventario.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class StockCriticoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer idProducto;
    private String codigoSku;
    private String nombre;
    private String categoria;
    private Integer stockActual;
    private Integer stockMinimo;
    private BigDecimal precioUnitario;
    private Integer deficit;

    public StockCriticoDTO() {}

    public StockCriticoDTO(Integer idProducto, String codigoSku, String nombre, String categoria, 
                           Integer stockActual, Integer stockMinimo, BigDecimal precioUnitario) {
        this.idProducto = idProducto;
        this.codigoSku = codigoSku;
        this.nombre = nombre;
        this.categoria = categoria;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.precioUnitario = precioUnitario;
        this.deficit = (stockMinimo != null && stockActual != null) ? Math.max(0, stockMinimo - stockActual) : 0;
    }

    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public String getCodigoSku() {
        return codigoSku;
    }

    public void setCodigoSku(String codigoSku) {
        this.codigoSku = codigoSku;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Integer getStockActual() {
        return stockActual;
    }

    public void setStockActual(Integer stockActual) {
        this.stockActual = stockActual;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public Integer getDeficit() {
        return deficit;
    }

    public void setDeficit(Integer deficit) {
        this.deficit = deficit;
    }
}
