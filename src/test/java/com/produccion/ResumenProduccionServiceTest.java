package com.produccion;

import com.cocinarubi.Constants;
import com.cocinarubi.DBConstants.TipoPedido;
import com.cocinarubi.dao.CategoriaRepository;
import com.cocinarubi.dao.ResumenProduccionRepository;
import com.cocinarubi.domain.entity.Categoria;
import com.cocinarubi.domain.service.ResumenProduccionService;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.response.DetalleProduccionResponseDTO;
import com.cocinarubi.presentation.dto.response.ResumenProduccionResponseDTO;
import com.cocinarubi.presentation.dto.response.TotalPorCategoriaDTO;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ResumenProduccionServiceTest {

    @Mock
    private ResumenProduccionRepository resumenProduccionRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ResumenProduccionService resumenProduccionService;

    @Captor
    private ArgumentCaptor<Boolean> filtrarRutaCaptor;

    @Captor
    private ArgumentCaptor<List<Integer>> idRutasCaptor;

    @Captor
    private ArgumentCaptor<TipoPedido> tipoPedidoCaptor;

    // ── Datos mock compartidos ───────────────────────────────────────────────
    // Ids alineados con V23: BEBIDA=1, CHAROLA=2, SNACK=3, POSTRE=4
    private final List<Object[]> PRODUCTOS_POR_CATEGORIA = List.of(
            new Object[]{1, "BEBIDA",  3L},
            new Object[]{2, "CHAROLA", 2L},
            new Object[]{3, "SNACK",   5L},
            new Object[]{4, "POSTRE",  1L}
    );

    private final List<Object[]> DETALLE_COMIDAS = List.of(
            new Object[]{"Pollo en salsa roja", 3L},
            new Object[]{"Pescado frito",        2L}
    );

    private void stubConteoBase(long comidas, long desayunos, long basicos) {
        when(resumenProduccionRepository.countComidas(any(), any(), any(), any(), anyBoolean(), anyList()))
                .thenReturn(comidas);
        when(resumenProduccionRepository.countDesayunos(any(), any(), any(), any(), anyBoolean(), anyList()))
                .thenReturn(desayunos);
        when(resumenProduccionRepository.countBasicos(any(), any(), any(), any(), anyBoolean(), anyList()))
                .thenReturn(basicos);
        when(resumenProduccionRepository.countProductosCocinaAgrupadoPorCategoria(
                any(), any(), any(), any(), anyBoolean(), anyList()))
                .thenReturn(PRODUCTOS_POR_CATEGORIA);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tests de resumenProduccion()
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("resumenProduccion - retorna totales agregados y el arreglo dinámico por categoría")
    public void resumenProduccion_sinFiltros_retornaTotales() {
        stubConteoBase(4L, 3L, 2L);

        ResumenProduccionResponseDTO result = resumenProduccionService.resumenProduccion(null, null, null);

        assertNotNull(result);
        assertEquals(4L, result.getTotalComidas());
        assertEquals(3L, result.getTotalDesayunos());
        assertEquals(2L, result.getTotalBasicos());

        List<TotalPorCategoriaDTO> totales = result.getTotalesProductosCocina();
        assertEquals(4, totales.size());
        // Verifica que cada categoría del mock aparece con su total
        assertEquals(3L, totales.stream().filter(t -> "BEBIDA".equals(t.getNombreCategoria()))
                .findFirst().orElseThrow().getTotal());
        assertEquals(5L, totales.stream().filter(t -> "SNACK".equals(t.getNombreCategoria()))
                .findFirst().orElseThrow().getTotal());
        System.out.println("[OK] resumenProduccion sin filtros: totales por categoría = " + totales.size());
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
        System.out.println("[OK] detalleProduccion desayunos");
    }

    @Test
    @DisplayName("detalleProduccion - basicos debe llamar findDetalleBasicos")
    public void detalleProduccion_basicos_llamaFindDetalleBasicos() {
        when(resumenProduccionRepository.findDetalleBasicos(any(), any(), any(), any(), anyBoolean(), anyList()))
                .thenReturn(List.<Object[]>of(new Object[]{"Básico completo", 4L}));

        DetalleProduccionResponseDTO result = resumenProduccionService.detalleProduccion("basicos", null, null, null);

        assertEquals("basicos", result.getCategoria());
        assertEquals(1, result.getItems().size());
        System.out.println("[OK] detalleProduccion basicos");
    }

    @Test
    @DisplayName("detalleProduccion - snacks debe llamar findDetalleProductosCocinaPorCategoria con id de SNACK")
    public void detalleProduccion_snacks_llamaFindDetalleConIdCategoriaSnack() {
        Categoria snack = Categoria.builder().idCategoria(3).nombre("SNACK").build();
        when(categoriaRepository.findByNombreIgnoreCase("snacks")).thenReturn(Optional.of(snack));
        when(resumenProduccionRepository.findDetalleProductosCocinaPorCategoria(
                eq(3), any(), any(), any(), any(), anyBoolean(), anyList()))
                .thenReturn(List.<Object[]>of(new Object[]{"Papas", 5L}));

        DetalleProduccionResponseDTO result = resumenProduccionService.detalleProduccion("snacks", null, null, null);

        assertEquals("snacks", result.getCategoria());
        assertEquals(1, result.getItems().size());
        System.out.println("[OK] detalleProduccion snacks resolvió por Categoria id=3");
    }

    @Test
    @DisplayName("detalleProduccion - categoría dinámica arbitraria (HELADO) delega en findByNombreIgnoreCase")
    public void detalleProduccion_categoriaDinamica_resuelvePorCategoriaRepo() {
        Categoria helado = Categoria.builder().idCategoria(5).nombre("HELADO").build();
        when(categoriaRepository.findByNombreIgnoreCase("HELADO")).thenReturn(Optional.of(helado));
        when(resumenProduccionRepository.findDetalleProductosCocinaPorCategoria(
                eq(5), any(), any(), any(), any(), anyBoolean(), anyList()))
                .thenReturn(List.<Object[]>of(new Object[]{"Cono", 3L}));

        DetalleProduccionResponseDTO result = resumenProduccionService.detalleProduccion("HELADO", null, null, null);

        assertEquals("helado", result.getCategoria());
        assertEquals(1, result.getItems().size());
        System.out.println("[OK] detalleProduccion resolvió categoría dinámica HELADO");
    }

    @Test
    @DisplayName("detalleProduccion - categoría inválida debe lanzar BusinessException")
    public void detalleProduccion_categoriaInvalida_lanzaBusinessException() {
        when(categoriaRepository.findByNombreIgnoreCase("xyz")).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () ->
                resumenProduccionService.detalleProduccion("xyz", null, null, null));

        System.out.println("[OK] detalleProduccion 'xyz' lanzó BusinessException");
    }
}
