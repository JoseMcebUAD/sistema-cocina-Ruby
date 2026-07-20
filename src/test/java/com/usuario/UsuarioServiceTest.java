package com.usuario;

import com.cocinarubi.dao.RolUsuarioRepository;
import com.cocinarubi.dao.UsuarioRepository;
import com.cocinarubi.domain.entity.RolUsuario;
import com.cocinarubi.domain.entity.Usuario;
import com.cocinarubi.domain.service.UsuarioService;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.UsuarioRequestDTO;
import com.cocinarubi.presentation.dto.response.UsuarioResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolUsuarioRepository rolUsuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    public RolUsuario ROL_PREPARED = RolUsuario.builder()
            .idRol(2)
            .nombreRol("COCINA")
            .descripcion("Operador de cocina")
            .build();

    public Usuario USUARIO_PREPARED = Usuario.builder()
            .idUsuario(10)
            .rolUsuario(ROL_PREPARED)
            .nombreUsuario("testUser")
            .contrasena("$2b$10$hashBcrypt")
            .creadoEn(LocalDateTime.of(2025, 1, 1, 10, 0, 0))
            .build();

    public UsuarioRequestDTO USUARIO_DTO = new UsuarioRequestDTO(2, "testUser", "12345");

    public UsuarioRequestDTO USUARIO_DTO_MODIFIED = new UsuarioRequestDTO(2, "testUser2", "54321");

    @Test
    @DisplayName("findAll - Debe retornar la lista de usuarios registrados")
    public void findAll() {
        when(usuarioRepository.findAll()).thenReturn(List.of(USUARIO_PREPARED));

        List<UsuarioResponseDTO> result = usuarioService.findAll();

        assertEquals(1, result.size());
        assertEquals("testUser", result.get(0).getNombreUsuario());
        System.out.println("[OK] findAll retornó " + result.size() + " usuario(s): " + result.get(0).getNombreUsuario());
    }

    @Test
    @DisplayName("findById - Debe retornar el usuario cuando el ID existe")
    public void findById_encontrado() {
        when(usuarioRepository.findById(10)).thenReturn(Optional.of(USUARIO_PREPARED));

        UsuarioResponseDTO result = usuarioService.findById(10);

        assertNotNull(result);
        assertEquals(10, result.getIdUsuario());
        assertEquals("testUser", result.getNombreUsuario());
        System.out.println("[OK] findById retornó usuario id=" + result.getIdUsuario());
    }

    @Test
    @DisplayName("findById - Debe lanzar excepción cuando el ID no existe")
    public void findById_noEncontrado() {
        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> usuarioService.findById(99));
        System.out.println("[OK] findById lanzó BusinessException para id=99");
    }

    @Test
    @DisplayName("save - Debe guardar y retornar el usuario correctamente")
    public void saveUsuario() {
        when(usuarioRepository.existsByNombreUsuario("testUser")).thenReturn(false);
        when(rolUsuarioRepository.findById(2)).thenReturn(Optional.of(ROL_PREPARED));
        when(passwordEncoder.encode("12345")).thenReturn("$2b$10$hashBcrypt");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(USUARIO_PREPARED);

        UsuarioResponseDTO result = usuarioService.save(USUARIO_DTO);

        assertNotNull(result);
        assertEquals("testUser", result.getNombreUsuario());
        verify(usuarioRepository).save(any(Usuario.class));
        System.out.println("[OK] save guardó usuario: " + result.getNombreUsuario());
    }

    @Test
    @DisplayName("save - Debe lanzar excepción cuando el nombre de usuario ya existe")
    public void saveUsuario_nombreDuplicado() {
        when(usuarioRepository.existsByNombreUsuario("testUser")).thenReturn(true);

        assertThrows(BusinessException.class, () -> usuarioService.save(USUARIO_DTO));
        verify(usuarioRepository, never()).save(any(Usuario.class));
        System.out.println("[OK] save lanzó CONFLICT por nombreUsuario duplicado");
    }

    @Test
    @DisplayName("update - Debe actualizar y retornar el usuario correctamente")
    public void updateUsuario() {
        when(usuarioRepository.findById(10)).thenReturn(Optional.of(USUARIO_PREPARED));
        when(usuarioRepository.existsByNombreUsuario("testUser2")).thenReturn(false);
        when(rolUsuarioRepository.findById(2)).thenReturn(Optional.of(ROL_PREPARED));
        when(passwordEncoder.encode("54321")).thenReturn("$2b$10$newHash");
        Usuario actualizado = Usuario.builder().idUsuario(10).rolUsuario(ROL_PREPARED)
                .nombreUsuario("testUser2").contrasena("$2b$10$newHash").creadoEn(USUARIO_PREPARED.getCreadoEn()).build();
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(actualizado);

        UsuarioResponseDTO result = usuarioService.update(10, USUARIO_DTO_MODIFIED);

        assertNotNull(result);
        assertEquals("testUser2", result.getNombreUsuario());
        System.out.println("[OK] update actualizó usuario: " + result.getNombreUsuario());
    }

    @Test
    @DisplayName("delete - Debe eliminar el usuario cuando el ID existe")
    public void deleteUsuario() {
        when(usuarioRepository.existsById(10)).thenReturn(true);

        assertDoesNotThrow(() -> usuarioService.delete(10));
        verify(usuarioRepository).deleteById(10);
        System.out.println("[OK] delete eliminó usuario id=10");
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción cuando el ID no existe")
    public void deleteUsuario_noEncontrado() {
        when(usuarioRepository.existsById(99)).thenReturn(false);

        assertThrows(BusinessException.class, () -> usuarioService.delete(99));
        verify(usuarioRepository, never()).deleteById(anyInt());
        System.out.println("[OK] delete lanzó BusinessException para id=99");
    }
}
