package com.tienda.inventario.controller;

import com.tienda.inventario.dto.ApiResponse;
import com.tienda.inventario.dto.JwtResponse;
import com.tienda.inventario.dto.LoginRequest;
import com.tienda.inventario.dto.RegisterRequest;
import com.tienda.inventario.entity.Rol;
import com.tienda.inventario.entity.Usuario;
import com.tienda.inventario.repository.RolRepository;
import com.tienda.inventario.repository.UsuarioRepository;
import com.tienda.inventario.security.jwt.JwtUtils;
import com.tienda.inventario.security.service.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtils jwtUtils,
                          UsuarioRepository usuarioRepository,
                          RolRepository rolRepository,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Inicio de sesión de usuario y generación de token Bearer JWT.
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .collect(Collectors.toList());

        List<String> permisos = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> !auth.startsWith("ROLE_"))
                .collect(Collectors.toList());

        // Actualizar último acceso
        usuarioRepository.findById(userDetails.getId()).ifPresent(u -> {
            u.setUltimoAcceso(LocalDateTime.now());
            usuarioRepository.save(u);
        });

        JwtResponse jwtResponse = new JwtResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getNombres(),
                userDetails.getApellidos(),
                roles,
                permisos
        );

        return ResponseEntity.ok(ApiResponse.ok("Autenticación exitosa", jwtResponse));
    }

    /**
     * Registro de nuevo usuario mediante carga útil JSON (Sign Up).
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Usuario>> registerUser(@Valid @RequestBody RegisterRequest signUpRequest) {
        if (usuarioRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("El nombre de usuario '" + signUpRequest.getUsername() + "' ya se encuentra registrado"));
        }

        if (usuarioRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("El correo electrónico '" + signUpRequest.getEmail() + "' ya se encuentra registrado"));
        }

        // Crear nueva cuenta con contraseña encriptada con BCrypt
        Usuario usuario = new Usuario(
                signUpRequest.getUsername(),
                passwordEncoder.encode(signUpRequest.getPassword()),
                signUpRequest.getNombres(),
                signUpRequest.getApellidos(),
                signUpRequest.getEmail()
        );
        usuario.setTelefono(signUpRequest.getTelefono());
        usuario.setEstado(true);

        // Asignar el rol solicitado (default: ROLE_CAJERO_VENDEDOR)
        String nombreRol = signUpRequest.getRol();
        if (nombreRol == null || nombreRol.isBlank()) {
            nombreRol = "ROLE_CAJERO_VENDEDOR";
        } else if (!nombreRol.startsWith("ROLE_")) {
            nombreRol = "ROLE_" + nombreRol.toUpperCase();
        }

        Rol rol = rolRepository.findByNombre(nombreRol)
                .orElseGet(() -> rolRepository.findByNombre("ROLE_CAJERO_VENDEDOR")
                        .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado en la base de datos")));

        usuario.setRoles(Set.of(rol));
        Usuario guardado = usuarioRepository.save(usuario);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Usuario registrado exitosamente mediante JSON", guardado));
    }

    /**
     * Obtener el perfil del usuario autenticado en la sesión actual.
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDetailsImpl>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            return ResponseEntity.ok(ApiResponse.ok("Usuario autenticado obtenido", userDetails));
        }
        return ResponseEntity.badRequest().body(ApiResponse.error("No hay sesión activa"));
    }
}
