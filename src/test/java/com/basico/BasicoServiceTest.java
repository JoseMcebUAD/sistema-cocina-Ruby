package com.basico;

import com.cocinarubi.DBConstants.Estatus;
import com.cocinarubi.dao.BasicoRepository;
import com.cocinarubi.domain.entity.Basico;
import com.cocinarubi.domain.entity.Comida;
import com.cocinarubi.domain.service.BasicoService;
import com.cocinarubi.domain.service.ComidaService;
import com.cocinarubi.domain.service.ComplementoService;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.BasicoRequestDTO;
import com.cocinarubi.presentation.dto.response.BasicoResponseDTO;
import com.cocinarubi.presentation.strategy.strategyImplementation.BasicoConfirmationImp;
import com.cocinarubi.presentation.strategy.strategyImplementation.BasicoValidationImp;
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
public class BasicoServiceTest {

    @Mock
    private BasicoRepository basicoRepository;

    @Mock
    private ComidaService comidaService;

    @Mock
    private ComplementoService complementoService;

    @Mock
    private BasicoValidationImp basicoValidation;

    @Mock
    private BasicoConfirmationImp basicoConfirmation;

    @InjectMocks
    private BasicoService basicoService;

    public Comida COMIDA_PREPARED = Comida.builder()
            .idComida(5)
            .nombreComida("Pollo en salsa")
            .build();

    public Basico BASICO_PREPARED = Basico.builder()
            .idBasico(1)
            .comida(COMIDA_PREPARED)
            .descripcion("Paquete con arroz y agua")
            .destacado(false)
            .precioBasico(BigDecimal.valueOf(65))
            .estatus(Estatus.DISPONIBLE)
            .build();

    public BasicoRequestDTO DTO_PREPARED = new BasicoRequestDTO(5, "Paquete con arroz y agua", false, BigDecimal.valueOf(65), Estatus.DISPONIBLE, null);

    public BasicoRequestDTO DTO_MODIFIED_PREPARED = new BasicoRequestDTO(5, "Paquete especial", true, BigDecimal.valueOf(75), Estatus.DISPONIBLE, null);

    @Test
    @DisplayName("findAll - Debe retornar la lista de básicos registrados")
    public void findAll() {
        when(basicoRepository.findAll()).thenReturn(List.of(BASICO_PREPARED));

        List<BasicoResponseDTO> result = basicoService.findAll();

        assertEquals(1, result.size());
        assertEquals("Pollo en salsa", result.get(0).getNombreComida());
        System.out.println("[OK] findAll retornó " + result.size() + " básico(s): " + result.get(0).getNombreComida());
    }

    @Test
    @DisplayName("findById - Debe retornar el básico cuando el ID existe")
    public void findById_encontrado() {
        when(basicoRepository.findByIdWithComplementos(1)).thenReturn(Optional.of(BASICO_PREPARED));

        BasicoResponseDTO result = basicoService.findById(1);

        assertNotNull(result);
        assertEquals(1, result.getIdBasico());
        assertEquals("Pollo en salsa", result.getNombreComida());
        System.out.println("[OK] findById retornó básico id=" + result.getIdBasico());
    }

    @Test
    @DisplayName("findById - Debe lanzar excepción cuando el ID no existe")
    public void findById_noEncontrado() {
        when(basicoRepository.findByIdWithComplementos(99)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> basicoService.findById(99));
        System.out.println("[OK] findById lanzó BusinessException para id=99");
    }

    @Test
    @DisplayName("save - Debe guardar y retornar el básico correctamente")
    public void saveBasico() {
        when(comidaService.findById(5)).thenReturn(COMIDA_PREPARED);
        when(basicoRepository.save(any(Basico.class))).thenReturn(BASICO_PREPARED);

        BasicoResponseDTO result = basicoService.save(DTO_PREPARED);

        assertNotNull(result);
        assertEquals("Pollo en salsa", result.getNombreComida());
        verify(basicoRepository).save(any(Basico.class));
        System.out.println("[OK] save guardó básico: " + result.getNombreComida());
    }

    @Test
    @DisplayName("update - Debe actualizar y retornar el básico correctamente")
    public void updateBasico() {
        when(basicoRepository.findByIdWithComplementos(1)).thenReturn(Optional.of(BASICO_PREPARED));
        when(comidaService.findById(5)).thenReturn(COMIDA_PREPARED);
        Basico basicoActualizado = Basico.builder()
                .idBasico(1).comida(COMIDA_PREPARED).descripcion("Paquete especial").destacado(true).precioBasico(BigDecimal.valueOf(75)).estatus(Estatus.DISPONIBLE).build();
        when(basicoRepository.save(any(Basico.class))).thenReturn(basicoActualizado);

        BasicoResponseDTO result = basicoService.update(1, DTO_MODIFIED_PREPARED);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(75), result.getPrecioBasico());
        System.out.println("[OK] update actualizó básico: precio=" + result.getPrecioBasico());
    }

    @Test
    @DisplayName("delete - Debe eliminar el básico cuando el ID existe")
    public void deleteBasico() {
        when(basicoRepository.existsById(1)).thenReturn(true);

        assertDoesNotThrow(() -> basicoService.delete(1));
        verify(basicoRepository).deleteById(1);
        System.out.println("[OK] delete eliminó básico id=1");
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción cuando el ID no existe")
    public void deleteBasico_noEncontrado() {
        when(basicoRepository.existsById(99)).thenReturn(false);

        assertThrows(BusinessException.class, () -> basicoService.delete(99));
        verify(basicoRepository, never()).deleteById(anyInt());
        System.out.println("[OK] delete lanzó BusinessException para id=99");
    }
}
