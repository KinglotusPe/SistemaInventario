package com.tienda.inventario.security.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tienda.inventario.entity.Permiso;
import com.tienda.inventario.entity.Rol;
import com.tienda.inventario.entity.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class UserDetailsImpl implements UserDetails {

    private static final long serialVersionUID = 1L;

    private final Integer id;
    private final String username;
    private final String email;
    private final String nombres;
    private final String apellidos;
    private final Boolean estado;

    @JsonIgnore
    private final String password;

    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Integer id, String username, String email, String password,
                           String nombres, String apellidos, Boolean estado,
                           Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.estado = estado;
        this.authorities = authorities;
    }

    public static UserDetailsImpl build(Usuario usuario) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Mapeo dual: Roles (ej. ROLE_ADMINISTRADOR) + Permisos Granulares (ej. PRODUCTO_CREAR, INVENTARIO_AJUSTAR)
        if (usuario.getRoles() != null) {
            for (Rol rol : usuario.getRoles()) {
                // Registrar el Rol como Authority con prefijo si no lo tiene
                String rolNombre = rol.getNombre().startsWith("ROLE_") ? rol.getNombre() : "ROLE_" + rol.getNombre();
                authorities.add(new SimpleGrantedAuthority(rolNombre));

                // Registrar cada Permiso granular asociado a este rol
                if (rol.getPermisos() != null) {
                    for (Permiso permiso : rol.getPermisos()) {
                        authorities.add(new SimpleGrantedAuthority(permiso.getNombre()));
                    }
                }
            }
        }

        return new UserDetailsImpl(
                usuario.getIdUsuario(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getPassword(),
                usuario.getNombres(),
                usuario.getApellidos(),
                usuario.getEstado(),
                authorities
        );
    }

    public Integer getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(estado);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

