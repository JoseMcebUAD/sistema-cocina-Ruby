package com.desayuno;

import com.cocinarubi.DBConstants.Estatus;
import com.cocinarubi.dao.DesayunoRepository;
import com.cocinarubi.domain.entity.Desayuno;
import com.cocinarubi.domain.service.DesayunoService;
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
public class DesayunoServiceTest {

    @Mock
    private DesayunoRepository desayunoRepository;

    @InjectMocks
    private DesayunoService desayunoService;

    public Desayuno DESAYUNO_PREPARED = Desayuno.builder()
            .idDesayuno(10)
            .uuidDesayuno("uuid-des-10")
            .nombreDesayuno("Chilaquiles verdes")
            .descripcion("Con pollo y crema")
            .precioMedia(BigDecimal.valueOf(35))
            .precioEntera(BigDecimal.valueOf(60))
            .estatus(Estatus.DISPONIBLE)
            .destacado(false)
            .build();

    @Test
    @DisplayName("findAll - Debe retornar la lista de desayunos registrados")
    public void findAll() {
        when(desayunoRepository.findAll()).thenReturn(List.of(DESAYUNO_PREPARED));

        List<Desayuno> result = desayunoService.findAll();

        assertEquals(1, result.size());
        assertEquals("Chilaquiles verdes", result.get(0).getNombreDesayuno());
        System.out.println("[OK] findAll retornó " + result.size() + " desayuno(s): " + result.get(0).getNombreDesayuno());
    }

    @Test
    @DisplayName("findById - Debe retornar el desayuno cuando el ID existe")
    public void findById_encontrado() {
        when(desayunoRepository.findById(10)).thenReturn(Optional.of(DESAYUNO_PREPARED));

        Desayuno result = desayunoService.findById(10);

        assertNotNull(result);
        assertEquals(10, result.getIdDesayuno());
        assertEquals("Chilaquiles verdes", result.getNombreDesayuno());
        System.out.println("[OK] findById retornó desayuno id=" + result.getIdDesayuno());
    }

    @Test
    @DisplayName("findById - Debe lanzar excepción cuando el ID no existe")
    public void findById_noEncontrado() {
        when(desayunoRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> desayunoService.findById(99));
        System.out.println("[OK] findById lanzó BusinessException para id=99");
    }

    @Test
    @DisplayName("save - Debe guardar y retornar el desayuno correctamente")
    public void saveDesayuno() {
        when(desayunoRepository.save(DESAYUNO_PREPARED)).thenReturn(DESAYUNO_PREPARED);

        Desayuno result = desayunoService.save(DESAYUNO_PREPARED);

        assertNotNull(result);
        assertEquals("Chilaquiles verdes", result.getNombreDesayuno());
        verify(desayunoRepository).save(DESAYUNO_PREPARED);
        System.out.println("[OK] save guardó desayuno: " + result.getNombreDesayuno());
    }

    @Test
    @DisplayName("delete - Debe eliminar el desayuno cuando el ID existe y no tiene referencias")
    public void deleteDesayuno() {
        when(desayunoRepository.existsById(10)).thenReturn(true);
        when(desayunoRepository.countEnPedidos(10)).thenReturn(0L);

        assertDoesNotThrow(() -> desayunoService.delete(10));
        verify(desayunoRepository).deleteById(10);
        System.out.println("[OK] delete eliminó desayuno id=10");
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción cuando el ID no existe")
    public void deleteDesayuno_noEncontrado() {
        when(desayunoRepository.existsById(99)).thenReturn(false);

        assertThrows(BusinessException.class, () -> desayunoService.delete(99));
        verify(desayunoRepository, never()).deleteById(anyInt());
        System.out.println("[OK] delete lanzó BusinessException para id=99");
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción cuando el desayuno está referenciado en pedidos")
    public void deleteDesayuno_enPedidos() {
        when(desayunoRepository.existsById(10)).thenReturn(true);
        when(desayunoRepository.countEnPedidos(10)).thenReturn(2L);

        assertThrows(BusinessException.class, () -> desayunoService.delete(10));
        verify(desayunoRepository, never()).deleteById(anyInt());
        System.out.println("[OK] delete lanzó CONFLICT por desayuno en pedidos");
    }
}
