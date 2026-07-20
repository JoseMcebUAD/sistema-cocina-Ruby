package com.comida;

import com.cocinarubi.DBConstants.Estatus;
import com.cocinarubi.dao.ComidaRepository;
import com.cocinarubi.domain.entity.Comida;
import com.cocinarubi.domain.service.ComidaService;
import com.cocinarubi.exception.BusinessException;
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
public class ComidaServiceTest {

    @Mock
    private ComidaRepository comidaRepository;

    @InjectMocks
    private ComidaService comidaService;

    public Comida COMIDA_PREPARED = Comida.builder()
            .idComida(10)
            .uuidComida("uuid-com-10")
            .nombreComida("Pollo en salsa roja")
            .descripcion("Con arroz y frijoles")
            .precioMedia(BigDecimal.valueOf(55))
            .precioEntera(BigDecimal.valueOf(90))
            .estatus(Estatus.DISPONIBLE)
            .destacado(true)
            .build();

    @Test
    @DisplayName("findAll - Debe retornar la lista de comidas registradas")
    public void findAll() {
        when(comidaRepository.findAll()).thenReturn(List.of(COMIDA_PREPARED));

        List<Comida> result = comidaService.findAll();

        assertEquals(1, result.size());
        assertEquals("Pollo en salsa roja", result.get(0).getNombreComida());
        System.out.println("[OK] findAll retornó " + result.size() + " comida(s): " + result.get(0).getNombreComida());
    }

    @Test
    @DisplayName("findById - Debe retornar la comida cuando el ID existe")
    public void findById_encontrado() {
        when(comidaRepository.findById(10)).thenReturn(Optional.of(COMIDA_PREPARED));

        Comida result = comidaService.findById(10);

        assertNotNull(result);
        assertEquals(10, result.getIdComida());
        assertEquals(Estatus.DISPONIBLE, result.getEstatus());
        System.out.println("[OK] findById retornó comida id=" + result.getIdComida());
    }

    @Test
    @DisplayName("findById - Debe lanzar excepción cuando el ID no existe")
    public void findById_noEncontrado() {
        when(comidaRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> comidaService.findById(99));
        System.out.println("[OK] findById lanzó BusinessException para id=99");
    }

    @Test
    @DisplayName("save - Debe guardar y retornar la comida correctamente")
    public void saveComida() {
        when(comidaRepository.save(COMIDA_PREPARED)).thenReturn(COMIDA_PREPARED);

        Comida result = comidaService.save(COMIDA_PREPARED);

        assertNotNull(result);
        assertEquals("Pollo en salsa roja", result.getNombreComida());
        verify(comidaRepository).save(COMIDA_PREPARED);
        System.out.println("[OK] save guardó comida: " + result.getNombreComida());
    }

    @Test
    @DisplayName("delete - Debe eliminar la comida cuando no tiene referencias")
    public void deleteComida() {
        when(comidaRepository.existsById(10)).thenReturn(true);
        when(comidaRepository.countEnPedidos(10)).thenReturn(0L);
        when(comidaRepository.countEnBasicos(10)).thenReturn(0L);

        assertDoesNotThrow(() -> comidaService.delete(10));
        verify(comidaRepository).deleteById(10);
        System.out.println("[OK] delete eliminó comida id=10");
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción cuando el ID no existe")
    public void deleteComida_noEncontrado() {
        when(comidaRepository.existsById(99)).thenReturn(false);

        assertThrows(BusinessException.class, () -> comidaService.delete(99));
        verify(comidaRepository, never()).deleteById(anyInt());
        System.out.println("[OK] delete lanzó BusinessException para id=99");
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción cuando la comida está referenciada en pedidos")
    public void deleteComida_enPedidos() {
        when(comidaRepository.existsById(10)).thenReturn(true);
        when(comidaRepository.countEnPedidos(10)).thenReturn(5L);

        assertThrows(BusinessException.class, () -> comidaService.delete(10));
        verify(comidaRepository, never()).deleteById(anyInt());
        System.out.println("[OK] delete lanzó CONFLICT por comida en pedidos");
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción cuando la comida está referenciada en paquetes básicos")
    public void deleteComida_enBasicos() {
        when(comidaRepository.existsById(10)).thenReturn(true);
        when(comidaRepository.countEnPedidos(10)).thenReturn(0L);
        when(comidaRepository.countEnBasicos(10)).thenReturn(2L);

        assertThrows(BusinessException.class, () -> comidaService.delete(10));
        verify(comidaRepository, never()).deleteById(anyInt());
        System.out.println("[OK] delete lanzó CONFLICT por comida en básicos");
    }
}
