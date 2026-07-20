package com.complemento;

import com.cocinarubi.DBConstants.Estatus;
import com.cocinarubi.dao.ComplementoRepository;
import com.cocinarubi.domain.entity.Complemento;
import com.cocinarubi.domain.service.ComplementoService;
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
public class ComplementoServiceTest {

    @Mock
    private ComplementoRepository complementoRepository;

    @InjectMocks
    private ComplementoService complementoService;

    public Complemento COMPLEMENTO_PREPARED = Complemento.builder()
            .idComplemento(10)
            .uuidComplemento("uuid-comp-10")
            .nombreComplemento("Arroz")
            .descripcion("Arroz blanco de acompañamiento")
            .precioExtra(BigDecimal.ZERO)
            .estatus(Estatus.DISPONIBLE)
            .destacado(false)
            .cobrarSiempre(false)
            .build();

    @Test
    @DisplayName("findAll - Debe retornar la lista de complementos registrados")
    public void findAll() {
        when(complementoRepository.findAll()).thenReturn(List.of(COMPLEMENTO_PREPARED));

        List<Complemento> result = complementoService.findAll();

        assertEquals(1, result.size());
        assertEquals("Arroz", result.get(0).getNombreComplemento());
        System.out.println("[OK] findAll retornó " + result.size() + " complemento(s): " + result.get(0).getNombreComplemento());
    }

    @Test
    @DisplayName("findById - Debe retornar el complemento cuando el ID existe")
    public void findById_encontrado() {
        when(complementoRepository.findById(10)).thenReturn(Optional.of(COMPLEMENTO_PREPARED));

        Complemento result = complementoService.findById(10);

        assertNotNull(result);
        assertEquals(10, result.getIdComplemento());
        assertEquals("Arroz", result.getNombreComplemento());
        System.out.println("[OK] findById retornó complemento id=" + result.getIdComplemento());
    }

    @Test
    @DisplayName("findById - Debe lanzar excepción cuando el ID no existe")
    public void findById_noEncontrado() {
        when(complementoRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> complementoService.findById(99));
        System.out.println("[OK] findById lanzó BusinessException para id=99");
    }

    @Test
    @DisplayName("save - Debe guardar y retornar el complemento correctamente")
    public void saveComplemento() {
        when(complementoRepository.save(COMPLEMENTO_PREPARED)).thenReturn(COMPLEMENTO_PREPARED);

        Complemento result = complementoService.save(COMPLEMENTO_PREPARED);

        assertNotNull(result);
        assertEquals("Arroz", result.getNombreComplemento());
        verify(complementoRepository).save(COMPLEMENTO_PREPARED);
        System.out.println("[OK] save guardó complemento: " + result.getNombreComplemento());
    }

    @Test
    @DisplayName("delete - Debe eliminar el complemento cuando el ID existe y no tiene referencias")
    public void deleteComplemento() {
        when(complementoRepository.existsById(10)).thenReturn(true);
        when(complementoRepository.countEnBasicos(10)).thenReturn(0L);
        when(complementoRepository.countEnPedidos(10)).thenReturn(0L);

        assertDoesNotThrow(() -> complementoService.delete(10));
        verify(complementoRepository).deleteById(10);
        System.out.println("[OK] delete eliminó complemento id=10");
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción cuando el ID no existe")
    public void deleteComplemento_noEncontrado() {
        when(complementoRepository.existsById(99)).thenReturn(false);

        assertThrows(BusinessException.class, () -> complementoService.delete(99));
        verify(complementoRepository, never()).deleteById(anyInt());
        System.out.println("[OK] delete lanzó BusinessException para id=99");
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción cuando el complemento está en paquetes básicos")
    public void deleteComplemento_enBasicos() {
        when(complementoRepository.existsById(10)).thenReturn(true);
        when(complementoRepository.countEnBasicos(10)).thenReturn(2L);

        assertThrows(BusinessException.class, () -> complementoService.delete(10));
        verify(complementoRepository, never()).deleteById(anyInt());
        System.out.println("[OK] delete lanzó CONFLICT por complemento en básicos");
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción cuando el complemento está referenciado en pedidos")
    public void deleteComplemento_enPedidos() {
        when(complementoRepository.existsById(10)).thenReturn(true);
        when(complementoRepository.countEnBasicos(10)).thenReturn(0L);
        when(complementoRepository.countEnPedidos(10)).thenReturn(3L);

        assertThrows(BusinessException.class, () -> complementoService.delete(10));
        verify(complementoRepository, never()).deleteById(anyInt());
        System.out.println("[OK] delete lanzó CONFLICT por complemento en pedidos");
    }
}
