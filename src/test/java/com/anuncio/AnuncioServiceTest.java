package com.anuncio;

import com.cocinarubi.dao.AnuncioRepository;
import com.cocinarubi.domain.entity.Anuncio;
import com.cocinarubi.domain.service.AnuncioService;
import com.cocinarubi.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnuncioServiceTest {

    @Mock
    private AnuncioRepository anuncioRepository;

    @InjectMocks
    private AnuncioService anuncioService;

    public Anuncio ANUNCIO_PREPARED = Anuncio.builder()
            .idAnuncio(10)
            .descripcionAnuncio("Cerrado el lunes por festivo")
            .color("#FF5733")
            .fechaExpiracionAnuncio(LocalDateTime.of(2025, 12, 31, 23, 59, 59))
            .build();

    public Anuncio ANUNCIO_MODIFIED_PREPARED = Anuncio.builder()
            .idAnuncio(10)
            .descripcionAnuncio("Reabrimos el martes")
            .color("#00FF00")
            .fechaExpiracionAnuncio(LocalDateTime.of(2026, 1, 2, 23, 59, 59))
            .build();

    @Test
    @DisplayName("findAll - Debe retornar la lista de anuncios registrados")
    public void findAll() {
        when(anuncioRepository.findAll()).thenReturn(List.of(ANUNCIO_PREPARED));

        List<Anuncio> result = anuncioService.findAll();

        assertEquals(1, result.size());
        assertEquals("Cerrado el lunes por festivo", result.get(0).getDescripcionAnuncio());
        System.out.println("[OK] findAll retornó " + result.size() + " anuncio(s): " + result.get(0).getDescripcionAnuncio());
    }

    @Test
    @DisplayName("findById - Debe retornar el anuncio cuando el ID existe")
    public void findById_encontrado() {
        when(anuncioRepository.findById(10)).thenReturn(Optional.of(ANUNCIO_PREPARED));

        Anuncio result = anuncioService.findById(10);

        assertNotNull(result);
        assertEquals(10, result.getIdAnuncio());
        assertEquals("Cerrado el lunes por festivo", result.getDescripcionAnuncio());
        System.out.println("[OK] findById retornó anuncio id=" + result.getIdAnuncio());
    }

    @Test
    @DisplayName("findById - Debe lanzar excepción cuando el ID no existe")
    public void findById_noEncontrado() {
        when(anuncioRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> anuncioService.findById(99));
        System.out.println("[OK] findById lanzó BusinessException para id=99");
    }

    @Test
    @DisplayName("save - Debe guardar y retornar el anuncio correctamente")
    public void saveAnuncio() {
        when(anuncioRepository.save(ANUNCIO_PREPARED)).thenReturn(ANUNCIO_PREPARED);

        Anuncio result = anuncioService.save(ANUNCIO_PREPARED);

        assertNotNull(result);
        assertEquals("Cerrado el lunes por festivo", result.getDescripcionAnuncio());
        verify(anuncioRepository).save(ANUNCIO_PREPARED);
        System.out.println("[OK] save guardó anuncio: " + result.getDescripcionAnuncio());
    }

    @Test
    @DisplayName("delete - Debe eliminar el anuncio cuando el ID existe")
    public void deleteAnuncio() {
        when(anuncioRepository.existsById(10)).thenReturn(true);

        assertDoesNotThrow(() -> anuncioService.delete(10));
        verify(anuncioRepository).deleteById(10);
        System.out.println("[OK] delete eliminó anuncio id=10");
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción cuando el ID no existe")
    public void deleteAnuncio_noEncontrado() {
        when(anuncioRepository.existsById(99)).thenReturn(false);

        assertThrows(BusinessException.class, () -> anuncioService.delete(99));
        verify(anuncioRepository, never()).deleteById(anyInt());
        System.out.println("[OK] delete lanzó BusinessException para id=99");
    }
}
