package com.registrocliente;

import com.cocinarubi.dao.RegistroClienteRepository;
import com.cocinarubi.dao.RutaRepository;
import com.cocinarubi.domain.entity.RegistroCliente;
import com.cocinarubi.domain.entity.Ruta;
import com.cocinarubi.domain.service.RegistroClienteService;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.RegistroClienteRequestDTO;
import com.cocinarubi.presentation.dto.response.RegistroClienteResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RegistroClienteServiceTest {

    @Mock private RegistroClienteRepository registroClienteRepository;
    @Mock private RutaRepository rutaRepository;
    @InjectMocks private RegistroClienteService registroClienteService;

    public final Ruta RUTA_PREPARED = Ruta.builder()
            .idRuta(1)
            .nombre("Zona Centro")
            .tarifaEnvio(BigDecimal.valueOf(30))
            .build();

    public final RegistroCliente CLIENTE_PREPARADO = RegistroCliente.builder()
            .idRegistroCliente(5)
            .nombre("Juan Pérez")
            .telefono("5551234567")
            .ruta(null)
            .direccion(null)
            .build();

    public final RegistroCliente CLIENTE_CON_RUTA = RegistroCliente.builder()
            .idRegistroCliente(6)
            .nombre("María López")
            .telefono("5559876543")
            .ruta(Ruta.builder().idRuta(1).nombre("Zona Centro").tarifaEnvio(BigDecimal.valueOf(30)).build())
            .direccion("Calle Principal 42")
            .build();

    @Test
    @DisplayName("findAll - Debe retornar la página de registros de cliente")
    public void findAll() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<RegistroCliente> page = new PageImpl<>(List.of(CLIENTE_PREPARADO));
        when(registroClienteRepository.findAll(pageable)).thenReturn(page);

        Page<RegistroClienteResponseDTO> result = registroClienteService.findAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Juan Pérez", result.getContent().get(0).getNombre());
        System.out.println("[OK] findAll retornó " + result.getTotalElements() + " registro(s)");
    }

    @Test
    @DisplayName("findById - Debe retornar el registro cuando el ID existe")
    public void findById_encontrado() {
        when(registroClienteRepository.findById(5)).thenReturn(Optional.of(CLIENTE_PREPARADO));

        RegistroClienteResponseDTO result = registroClienteService.findById(5);

        assertNotNull(result);
        assertEquals(5, result.getIdRegistroCliente());
        assertEquals("Juan Pérez", result.getNombre());
        assertNull(result.getIdRuta());
        System.out.println("[OK] findById retornó cliente id=" + result.getIdRegistroCliente());
    }

    @Test
    @DisplayName("findById - Debe lanzar excepción cuando el ID no existe")
    public void findById_noEncontrado() {
        when(registroClienteRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> registroClienteService.findById(99));
        System.out.println("[OK] findById lanzó BusinessException para id=99");
    }

    @Test
    @DisplayName("save - Debe guardar un registro sin ruta y retornar el DTO")
    public void save_sinRuta() {
        RegistroClienteRequestDTO dto = new RegistroClienteRequestDTO("Juan Pérez", "5551234567", null, null);
        when(registroClienteRepository.save(any(RegistroCliente.class))).thenReturn(CLIENTE_PREPARADO);

        RegistroClienteResponseDTO result = registroClienteService.save(dto);

        assertNotNull(result);
        assertEquals("Juan Pérez", result.getNombre());
        assertNull(result.getIdRuta());
        verify(rutaRepository, never()).findById(anyInt());
        System.out.println("[OK] save sin ruta: nombre=" + result.getNombre());
    }

    @Test
    @DisplayName("save - Debe guardar un registro con ruta y retornar nombre de ruta")
    public void save_conRuta() {
        RegistroClienteRequestDTO dto = new RegistroClienteRequestDTO("María López", "5559876543", 1, "Calle Principal 42");
        when(rutaRepository.findById(1)).thenReturn(Optional.of(RUTA_PREPARED));
        when(registroClienteRepository.save(any(RegistroCliente.class))).thenReturn(CLIENTE_CON_RUTA);

        RegistroClienteResponseDTO result = registroClienteService.save(dto);

        assertNotNull(result);
        assertEquals(1, result.getIdRuta());
        assertEquals("Zona Centro", result.getNombreRuta());
        verify(rutaRepository).findById(1);
        System.out.println("[OK] save con ruta: nombreRuta=" + result.getNombreRuta());
    }

    @Test
    @DisplayName("save - Debe lanzar excepción cuando la ruta indicada no existe")
    public void save_rutaNoExiste() {
        RegistroClienteRequestDTO dto = new RegistroClienteRequestDTO("Test", "123", 99, null);
        when(rutaRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> registroClienteService.save(dto));
        verify(registroClienteRepository, never()).save(any());
        System.out.println("[OK] save lanzó BusinessException por ruta no encontrada id=99");
    }

    @Test
    @DisplayName("findByTelefono - Debe retornar registros que contengan el número buscado")
    public void findByTelefono() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<RegistroCliente> page = new PageImpl<>(List.of(CLIENTE_PREPARADO));
        when(registroClienteRepository.findByTelefonoContaining("555", pageable)).thenReturn(page);

        Page<RegistroClienteResponseDTO> result = registroClienteService.findByTelefono("555", pageable);

        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getTelefono().contains("555"));
        System.out.println("[OK] findByTelefono retornó " + result.getTotalElements() + " resultado(s)");
    }

    @Test
    @DisplayName("update - Debe actualizar el registro y retornar el resultado")
    public void update_exitoso() {
        RegistroCliente clienteActualizado = RegistroCliente.builder()
                .idRegistroCliente(5)
                .nombre("Juan Pérez Actualizado")
                .telefono("5550000000")
                .ruta(null)
                .direccion("Nueva Dirección 123")
                .build();
        when(registroClienteRepository.findById(5)).thenReturn(Optional.of(CLIENTE_PREPARADO));
        when(registroClienteRepository.save(any(RegistroCliente.class))).thenReturn(clienteActualizado);

        RegistroClienteRequestDTO dto = new RegistroClienteRequestDTO("Juan Pérez Actualizado", "5550000000", null, "Nueva Dirección 123");
        RegistroClienteResponseDTO result = registroClienteService.update(5, dto);

        assertNotNull(result);
        assertEquals("Juan Pérez Actualizado", result.getNombre());
        System.out.println("[OK] update actualizó registro: nombre=" + result.getNombre());
    }

    @Test
    @DisplayName("delete - Debe eliminar el registro cuando el ID existe")
    public void delete_exitoso() {
        when(registroClienteRepository.existsById(5)).thenReturn(true);

        assertDoesNotThrow(() -> registroClienteService.delete(5));
        verify(registroClienteRepository).deleteById(5);
        System.out.println("[OK] delete eliminó registro id=5");
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción cuando el ID no existe")
    public void delete_noEncontrado() {
        when(registroClienteRepository.existsById(99)).thenReturn(false);

        assertThrows(BusinessException.class, () -> registroClienteService.delete(99));
        verify(registroClienteRepository, never()).deleteById(anyInt());
        System.out.println("[OK] delete lanzó BusinessException para id=99");
    }
}
