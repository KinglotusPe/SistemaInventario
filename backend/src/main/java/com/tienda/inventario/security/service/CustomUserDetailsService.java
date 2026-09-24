package com.tienda.inventario.security.service;

import com.tienda.inventario.entity.Usuario;
import com.tienda.inventario.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsernameWithRolesAndPermisos(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con credencial: " + username));

        if (!Boolean.TRUE.equals(usuario.getEstado())) {
            throw new UsernameNotFoundException("La cuenta del usuario está inactiva o deshabilitada: " + username);
        }

        return UserDetailsImpl.build(usuario);
    }
}

