package com.tienda.inventario.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "unidades_medida")
public class UnidadMedida implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_unidad")
    private Integer idUnidad;

    @Column(name = "codigo", nullable = false, unique = true, length = 10)
    private String codigo; // NIU, KGM, BX, LTR

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "abreviatura", nullable = false, length = 10)
    private String abreviatura;

    public UnidadMedida() {}

    public UnidadMedida(Integer idUnidad, String codigo, String nombre, String abreviatura) {
        this.idUnidad = idUnidad;
        this.codigo = codigo;
        this.nombre = nombre;
        this.abreviatura = abreviatura;
    }

    public Integer getIdUnidad() {
        return idUnidad;
    }

    public void setIdUnidad(Integer idUnidad) {
        this.idUnidad = idUnidad;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getAbreviatura() {
        return abreviatura;
    }

    public void setAbreviatura(String abreviatura) {
        this.abreviatura = abreviatura;
    }
}

