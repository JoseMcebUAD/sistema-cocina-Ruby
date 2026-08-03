package com.produccion;

import com.cocinarubi.Constants;
import com.cocinarubi.DBConstants.PedidoCreadoDesde;
import com.cocinarubi.DBConstants.TipoPedido;
import com.cocinarubi.DBConstants.TipoProducto;
import com.cocinarubi.dao.ResumenProduccionRepository;
import com.cocinarubi.domain.service.ResumenProduccionService;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.response.DetalleProduccionResponseDTO;
import com.cocinarubi.presentation.dto.response.ResumenProduccionResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ResumenProduccionServiceTest {

    @Mock
    private ResumenProduccionRepository resumenProduccionRepository;

    @InjectMocks
    private ResumenProduccionService resumenProduccionService;

    @Captor
    private ArgumentCaptor<Boolean> filtrarRutaCaptor;

    @Captor
    private ArgumentCaptor<List<Integer>> idRutasCaptor;

    @Captor
    private ArgumentCaptor<TipoPedido> tipoPedidoCaptor;

    // ── Datos mock compartidos ───────────────────────────────────────────────

    private final List<Object[]> PRODUCTOS_POR_TIPO = List.of(
            new Object[]{TipoProducto.SNACK,   5L},
            new Object[]{TipoProducto.BEBIDA,  3L},
            new Object[]{TipoProducto.CHAROLA, 2L},
            new Object[]{TipoProducto.POSTRE,  1L}
    );

    private final List<Object[]> DETALLE_COMIDAS = List.of(
            new Object[]{"Pollo en salsa roja", 3L},
            new Object[]{"Pescado frito",        2L}
    );

    // ── Helper: configura stubs de conteo para resumenProduccion() ───────────

    private void stubConteoBase(long comidas, long desayunos, long basicos) {
        when(resumenProduccionRepository.countComidas(any(), any(), any(), any(), anyBoolean(), anyList()))
                .thenReturn(comidas);
        when(resumenProduccionRepository.countDesayunos(any(), any(), any(), any(), anyBoolean(), anyList()))
                .thenReturn(desayunos);
        when(resumenProduccionRepository.countBasicos(any(), any(), any(), any(), anyBoolean(), anyList()))
                .thenReturn(basicos);
        when(resumenProduccionRepository.countProductosCocinaAgrupadoPorTipo(any(), any(), any(), any(), anyBoolean(), anyList()))
                .thenReturn(PRODUCTOS_POR_TIPO);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tests de resumenProduccion()
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("resumenProduccion - sin filtros debe retornar los totales correctos por categoría")
    public void resumenProduccion_sinFiltros_retornaTotales() {
        stubConteoBase(4L, 3L, 2L);

        ResumenProduccionResponseDTO result = resumenProduccionService.resumenProduccion(null, null, null);

        assertNotNull(result);
        assertEquals(4L, result.getTotalComidas());
        assertEquals(3L, result.getTotalDesayunos());
        assertEquals(2L, result.getTotalBasicos());
        assertEquals(5L, result.getTotalSnacks());
        assertEquals(3L, result.getTotalBebidas());
        assertEquals(2L, result.getTotalCharolas());
        assertEquals(1L, result.getTotalPostres());
        System.out.println("[OK] resumenProduccion sin filtros: comidas=" + result.getTotalComidas()
                + " snacks=" + result.getTotalSnacks() + " postres=" + result.getTotalPostres());
    }

    @Test
    @DisplayName("resumenProduccion - sin idRutas debe pasar filtrarRuta=false e idRutas=[-1] al repo")
    public void resumenProduccion_sinRutas_usaFiltrarRutaFalseEIdRutasSafe() {
        stubConteoBase(0L, 0L, 0L);

        resumenProduccionService.resumenProduccion(null, null, null);

        verify(resumenProduccionRepository).countComidas(
                any(), any(), isNull(), isNull(),
                filtrarRutaCaptor.capture(), idRutasCaptor.capture());
        assertFalse(filtrarRutaCaptor.getValue());
        assertEquals(List.of(-1), idRutasCaptor.getValue());
        System.out.println("[OK] sin idRutas: filtrarRuta=" + filtrarRutaCaptor.getValue()
                + ", idRutas=" + idRutasCaptor.getValue());
    }

    @Test
    @DisplayName("resumenProduccion - con idRutas debe activar filtrarRuta=true y pasar la lista al repo")
    public void resumenProduccion_conIdRutas_activaFiltrarRutaTrue() {
        stubConteoBase(0L, 0L, 0L);
        List<Integer> rutas = List.of(1, 2);

        resumenProduccionService.resumenProduccion(null, null, rutas);

        verify(resumenProduccionRepository).countComidas(
                any(), any(), isNull(), isNull(),
                filtrarRutaCaptor.capture(), idRutasCaptor.capture());
        assertTrue(filtrarRutaCaptor.getValue());
        assertEquals(rutas, idRutasCaptor.getValue());
        System.out.println("[OK] con idRutas=" + rutas + ": filtrarRuta=" + filtrarRutaCaptor.getValue());
    }

    @Test
    @DisplayName("resumenProduccion - con tipoPedido debe pasar el filtro exacto al repositorio")
    public void resumenProduccion_conTipoPedido_pasaFiltroAlRepo() {
        stubConteoBase(0L, 0L, 0L);

        resumenProduccionService.resumenProduccion(TipoPedido.DOMICILIO, null, null);

        verify(resumenProduccionRepository).countComidas(
                any(), any(), tipoPedidoCaptor.capture(), isNull(), anyBoolean(), anyList());
        assertEquals(TipoPedido.DOMICILIO, tipoPedidoCaptor.getValue());
        System.out.println("[OK] tipoPedido pasado al repo: " + tipoPedidoCaptor.getValue());
    }

    @Test
    @DisplayName("resumenProduccion - la fecha retornada debe ser hoy en zona Mérida")
    public void resumenProduccion_fechaRetornadaEsHoy() {
        stubConteoBase(0L, 0L, 0L);

        ResumenProduccionResponseDTO result = resumenProduccionService.resumenProduccion(null, null, null);

        assertEquals(LocalDate.now(Constants.ZONA_MERIDA), result.getFecha());
        System.out.println("[OK] fecha retornada es hoy: " + result.getFecha());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tests de detalleProduccion()
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("detalleProduccion - comidas debe llamar findDetalleComidas y mapear items correctamente")
    public void detalleProduccion_comidas_llamaFindDetalleComidas() {
        when(resumenProduccionRepository.findDetalleComidas(any(), any(), any(), any(), anyBoolean(), anyList()))
                .thenReturn(DETALLE_COMIDAS);

        DetalleProduccionResponseDTO result = resumenProduccionService.detalleProduccion("comidas", null, null, null);

        assertNotNull(result);
        assertEquals("comidas", result.getCategoria());
        assertEquals(2, result.getItems().size());
        assertEquals("Pollo en salsa roja", result.getItems().get(0).getNombre());
        assertEquals(3, result.getItems().get(0).getCantidad());
        assertEquals("Pescado frito", result.getItems().get(1).getNombre());
        verify(resumenProduccionRepository).findDetalleComidas(any(), any(), any(), any(), anyBoolean(), anyList());
        System.out.println("[OK] detalleProduccion comidas: " + result.getItems().size() + " items");
    }

    @Test
    @DisplayName("detalleProduccion - desayunos debe llamar findDetalleDesayunos")
    public void detalleProduccion_desayunos_llamaFindDetalleDesayunos() {
        when(resumenProduccionRepository.findDetalleDesayunos(any(), any(), any(), any(), anyBoolean(), anyList()))
                .thenReturn(List.<Object[]>of(new Object[]{"Huevos con jamón", 2L}));

        DetalleProduccionResponseDTO result = resumenProduccionService.detalleProduccion("desayunos", null, null, null);

        assertEquals("desayunos", result.getCategoria());
        assertEquals(1, result.getItems().size());
        assertEquals("Huevos con jamón", result.getItems().get(0).getNombre());
        verify(resumenProduccionRepository).findDetalleDesayunos(any(), any(), any(), any(), anyBoolean(), anyList());
        System.out.println("[OK] detalleProduccion desayunos: " + result.getItems().get(0).getNombre());
    }

    @Test
    @DisplayName("detalleProduccion - basicos debe llamar findDetalleBasicos")
    public void detalleProduccion_basicos_llamaFindDetalleBasicos() {
        when(resumenProduccionRepository.findDetalleBasicos(any(), any(), any(), any(), anyBoolean(), anyList()))
                .thenReturn(List.<Object[]>of(new Object[]{"Básico completo", 4L}));

        DetalleProduccionResponseDTO result = resumenProduccionService.detalleProduccion("basicos", null, null, null);

        assertEquals("basicos", result.getCategoria());
        assertEquals(1, result.getItems().size());
        verify(resumenProduccionRepository).findDetalleBasicos(any(), any(), any(), any(), anyBoolean(), anyList());
        System.out.println("[OK] detalleProduccion basicos: " + result.getItems().get(0).getNombre());
    }

    @Test
    @DisplayName("detalleProduccion - snacks debe llamar findDetalleProductosCocina con TipoProducto.SNACK")
    public void detalleProduccion_snacks_llamaFindDetalleConTipoSnack() {
        when(resumenProduccionRepository.findDetalleProductosCocina(
                eq(TipoProducto.SNACK), any(), any(), any(), any(), anyBoolean(), anyList()))
                .thenReturn(List.<Object[]>of(new Object[]{"Papas", 5L}));

        DetalleProduccionResponseDTO result = resumenProduccionService.detalleProduccion("snacks", null, null, null);

        assertEquals("snacks", result.getCategoria());
        verify(resumenProduccionRepository).findDetalleProductosCocina(
                eq(TipoProducto.SNACK), any(), any(), any(), any(), anyBoolean(), anyList());
        System.out.println("[OK] detalleProduccion snacks llamó repo con TipoProducto.SNACK");
    }

    @Test
    @DisplayName("detalleProduccion - bebidas debe llamar findDetalleProductosCocina con TipoProducto.BEBIDA")
    public void detalleProduccion_bebidas_llamaFindDetalleConTipoBebida() {
        when(resumenProduccionRepository.findDetalleProductosCocina(
                eq(TipoProducto.BEBIDA), any(), any(), any(), any(), anyBoolean(), anyList()))
                .thenReturn(List.of());

        DetalleProduccionResponseDTO result = resumenProduccionService.detalleProduccion("bebidas", null, null, null);

        assertEquals("bebidas", result.getCategoria());
        verify(resumenProduccionRepository).findDetalleProductosCocina(
                eq(TipoProducto.BEBIDA), any(), any(), any(), any(), anyBoolean(), anyList());
        System.out.println("[OK] detalleProduccion bebidas llamó repo con TipoProducto.BEBIDA");
    }

    @Test
    @DisplayName("detalleProduccion - charolas debe llamar findDetalleProductosCocina con TipoProducto.CHAROLA")
    public void detalleProduccion_charolas_llamaFindDetalleConTipoCharola() {
        when(resumenProduccionRepository.findDetalleProductosCocina(
                eq(TipoProducto.CHAROLA), any(), any(), any(), any(), anyBoolean(), anyList()))
                .thenReturn(List.of());

        DetalleProduccionResponseDTO result = resumenProduccionService.detalleProduccion("charolas", null, null, null);

        assertEquals("charolas", result.getCategoria());
        verify(resumenProduccionRepository).findDetalleProductosCocina(
                eq(TipoProducto.CHAROLA), any(), any(), any(), any(), anyBoolean(), anyList());
        System.out.println("[OK] detalleProduccion charolas llamó repo con TipoProducto.CHAROLA");
    }

    @Test
    @DisplayName("detalleProduccion - postres debe llamar findDetalleProductosCocina con TipoProducto.POSTRE")
    public void detalleProduccion_postres_llamaFindDetalleConTipoPostre() {
        when(resumenProduccionRepository.findDetalleProductosCocina(
                eq(TipoProducto.POSTRE), any(), any(), any(), any(), anyBoolean(), anyList()))
                .thenReturn(List.of());

        DetalleProduccionResponseDTO result = resumenProduccionService.detalleProduccion("postres", null, null, null);

        assertEquals("postres", result.getCategoria());
        verify(resumenProduccionRepository).findDetalleProductosCocina(
                eq(TipoProducto.POSTRE), any(), any(), any(), any(), anyBoolean(), anyList());
        System.out.println("[OK] detalleProduccion postres llamó repo con TipoProducto.POSTRE");
    }

    @Test
    @DisplayName("detalleProduccion - categoría inválida debe lanzar BusinessException sin llamar al repo")
    public void detalleProduccion_categoriaInvalida_lanzaBusinessException() {
        assertThrows(BusinessException.class, () ->
                resumenProduccionService.detalleProduccion("xyz", null, null, null));

        verifyNoInteractions(resumenProduccionRepository);
        System.out.println("[OK] detalleProduccion 'xyz' lanzó BusinessException sin invocar el repo");
    }

    @Test
    @DisplayName("detalleProduccion - la cantidad Long del repo debe mapearse a int en ItemProduccionDTO")
    public void detalleProduccion_cantidadMapeadaCorrectamente() {
        when(resumenProduccionRepository.findDetalleProductosCocina(
                eq(TipoProducto.SNACK), any(), any(), any(), any(), anyBoolean(), anyList()))
                .thenReturn(List.<Object[]>of(new Object[]{"Papas", 7L}));

        DetalleProduccionResponseDTO result = resumenProduccionService.detalleProduccion("snacks", null, null, null);

        assertEquals(1, result.getItems().size());
        assertEquals("Papas", result.getItems().get(0).getNombre());
        assertEquals(7, result.getItems().get(0).getCantidad());
        System.out.println("[OK] cantidad mapeada Long→int correctamente: " + result.getItems().get(0).getCantidad());
    }
}
