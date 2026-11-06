package com.oferta;

import com.cocinarubi.DBConstants.ModoMatchOferta;
import com.cocinarubi.DBConstants.TipoComida;
import com.cocinarubi.DBConstants.TipoEntidadOferta;
import com.cocinarubi.domain.entity.*;
import com.cocinarubi.domain.model.OfertaAplicacion;
import com.cocinarubi.domain.service.OfertaMatcherService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios del algoritmo greedy de matching de ofertas.
 * Sin Spring context ni acceso a BD — únicamente lógica pura.
 */
public class OfertaMatcherServiceTest {

    private final OfertaMatcherService matcher = new OfertaMatcherService();

    // -------------------------------------------------------------------------
    // Helpers de construcción en memoria
    // -------------------------------------------------------------------------

    private Oferta oferta(int id, double precio, int prioridad, List<OfertaLinea> lineas) {
        Oferta o = Oferta.builder()
                .idOferta(id).nombre("Oferta #" + id)
                .precioOferta(BigDecimal.valueOf(precio))
                .prioridad(prioridad).activo(true).build();
        lineas.forEach(o::addLinea);
        return o;
    }

    private OfertaLinea linea(ModoMatchOferta modo, Integer idRef, TipoComida filtro, int cant) {
        return OfertaLinea.builder()
                .modoMatch(modo).idReferencia(idRef).tipoComidaFiltro(filtro).cantidad(cant).build();
    }

    private ComidaPedido comidaPedido(int idComida, TipoComida tipo, double precio) {
        Comida c = Comida.builder()
                .idComida(idComida).nombreComida("Comida #" + idComida).tipoComida(tipo).build();
        return ComidaPedido.builder()
                .comida(c).precioUnitario(BigDecimal.valueOf(precio)).build();
    }

    private ProductoCocinaPedido productoPedido(int idProducto, Integer idCategoria,
                                                 List<Integer> idSubcategorias, double precio) {
        List<Subcategoria> subs = idSubcategorias.stream()
                .map(id -> Subcategoria.builder().idSubcategoria(id).build())
                .toList();
        Categoria cat = idCategoria != null
                ? Categoria.builder().idCategoria(idCategoria).build() : null;
        ProductoCocina pc = ProductoCocina.builder()
                .idProductoCocina(idProducto).nombreProducto("Producto #" + idProducto)
                .categoria(cat).subcategorias(new ArrayList<>(subs)).build();
        return ProductoCocinaPedido.builder()
                .productoCocina(pc).precioUnitario(BigDecimal.valueOf(precio)).cantidad((byte) 1).build();
    }

    private BasicoPedido basicoPedido(int idBasico, double precio) {
        Basico b = Basico.builder().idBasico(idBasico).descripcion("Basico #" + idBasico).build();
        return BasicoPedido.builder().basico(b).precioUnitario(BigDecimal.valueOf(precio)).build();
    }

    private ComboPedido comboPedido(int idCombo, double precio) {
        Combo c = Combo.builder().idCombo(idCombo).descripcion("Combo #" + idCombo).build();
        return ComboPedido.builder().combo(c).precioUnitario(BigDecimal.valueOf(precio)).cantidad(1).build();
    }

    // -------------------------------------------------------------------------
    // CA1 — COMIDA_ESPECIFICA + PRODUCTO_POR_CATEGORIA
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("CA1 - comida específica + producto por categoría → 1 aplicación")
    void ca1_comidaEspecifica_masProductoCategoria() {
        ComidaPedido cp = comidaPedido(10, TipoComida.FIJA, 45.0);
        ProductoCocinaPedido pcp = productoPedido(5, 2, List.of(), 25.0);

        Oferta o = oferta(1, 60.0, 0, List.of(
                linea(ModoMatchOferta.COMIDA_ESPECIFICA, 10, null, 1),
                linea(ModoMatchOferta.PRODUCTO_POR_CATEGORIA, 2, null, 1)
        ));

        List<OfertaAplicacion> result = matcher.detectar(
                List.of(cp), List.of(pcp), List.of(), List.of(), List.of(o));

        assertEquals(1, result.size());
        assertEquals(BigDecimal.valueOf(60.0), result.get(0).getPrecioAplicado());
        assertEquals(2, result.get(0).getLineas().size());
        System.out.println("[OK] CA1 → 1 aplicación, precio=" + result.get(0).getPrecioAplicado()
                + " ahorro=" + result.get(0).getAhorro());
    }

    // -------------------------------------------------------------------------
    // CA2 — COMIDA_POR_TIPO (FIJA) + PRODUCTO_ESPECIFICO
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("CA2 - comida tipo FIJA + producto específico → 1 aplicación")
    void ca2_comidaPorTipoFija_masProductoEspecifico() {
        ComidaPedido cp = comidaPedido(11, TipoComida.FIJA, 45.0);
        ProductoCocinaPedido pcp = productoPedido(7, 3, List.of(), 20.0);

        Oferta o = oferta(2, 55.0, 0, List.of(
                linea(ModoMatchOferta.COMIDA_POR_TIPO, null, TipoComida.FIJA, 1),
                linea(ModoMatchOferta.PRODUCTO_ESPECIFICO, 7, null, 1)
        ));

        List<OfertaAplicacion> result = matcher.detectar(
                List.of(cp), List.of(pcp), List.of(), List.of(), List.of(o));

        assertEquals(1, result.size());
        assertEquals(BigDecimal.valueOf(55.0), result.get(0).getPrecioAplicado());
        assertEquals(2, result.get(0).getLineas().size());
        System.out.println("[OK] CA2 → 1 aplicación, lineas=" + result.get(0).getLineas().size());
    }

    // -------------------------------------------------------------------------
    // CA3 — 2 COMIDA_FIJA + PRODUCTO_POR_SUBCATEGORIA
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("CA3 - 2 comidas FIJA + producto con subcategoría → 1 aplicación con 3 líneas")
    void ca3_dosComidaFija_masProductoSubcategoria() {
        ComidaPedido cp1 = comidaPedido(1, TipoComida.FIJA, 45.0);
        ComidaPedido cp2 = comidaPedido(2, TipoComida.FIJA, 45.0);
        ProductoCocinaPedido pcp = productoPedido(9, 4, List.of(99), 15.0);

        Oferta o = oferta(3, 90.0, 0, List.of(
                linea(ModoMatchOferta.COMIDA_POR_TIPO, null, TipoComida.FIJA, 2),
                linea(ModoMatchOferta.PRODUCTO_POR_SUBCATEGORIA, 99, null, 1)
        ));

        List<OfertaAplicacion> result = matcher.detectar(
                List.of(cp1, cp2), List.of(pcp), List.of(), List.of(), List.of(o));

        assertEquals(1, result.size());
        assertEquals(3, result.get(0).getLineas().size());
        System.out.println("[OK] CA3 → líneas consumidas=" + result.get(0).getLineas().size());
    }

    // -------------------------------------------------------------------------
    // CA4 — 3 líneas de tipos distintos (COMIDA + BASICO + COMBO)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("CA4 - comida ESPECIAL + básico específico + combo específico → 1 aplicación")
    void ca4_tresLineasDiferentes() {
        ComidaPedido cp = comidaPedido(3, TipoComida.ESPECIAL, 55.0);
        BasicoPedido bp = basicoPedido(6, 30.0);
        ComboPedido combop = comboPedido(4, 80.0);

        Oferta o = oferta(4, 140.0, 0, List.of(
                linea(ModoMatchOferta.COMIDA_POR_TIPO, null, TipoComida.ESPECIAL, 1),
                linea(ModoMatchOferta.BASICO_ESPECIFICO, 6, null, 1),
                linea(ModoMatchOferta.COMBO_ESPECIFICO, 4, null, 1)
        ));

        List<OfertaAplicacion> result = matcher.detectar(
                List.of(cp), List.of(), List.of(bp), List.of(combop), List.of(o));

        assertEquals(1, result.size());
        assertEquals(3, result.get(0).getLineas().size());
        System.out.println("[OK] CA4 → 3 tipos distintos, 1 aplicación");
    }

    // -------------------------------------------------------------------------
    // CA5 — Misma oferta aplicada 2 veces
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("CA5 - 2 FIJA + 2 productos de la categoría → oferta aplicada 2 veces")
    void ca5_ofertaAplicadaDosVeces() {
        ComidaPedido cp1 = comidaPedido(1, TipoComida.FIJA, 45.0);
        ComidaPedido cp2 = comidaPedido(2, TipoComida.FIJA, 45.0);
        ProductoCocinaPedido pcp1 = productoPedido(5, 2, List.of(), 25.0);
        ProductoCocinaPedido pcp2 = productoPedido(6, 2, List.of(), 25.0);

        Oferta o = oferta(1, 60.0, 0, List.of(
                linea(ModoMatchOferta.COMIDA_POR_TIPO, null, TipoComida.FIJA, 1),
                linea(ModoMatchOferta.PRODUCTO_POR_CATEGORIA, 2, null, 1)
        ));

        List<OfertaAplicacion> result = matcher.detectar(
                List.of(cp1, cp2), List.of(pcp1, pcp2), List.of(), List.of(), List.of(o));

        assertEquals(2, result.size());
        System.out.println("[OK] CA5 → oferta aplicada " + result.size() + " veces");
    }

    // -------------------------------------------------------------------------
    // CA6 — Comida ESPECIAL no satisface regla FIJA → 0 aplicaciones
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("CA6 - comida ESPECIAL no satisface oferta que pide FIJA → 0 aplicaciones")
    void ca6_comidaEspecialNoMatchaOfertaFija() {
        ComidaPedido cp = comidaPedido(1, TipoComida.ESPECIAL, 60.0);
        ProductoCocinaPedido pcp = productoPedido(5, 2, List.of(), 20.0);

        Oferta o = oferta(1, 70.0, 0, List.of(
                linea(ModoMatchOferta.COMIDA_POR_TIPO, null, TipoComida.FIJA, 1),
                linea(ModoMatchOferta.PRODUCTO_POR_CATEGORIA, 2, null, 1)
        ));

        List<OfertaAplicacion> result = matcher.detectar(
                List.of(cp), List.of(pcp), List.of(), List.of(), List.of(o));

        assertTrue(result.isEmpty());
        System.out.println("[OK] CA6 → 0 aplicaciones (ESPECIAL no cumple regla FIJA)");
    }

    // -------------------------------------------------------------------------
    // BASICO_ESPECIFICO
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("BASICO_ESPECIFICO - idBasico exacto coincide → 1 aplicación")
    void basicoEspecifico_match() {
        BasicoPedido bp = basicoPedido(10, 40.0);
        Oferta o = oferta(5, 35.0, 0, List.of(
                linea(ModoMatchOferta.BASICO_ESPECIFICO, 10, null, 1)
        ));

        List<OfertaAplicacion> result = matcher.detectar(
                List.of(), List.of(), List.of(bp), List.of(), List.of(o));

        assertEquals(1, result.size());
        assertEquals(TipoEntidadOferta.BASICO, result.get(0).getLineas().get(0).getTipoEntidad());
        System.out.println("[OK] BASICO_ESPECIFICO → 1 aplicación");
    }

    @Test
    @DisplayName("BASICO_ESPECIFICO - idBasico diferente → 0 aplicaciones")
    void basicoEspecifico_noMatch() {
        BasicoPedido bp = basicoPedido(99, 40.0);
        Oferta o = oferta(5, 35.0, 0, List.of(
                linea(ModoMatchOferta.BASICO_ESPECIFICO, 10, null, 1)
        ));

        List<OfertaAplicacion> result = matcher.detectar(
                List.of(), List.of(), List.of(bp), List.of(), List.of(o));

        assertTrue(result.isEmpty());
        System.out.println("[OK] BASICO_ESPECIFICO id diferente → 0 aplicaciones");
    }

    // -------------------------------------------------------------------------
    // BASICO_GENERAL
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("BASICO_GENERAL - cualquier básico coincide → 1 aplicación")
    void basicoGeneral_match() {
        BasicoPedido bp = basicoPedido(42, 40.0);
        Oferta o = oferta(6, 30.0, 0, List.of(
                linea(ModoMatchOferta.BASICO_GENERAL, null, null, 1)
        ));

        List<OfertaAplicacion> result = matcher.detectar(
                List.of(), List.of(), List.of(bp), List.of(), List.of(o));

        assertEquals(1, result.size());
        System.out.println("[OK] BASICO_GENERAL → 1 aplicación");
    }

    // -------------------------------------------------------------------------
    // COMBO_ESPECIFICO
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("COMBO_ESPECIFICO - idCombo exacto coincide → 1 aplicación")
    void comboEspecifico_match() {
        ComboPedido cp = comboPedido(7, 120.0);
        Oferta o = oferta(7, 100.0, 0, List.of(
                linea(ModoMatchOferta.COMBO_ESPECIFICO, 7, null, 1)
        ));

        List<OfertaAplicacion> result = matcher.detectar(
                List.of(), List.of(), List.of(), List.of(cp), List.of(o));

        assertEquals(1, result.size());
        assertEquals(TipoEntidadOferta.COMBO, result.get(0).getLineas().get(0).getTipoEntidad());
        System.out.println("[OK] COMBO_ESPECIFICO → 1 aplicación");
    }

    // -------------------------------------------------------------------------
    // COMBO_GENERAL
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("COMBO_GENERAL - cualquier combo coincide → 1 aplicación")
    void comboGeneral_match() {
        ComboPedido cp = comboPedido(33, 100.0);
        Oferta o = oferta(8, 80.0, 0, List.of(
                linea(ModoMatchOferta.COMBO_GENERAL, null, null, 1)
        ));

        List<OfertaAplicacion> result = matcher.detectar(
                List.of(), List.of(), List.of(), List.of(cp), List.of(o));

        assertEquals(1, result.size());
        System.out.println("[OK] COMBO_GENERAL → 1 aplicación");
    }

    // -------------------------------------------------------------------------
    // PRODUCTO_GENERAL
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("PRODUCTO_GENERAL - cualquier ProductoCocina coincide → 1 aplicación")
    void productoGeneral_match() {
        ProductoCocinaPedido pcp = productoPedido(20, 5, List.of(), 30.0);
        Oferta o = oferta(9, 25.0, 0, List.of(
                linea(ModoMatchOferta.PRODUCTO_GENERAL, null, null, 1)
        ));

        List<OfertaAplicacion> result = matcher.detectar(
                List.of(), List.of(pcp), List.of(), List.of(), List.of(o));

        assertEquals(1, result.size());
        System.out.println("[OK] PRODUCTO_GENERAL → 1 aplicación");
    }

    // -------------------------------------------------------------------------
    // Prioridad — oferta de mayor prioridad se aplica primero
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Prioridad - oferta alta consume el pool antes que la oferta baja")
    void prioridadOrden_altaAntesQueBaja() {
        ComidaPedido cp = comidaPedido(1, TipoComida.FIJA, 45.0);

        Oferta alta = oferta(1, 30.0, 10, List.of(
                linea(ModoMatchOferta.COMIDA_POR_TIPO, null, TipoComida.FIJA, 1)
        ));
        Oferta baja = oferta(2, 25.0, 1, List.of(
                linea(ModoMatchOferta.COMIDA_POR_TIPO, null, TipoComida.FIJA, 1)
        ));

        // Se pasan ya ordenadas DESC por prioridad (igual que OfertaRepository.findActivasConLineas)
        List<OfertaAplicacion> result = matcher.detectar(
                List.of(cp), List.of(), List.of(), List.of(), List.of(alta, baja));

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getOferta().getIdOferta(),
                "La oferta de prioridad alta debe haber consumido el pool primero");
        System.out.println("[OK] prioridad → oferta id=" + result.get(0).getOferta().getIdOferta() + " ganó");
    }

    // -------------------------------------------------------------------------
    // Pool exhausted — una línea no puede entrar en dos ofertas
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Pool exhaustion - línea consumida por primera oferta no es usada por segunda")
    void poolExhausted_noDuplicateConsumption() {
        ComidaPedido cp = comidaPedido(1, TipoComida.FIJA, 45.0);

        Oferta o1 = oferta(1, 35.0, 0, List.of(
                linea(ModoMatchOferta.COMIDA_POR_TIPO, null, TipoComida.FIJA, 1)
        ));
        Oferta o2 = oferta(2, 35.0, 0, List.of(
                linea(ModoMatchOferta.COMIDA_POR_TIPO, null, TipoComida.FIJA, 1)
        ));

        List<OfertaAplicacion> result = matcher.detectar(
                List.of(cp), List.of(), List.of(), List.of(), List.of(o1, o2));

        assertEquals(1, result.size(), "Solo 1 oferta debe aplicar; la línea ya fue consumida");
        System.out.println("[OK] pool exhaustion → " + result.size() + " aplicación (sin duplicación)");
    }

    // -------------------------------------------------------------------------
    // Ahorro calculado correctamente
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Ahorro = suma precios normales - precioOferta")
    void ahorroCorrecto() {
        ComidaPedido cp = comidaPedido(1, TipoComida.FIJA, 45.0);
        ProductoCocinaPedido pcp = productoPedido(5, 2, List.of(), 25.0);

        Oferta o = oferta(1, 60.0, 0, List.of(
                linea(ModoMatchOferta.COMIDA_POR_TIPO, null, TipoComida.FIJA, 1),
                linea(ModoMatchOferta.PRODUCTO_POR_CATEGORIA, 2, null, 1)
        ));

        List<OfertaAplicacion> result = matcher.detectar(
                List.of(cp), List.of(pcp), List.of(), List.of(), List.of(o));

        assertEquals(1, result.size());
        // ahorro = (45 + 25) - 60 = 10
        assertEquals(0, BigDecimal.valueOf(10.0).compareTo(result.get(0).getAhorro()),
                "Ahorro esperado = 10.0");
        System.out.println("[OK] ahorro=" + result.get(0).getAhorro());
    }

    // -------------------------------------------------------------------------
    // Pool vacío → sin aplicaciones
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Pool vacío → 0 aplicaciones aunque haya ofertas activas")
    void poolVacio_sinAplicaciones() {
        Oferta o = oferta(1, 60.0, 0, List.of(
                linea(ModoMatchOferta.COMIDA_POR_TIPO, null, TipoComida.FIJA, 1)
        ));

        List<OfertaAplicacion> result = matcher.detectar(
                List.of(), List.of(), List.of(), List.of(), List.of(o));

        assertTrue(result.isEmpty());
        System.out.println("[OK] pool vacío → 0 aplicaciones");
    }
}
