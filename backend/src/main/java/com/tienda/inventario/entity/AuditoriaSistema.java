package com.tienda.inventario.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_sistema", indexes = {
    @Index(name = "idx_auditoria_usuario", columnList = "username"),
    @Index(name = "idx_auditoria_fecha", columnList = "fecha_hora")
})
public class AuditoriaSistema implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_auditoria")
    private Integer idAuditoria;

    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "accion", nullable = false, length = 80)
    private String accion; // CREACION, MODIFICACION, ELIMINACION, LOGIN, AJUSTE_STOCK, VENTA_REGISTRO

    @Column(name = "tabla_afectada", nullable = false, length = 60)
    private String tablaAfectada;

    @Column(name = "id_registro_afectado")
    private Integer idRegistroAfectado;

    @Column(name = "detalle", columnDefinition = "TEXT")
    private String detalle;

    @Column(name = "ip_origen", length = 45)
    private String ipOrigen;

    @Column(name = "fecha_hora", insertable = false, updatable = false)
    private LocalDateTime fechaHora;

    public AuditoriaSistema() {}

    public AuditoriaSistema(Integer idUsuario, String username, String accion, String tablaAfectada, Integer idRegistroAfectado, String detalle, String ipOrigen) {
        this.idUsuario = idUsuario;
        this.username = username;
        this.accion = accion;
        this.tablaAfectada = tablaAfectada;
        this.idRegistroAfectado = idRegistroAfectado;
        this.detalle = detalle;
        this.ipOrigen = ipOrigen;
    }

    public Integer getIdAuditoria() {
        return idAuditoria;
    }

    public void setIdAuditoria(Integer idAuditoria) {
        this.idAuditoria = idAuditoria;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getTablaAfectada() {
        return tablaAfectada;
    }

    public void setTablaAfectada(String tablaAfectada) {
        this.tablaAfectada = tablaAfectada;
    }

    public Integer getIdRegistroAfectado() {
        return idRegistroAfectado;
    }

    public void setIdRegistroAfectado(Integer idRegistroAfectado) {
        this.idRegistroAfectado = idRegistroAfectado;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public String getIpOrigen() {
        return ipOrigen;
    }

    public void setIpOrigen(String ipOrigen) {
        this.ipOrigen = ipOrigen;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }
}

