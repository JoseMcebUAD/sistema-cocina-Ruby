package com.ruta;

import com.cocinarubi.dao.RutaRepository;
import com.cocinarubi.domain.entity.Ruta;
import com.cocinarubi.domain.service.RutaService;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.RutaRequestDTO;
import com.cocinarubi.presentation.dto.response.RutaResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RutaServiceTest {

    private static final String WKT_POLYGON = "POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))";

    @Mock
    private RutaRepository rutaRepository;

    @InjectMocks
    private RutaService rutaService;

    public Geometry geom = parseGeom(WKT_POLYGON);

    public Ruta RUTA_PREPARED = Ruta.builder()
            .idRuta(10)
            .nombre("Zona Norte")
            .boundary(geom)
            .isActive(true)
            .tarifaEnvio(BigDecimal.valueOf(30))
            .tiempoEstimadoMin(20)
            .build();

    public RutaRequestDTO RUTA_DTO = new RutaRequestDTO(
            "Zona Norte", WKT_POLYGON, true, BigDecimal.valueOf(30), 20);

    public RutaRequestDTO RUTA_DTO_MODIFIED = new RutaRequestDTO(
            "Zona Sur", WKT_POLYGON, false, BigDecimal.valueOf(50), 35);

    private static Geometry parseGeom(String wkt) {
        try {
            return new WKTReader().read(wkt);
        } catch (ParseException e) {
            throw new RuntimeException("WKT inválido en fixture de test", e);
        }
    }

    @Test
    @DisplayName("findAll - Debe retornar la lista de rutas registradas")
    public void findAll() {
        when(rutaRepository.findAll()).thenReturn(List.of(RUTA_PREPARED));

        List<RutaResponseDTO> result = rutaService.findAll();

        assertEquals(1, result.size());
        assertEquals("Zona Norte", result.get(0).getNombre());
        assertTrue(result.get(0).isActive());
        System.out.println("[OK] findAll retornó " + result.size() + " ruta(s): " + result.get(0).getNombre());
    }

    @Test
    @DisplayName("findById - Debe retornar la ruta cuando el ID existe")
    public void findById_encontrado() {
        when(rutaRepository.findById(10)).thenReturn(Optional.of(RUTA_PREPARED));

        RutaResponseDTO result = rutaService.findById(10);

        assertNotNull(result);
        assertEquals(10, result.getIdRuta());
        assertEquals("Zona Norte", result.getNombre());
        System.out.println("[OK] findById retornó ruta id=" + result.getIdRuta());
    }

    @Test
    @DisplayName("findById - Debe lanzar excepción cuando el ID no existe")
    public void findById_noEncontrada() {
        when(rutaRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> rutaService.findById(99));
        System.out.println("[OK] findById lanzó BusinessException para id=99");
    }

    @Test
    @DisplayName("save - Debe guardar y retornar la ruta correctamente")
    public void saveRuta() {
        when(rutaRepository.save(any(Ruta.class))).thenReturn(RUTA_PREPARED);

        RutaResponseDTO result = rutaService.save(RUTA_DTO);

        assertNotNull(result);
        assertEquals("Zona Norte", result.getNombre());
        verify(rutaRepository).save(any(Ruta.class));
        System.out.println("[OK] save guardó ruta: " + result.getNombre());
    }

    @Test
    @DisplayName("update - Debe actualizar y retornar la ruta correctamente")
    public void updateRuta() {
        when(rutaRepository.findById(10)).thenReturn(Optional.of(RUTA_PREPARED));
        Ruta rutaActualizada = Ruta.builder().idRuta(10).nombre("Zona Sur")
                .boundary(geom).isActive(false).tarifaEnvio(BigDecimal.valueOf(50)).tiempoEstimadoMin(35).build();
        when(rutaRepository.save(any(Ruta.class))).thenReturn(rutaActualizada);

        RutaResponseDTO result = rutaService.update(10, RUTA_DTO_MODIFIED);

        assertNotNull(result);
        assertEquals("Zona Sur", result.getNombre());
        assertEquals(BigDecimal.valueOf(50), result.getTarifaEnvio());
        System.out.println("[OK] update actualizó ruta: nombre=" + result.getNombre());
    }

    @Test
    @DisplayName("delete - Debe eliminar la ruta cuando el ID existe y no tiene referencias")
    public void deleteRuta() {
        when(rutaRepository.existsById(10)).thenReturn(true);
        when(rutaRepository.countClientesConRuta(10)).thenReturn(0L);
        when(rutaRepository.countPedidosDomicilioConRuta(10)).thenReturn(0L);

        assertDoesNotThrow(() -> rutaService.delete(10));
        verify(rutaRepository).deleteById(10);
        System.out.println("[OK] delete eliminó ruta id=10");
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción cuando el ID no existe")
    public void deleteRuta_noEncontrada() {
        when(rutaRepository.existsById(99)).thenReturn(false);

        assertThrows(BusinessException.class, () -> rutaService.delete(99));
        verify(rutaRepository, never()).deleteById(anyInt());
        System.out.println("[OK] delete lanzó BusinessException para id=99");
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción cuando la ruta está asignada a clientes")
    public void deleteRuta_conClientes() {
        when(rutaRepository.existsById(10)).thenReturn(true);
        when(rutaRepository.countClientesConRuta(10)).thenReturn(3L);

        assertThrows(BusinessException.class, () -> rutaService.delete(10));
        verify(rutaRepository, never()).deleteById(anyInt());
        System.out.println("[OK] delete lanzó CONFLICT por ruta con clientes asignados");
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción cuando la ruta está referenciada en pedidos a domicilio")
    public void deleteRuta_conPedidos() {
        when(rutaRepository.existsById(10)).thenReturn(true);
        when(rutaRepository.countClientesConRuta(10)).thenReturn(0L);
        when(rutaRepository.countPedidosDomicilioConRuta(10)).thenReturn(2L);

        assertThrows(BusinessException.class, () -> rutaService.delete(10));
        verify(rutaRepository, never()).deleteById(anyInt());
        System.out.println("[OK] delete lanzó CONFLICT por ruta en pedidos domicilio");
    }
}
