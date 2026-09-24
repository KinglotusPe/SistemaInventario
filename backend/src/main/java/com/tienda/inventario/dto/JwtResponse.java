package com.tienda.inventario.dto;

import java.io.Serializable;
import java.util.List;

public class JwtResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String token;
    private String type = "Bearer";
    private Integer id;
    private String username;
    private String email;
    private String nombres;
    private String apellidos;
    private List<String> roles;
    private List<String> permisos;

    public JwtResponse() {}

    public JwtResponse(String token, Integer id, String username, String email,
                       String nombres, String apellidos, List<String> roles, List<String> permisos) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.roles = roles;
        this.permisos = permisos;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getPermisos() {
        return permisos;
    }

    public void setPermisos(List<String> permisos) {
        this.permisos = permisos;
    }
}
