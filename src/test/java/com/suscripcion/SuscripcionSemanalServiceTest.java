package com.suscripcion;

import com.cocinarubi.DBConstants.MetodoPago;
import com.cocinarubi.DBConstants.OrigenSuscripcion;
import com.cocinarubi.dao.SuscripcionSemanalRepository;
import com.cocinarubi.domain.entity.SuscripcionSemanal;
import com.cocinarubi.domain.service.SuscripcionSemanalService;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.SuscripcionSemanalRequestDTO;
import com.cocinarubi.presentation.dto.response.SuscripcionSemanalResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SuscripcionSemanalServiceTest {

    @Mock
    private SuscripcionSemanalRepository suscripcionSemanalRepository;

    @InjectMocks
    private SuscripcionSemanalService suscripcionSemanalService;

    // ── Fixtures ──────────────────────────────────────────────────────────────

    public final SuscripcionSemanal PREPARED = SuscripcionSemanal.builder()
            .idSuscripcionSemanal(10)
            .identificadorCliente("Susana García")
            .uuidCliente(null)
            .semanaInicio(LocalDate.of(2025, 10, 27))
            .comidasPorDia(1)
            .precioSuscripcion(new BigDecimal("150.00"))
            .metodoPago(MetodoPago.EFECTIVO)
            .codigoRecuperacion("ABC12345")
            .activo(true)
            .origen(OrigenSuscripcion.COCINA)
            .build();

    public final SuscripcionSemanalRequestDTO DTO = crearDto("Susana García", 1,
            new BigDecimal("150.00"), MetodoPago.EFECTIVO, "ABC12345", true, OrigenSuscripcion.COCINA);

    public final SuscripcionSemanalRequestDTO DTO_MODIFICADO = crearDto("Susana García VIP", 2,
            new BigDecimal("180.00"), MetodoPago.TRANSFERENCIA, "ABC12345", true, OrigenSuscripcion.COCINA);

    private SuscripcionSemanalRequestDTO crearDto(String identificador, int comidas,
                                                   BigDecimal precio, MetodoPago metodo,
                                                   String codigo, boolean activo,
                                                   OrigenSuscripcion origen) {
        SuscripcionSemanalRequestDTO dto = new SuscripcionSemanalRequestDTO();
        dto.setIdentificadorCliente(identificador);
        dto.setSemanaInicio(LocalDate.of(2025, 10, 27));
        dto.setComidasPorDia(comidas);
        dto.setPrecioSuscripcion(precio);
        dto.setMetodoPago(metodo);
        dto.setCodigoRecuperacion(codigo);
        dto.setActivo(activo);
        dto.setOrigen(origen);
        return dto;
    }

    // ── findAll ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findAll - Debe retornar la lista de suscripciones registradas")
    public void findAll_retornaLista() {
        when(suscripcionSemanalRepository.findAll()).thenReturn(List.of(PREPARED));

        List<SuscripcionSemanalResponseDTO> result = suscripcionSemanalService.findAll();

        assertEquals(1, result.size());
        assertEquals("Susana García", result.get(0).getIdentificadorCliente());
        System.out.println("[OK] findAll retornó " + result.size() + " suscripción(es): "
                + result.get(0).getIdentificadorCliente());
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findById - Debe retornar la suscripción cuando el ID existe")
    public void findById_encontrado() {
        when(suscripcionSemanalRepository.findById(10)).thenReturn(Optional.of(PREPARED));

        SuscripcionSemanalResponseDTO result = suscripcionSemanalService.findById(10);

        assertNotNull(result);
        assertEquals(10, result.getIdSuscripcionSemanal());
        assertEquals("Susana García", result.getIdentificadorCliente());
        assertEquals("ABC12345", result.getCodigoRecuperacion());
        System.out.println("[OK] findById retornó suscripción id=" + result.getIdSuscripcionSemanal());
    }

    @Test
    @DisplayName("findById - Debe lanzar excepción cuando el ID no existe")
    public void findById_noEncontrado() {
        when(suscripcionSemanalRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> suscripcionSemanalService.findById(99));
        System.out.println("[OK] findById lanzó BusinessException para id=99");
    }

    // ── save ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("save - Debe guardar con el código explícito cuando no existe duplicado")
    public void save_codigoExplicitoExitoso() {
        when(suscripcionSemanalRepository.existsByCodigoRecuperacion("ABC12345")).thenReturn(false);
        when(suscripcionSemanalRepository.save(any(SuscripcionSemanal.class))).thenReturn(PREPARED);

        SuscripcionSemanalResponseDTO result = suscripcionSemanalService.save(DTO);

        assertNotNull(result);
        assertEquals("ABC12345", result.getCodigoRecuperacion());
        verify(suscripcionSemanalRepository).save(any(SuscripcionSemanal.class));
        System.out.println("[OK] save guardó suscripción con código: " + result.getCodigoRecuperacion());
    }

    @Test
    @DisplayName("save - Debe auto-generar código de 8 caracteres cuando no se provee uno")
    public void save_codigoAutogenerado() {
        SuscripcionSemanalRequestDTO dtoSinCodigo = crearDto("Carlos López", 1,
                new BigDecimal("100.00"), MetodoPago.EFECTIVO, null, true, OrigenSuscripcion.COCINA);

        SuscripcionSemanal guardado = SuscripcionSemanal.builder()
                .idSuscripcionSemanal(11)
                .identificadorCliente("Carlos López")
                .semanaInicio(LocalDate.of(2025, 10, 27))
                .comidasPorDia(1)
                .precioSuscripcion(new BigDecimal("100.00"))
                .metodoPago(MetodoPago.EFECTIVO)
                .codigoRecuperacion("XXXXXXXX")
                .activo(true)
                .origen(OrigenSuscripcion.COCINA)
                .build();

        when(suscripcionSemanalRepository.existsByCodigoRecuperacion(anyString())).thenReturn(false);
        when(suscripcionSemanalRepository.save(any(SuscripcionSemanal.class))).thenReturn(guardado);

        SuscripcionSemanalResponseDTO result = suscripcionSemanalService.save(dtoSinCodigo);

        assertNotNull(result);
        assertNotNull(result.getCodigoRecuperacion());
        // El código auto-generado es de 8 caracteres en mayúsculas
        assertEquals(8, result.getCodigoRecuperacion().length());
        assertEquals(result.getCodigoRecuperacion(), result.getCodigoRecuperacion().toUpperCase());
        System.out.println("[OK] save auto-generó código de 8 chars: " + result.getCodigoRecuperacion());
    }

    @Test
    @DisplayName("save - Debe lanzar excepción cuando el código de recuperación ya existe")
    public void save_codigoDuplicado() {
        when(suscripcionSemanalRepository.existsByCodigoRecuperacion("ABC12345")).thenReturn(true);

        assertThrows(BusinessException.class, () -> suscripcionSemanalService.save(DTO));
        verify(suscripcionSemanalRepository, never()).save(any(SuscripcionSemanal.class));
        System.out.println("[OK] save lanzó CONFLICT por código de recuperación duplicado");
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update - Debe actualizar los campos editables y preservar los inmutables")
    public void update_exitoso() {
        when(suscripcionSemanalRepository.findById(10)).thenReturn(Optional.of(PREPARED));
        SuscripcionSemanal actualizado = SuscripcionSemanal.builder()
                .idSuscripcionSemanal(10)
                .identificadorCliente("Susana García VIP")
                .uuidCliente(null)
                .semanaInicio(LocalDate.of(2025, 10, 27))
                .comidasPorDia(2)
                .precioSuscripcion(new BigDecimal("180.00"))
                .metodoPago(MetodoPago.TRANSFERENCIA)
                .codigoRecuperacion("ABC12345")
                .activo(true)
                .origen(OrigenSuscripcion.COCINA)
                .build();
        when(suscripcionSemanalRepository.save(any(SuscripcionSemanal.class))).thenReturn(actualizado);

        SuscripcionSemanalResponseDTO result = suscripcionSemanalService.update(10, DTO_MODIFICADO);

        assertNotNull(result);
        assertEquals("Susana García VIP", result.getIdentificadorCliente());
        assertEquals(2, result.getComidasPorDia());
        assertEquals(MetodoPago.TRANSFERENCIA, result.getMetodoPago());
        // codigoRecuperacion y semanaInicio permanecen intactos
        assertEquals("ABC12345", result.getCodigoRecuperacion());
        assertEquals(LocalDate.of(2025, 10, 27), result.getSemanaInicio());
        System.out.println("[OK] update aplicó cambios y preservó código e inmutables");
    }

    @Test
    @DisplayName("update - Debe lanzar excepción cuando el ID no existe")
    public void update_noEncontrado() {
        when(suscripcionSemanalRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> suscripcionSemanalService.update(99, DTO));
        verify(suscripcionSemanalRepository, never()).save(any(SuscripcionSemanal.class));
        System.out.println("[OK] update lanzó BusinessException para id=99");
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete - Debe eliminar la suscripción cuando el ID existe")
    public void delete_exitoso() {
        when(suscripcionSemanalRepository.existsById(10)).thenReturn(true);

        assertDoesNotThrow(() -> suscripcionSemanalService.delete(10));
        verify(suscripcionSemanalRepository).deleteById(10);
        System.out.println("[OK] delete eliminó suscripción id=10");
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción cuando el ID no existe")
    public void delete_noEncontrado() {
        when(suscripcionSemanalRepository.existsById(99)).thenReturn(false);

        assertThrows(BusinessException.class, () -> suscripcionSemanalService.delete(99));
        verify(suscripcionSemanalRepository, never()).deleteById(anyInt());
        System.out.println("[OK] delete lanzó BusinessException para id=99");
    }
}
