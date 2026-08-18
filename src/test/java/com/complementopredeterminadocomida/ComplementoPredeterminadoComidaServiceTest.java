package com.complementopredeterminadocomida;

import com.cocinarubi.DBConstants.Estatus;
import com.cocinarubi.dao.ComplementoPredeterminadoComidaRepository;
import com.cocinarubi.domain.entity.Comida;
import com.cocinarubi.domain.entity.Complemento;
import com.cocinarubi.domain.entity.ComplementoPredeterminadoComida;
import com.cocinarubi.domain.service.ComidaService;
import com.cocinarubi.domain.service.ComplementoService;
import com.cocinarubi.domain.service.ComplementoPredeterminadoComidaService;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.ComplementoPredeterminadoComidaRequestDTO;
import com.cocinarubi.presentation.dto.response.ComplementoPredeterminadoComidaResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ComplementoPredeterminadoComidaServiceTest {

    @Mock private ComplementoPredeterminadoComidaRepository repo;
    @Mock private ComidaService comidaService;
    @Mock private ComplementoService complementoService;

    @InjectMocks
    private ComplementoPredeterminadoComidaService service;

    private final Comida COMIDA = Comida.builder()
            .idComida(1)
            .nombreComida("Pollo en salsa")
            .precioMedia(BigDecimal.valueOf(55))
            .precioEntera(BigDecimal.valueOf(90))
            .estatus(Estatus.DISPONIBLE)
            .build();

    private final Complemento COMPLEMENTO = Complemento.builder()
            .idComplemento(10)
            .nombreComplemento("Arroz")
            .precioExtra(BigDecimal.ZERO)
            .estatus(Estatus.DISPONIBLE)
            .build();

    private final ComplementoPredeterminadoComida PREDETERMINADO = ComplementoPredeterminadoComida.builder()
            .idComplementoPredeterminadoComida(100)
            .comida(COMIDA)
            .complemento(COMPLEMENTO)
            .cantidad(1)
            .build();

    @Test
    @DisplayName("findByIdComida - Debe retornar la lista de predeterminados mapeados a DTO")
    public void findByIdComida() {
        when(repo.findByComida_IdComida(1)).thenReturn(List.of(PREDETERMINADO));

        List<ComplementoPredeterminadoComidaResponseDTO> result = service.findByIdComida(1);

        assertEquals(1, result.size());
        assertEquals(100, result.get(0).getIdComplementoPredeterminadoComida());
        assertEquals(10, result.get(0).getIdComplemento());
        assertEquals("Arroz", result.get(0).getNombreComplemento());
        assertEquals(1, result.get(0).getCantidad());
        System.out.println("[OK] findByIdComida retornó " + result.size() + " predeterminado(s)");
    }

    @Test
    @DisplayName("save - Debe asignar el complemento predeterminado y retornar DTO")
    public void save_exitoso() {
        ComplementoPredeterminadoComidaRequestDTO dto = new ComplementoPredeterminadoComidaRequestDTO(10, 2);

        when(comidaService.findById(1)).thenReturn(COMIDA);
        when(complementoService.findById(10)).thenReturn(COMPLEMENTO);
        when(repo.existsByComida_IdComidaAndComplemento_IdComplemento(1, 10)).thenReturn(false);
        when(repo.save(any())).thenReturn(PREDETERMINADO);

        ComplementoPredeterminadoComidaResponseDTO result = service.save(1, dto);

        assertNotNull(result);
        assertEquals("Arroz", result.getNombreComplemento());
        verify(repo).save(any());
        System.out.println("[OK] save asignó predeterminado: " + result.getNombreComplemento());
    }

    @Test
    @DisplayName("save - Debe lanzar NOT_FOUND si la comida no existe")
    public void save_comidaNoExiste() {
        when(comidaService.findById(99)).thenThrow(new BusinessException("Comida no encontrada", org.springframework.http.HttpStatus.NOT_FOUND));

        assertThrows(BusinessException.class,
                () -> service.save(99, new ComplementoPredeterminadoComidaRequestDTO(10, 1)));
        verify(repo, never()).save(any());
        System.out.println("[OK] save lanzó excepción por comida inexistente");
    }

    @Test
    @DisplayName("save - Debe lanzar NOT_FOUND si el complemento no existe")
    public void save_complementoNoExiste() {
        when(comidaService.findById(1)).thenReturn(COMIDA);
        when(complementoService.findById(99)).thenThrow(new BusinessException("Complemento no encontrado", org.springframework.http.HttpStatus.NOT_FOUND));

        assertThrows(BusinessException.class,
                () -> service.save(1, new ComplementoPredeterminadoComidaRequestDTO(99, 1)));
        verify(repo, never()).save(any());
        System.out.println("[OK] save lanzó excepción por complemento inexistente");
    }

    @Test
    @DisplayName("save - Debe lanzar CONFLICT si la combinación ya existe")
    public void save_duplicado() {
        when(comidaService.findById(1)).thenReturn(COMIDA);
        when(complementoService.findById(10)).thenReturn(COMPLEMENTO);
        when(repo.existsByComida_IdComidaAndComplemento_IdComplemento(1, 10)).thenReturn(true);

        assertThrows(BusinessException.class,
                () -> service.save(1, new ComplementoPredeterminadoComidaRequestDTO(10, 1)));
        verify(repo, never()).save(any());
        System.out.println("[OK] save lanzó CONFLICT por combinación duplicada");
    }

    @Test
    @DisplayName("delete - Debe eliminar el registro cuando existe")
    public void delete_exitoso() {
        when(repo.existsById(100)).thenReturn(true);

        assertDoesNotThrow(() -> service.delete(100));
        verify(repo).deleteById(100);
        System.out.println("[OK] delete eliminó predeterminado id=100");
    }

    @Test
    @DisplayName("delete - Debe lanzar NOT_FOUND cuando el registro no existe")
    public void delete_noEncontrado() {
        when(repo.existsById(999)).thenReturn(false);

        assertThrows(BusinessException.class, () -> service.delete(999));
        verify(repo, never()).deleteById(anyInt());
        System.out.println("[OK] delete lanzó NOT_FOUND para id=999");
    }
}
