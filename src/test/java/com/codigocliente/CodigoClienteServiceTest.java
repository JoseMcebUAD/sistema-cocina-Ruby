package com.codigocliente;

import com.cocinarubi.DBConstants.Estatus;
import com.cocinarubi.dao.CodigoClienteRepository;
import com.cocinarubi.domain.entity.CodigoCliente;
import com.cocinarubi.domain.service.CodigoClienteService;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.CodigoClienteRequestDTO;
import com.cocinarubi.presentation.dto.response.CodigoClienteResponseDTO;
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
public class CodigoClienteServiceTest {

    @Mock
    private CodigoClienteRepository codigoClienteRepository;

    @InjectMocks
    private CodigoClienteService codigoClienteService;

    public CodigoCliente PREPARED = CodigoCliente.builder()
            .idCodigoCliente(10)
            .identificador("Código Susanita")
            .codigoCliente("SUSI-0001")
            .tarifaEspecial(BigDecimal.valueOf(20.00))
            .estatus(Estatus.DISPONIBLE)
            .build();

    public CodigoClienteRequestDTO DTO = crearDto("Código Susanita", "SUSI-0001", BigDecimal.valueOf(20.00));
    public CodigoClienteRequestDTO DTO_MODIFIED = crearDto("Código Susanita VIP", "SUSI-0001", BigDecimal.valueOf(15.00));
    public CodigoClienteRequestDTO DTO_CODIGO_CAMBIADO = crearDto("Código Susanita", "SUSI-0002", BigDecimal.valueOf(20.00));

    private CodigoClienteRequestDTO crearDto(String identificador, String codigo, BigDecimal tarifa) {
        CodigoClienteRequestDTO dto = new CodigoClienteRequestDTO();
        dto.setIdentificador(identificador);
        dto.setCodigoCliente(codigo);
        dto.setTarifaEspecial(tarifa);
        dto.setEstatus(Estatus.DISPONIBLE);
        return dto;
    }

    @Test
    @DisplayName("findAll - Debe retornar la lista de códigos de cliente registrados")
    public void findAll() {
        when(codigoClienteRepository.findAll()).thenReturn(List.of(PREPARED));

        List<CodigoClienteResponseDTO> result = codigoClienteService.findAll();

        assertEquals(1, result.size());
        assertEquals("Código Susanita", result.get(0).getIdentificador());
        System.out.println("[OK] findAll retornó " + result.size() + " código(s): " + result.get(0).getIdentificador());
    }

    @Test
    @DisplayName("findById - Debe retornar el código cuando el ID existe")
    public void findById_encontrado() {
        when(codigoClienteRepository.findById(10)).thenReturn(Optional.of(PREPARED));

        CodigoClienteResponseDTO result = codigoClienteService.findById(10);

        assertNotNull(result);
        assertEquals(10, result.getIdCodigoCliente());
        assertEquals("SUSI-0001", result.getCodigoCliente());
        System.out.println("[OK] findById retornó código id=" + result.getIdCodigoCliente());
    }

    @Test
    @DisplayName("findById - Debe lanzar excepción cuando el ID no existe")
    public void findById_noEncontrado() {
        when(codigoClienteRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> codigoClienteService.findById(99));
        System.out.println("[OK] findById lanzó BusinessException para id=99");
    }

    @Test
    @DisplayName("save - Debe guardar y retornar el código cuando el código no existe")
    public void save_exitoso() {
        when(codigoClienteRepository.existsByCodigoCliente("SUSI-0001")).thenReturn(false);
        when(codigoClienteRepository.save(any(CodigoCliente.class))).thenReturn(PREPARED);

        CodigoClienteResponseDTO result = codigoClienteService.save(DTO);

        assertNotNull(result);
        assertEquals("SUSI-0001", result.getCodigoCliente());
        verify(codigoClienteRepository).save(any(CodigoCliente.class));
        System.out.println("[OK] save guardó código: " + result.getCodigoCliente());
    }

    @Test
    @DisplayName("save - Debe lanzar excepción cuando el código ya existe")
    public void save_codigoDuplicado() {
        when(codigoClienteRepository.existsByCodigoCliente("SUSI-0001")).thenReturn(true);

        assertThrows(BusinessException.class, () -> codigoClienteService.save(DTO));
        verify(codigoClienteRepository, never()).save(any(CodigoCliente.class));
        System.out.println("[OK] save lanzó CONFLICT por código duplicado");
    }

    @Test
    @DisplayName("update - Debe actualizar identificador, tarifa y estatus cuando el código no cambia")
    public void update_exitoso() {
        when(codigoClienteRepository.findById(10)).thenReturn(Optional.of(PREPARED));
        CodigoCliente actualizado = CodigoCliente.builder()
                .idCodigoCliente(10)
                .identificador("Código Susanita VIP")
                .codigoCliente("SUSI-0001")
                .tarifaEspecial(BigDecimal.valueOf(15.00))
                .estatus(Estatus.DISPONIBLE)
                .build();
        when(codigoClienteRepository.save(any(CodigoCliente.class))).thenReturn(actualizado);

        CodigoClienteResponseDTO result = codigoClienteService.update(10, DTO_MODIFIED);

        assertNotNull(result);
        assertEquals("Código Susanita VIP", result.getIdentificador());
        assertEquals(BigDecimal.valueOf(15.00), result.getTarifaEspecial());
        System.out.println("[OK] update actualizó código: identificador=" + result.getIdentificador());
    }

    @Test
    @DisplayName("update - Debe lanzar excepción cuando se intenta cambiar el código del cliente (RF-047)")
    public void update_codigoCambiado_rf047() {
        when(codigoClienteRepository.findById(10)).thenReturn(Optional.of(PREPARED));

        assertThrows(BusinessException.class, () -> codigoClienteService.update(10, DTO_CODIGO_CAMBIADO));
        verify(codigoClienteRepository, never()).save(any(CodigoCliente.class));
        System.out.println("[OK] update lanzó CONFLICT por intento de cambio de código (RF-047)");
    }

    @Test
    @DisplayName("delete - Debe eliminar el código cuando el ID existe")
    public void delete_exitoso() {
        when(codigoClienteRepository.existsById(10)).thenReturn(true);

        assertDoesNotThrow(() -> codigoClienteService.delete(10));
        verify(codigoClienteRepository).deleteById(10);
        System.out.println("[OK] delete eliminó código id=10");
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción cuando el ID no existe")
    public void delete_noEncontrado() {
        when(codigoClienteRepository.existsById(99)).thenReturn(false);

        assertThrows(BusinessException.class, () -> codigoClienteService.delete(99));
        verify(codigoClienteRepository, never()).deleteById(anyInt());
        System.out.println("[OK] delete lanzó BusinessException para id=99");
    }
}
