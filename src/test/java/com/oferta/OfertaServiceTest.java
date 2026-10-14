package com.oferta;

import com.cocinarubi.DBConstants.ModoMatchOferta;
import com.cocinarubi.DBConstants.TipoComida;
import com.cocinarubi.DBConstants.TipoPedido;
import com.cocinarubi.dao.OfertaRepository;
import com.cocinarubi.domain.entity.Oferta;
import com.cocinarubi.domain.mapper.OfertaMapper;
import com.cocinarubi.domain.service.OfertaService;
import com.cocinarubi.exception.AdvertenciaEliminacionException;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.domain.entity.OfertaLinea;
import com.cocinarubi.presentation.dto.request.OfertaLineaRequestDTO;
import com.cocinarubi.presentation.dto.request.OfertaRequestDTO;
import com.cocinarubi.presentation.dto.response.OfertaResponseDTO;
import com.cocinarubi.presentation.strategy.strategyImplementation.OfertaValidationImp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OfertaServiceTest {

    @Mock private OfertaRepository ofertaRepository;

    // Mapper y validador reales: sin dependencias externas
    private final OfertaMapper ofertaMapper = new OfertaMapper();
    private final OfertaValidationImp ofertaValidation = new OfertaValidationImp();

    private OfertaService buildService() {
        return new OfertaService(ofertaRepository, ofertaMapper, ofertaValidation);
    }

    // -------------------------------------------------------------------------
    // Fixtures
    // -------------------------------------------------------------------------

    private OfertaRequestDTO buildRequest() {
        OfertaLineaRequestDTO linea = new OfertaLineaRequestDTO();
        linea.setModoMatch(ModoMatchOferta.COMIDA_POR_TIPO);
        linea.setTipoComidaFiltro(TipoComida.FIJA);
        linea.setCantidad(1);

        OfertaRequestDTO dto = new OfertaRequestDTO();
        dto.setNombre("Oferta Test");
        dto.setDescripcion("Descripcion de prueba");
        dto.setPrecioOferta(new BigDecimal("75.00"));
        dto.setPrioridad(5);
        dto.setActivo(true);
        dto.setLineas(List.of(linea));
        return dto;
    }

    private Oferta buildEntity() {
        return Oferta.builder()
                .idOferta(1).nombre("Oferta Test").descripcion("Descripcion de prueba")
                .precioOferta(new BigDecimal("75.00")).prioridad(5).activo(true).build();
    }

    // -------------------------------------------------------------------------
    // findAll
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findAll - retorna lista mapeada de ofertas")
    void findAll_retornaLista() {
        when(ofertaRepository.findAll()).thenReturn(List.of(buildEntity()));

        List<OfertaResponseDTO> result = buildService().findAll();

        assertEquals(1, result.size());
        assertEquals("Oferta Test", result.get(0).getNombre());
        System.out.println("[OK] findAll → " + result.size() + " oferta(s)");
    }

    // -------------------------------------------------------------------------
    // findById
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findById - retorna DTO cuando el ID existe")
    void findById_encontrado() {
        when(ofertaRepository.findByIdConLineas(1)).thenReturn(Optional.of(buildEntity()));

        OfertaResponseDTO result = buildService().findById(1);

        assertNotNull(result);
        assertEquals(1, result.getIdOferta());
        System.out.println("[OK] findById → id=" + result.getIdOferta());
    }

    @Test
    @DisplayName("findById - lanza 404 cuando el ID no existe")
    void findById_noEncontrado() {
        when(ofertaRepository.findByIdConLineas(99)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> buildService().findById(99));
        assertEquals(404, ex.getHttpStatus().value());
        System.out.println("[OK] findById lanzó 404 para id=99");
    }

    // -------------------------------------------------------------------------
    // save
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("save - crea la oferta y retorna el DTO")
    void save_creaOferta() {
        when(ofertaRepository.save(any(Oferta.class))).thenReturn(buildEntity());

        OfertaResponseDTO result = buildService().save(buildRequest());

        assertNotNull(result);
        assertEquals("Oferta Test", result.getNombre());
        assertEquals(new BigDecimal("75.00"), result.getPrecioOferta());
        verify(ofertaRepository).save(any(Oferta.class));
        System.out.println("[OK] save → nombre=" + result.getNombre());
    }

    @Test
    @DisplayName("save - lanza 400 cuando cantidad de línea es < 1")
    void save_lanza400_cantidadInvalida() {
        OfertaLineaRequestDTO lineaInvalida = new OfertaLineaRequestDTO();
        lineaInvalida.setModoMatch(ModoMatchOferta.PRODUCTO_GENERAL);
        lineaInvalida.setCantidad(0);

        OfertaRequestDTO dto = buildRequest();
        dto.setLineas(List.of(lineaInvalida));

        BusinessException ex = assertThrows(BusinessException.class, () -> buildService().save(dto));
        assertEquals(400, ex.getHttpStatus().value());
        verify(ofertaRepository, never()).save(any());
        System.out.println("[OK] save lanzó 400 por cantidad=0");
    }

    @Test
    @DisplayName("save - lanza 400 cuando COMIDA_POR_TIPO no tiene tipoComidaFiltro")
    void save_lanza400_comidaPorTipoSinFiltro() {
        OfertaLineaRequestDTO lineaInvalida = new OfertaLineaRequestDTO();
        lineaInvalida.setModoMatch(ModoMatchOferta.COMIDA_POR_TIPO);
        lineaInvalida.setCantidad(1);
        // tipoComidaFiltro no asignado → null

        OfertaRequestDTO dto = buildRequest();
        dto.setLineas(List.of(lineaInvalida));

        BusinessException ex = assertThrows(BusinessException.class, () -> buildService().save(dto));
        assertEquals(400, ex.getHttpStatus().value());
        System.out.println("[OK] save lanzó 400 por COMIDA_POR_TIPO sin tipoComidaFiltro");
    }

    // -------------------------------------------------------------------------
    // update
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("update - actualiza nombre, precio y líneas de la oferta")
    void update_actualizaCampos() {
        Oferta existente = buildEntity();
        existente.setLineas(new ArrayList<>());
        when(ofertaRepository.findByIdConLineas(1)).thenReturn(Optional.of(existente));
        when(ofertaRepository.save(any(Oferta.class))).thenAnswer(inv -> {
            Oferta saved = inv.getArgument(0);
            saved.setIdOferta(1);
            int idLinea = 1;
            for (OfertaLinea linea : saved.getLineas()) {
                linea.setIdOfertaLinea(idLinea++);
            }
            return saved;
        });

        OfertaRequestDTO dto = buildRequest();
        dto.setNombre("Oferta Actualizada");
        dto.setPrecioOferta(new BigDecimal("80.00"));

        OfertaResponseDTO result = buildService().update(1, dto);

        assertEquals("Oferta Actualizada", result.getNombre());
        assertEquals(new BigDecimal("80.00"), result.getPrecioOferta());
        System.out.println("[OK] update → nombre=" + result.getNombre());
    }

    // -------------------------------------------------------------------------
    // toggleActivo
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("toggleActivo - invierte activo=true a false y persiste")
    void toggleActivo_invierte() {
        when(ofertaRepository.findByIdConLineas(1)).thenReturn(Optional.of(buildEntity()));
        when(ofertaRepository.save(any(Oferta.class))).thenAnswer(inv -> inv.getArgument(0));

        OfertaResponseDTO result = buildService().toggleActivo(1);

        assertFalse(result.isActivo());
        verify(ofertaRepository).save(any(Oferta.class));
        System.out.println("[OK] toggleActivo → activo=" + result.isActivo());
    }

    // -------------------------------------------------------------------------
    // delete
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("delete - sin pedidos y saltarConfirmacion=false → elimina exitosamente")
    void delete_sinPedidos_exitoso() {
        when(ofertaRepository.findByIdConLineas(1)).thenReturn(Optional.of(buildEntity()));
        when(ofertaRepository.existeEnPedidos(1)).thenReturn(false);

        assertDoesNotThrow(() -> buildService().delete(1, false));
        verify(ofertaRepository).deleteById(1);
        System.out.println("[OK] delete exitoso (sin pedidos asociados)");
    }

    @Test
    @DisplayName("delete - con pedidos y saltarConfirmacion=false → lanza AdvertenciaEliminacionException (409)")
    void delete_conPedidos_lanzaAdvertencia() {
        when(ofertaRepository.findByIdConLineas(1)).thenReturn(Optional.of(buildEntity()));
        when(ofertaRepository.existeEnPedidos(1)).thenReturn(true);

        AdvertenciaEliminacionException ex = assertThrows(
                AdvertenciaEliminacionException.class, () -> buildService().delete(1, false));
        assertEquals(409, ex.getHttpStatus().value());
        verify(ofertaRepository, never()).deleteById(anyInt());
        System.out.println("[OK] delete con pedidos lanzó AdvertenciaEliminacionException 409");
    }

    @Test
    @DisplayName("delete - con pedidos y saltarConfirmacion=true → omite verificación y elimina")
    void delete_conPedidos_saltarConfirmacion_elimina() {
        when(ofertaRepository.findByIdConLineas(1)).thenReturn(Optional.of(buildEntity()));

        assertDoesNotThrow(() -> buildService().delete(1, true));
        verify(ofertaRepository).deleteById(1);
        verify(ofertaRepository, never()).existeEnPedidos(anyInt());
        System.out.println("[OK] delete saltarConfirmacion=true eliminó sin verificar pedidos");
    }

    @Test
    @DisplayName("delete - ID no existe → lanza 404")
    void delete_noExiste_lanza404() {
        when(ofertaRepository.findByIdConLineas(99)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> buildService().delete(99, false));
        assertEquals(404, ex.getHttpStatus().value());
        System.out.println("[OK] delete lanzó 404 para id=99");
    }

    // -------------------------------------------------------------------------
    // tiposPedidoRestriccion
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("save - con tiposPedidoRestriccion → la restricción se persiste en la entidad")
    void save_conRestriccion_persisteRestriccion() {
        AtomicReference<Oferta> capturada = new AtomicReference<>();
        when(ofertaRepository.save(any(Oferta.class))).thenAnswer(inv -> {
            Oferta arg = inv.getArgument(0);
            capturada.set(arg);
            arg.setIdOferta(1);
            int idLinea = 1;
            for (OfertaLinea linea : arg.getLineas()) {
                linea.setIdOfertaLinea(idLinea++);
            }
            return arg;
        });

        OfertaRequestDTO dto = buildRequest();
        dto.setTiposPedidoRestriccion(Set.of(TipoPedido.MOSTRADOR, TipoPedido.PICK_UP));

        buildService().save(dto);

        assertNotNull(capturada.get());
        assertTrue(capturada.get().getTiposPedidoRestriccion().contains(TipoPedido.MOSTRADOR));
        assertTrue(capturada.get().getTiposPedidoRestriccion().contains(TipoPedido.PICK_UP));
        assertFalse(capturada.get().getTiposPedidoRestriccion().contains(TipoPedido.DOMICILIO));
        System.out.println("[OK] save con restricción → tiposPedidoRestriccion="
                + capturada.get().getTiposPedidoRestriccion());
    }
}
