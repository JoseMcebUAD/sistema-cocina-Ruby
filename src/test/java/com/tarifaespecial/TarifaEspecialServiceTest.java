package com.tarifaespecial;

import com.cocinarubi.dao.TarifaEspecialRepository;
import com.cocinarubi.domain.entity.TarifaEspecial;
import com.cocinarubi.domain.service.TarifaEspecialService;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.TarifaEspecialRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TarifaEspecialServiceTest {

    @Mock
    private TarifaEspecialRepository tarifaEspecialRepository;

    @InjectMocks
    private TarifaEspecialService tarifaEspecialService;

    public TarifaEspecial TARIFA_PREPARED = TarifaEspecial.builder()
            .idTarifaLluvia(10)
            .nombreTarifa("Tarifa lluvia")
            .tarifa(BigDecimal.valueOf(15.00))
            .isActive(false)
            .build();

    public TarifaEspecialRequestDTO DTO_PREPARED = new TarifaEspecialRequestDTO("Tarifa lluvia", BigDecimal.valueOf(15.00), false);

    public TarifaEspecialRequestDTO DTO_MODIFIED_PREPARED = new TarifaEspecialRequestDTO("Tarifa festivo", BigDecimal.valueOf(25.00), true);

    @Test
    @DisplayName("findAll - Debe retornar la lista de tarifas especiales registradas")
    public void findAll() {
        when(tarifaEspecialRepository.findAll()).thenReturn(List.of(TARIFA_PREPARED));

        List<TarifaEspecial> result = tarifaEspecialService.findAll();

        assertEquals(1, result.size());
        assertEquals("Tarifa lluvia", result.get(0).getNombreTarifa());
        System.out.println("[OK] findAll retornó " + result.size() + " tarifa(s): " + result.get(0).getNombreTarifa());
    }

    @Test
    @DisplayName("findById - Debe retornar la tarifa cuando el ID existe")
    public void findById_encontrada() {
        when(tarifaEspecialRepository.findById(10)).thenReturn(Optional.of(TARIFA_PREPARED));

        TarifaEspecial result = tarifaEspecialService.findById(10);

        assertNotNull(result);
        assertEquals(10, result.getIdTarifaLluvia());
        assertEquals("Tarifa lluvia", result.getNombreTarifa());
        System.out.println("[OK] findById retornó tarifa id=" + result.getIdTarifaLluvia());
    }

    @Test
    @DisplayName("findById - Debe lanzar excepción cuando el ID no existe")
    public void findById_noEncontrada() {
        when(tarifaEspecialRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> tarifaEspecialService.findById(99));
        System.out.println("[OK] findById lanzó BusinessException para id=99");
    }

    @Test
    @DisplayName("save - Debe guardar y retornar la tarifa correctamente")
    public void saveTarifaEspecial() {
        when(tarifaEspecialRepository.save(any(TarifaEspecial.class))).thenReturn(TARIFA_PREPARED);

        TarifaEspecial result = tarifaEspecialService.save(DTO_PREPARED);

        assertNotNull(result);
        assertEquals("Tarifa lluvia", result.getNombreTarifa());
        verify(tarifaEspecialRepository).save(any(TarifaEspecial.class));
        System.out.println("[OK] save guardó tarifa: " + result.getNombreTarifa());
    }

    @Test
    @DisplayName("update - Debe actualizar y retornar la tarifa correctamente")
    public void updateTarifaEspecial() {
        when(tarifaEspecialRepository.findById(10)).thenReturn(Optional.of(TARIFA_PREPARED));
        TarifaEspecial tarifaActualizada = TarifaEspecial.builder()
                .idTarifaLluvia(10).nombreTarifa("Tarifa festivo").tarifa(BigDecimal.valueOf(25.00)).isActive(true).build();
        when(tarifaEspecialRepository.save(any(TarifaEspecial.class))).thenReturn(tarifaActualizada);

        TarifaEspecial result = tarifaEspecialService.update(10, DTO_MODIFIED_PREPARED);

        assertNotNull(result);
        assertEquals("Tarifa festivo", result.getNombreTarifa());
        assertTrue(result.isActive());
        System.out.println("[OK] update actualizó tarifa: " + result.getNombreTarifa() + " activa=" + result.isActive());
    }

    @Test
    @DisplayName("delete - Debe eliminar la tarifa cuando el ID existe")
    public void deleteTarifaEspecial() {
        when(tarifaEspecialRepository.existsById(10)).thenReturn(true);

        assertDoesNotThrow(() -> tarifaEspecialService.delete(10));
        verify(tarifaEspecialRepository).deleteById(10);
        System.out.println("[OK] delete eliminó tarifa id=10");
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción cuando el ID no existe")
    public void deleteTarifaEspecial_noEncontrada() {
        when(tarifaEspecialRepository.existsById(99)).thenReturn(false);

        assertThrows(BusinessException.class, () -> tarifaEspecialService.delete(99));
        verify(tarifaEspecialRepository, never()).deleteById(anyInt());
        System.out.println("[OK] delete lanzó BusinessException para id=99");
    }
}
