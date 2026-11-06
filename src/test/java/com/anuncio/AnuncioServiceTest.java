package com.anuncio;

import com.cloudinary.Cloudinary;
import com.cocinarubi.DBConstants.PrioridadAnuncio;
import com.cocinarubi.dao.AnuncioRepository;
import com.cocinarubi.domain.entity.Anuncio;
import com.cocinarubi.domain.mapper.AnuncioMapper;
import com.cocinarubi.domain.service.AnuncioService;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.AnuncioRequestDTO;
import com.cocinarubi.presentation.dto.response.AnuncioResponseDTO;
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

    @Mock private AnuncioRepository anuncioRepository;
    @Mock private AnuncioMapper anuncioMapper;
    @Mock private Cloudinary cloudinary;

    @InjectMocks
    private AnuncioService anuncioService;

    private final Anuncio ANUNCIO = Anuncio.builder()
            .idAnuncio(10)
            .descripcionAnuncio("Cerrado el lunes por festivo")
            .color("#FF5733")
            .prioridad(PrioridadAnuncio.ALTA)
            .fechaExpiracionAnuncio(LocalDateTime.of(2025, 12, 31, 23, 59, 59))
            .build();

    private final AnuncioResponseDTO RESPONSE = AnuncioResponseDTO.builder()
            .idAnuncio(10)
            .descripcionAnuncio("Cerrado el lunes por festivo")
            .color("#FF5733")
            .prioridad(PrioridadAnuncio.ALTA)
            .activo(true)
            .fechaExpiracionAnuncio(LocalDateTime.of(2025, 12, 31, 23, 59, 59))
            .build();

    private final AnuncioRequestDTO REQUEST = AnuncioRequestDTO.builder()
            .descripcionAnuncio("Cerrado el lunes por festivo")
            .color("#FF5733")
            .prioridad(PrioridadAnuncio.ALTA)
            .activo(true)
            .fechaExpiracionAnuncio(LocalDateTime.of(2025, 12, 31, 23, 59, 59))
            .build();

    @Test
    @DisplayName("findAll - Debe retornar todos los anuncios ordenados")
    public void findAll() {
        when(anuncioRepository.findAllOrdered()).thenReturn(List.of(ANUNCIO));
        when(anuncioMapper.toResponseList(List.of(ANUNCIO))).thenReturn(List.of(RESPONSE));

        List<AnuncioResponseDTO> result = anuncioService.findAll();

        assertEquals(1, result.size());
        assertEquals("Cerrado el lunes por festivo", result.get(0).getDescripcionAnuncio());
        System.out.println("[OK] findAll retornó " + result.size() + " anuncio(s): " + result.get(0).getDescripcionAnuncio());
    }

    @Test
    @DisplayName("findActivos - Debe retornar solo los anuncios con activo=true")
    public void findActivos() {
        when(anuncioRepository.findAllActivosOrdered()).thenReturn(List.of(ANUNCIO));
        when(anuncioMapper.toResponseList(List.of(ANUNCIO))).thenReturn(List.of(RESPONSE));

        List<AnuncioResponseDTO> result = anuncioService.findActivos();

        assertEquals(1, result.size());
        assertTrue(result.get(0).isActivo());
        System.out.println("[OK] findActivos retornó " + result.size() + " anuncio(s) activo(s)");
    }

    @Test
    @DisplayName("findById - Debe retornar el DTO cuando el ID existe")
    public void findById_encontrado() {
        when(anuncioRepository.findById(10)).thenReturn(Optional.of(ANUNCIO));
        when(anuncioMapper.toResponse(ANUNCIO)).thenReturn(RESPONSE);

        AnuncioResponseDTO result = anuncioService.findById(10);

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
    @DisplayName("save - Debe crear y retornar el DTO del anuncio correctamente")
    public void saveAnuncio() {
        when(anuncioMapper.toEntity(REQUEST)).thenReturn(ANUNCIO);
        when(anuncioRepository.save(ANUNCIO)).thenReturn(ANUNCIO);
        when(anuncioMapper.toResponse(ANUNCIO)).thenReturn(RESPONSE);

        AnuncioResponseDTO result = anuncioService.save(REQUEST);

        assertNotNull(result);
        assertEquals("Cerrado el lunes por festivo", result.getDescripcionAnuncio());
        assertEquals(PrioridadAnuncio.ALTA, result.getPrioridad());
        verify(anuncioRepository).save(ANUNCIO);
        System.out.println("[OK] save guardó anuncio: " + result.getDescripcionAnuncio() + " prioridad=" + result.getPrioridad());
    }

    @Test
    @DisplayName("delete - Debe eliminar el anuncio cuando el ID existe y no tiene imagen")
    public void deleteAnuncio() {
        when(anuncioRepository.findById(10)).thenReturn(Optional.of(ANUNCIO));

        assertDoesNotThrow(() -> anuncioService.delete(10));
        verify(anuncioRepository).delete(ANUNCIO);
        System.out.println("[OK] delete eliminó anuncio id=10");
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción cuando el ID no existe")
    public void deleteAnuncio_noEncontrado() {
        when(anuncioRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> anuncioService.delete(99));
        verify(anuncioRepository, never()).delete(any());
        System.out.println("[OK] delete lanzó BusinessException para id=99");
    }
}
