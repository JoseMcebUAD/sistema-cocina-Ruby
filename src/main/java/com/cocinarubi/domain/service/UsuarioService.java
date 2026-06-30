package com.cocinarubi.domain.service;

import com.cocinarubi.dao.RolUsuarioRepository;
import com.cocinarubi.dao.UsuarioRepository;
import com.cocinarubi.presentation.dto.request.UsuarioRequestDTO;
import com.cocinarubi.presentation.dto.response.UsuarioResponseDTO;
import com.cocinarubi.domain.entity.RolUsuario;
import com.cocinarubi.domain.entity.Usuario;
import com.cocinarubi.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Gestiona los usuarios del sistema (administradores, repartidores, etc.).
 * La contraseña siempre se almacena cifrada con BCrypt.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolUsuarioRepository rolUsuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          RolUsuarioRepository rolUsuarioRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolUsuarioRepository = rolUsuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UsuarioResponseDTO> findAll() {
        return usuarioRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public UsuarioResponseDTO findById(int id) {
        return toResponseDTO(findEntityById(id));
    }

    public UsuarioResponseDTO save(UsuarioRequestDTO dto) {
        // El nombreUsuario es la clave de autenticación; no puede estar duplicado
        if (usuarioRepository.existsByNombreUsuario(dto.getNombreUsuario())) {
            throw new BusinessException(
                    "Ya existe un usuario con el nombre: " + dto.getNombreUsuario(), HttpStatus.CONFLICT);
        }
        RolUsuario rol = findRol(dto.getIdRol());
        Usuario usuario = Usuario.builder()
                .rolUsuario(rol)
                .nombreUsuario(dto.getNombreUsuario())
                .contrasena(passwordEncoder.encode(dto.getContrasena()))
                // Zona horaria fija del restaurante para evitar ambigüedades al comparar fechas
                .creadoEn(LocalDateTime.now(ZoneId.of("America/Merida")))
                .build();
        return toResponseDTO(usuarioRepository.save(usuario));
    }

    public UsuarioResponseDTO update(int id, UsuarioRequestDTO dto) {
        Usuario existente = findEntityById(id);
        // Solo se valida unicidad si el nombre realmente cambió
        if (!existente.getNombreUsuario().equals(dto.getNombreUsuario())
                && usuarioRepository.existsByNombreUsuario(dto.getNombreUsuario())) {
            throw new BusinessException(
                    "Ya existe un usuario con el nombre: " + dto.getNombreUsuario(), HttpStatus.CONFLICT);
        }
        existente.setRolUsuario(findRol(dto.getIdRol()));
        existente.setNombreUsuario(dto.getNombreUsuario());
        existente.setContrasena(passwordEncoder.encode(dto.getContrasena()));
        return toResponseDTO(usuarioRepository.save(existente));
    }

    public UsuarioResponseDTO patch(int id, Map<String, Object> payload) {
        Usuario existente = findEntityById(id);
        if (payload.containsKey("nombreUsuario")) {
            String nuevoNombre = (String) payload.get("nombreUsuario");
            // Solo se valida unicidad si el nombre realmente cambió
            if (!existente.getNombreUsuario().equals(nuevoNombre)
                    && usuarioRepository.existsByNombreUsuario(nuevoNombre)) {
                throw new BusinessException(
                        "Ya existe un usuario con el nombre: " + nuevoNombre, HttpStatus.CONFLICT);
            }
            existente.setNombreUsuario(nuevoNombre);
        }
        if (payload.containsKey("contrasena")) {
            existente.setContrasena(passwordEncoder.encode((String) payload.get("contrasena")));
        }
        if (payload.containsKey("idRol")) {
            // Jackson deserializa números de JSON como Integer o Long; se normaliza con Number
            existente.setRolUsuario(findRol(((Number) payload.get("idRol")).intValue()));
        }
        return toResponseDTO(usuarioRepository.save(existente));
    }

    public void delete(int id) {
        // Verificar existencia antes de borrar para devolver 404 en lugar del silencioso no-op de JPA
        if (!usuarioRepository.existsById(id)) {
            throw new BusinessException("Usuario no encontrado con id: " + id, HttpStatus.NOT_FOUND);
        }
        usuarioRepository.deleteById(id);
    }

    private Usuario findEntityById(int id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Usuario no encontrado con id: " + id, HttpStatus.NOT_FOUND));
    }

    private RolUsuario findRol(Integer idRol) {
        return rolUsuarioRepository.findById(idRol)
                .orElseThrow(() -> new BusinessException(
                        "Rol no encontrado con id: " + idRol, HttpStatus.NOT_FOUND));
    }

    private UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getIdUsuario(),
                usuario.getNombreUsuario(),
                usuario.getRolUsuario().getNombreRol(),
                usuario.getCreadoEn(),
                usuario.getUltimoLogin()
        );
    }
}
