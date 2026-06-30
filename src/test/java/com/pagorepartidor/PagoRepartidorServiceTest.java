package com.pagorepartidor;

import com.cocinarubi.dao.PagoRepartidorRepository;
import com.cocinarubi.domain.entity.PagoRepartidor;
import com.cocinarubi.domain.service.PagoRepartidorService;
import com.cocinarubi.exception.BusinessException;
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

    @InjectMocks
    private PagoRepartidorService pagoRepartidorService;

    public PagoRepartidor PAGO_PREPARED = PagoRepartidor.builder()
            .idPagoRepartidor(10)
            .pago(BigDecimal.valueOf(150.00))
            .fechaPago(LocalDateTime.of(2025, 6, 15, 18, 0, 0))
            .build();

    public PagoRepartidor PAGO_MODIFIED_PREPARED = PagoRepartidor.builder()
            .idPagoRepartidor(10)
            .pago(BigDecimal.valueOf(200.00))
            .fechaPago(LocalDateTime.of(2025, 6, 16, 18, 0, 0))
            .build();

    @Test
    @DisplayName("findAll - Debe retornar la lista de pagos a repartidores registrados")
    public void findAll() {
        when(pagoRepartidorRepository.findAll()).thenReturn(List.of(PAGO_PREPARED));

        List<PagoRepartidor> result = pagoRepartidorService.findAll();

        assertEquals(1, result.size());
        assertEquals(BigDecimal.valueOf(150.00), result.get(0).getPago());
        System.out.println("[OK] findAll retornó " + result.size() + " pago(s): $" + result.get(0).getPago());
    }

    @Test
    @DisplayName("findById - Debe retornar el pago cuando el ID existe")
    public void findById_encontrado() {
        when(pagoRepartidorRepository.findById(10)).thenReturn(Optional.of(PAGO_PREPARED));

        PagoRepartidor result = pagoRepartidorService.findById(10);

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
    @DisplayName("save - Debe guardar y retornar el pago correctamente")
    public void savePagoRepartidor() {
        when(pagoRepartidorRepository.save(PAGO_PREPARED)).thenReturn(PAGO_PREPARED);

        PagoRepartidor result = pagoRepartidorService.save(PAGO_PREPARED);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(150.00), result.getPago());
        verify(pagoRepartidorRepository).save(PAGO_PREPARED);
        System.out.println("[OK] save guardó pago: $" + result.getPago());
    }

    @Test
    @DisplayName("delete - Debe eliminar el pago cuando el ID existe")
    public void deletePagoRepartidor() {
        when(pagoRepartidorRepository.existsById(10)).thenReturn(true);

        assertDoesNotThrow(() -> pagoRepartidorService.delete(10));
        verify(pagoRepartidorRepository).deleteById(10);
        System.out.println("[OK] delete eliminó pago id=10");
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción cuando el ID no existe")
    public void deletePagoRepartidor_noEncontrado() {
        when(pagoRepartidorRepository.existsById(99)).thenReturn(false);

        assertThrows(BusinessException.class, () -> pagoRepartidorService.delete(99));
        verify(pagoRepartidorRepository, never()).deleteById(anyInt());
        System.out.println("[OK] delete lanzó BusinessException para id=99");
    }
}
