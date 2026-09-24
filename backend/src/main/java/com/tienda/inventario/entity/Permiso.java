package com.tienda.inventario.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "permisos")
public class Permiso implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permiso")
    private Integer idPermiso;

    @Column(name = "nombre", nullable = false, unique = true, length = 60)
    private String nombre; // Ej: PRODUCTO_CREAR, INVENTARIO_AJUSTAR

    @Column(name = "descripcion", nullable = false, length = 150)
    private String descripcion;

    @Column(name = "modulo", nullable = false, length = 50)
    private String modulo; // SEGURIDAD, CATALOGO, INVENTARIO, VENTAS, etc.

    public Permiso() {}

    public Permiso(Integer idPermiso, String nombre, String descripcion, String modulo) {
        this.idPermiso = idPermiso;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.modulo = modulo;
    }

    public Integer getIdPermiso() {
        return idPermiso;
    }

    public void setIdPermiso(Integer idPermiso) {
        this.idPermiso = idPermiso;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }
}

