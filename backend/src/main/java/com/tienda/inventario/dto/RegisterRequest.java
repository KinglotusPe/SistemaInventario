package com.tienda.inventario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

public class RegisterRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 100, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 80, message = "Los nombres no pueden superar los 80 caracteres")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 80, message = "Los apellidos no pueden superar los 80 caracteres")
    private String apellidos;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "Formato de correo electrónico no válido")
    private String email;

    private String telefono;

    private String rol; // ROLE_ADMINISTRADOR, ROLE_SUPERVISOR_ALMACEN, ROLE_OPERADOR_ALMACEN, ROLE_CAJERO_VENDEDOR

    public RegisterRequest() {}

    public RegisterRequest(String username, String password, String nombres, String apellidos, String email, String rol) {
        this.username = username;
        this.password = password;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.email = email;
        this.rol = rol;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}
