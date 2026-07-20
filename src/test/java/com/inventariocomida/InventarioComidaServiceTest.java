package com.inventariocomida;

import com.cocinarubi.DBConstants.TipoContadorComida;
import com.cocinarubi.dao.InventarioComidaRepository;
import com.cocinarubi.domain.entity.Comida;
import com.cocinarubi.domain.entity.InventarioComida;
import com.cocinarubi.domain.service.ComidaService;
import com.cocinarubi.domain.service.InventarioComidaService;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.InventarioComidaRequestDTO;
import com.cocinarubi.presentation.dto.response.InventarioComidaResponseDTO;
import com.cocinarubi.presentation.strategy.strategyImplementation.InventarioComidaValidationImp;
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
public class InventarioComidaServiceTest {

    @Mock
    private InventarioComidaRepository inventarioComidaRepository;

    @Mock
    private ComidaService comidaService;

    @Mock
    private InventarioComidaValidationImp inventarioComidaValidation;

    @InjectMocks
    private InventarioComidaService inventarioComidaService;

    public Comida COMIDA_PREPARED = Comida.builder()
            .idComida(5)
            .nombreComida("Pollo en salsa")
            .build();

    public InventarioComida INVENTARIO_PREPARED = InventarioComida.builder()
            .idInventarioComida(10)
            .comida(COMIDA_PREPARED)
            .cantidad(3)
            .tipo_contador_comida(TipoContadorComida.UNIDAD)
            .build();

    public InventarioComidaRequestDTO DTO = crearDto(5, 3, TipoContadorComida.UNIDAD);
    public InventarioComidaRequestDTO DTO_MODIFIED = crearDto(5, 10, TipoContadorComida.KILOGRAMO);

    private InventarioComidaRequestDTO crearDto(int idComida, int cantidad, TipoContadorComida tipo) {
        InventarioComidaRequestDTO dto = new InventarioComidaRequestDTO();
        dto.setIdComida(idComida);
        dto.setCantidad(cantidad);
        dto.setTipoContadorComida(tipo);
        return dto;
    }

    @Test
    @DisplayName("findAll - Debe retornar la lista de registros de inventario")
    public void findAll() {
        when(inventarioComidaRepository.findAll()).thenReturn(List.of(INVENTARIO_PREPARED));

        List<InventarioComidaResponseDTO> result = inventarioComidaService.findAll();

        assertEquals(1, result.size());
        assertEquals("Pollo en salsa", result.get(0).getNombreComida());
        assertEquals(3, result.get(0).getCantidad());
        System.out.println("[OK] findAll retornó " + result.size() + " registro(s): comida=" + result.get(0).getNombreComida());
    }

    @Test
    @DisplayName("findById - Debe retornar el registro cuando el ID existe")
    public void findById_encontrado() {
        when(inventarioComidaRepository.findById(10)).thenReturn(Optional.of(INVENTARIO_PREPARED));

        InventarioComidaResponseDTO result = inventarioComidaService.findById(10);

        assertNotNull(result);
        assertEquals(10, result.getIdInventarioComida());
        assertEquals(TipoContadorComida.UNIDAD, result.getTipoContadorComida());
        System.out.println("[OK] findById retornó inventario id=" + result.getIdInventarioComida());
    }

    @Test
    @DisplayName("findById - Debe lanzar excepción cuando el ID no existe")
    public void findById_noEncontrado() {
        when(inventarioComidaRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> inventarioComidaService.findById(99));
        System.out.println("[OK] findById lanzó BusinessException para id=99");
    }

    @Test
    @DisplayName("findByComida - Debe retornar los registros de inventario de una comida")
    public void findByComida() {
        when(inventarioComidaRepository.findByComidaIdComida(5)).thenReturn(List.of(INVENTARIO_PREPARED));

        List<InventarioComidaResponseDTO> result = inventarioComidaService.findByComida(5);

        assertEquals(1, result.size());
        assertEquals(5, result.get(0).getIdComida());
        System.out.println("[OK] findByComida retornó " + result.size() + " registro(s) para comida id=5");
    }

    @Test
    @DisplayName("save - Debe guardar y retornar el registro de inventario correctamente")
    public void save_exitoso() {
        when(comidaService.findById(5)).thenReturn(COMIDA_PREPARED);
        when(inventarioComidaRepository.save(any(InventarioComida.class))).thenReturn(INVENTARIO_PREPARED);

        InventarioComidaResponseDTO result = inventarioComidaService.save(DTO);

        assertNotNull(result);
        assertEquals(3, result.getCantidad());
        assertEquals(TipoContadorComida.UNIDAD, result.getTipoContadorComida());
        verify(inventarioComidaRepository).save(any(InventarioComida.class));
        System.out.println("[OK] save guardó inventario: cantidad=" + result.getCantidad() + ", tipo=" + result.getTipoContadorComida());
    }

    @Test
    @DisplayName("update - Debe actualizar y retornar el registro de inventario correctamente")
    public void update_exitoso() {
        when(inventarioComidaRepository.findById(10)).thenReturn(Optional.of(INVENTARIO_PREPARED));
        when(comidaService.findById(5)).thenReturn(COMIDA_PREPARED);
        InventarioComida actualizado = InventarioComida.builder()
                .idInventarioComida(10)
                .comida(COMIDA_PREPARED)
                .cantidad(10)
                .tipo_contador_comida(TipoContadorComida.KILOGRAMO)
                .build();
        when(inventarioComidaRepository.save(any(InventarioComida.class))).thenReturn(actualizado);

        InventarioComidaResponseDTO result = inventarioComidaService.update(10, DTO_MODIFIED);

        assertNotNull(result);
        assertEquals(10, result.getCantidad());
        assertEquals(TipoContadorComida.KILOGRAMO, result.getTipoContadorComida());
        System.out.println("[OK] update actualizó inventario: cantidad=" + result.getCantidad() + ", tipo=" + result.getTipoContadorComida());
    }

    @Test
    @DisplayName("delete - Debe eliminar el registro cuando el ID existe")
    public void delete_exitoso() {
        when(inventarioComidaRepository.existsById(10)).thenReturn(true);

        assertDoesNotThrow(() -> inventarioComidaService.delete(10));
        verify(inventarioComidaRepository).deleteById(10);
        System.out.println("[OK] delete eliminó inventario id=10");
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción cuando el ID no existe")
    public void delete_noEncontrado() {
        when(inventarioComidaRepository.existsById(99)).thenReturn(false);

        assertThrows(BusinessException.class, () -> inventarioComidaService.delete(99));
        verify(inventarioComidaRepository, never()).deleteById(anyInt());
        System.out.println("[OK] delete lanzó BusinessException para id=99");
    }
}
