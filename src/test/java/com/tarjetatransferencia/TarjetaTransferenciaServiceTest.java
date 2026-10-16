package com.tarjetatransferencia;

import com.cocinarubi.DBConstants.Estatus;
import com.cocinarubi.dao.TarjetaTransferenciaRepository;
import com.cocinarubi.domain.entity.TarjetaTransferencia;
import com.cocinarubi.domain.service.TarjetaTransferenciaService;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.TarjetaTransferenciaEstatusRequestDTO;
import com.cocinarubi.presentation.dto.request.TarjetaTransferenciaRequestDTO;
import com.cocinarubi.presentation.dto.response.TarjetaTransferenciaActivaDTO;
import com.cocinarubi.presentation.dto.response.TarjetaTransferenciaResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TarjetaTransferenciaServiceTest {

    @Mock
    private TarjetaTransferenciaRepository tarjetaRepository;

    @InjectMocks
    private TarjetaTransferenciaService tarjetaService;

    // ── Fixtures — instancia nueva por cada test (PER_METHOD) ───────────────

    public TarjetaTransferencia TARJETA_INACTIVA = TarjetaTransferencia.builder()
            .idTarjeta(1)
            .clabe("123456789012345678")
            .numeroTarjeta("1234567890123456")
            .nombreDestinatario("Rubí García")
            .banco("BBVA")
            .estatus(Estatus.NO_DISPONIBLE)
            .build();

    public TarjetaTransferencia TARJETA_ACTIVA = TarjetaTransferencia.builder()
            .idTarjeta(2)
            .clabe("876543210987654321")
            .numeroTarjeta("9876543210987654")
            .nombreDestinatario("Rubí García")
            .banco("SANTANDER")
            .estatus(Estatus.DISPONIBLE)
            .build();

    private TarjetaTransferenciaRequestDTO buildRequestDTO() {
        TarjetaTransferenciaRequestDTO dto = new TarjetaTransferenciaRequestDTO();
        dto.setClabe("123456789012345678");
        dto.setNumeroTarjeta("1234567890123456");
        dto.setNombreDestinatario("Rubí García");
        dto.setBanco("BBVA");
        return dto;
    }

    private TarjetaTransferenciaEstatusRequestDTO estatusDto(Estatus estatus) {
        TarjetaTransferenciaEstatusRequestDTO dto = new TarjetaTransferenciaEstatusRequestDTO();
        dto.setEstatus(estatus);
        return dto;
    }

    // ── findAll ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findAll - Debe retornar todos los DTOs mapeados correctamente")
    public void findAll() {
        when(tarjetaRepository.findAll()).thenReturn(List.of(TARJETA_INACTIVA, TARJETA_ACTIVA));

        List<TarjetaTransferenciaResponseDTO> result = tarjetaService.findAll();

        assertEquals(2, result.size());
        assertEquals("BBVA", result.get(0).getBanco());
        assertEquals(Estatus.NO_DISPONIBLE, result.get(0).getEstatus());
        assertEquals(Estatus.DISPONIBLE, result.get(1).getEstatus());
        System.out.println("[OK] findAll retornó " + result.size() + " tarjeta(s)");
    }

    // ── save ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("save - Debe guardar la tarjeta con estatus NO_DISPONIBLE por defecto")
    public void save_creaConEstatusNoDisponible() {
        when(tarjetaRepository.save(any(TarjetaTransferencia.class))).thenReturn(TARJETA_INACTIVA);

        TarjetaTransferenciaResponseDTO result = tarjetaService.save(buildRequestDTO());

        assertNotNull(result);
        assertEquals(Estatus.NO_DISPONIBLE, result.getEstatus());
        // Verifica que la entidad persistida tenga NO_DISPONIBLE sin importar lo que venga en el request
        verify(tarjetaRepository).save(argThat(t -> t.getEstatus() == Estatus.NO_DISPONIBLE));
        System.out.println("[OK] save guardó tarjeta estatus=" + result.getEstatus());
    }

    // ── cambiarEstatus ────────────────────────────────────────────────────────

    @Test
    @DisplayName("cambiarEstatus - Debe lanzar 400 antes de buscar la tarjeta si el estatus es AGOTADO")
    public void cambiarEstatus_AGOTADO_lanza400() {
        assertThrows(BusinessException.class,
                () -> tarjetaService.cambiarEstatus(1, estatusDto(Estatus.AGOTADO)));

        // La validación ocurre antes de consultar el repositorio
        verify(tarjetaRepository, never()).findById(anyInt());
        System.out.println("[OK] cambiarEstatus AGOTADO lanzó BusinessException sin consultar BD");
    }

    @Test
    @DisplayName("cambiarEstatus - Debe lanzar 404 cuando el ID no existe")
    public void cambiarEstatus_idNoExiste_lanza404() {
        when(tarjetaRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
                () -> tarjetaService.cambiarEstatus(99, estatusDto(Estatus.DISPONIBLE)));
        System.out.println("[OK] cambiarEstatus id=99 inexistente lanzó BusinessException");
    }

    @Test
    @DisplayName("cambiarEstatus - Activar tarjeta sin otra activa desactiva todas y activa la indicada")
    public void cambiarEstatus_DISPONIBLE_desactivaTodasPrimero() {
        when(tarjetaRepository.findById(1)).thenReturn(Optional.of(TARJETA_INACTIVA));
        when(tarjetaRepository.save(any(TarjetaTransferencia.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        TarjetaTransferenciaResponseDTO result =
                tarjetaService.cambiarEstatus(1, estatusDto(Estatus.DISPONIBLE));

        verify(tarjetaRepository).updateAllEstatus(Estatus.NO_DISPONIBLE);
        assertEquals(Estatus.DISPONIBLE, result.getEstatus());
        System.out.println("[OK] cambiarEstatus DISPONIBLE → updateAllEstatus + tarjeta activada id=1");
    }

    @Test
    @DisplayName("cambiarEstatus - Activar tarjeta NO_DISPONIBLE mientras hay otra DISPONIBLE provoca intercambio")
    public void cambiarEstatus_DISPONIBLE_intercambia_conOtraActiva() {
        // Escenario: id=2 ya está DISPONIBLE, se activa id=1 (NO_DISPONIBLE)
        when(tarjetaRepository.findById(1)).thenReturn(Optional.of(TARJETA_INACTIVA));
        when(tarjetaRepository.save(any(TarjetaTransferencia.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        TarjetaTransferenciaResponseDTO result =
                tarjetaService.cambiarEstatus(1, estatusDto(Estatus.DISPONIBLE));

        // updateAllEstatus(NO_DISPONIBLE) desactiva a id=2 antes de activar id=1
        verify(tarjetaRepository).updateAllEstatus(Estatus.NO_DISPONIBLE);
        // La tarjeta recién activada queda DISPONIBLE
        assertEquals(Estatus.DISPONIBLE, result.getEstatus());
        assertEquals(1, result.getIdTarjeta());
        System.out.println("[OK] intercambio: id=1 pasó a DISPONIBLE, updateAllEstatus desactivó id=2");
    }

    @Test
    @DisplayName("cambiarEstatus - Desactivar tarjeta es posible si hay otra con estatus DISPONIBLE")
    public void cambiarEstatus_noDisponible_conOtraActiva_exitoso() {
        when(tarjetaRepository.findById(2)).thenReturn(Optional.of(TARJETA_ACTIVA));
        // Hay 2 activas: la que se desactiva y al menos otra → invariante se mantiene
        when(tarjetaRepository.countByEstatus(Estatus.DISPONIBLE)).thenReturn(2L);
        when(tarjetaRepository.save(any(TarjetaTransferencia.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> tarjetaService.cambiarEstatus(2, estatusDto(Estatus.NO_DISPONIBLE)));
        verify(tarjetaRepository).save(any());
        System.out.println("[OK] cambiarEstatus NO_DISPONIBLE con otra activa existente → sin error");
    }

    @Test
    @DisplayName("cambiarEstatus - Debe lanzar 409 al intentar desactivar la única tarjeta activa")
    public void cambiarEstatus_noDisponible_unicaActiva_lanza409() {
        when(tarjetaRepository.findById(2)).thenReturn(Optional.of(TARJETA_ACTIVA));
        when(tarjetaRepository.countByEstatus(Estatus.DISPONIBLE)).thenReturn(1L);

        assertThrows(BusinessException.class,
                () -> tarjetaService.cambiarEstatus(2, estatusDto(Estatus.NO_DISPONIBLE)));

        verify(tarjetaRepository, never()).save(any());
        System.out.println("[OK] cambiarEstatus única activa → 409, save nunca llamado");
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete - Debe eliminar correctamente una tarjeta con estatus NO_DISPONIBLE")
    public void delete_noDisponible_exitoso() {
        when(tarjetaRepository.findById(1)).thenReturn(Optional.of(TARJETA_INACTIVA));

        assertDoesNotThrow(() -> tarjetaService.delete(1));
        verify(tarjetaRepository).deleteById(1);
        System.out.println("[OK] delete tarjeta NO_DISPONIBLE eliminada id=1");
    }

    @Test
    @DisplayName("delete - Debe lanzar 409 al intentar eliminar la única tarjeta activa")
    public void delete_unicaActiva_lanza409() {
        when(tarjetaRepository.findById(2)).thenReturn(Optional.of(TARJETA_ACTIVA));
        when(tarjetaRepository.countByEstatus(Estatus.DISPONIBLE)).thenReturn(1L);

        assertThrows(BusinessException.class, () -> tarjetaService.delete(2));

        verify(tarjetaRepository, never()).deleteById(anyInt());
        System.out.println("[OK] delete única activa → 409, deleteById nunca llamado");
    }

    // ── obtenerActiva ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("obtenerActiva - Debe retornar DTO con los 4 campos sin exponer idTarjeta ni estatus")
    public void obtenerActiva_exitoso() {
        when(tarjetaRepository.findFirstByEstatus(Estatus.DISPONIBLE))
                .thenReturn(Optional.of(TARJETA_ACTIVA));

        TarjetaTransferenciaActivaDTO result = tarjetaService.obtenerActiva();

        assertNotNull(result);
        assertEquals("876543210987654321", result.getClabe());
        assertEquals("9876543210987654", result.getNumeroTarjeta());
        assertEquals("Rubí García", result.getNombreDestinatario());
        assertEquals("SANTANDER", result.getBanco());
        System.out.println("[OK] obtenerActiva banco=" + result.getBanco() + " clabe=" + result.getClabe());
    }

    @Test
    @DisplayName("obtenerActiva - Debe lanzar 404 si no hay ninguna tarjeta con estatus DISPONIBLE")
    public void obtenerActiva_sinDisponible_lanza404() {
        when(tarjetaRepository.findFirstByEstatus(Estatus.DISPONIBLE)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> tarjetaService.obtenerActiva());
        System.out.println("[OK] obtenerActiva sin tarjeta activa → 404");
    }
}
