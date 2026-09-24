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

    /**
     * Registro de nuevo usuario mediante carga útil JSON por parte del Administrador.
     * Requiere privilegio: USUARIO_ADMIN o Rol ADMINISTRADOR.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('USUARIO_ADMIN') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<Usuario>> crearUsuario(@jakarta.validation.Valid @RequestBody com.tienda.inventario.dto.RegisterRequest request) {
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("El nombre de usuario '" + request.getUsername() + "' ya existe en el sistema"));
        }
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("El correo electrónico '" + request.getEmail() + "' ya existe en el sistema"));
        }

        Usuario usuario = new Usuario(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                request.getNombres(),
                request.getApellidos(),
                request.getEmail()
        );
        usuario.setTelefono(request.getTelefono());
        usuario.setEstado(true);

        String nombreRol = request.getRol();
        if (nombreRol == null || nombreRol.isBlank()) {
            nombreRol = "ROLE_CAJERO_VENDEDOR";
        } else if (!nombreRol.startsWith("ROLE_")) {
            nombreRol = "ROLE_" + nombreRol.toUpperCase();
        }

        Rol rol = rolRepository.findByNombre(nombreRol)
                .orElseGet(() -> rolRepository.findByNombre("ROLE_CAJERO_VENDEDOR")
                        .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado")));

        usuario.setRoles(Set.of(rol));
        Usuario guardado = usuarioRepository.save(usuario);

        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(ApiResponse.ok("Usuario registrado exitosamente por el Administrador mediante JSON", guardado));
    }

    /**
     * Carga masiva de usuarios mediante lista JSON.
     * Requiere privilegio: USUARIO_ADMIN o Rol ADMINISTRADOR.
     */
    @PostMapping("/importar-json")
    @PreAuthorize("hasAuthority('USUARIO_ADMIN') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<List<Usuario>>> importarUsuariosJson(@jakarta.validation.Valid @RequestBody List<com.tienda.inventario.dto.RegisterRequest> lista) {
        List<Usuario> creados = new java.util.ArrayList<>();
        for (com.tienda.inventario.dto.RegisterRequest req : lista) {
            if (usuarioRepository.existsByUsername(req.getUsername()) || usuarioRepository.existsByEmail(req.getEmail())) {
                continue; // Omitir duplicados
            }
            Usuario u = new Usuario(
                    req.getUsername(),
                    passwordEncoder.encode(req.getPassword()),
                    req.getNombres(),
                    req.getApellidos(),
                    req.getEmail()
            );
            u.setTelefono(req.getTelefono());
            u.setEstado(true);

            String rName = req.getRol();
            if (rName == null || rName.isBlank()) rName = "ROLE_CAJERO_VENDEDOR";
            else if (!rName.startsWith("ROLE_")) rName = "ROLE_" + rName.toUpperCase();

            Rol r = rolRepository.findByNombre(rName)
                    .orElseGet(() -> rolRepository.findByNombre("ROLE_CAJERO_VENDEDOR").orElse(null));
            if (r != null) {
                u.setRoles(Set.of(r));
            }
            creados.add(usuarioRepository.save(u));
        }
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(ApiResponse.ok("Se importaron " + creados.size() + " usuarios exitosamente desde JSON", creados));
    }
}

