package com.tienda.inventario.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "marcas")
public class Marca implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_marca")
    private Integer idMarca;

    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "pais_origen", length = 80)
    private String paisOrigen;

    @Column(name = "estado", nullable = false)
    private Boolean estado = true;

    public Marca() {}

    public Marca(Integer idMarca, String nombre, String paisOrigen, Boolean estado) {
        this.idMarca = idMarca;
        this.nombre = nombre;
        this.paisOrigen = paisOrigen;
        this.estado = estado;
    }

    public Integer getIdMarca() {
        return idMarca;
    }

    public void setIdMarca(Integer idMarca) {
        this.idMarca = idMarca;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPaisOrigen() {
        return paisOrigen;
    }

    public void setPaisOrigen(String paisOrigen) {
        this.paisOrigen = paisOrigen;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }
}

