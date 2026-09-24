package com.tienda.inventario.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "tipos_movimiento")
public class TipoMovimiento implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo")
    private Integer idTipo;

    @Column(name = "codigo", nullable = false, unique = true, length = 30)
    private String codigo; // ENTRADA_COMPRA, SALIDA_VENTA, AJUSTE_POSITIVO, AJUSTE_NEGATIVO, TRASLADO

    @Column(name = "descripcion", nullable = false, length = 120)
    private String descripcion;

    @Column(name = "naturaleza", nullable = false)
    private String naturaleza; // INGRESO, EGRESO, NEUTRO

    public TipoMovimiento() {}

    public TipoMovimiento(Integer idTipo, String codigo, String descripcion, String naturaleza) {
        this.idTipo = idTipo;
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.naturaleza = naturaleza;
    }

    public Integer getIdTipo() {
        return idTipo;
    }

    public void setIdTipo(Integer idTipo) {
        this.idTipo = idTipo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getNaturaleza() {
        return naturaleza;
    }

    public void setNaturaleza(String naturaleza) {
        this.naturaleza = naturaleza;
    }
}

