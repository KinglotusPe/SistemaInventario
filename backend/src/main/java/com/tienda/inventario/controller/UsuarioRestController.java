package com.tienda.inventario.controller;

import com.tienda.inventario.dto.ApiResponse;
import com.tienda.inventario.entity.Permiso;
import com.tienda.inventario.entity.Rol;
import com.tienda.inventario.entity.Usuario;
import com.tienda.inventario.repository.PermisoRepository;
import com.tienda.inventario.repository.RolRepository;
import com.tienda.inventario.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioRestController {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioRestController(UsuarioRepository usuarioRepository,
                                 RolRepository rolRepository,
                                 PermisoRepository permisoRepository,
                                 PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.permisoRepository = permisoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Listado general de usuarios con sus roles y permisos granulares.
     * Requiere privilegio: USUARIO_ADMIN o Rol ADMINISTRADOR.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('USUARIO_ADMIN') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<List<Usuario>>> listarUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        return ResponseEntity.ok(ApiResponse.ok("Listado de usuarios obtenido con éxito", usuarios));
    }

    /**
     * Catálogo completo de roles del sistema.
     */
    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('USUARIO_ADMIN') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<List<Rol>>> listarRoles() {
        List<Rol> roles = rolRepository.findAll();
        return ResponseEntity.ok(ApiResponse.ok("Catálogo de roles obtenido con éxito", roles));
    }

    /**
     * Catálogo completo de privilegios/permisos atómicos.
     */
    @GetMapping("/permisos")
    @PreAuthorize("hasAuthority('USUARIO_ADMIN') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<List<Permiso>>> listarPermisos() {
        List<Permiso> permisos = permisoRepository.findAll();
        return ResponseEntity.ok(ApiResponse.ok("Catálogo de permisos obtenido con éxito", permisos));
    }

    /**
     * Asignar roles a un usuario específico.
     */
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('USUARIO_ADMIN') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<Usuario>> asignarRoles(@PathVariable Integer id, @RequestBody List<Integer> idRoles) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));

        Set<Rol> nuevosRoles = new HashSet<>();
        for (Integer idRol : idRoles) {
            rolRepository.findById(idRol).ifPresent(nuevosRoles::add);
        }

        usuario.setRoles(nuevosRoles);
        Usuario actualizado = usuarioRepository.save(usuario);
        return ResponseEntity.ok(ApiResponse.ok("Roles asignados exitosamente al usuario", actualizado));
    }
}

