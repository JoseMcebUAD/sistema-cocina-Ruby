package com.horarioatencion;

import com.cocinarubi.DBConstants.TipoHorario;
import com.cocinarubi.dao.HorarioAtencionRepository;
import com.cocinarubi.domain.entity.HorarioAtencion;
import com.cocinarubi.domain.service.HorarioAtencionService;
import com.cocinarubi.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HorarioAtencionServiceTest {

    @Mock
    private HorarioAtencionRepository horarioAtencionRepository;

    @InjectMocks
    private HorarioAtencionService horarioAtencionService;

    public HorarioAtencion HORARIO_PREPARED = HorarioAtencion.builder()
            .idHorarioAtencionComidas(10)
            .horaInicioAtencionComidas(LocalTime.of(8, 30))
            .horaCierreAtencionComidas(LocalTime.of(15, 30))
            .diaSemana("L")
            .tipoHorario(TipoHorario.COMIDAS)
            .atendiendo(true)
            .build();

    public HorarioAtencion HORARIO_MODIFIED_PREPARED = HorarioAtencion.builder()
            .idHorarioAtencionComidas(10)
            .horaInicioAtencionComidas(LocalTime.of(9, 0))
            .horaCierreAtencionComidas(LocalTime.of(14, 0))
            .diaSemana("L")
            .tipoHorario(TipoHorario.COMIDAS)
            .atendiendo(false)
            .build();

    @Test
    @DisplayName("findAll - Debe retornar la lista de horarios de atención registrados")
    public void findAll() {
        when(horarioAtencionRepository.findAll()).thenReturn(List.of(HORARIO_PREPARED));

        List<HorarioAtencion> result = horarioAtencionService.findAll();

        assertEquals(1, result.size());
        assertEquals("L", result.get(0).getDiaSemana());
        assertEquals(TipoHorario.COMIDAS, result.get(0).getTipoHorario());
        System.out.println("[OK] findAll retornó " + result.size() + " horario(s): día=" + result.get(0).getDiaSemana());
    }

    @Test
    @DisplayName("findById - Debe retornar el horario cuando el ID existe")
    public void findById_encontrado() {
        when(horarioAtencionRepository.findById(10)).thenReturn(Optional.of(HORARIO_PREPARED));

        HorarioAtencion result = horarioAtencionService.findById(10);

        assertNotNull(result);
        assertEquals(10, result.getIdHorarioAtencionComidas());
        assertTrue(result.isAtendiendo());
        System.out.println("[OK] findById retornó horario id=" + result.getIdHorarioAtencionComidas());
    }

    @Test
    @DisplayName("findById - Debe lanzar excepción cuando el ID no existe")
    public void findById_noEncontrado() {
        when(horarioAtencionRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> horarioAtencionService.findById(99));
        System.out.println("[OK] findById lanzó BusinessException para id=99");
    }

    @Test
    @DisplayName("save - Debe guardar y retornar el horario correctamente")
    public void saveHorarioAtencion() {
        when(horarioAtencionRepository.save(HORARIO_PREPARED)).thenReturn(HORARIO_PREPARED);

        HorarioAtencion result = horarioAtencionService.save(HORARIO_PREPARED);

        assertNotNull(result);
        assertEquals(TipoHorario.COMIDAS, result.getTipoHorario());
        verify(horarioAtencionRepository).save(HORARIO_PREPARED);
        System.out.println("[OK] save guardó horario: tipo=" + result.getTipoHorario() + " día=" + result.getDiaSemana());
    }

    @Test
    @DisplayName("delete - Debe eliminar el horario cuando el ID existe")
    public void deleteHorarioAtencion() {
        when(horarioAtencionRepository.existsById(10)).thenReturn(true);

        assertDoesNotThrow(() -> horarioAtencionService.delete(10));
        verify(horarioAtencionRepository).deleteById(10);
        System.out.println("[OK] delete eliminó horario id=10");
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción cuando el ID no existe")
    public void deleteHorarioAtencion_noEncontrado() {
        when(horarioAtencionRepository.existsById(99)).thenReturn(false);

        assertThrows(BusinessException.class, () -> horarioAtencionService.delete(99));
        verify(horarioAtencionRepository, never()).deleteById(anyInt());
        System.out.println("[OK] delete lanzó BusinessException para id=99");
    }
}
