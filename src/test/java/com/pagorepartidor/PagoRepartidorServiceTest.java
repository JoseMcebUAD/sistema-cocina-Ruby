package com.pagorepartidor;

import com.cocinarubi.dao.PagoRepartidorRepository;
import com.cocinarubi.domain.entity.PagoRepartidor;
import com.cocinarubi.domain.mapper.PagoRepartidorMapper;
import com.cocinarubi.domain.service.PagoRepartidorService;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.PagoRepartidorRequestDTO;
import com.cocinarubi.presentation.dto.response.PagoRepartidorResponseDTO;
import com.cocinarubi.presentation.strategy.strategyImplementation.PagoRepartidorValidationImp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PagoRepartidorServiceTest {

    @Mock
    private PagoRepartidorRepository pagoRepartidorRepository;

    @Mock
    private PagoRepartidorMapper mapper;

    @Mock
    private PagoRepartidorValidationImp validator;

    @InjectMocks
    private PagoRepartidorService pagoRepartidorService;

    // Lunes 16 jun 2025 (día hábil)
    private final LocalDateTime FECHA_HABIL = LocalDateTime.of(2025, 6, 16, 18, 0, 0);

    private final PagoRepartidor ENTITY = PagoRepartidor.builder()
            .idPagoRepartidor(10)
            .pago(BigDecimal.valueOf(150.00))
            .fechaPago(FECHA_HABIL)
            .build();

    private final PagoRepartidorResponseDTO RESPONSE_DTO = PagoRepartidorResponseDTO.builder()
            .idPagoRepartidor(10)
            .pago(BigDecimal.valueOf(150.00))
            .fechaPago(FECHA_HABIL)
            .build();

    private final PagoRepartidorRequestDTO REQUEST_DTO = PagoRepartidorRequestDTO.builder()
            .pago(BigDecimal.valueOf(150.00))
            .fechaPago(FECHA_HABIL)
            .build();

    @Test
    @DisplayName("findAll - Debe retornar la lista de pagos como DTOs")
    public void findAll() {
        when(pagoRepartidorRepository.findAll()).thenReturn(List.of(ENTITY));
        when(mapper.toResponseList(List.of(ENTITY))).thenReturn(List.of(RESPONSE_DTO));

        List<PagoRepartidorResponseDTO> result = pagoRepartidorService.findAll();

        assertEquals(1, result.size());
        assertEquals(BigDecimal.valueOf(150.00), result.get(0).getPago());
        System.out.println("[OK] findAll retornó " + result.size() + " pago(s): $" + result.get(0).getPago());
    }

    @Test
    @DisplayName("findById - Debe retornar el pago como DTO cuando el ID existe")
    public void findById_encontrado() {
        when(pagoRepartidorRepository.findById(10)).thenReturn(Optional.of(ENTITY));
        when(mapper.toResponse(ENTITY)).thenReturn(RESPONSE_DTO);

        PagoRepartidorResponseDTO result = pagoRepartidorService.findById(10);

        assertNotNull(result);
        assertEquals(10, result.getIdPagoRepartidor());
        assertEquals(BigDecimal.valueOf(150.00), result.getPago());
        System.out.println("[OK] findById retornó pago id=" + result.getIdPagoRepartidor());
    }

    @Test
    @DisplayName("findById - Debe lanzar excepción cuando el ID no existe")
    public void findById_noEncontrado() {
        when(pagoRepartidorRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> pagoRepartidorService.findById(99));
        System.out.println("[OK] findById lanzó BusinessException para id=99");
    }

    @Test
    @DisplayName("save - Debe invocar validarPost, guardar y retornar el DTO")
    public void savePagoRepartidor() {
        when(mapper.toEntity(any(PagoRepartidorRequestDTO.class))).thenReturn(ENTITY);
        when(pagoRepartidorRepository.save(ENTITY)).thenReturn(ENTITY);
        when(mapper.toResponse(ENTITY)).thenReturn(RESPONSE_DTO);

        PagoRepartidorResponseDTO result = pagoRepartidorService.save(REQUEST_DTO);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(150.00), result.getPago());
        verify(validator).validarPost(any(PagoRepartidorRequestDTO.class));
        verify(pagoRepartidorRepository).save(ENTITY);
        System.out.println("[OK] save guardó pago: $" + result.getPago());
    }

    @Test
    @DisplayName("update - Debe invocar validarPut, actualizar y retornar el DTO")
    public void updatePagoRepartidor() {
        PagoRepartidorRequestDTO updateDTO = PagoRepartidorRequestDTO.builder()
                .idPagoRepartidor(10)
                .pago(BigDecimal.valueOf(200.00))
                .fechaPago(FECHA_HABIL)
                .build();
        PagoRepartidorResponseDTO updatedResponse = PagoRepartidorResponseDTO.builder()
                .idPagoRepartidor(10)
                .pago(BigDecimal.valueOf(200.00))
                .fechaPago(FECHA_HABIL)
                .build();

        when(pagoRepartidorRepository.existsById(10)).thenReturn(true);
        when(mapper.toEntity(updateDTO)).thenReturn(ENTITY);
        when(pagoRepartidorRepository.save(ENTITY)).thenReturn(ENTITY);
        when(mapper.toResponse(ENTITY)).thenReturn(updatedResponse);

        PagoRepartidorResponseDTO result = pagoRepartidorService.update(updateDTO);

        assertNotNull(result);
        assertEquals(10, result.getIdPagoRepartidor());
        verify(validator).validarPut(updateDTO);
        System.out.println("[OK] update actualizó pago id=" + result.getIdPagoRepartidor());
    }

    @Test
    @DisplayName("update - Debe lanzar excepción cuando el ID no existe")
    public void updatePagoRepartidor_noEncontrado() {
        PagoRepartidorRequestDTO updateDTO = PagoRepartidorRequestDTO.builder()
                .idPagoRepartidor(99)
                .pago(BigDecimal.valueOf(200.00))
                .fechaPago(FECHA_HABIL)
                .build();

        when(pagoRepartidorRepository.existsById(99)).thenReturn(false);

        assertThrows(BusinessException.class, () -> pagoRepartidorService.update(updateDTO));
        verify(pagoRepartidorRepository, never()).save(any());
        System.out.println("[OK] update lanzó BusinessException para id=99");
    }
}
