package com.catalogopedido;

import com.cocinarubi.DBConstants.Estatus;
import com.cocinarubi.DBConstants.TamanoPorcion;
import com.cocinarubi.DBConstants.TipoComida;
import com.cocinarubi.DBConstants.TipoDescuento;
import com.cocinarubi.dao.BasicoRepository;
import com.cocinarubi.dao.CodigoClienteRepository;
import com.cocinarubi.dao.RegistroClienteRepository;
import com.cocinarubi.domain.entity.Comida;
import com.cocinarubi.domain.entity.ComidaPedido;
import com.cocinarubi.domain.entity.Complemento;
import com.cocinarubi.domain.entity.Combo;
import com.cocinarubi.domain.entity.Pedido;
import com.cocinarubi.domain.service.CatalogoPedidoService;
import com.cocinarubi.domain.service.ComboService;
import com.cocinarubi.domain.service.ComidaService;
import com.cocinarubi.domain.service.ComplementoService;
import com.cocinarubi.domain.service.DesayunoService;
import com.cocinarubi.domain.service.ProductoCocinaService;
import com.cocinarubi.domain.service.RutaService;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.ComboPedidoDTO;
import com.cocinarubi.presentation.dto.request.ComidaPedidoDTO;
import com.cocinarubi.presentation.dto.request.ComplementoPedidoDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CatalogoPedidoServiceTest {

    @Mock private ComidaService comidaService;
    @Mock private ComplementoService complementoService;
    @Mock private DesayunoService desayunoService;
    @Mock private BasicoRepository basicoRepository;
    @Mock private ProductoCocinaService productoCocinaService;
    @Mock private RutaService rutaService;
    @Mock private RegistroClienteRepository registroClienteRepository;
    @Mock private ComboService comboService;
    @Mock private CodigoClienteRepository codigoClienteRepository;

    @InjectMocks
    private CatalogoPedidoService catalogoPedidoService;

    // ── Helpers de fixture ────────────────────────────────────────────────────

    private Comida comida(Integer limiteComplemento) {
        Comida c = new Comida();
        c.setIdComida(1);
        c.setNombreComida("Pollo");
        c.setLimiteComplemento(limiteComplemento);
        return c;
    }

    private Complemento complemento(int id, boolean cobrarSiempre, BigDecimal precioExtra) {
        Complemento c = new Complemento();
        c.setIdComplemento(id);
        c.setNombreComplemento("Comp-" + id);
        c.setCobrarSiempre(cobrarSiempre);
        c.setPrecioExtra(precioExtra);
        return c;
    }

    private ComplementoPedidoDTO compDto(int idComplemento, BigDecimal precioUnitario) {
        ComplementoPedidoDTO dto = new ComplementoPedidoDTO();
        dto.setIdComplemento(idComplemento);
        dto.setPrecioUnitario(precioUnitario);
        return dto;
    }

    private ComidaPedidoDTO lineaCon(List<ComplementoPedidoDTO> complementos) {
        ComidaPedidoDTO dto = new ComidaPedidoDTO();
        dto.setIdComida(1);
        dto.setPrecioUnitario(BigDecimal.valueOf(90));
        dto.setTamanoPorcion(TamanoPorcion.ENTERA);
        dto.setComplementos(complementos);
        return dto;
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("agregarComidas - Sin limite_complemento usa precio del catálogo (comportamiento original)")
    public void agregarComidas_sinLimite_usaPrecioDelCatalogo() {
        when(comidaService.findById(1)).thenReturn(comida(null));
        when(complementoService.findById(1)).thenReturn(complemento(1, false, BigDecimal.valueOf(5)));

        Pedido pedido = Pedido.builder().build();
        // El cliente no envía precioUnitario (era ignorado antes del cambio)
        catalogoPedidoService.agregarComidas(pedido, List.of(lineaCon(List.of(compDto(1, null)))));

        BigDecimal precio = pedido.getComidasPedido().get(0).getComplementos().get(0).getPrecioUnitario();
        assertEquals(0, BigDecimal.valueOf(5).compareTo(precio));
        System.out.println("[OK] sin límite usa precio catálogo=" + precio);
    }

    @Test
    @DisplayName("agregarComidas - Con límite y sin complementos no lanza error")
    public void agregarComidas_conLimite_sinComplementos_ok() {
        when(comidaService.findById(1)).thenReturn(comida(3));

        Pedido pedido = Pedido.builder().build();
        assertDoesNotThrow(() ->
                catalogoPedidoService.agregarComidas(pedido, List.of(lineaCon(List.of()))));

        assertTrue(pedido.getComidasPedido().get(0).getComplementos().isEmpty());
        System.out.println("[OK] sin complementos con límite=3 no lanza error");
    }

    @Test
    @DisplayName("agregarComidas - cobrar_siempre sin precio en la solicitud lanza BusinessException")
    public void agregarComidas_conLimite_cobrarSiempreSinPrecio_lanzaError() {
        when(comidaService.findById(1)).thenReturn(comida(3));
        when(complementoService.findById(1)).thenReturn(complemento(1, true, BigDecimal.valueOf(5)));

        Pedido pedido = Pedido.builder().build();
        // El cliente envía cobrar_siempre sin precio → error
        assertThrows(BusinessException.class, () ->
                catalogoPedidoService.agregarComidas(pedido,
                        List.of(lineaCon(List.of(compDto(1, null))))));
        System.out.println("[OK] cobrar_siempre sin precio lanza BusinessException");
    }

    @Test
    @DisplayName("agregarComidas - No-cobrar dentro del límite no requieren precio")
    public void agregarComidas_conLimite_noCobrarDentroDeLimite_ok() {
        // límite=3, 2 no_cobrar → slotsLibres=3, exceso=0 → no error aunque no lleven precio
        when(comidaService.findById(1)).thenReturn(comida(3));
        when(complementoService.findById(1)).thenReturn(complemento(1, false, BigDecimal.valueOf(3)));
        when(complementoService.findById(2)).thenReturn(complemento(2, false, BigDecimal.valueOf(3)));

        Pedido pedido = Pedido.builder().build();
        assertDoesNotThrow(() ->
                catalogoPedidoService.agregarComidas(pedido,
                        List.of(lineaCon(List.of(
                                compDto(1, null),
                                compDto(2, null))))));
        System.out.println("[OK] 2 no-cobrar sin precio dentro de límite=3 pasa sin error");
    }

    @Test
    @DisplayName("agregarComidas - límite=3, 2 cobrar_siempre + 2 no_cobrar → cobrarSiempre no consume slots, ambos no-cobrar son gratis")
    public void agregarComidas_conLimite_escenario_del_plan_validacionOk() {
        // cobrarSiempre NO consume slots → slotsLibres=3
        // 2 no_cobrar → exceso=0 → ambos quedan gratis aunque cobrarSiempre esté presente
        when(comidaService.findById(1)).thenReturn(comida(3));
        when(complementoService.findById(1)).thenReturn(complemento(1, true,  BigDecimal.valueOf(5)));
        when(complementoService.findById(2)).thenReturn(complemento(2, true,  BigDecimal.valueOf(5)));
        when(complementoService.findById(3)).thenReturn(complemento(3, false, BigDecimal.valueOf(3)));
        when(complementoService.findById(4)).thenReturn(complemento(4, false, BigDecimal.valueOf(3)));

        Pedido pedido = Pedido.builder().build();
        assertDoesNotThrow(() ->
                catalogoPedidoService.agregarComidas(pedido,
                        List.of(lineaCon(List.of(
                                compDto(1, BigDecimal.valueOf(5)),  // cobrar_siempre, con precio
                                compDto(2, BigDecimal.valueOf(5)),  // cobrar_siempre, con precio
                                compDto(3, null),                   // no_cobrar → gratis (slot 1)
                                compDto(4, null))))));              // no_cobrar → gratis (slot 2)

        var comps = pedido.getComidasPedido().get(0).getComplementos();
        assertEquals(0, BigDecimal.valueOf(5).compareTo(comps.get(0).getPrecioUnitario()));
        assertEquals(0, BigDecimal.valueOf(5).compareTo(comps.get(1).getPrecioUnitario()));
        assertEquals(0, BigDecimal.ZERO.compareTo(comps.get(2).getPrecioUnitario()));
        assertEquals(0, BigDecimal.ZERO.compareTo(comps.get(3).getPrecioUnitario()));
        System.out.println("[OK] cobrarSiempre no consume slots: 2 no-cobrar quedan gratis con límite=3");
    }

    @Test
    @DisplayName("agregarComidas - Exceso de no_cobrar sobre el límite sin precios suficientes lanza BusinessException con mensaje 'Al menos N'")
    public void agregarComidas_conLimite_exceso_sinPrecioSuficiente_lanzaError() {
        // límite=1, cobrarSiempre no consume slots → slotsLibres=1
        // 2 no_cobrar → exceso=1 → necesita al menos 1 con precio, pero ninguno lo lleva → error
        when(comidaService.findById(1)).thenReturn(comida(1));
        when(complementoService.findById(1)).thenReturn(complemento(1, true,  BigDecimal.valueOf(5)));
        when(complementoService.findById(2)).thenReturn(complemento(2, false, BigDecimal.valueOf(3)));
        when(complementoService.findById(3)).thenReturn(complemento(3, false, BigDecimal.valueOf(3)));

        Pedido pedido = Pedido.builder().build();
        BusinessException ex = assertThrows(BusinessException.class, () ->
                catalogoPedidoService.agregarComidas(pedido,
                        List.of(lineaCon(List.of(
                                compDto(1, BigDecimal.valueOf(5)),  // cobrar_siempre (no consume slot)
                                compDto(2, null),                   // no_cobrar sin precio
                                compDto(3, null))))));              // no_cobrar sin precio → exceso=1 → error
        assertTrue(ex.getMessage().contains("Al menos 1"));
        System.out.println("[OK] exceso sin precios suficientes lanza: " + ex.getMessage());
    }

    // ── Tests nueva lógica cobrarSiempre / slots ──────────────────────────────

    @Test
    @DisplayName("Escenario 1 — límite=1: primer no-cobrar gratis, segundo no-cobrar cobrado")
    public void agregarComidas_conLimite1_primerGratisSegundoCobrado_ok() {
        when(comidaService.findById(1)).thenReturn(comida(1));
        when(complementoService.findById(1)).thenReturn(complemento(1, false, BigDecimal.valueOf(3)));
        when(complementoService.findById(2)).thenReturn(complemento(2, false, BigDecimal.valueOf(3)));

        Pedido pedido = Pedido.builder().build();
        assertDoesNotThrow(() ->
                catalogoPedidoService.agregarComidas(pedido,
                        List.of(lineaCon(List.of(
                                compDto(1, null),                   // orden 1 → gratis (slot 1)
                                compDto(2, BigDecimal.valueOf(3)))))));  // orden 2 → cobrado (exceso)

        var comps = pedido.getComidasPedido().get(0).getComplementos();
        assertEquals(0, BigDecimal.ZERO.compareTo(comps.get(0).getPrecioUnitario()), "comp1 debe ser gratis");
        assertEquals(0, BigDecimal.valueOf(3).compareTo(comps.get(1).getPrecioUnitario()), "comp2 debe ser cobrado");
        System.out.println("[OK] límite=1: comp1=0, comp2=3");
    }

    @Test
    @DisplayName("Escenario 2 — límite=1: no-cobrar gratis + cobrarSiempre cobrado (cobrarSiempre no consume slot)")
    public void agregarComidas_conLimite1_noCobrarGratis_cobrarSiempreCobrado() {
        when(comidaService.findById(1)).thenReturn(comida(1));
        when(complementoService.findById(1)).thenReturn(complemento(1, false, BigDecimal.valueOf(3)));
        when(complementoService.findById(2)).thenReturn(complemento(2, true,  BigDecimal.valueOf(5)));

        Pedido pedido = Pedido.builder().build();
        assertDoesNotThrow(() ->
                catalogoPedidoService.agregarComidas(pedido,
                        List.of(lineaCon(List.of(
                                compDto(1, null),                   // no-cobrar → gratis (slot 1)
                                compDto(2, BigDecimal.valueOf(5)))))));  // cobrarSiempre → cobrado, NO consume slot

        var comps = pedido.getComidasPedido().get(0).getComplementos();
        assertEquals(0, BigDecimal.ZERO.compareTo(comps.get(0).getPrecioUnitario()), "no-cobrar debe ser gratis");
        assertEquals(0, BigDecimal.valueOf(5).compareTo(comps.get(1).getPrecioUnitario()), "cobrarSiempre debe ser cobrado");
        System.out.println("[OK] límite=1: no-cobrar=0, cobrarSiempre=5");
    }

    @Test
    @DisplayName("Escenario 3 — límite=3: tres gratis y el cuarto cobrado")
    public void agregarComidas_conLimite3_tresFreeUnoExceso_ok() {
        when(comidaService.findById(1)).thenReturn(comida(3));
        when(complementoService.findById(1)).thenReturn(complemento(1, false, BigDecimal.valueOf(3)));
        when(complementoService.findById(2)).thenReturn(complemento(2, false, BigDecimal.valueOf(3)));
        when(complementoService.findById(3)).thenReturn(complemento(3, false, BigDecimal.valueOf(3)));
        when(complementoService.findById(4)).thenReturn(complemento(4, false, BigDecimal.valueOf(3)));

        Pedido pedido = Pedido.builder().build();
        assertDoesNotThrow(() ->
                catalogoPedidoService.agregarComidas(pedido,
                        List.of(lineaCon(List.of(
                                compDto(1, null),                       // slot 1 → gratis
                                compDto(2, null),                       // slot 2 → gratis
                                compDto(3, null),                       // slot 3 → gratis
                                compDto(4, BigDecimal.valueOf(3)))))));  // exceso → cobrado

        var comps = pedido.getComidasPedido().get(0).getComplementos();
        assertEquals(4, comps.size());
        assertEquals(0, BigDecimal.ZERO.compareTo(comps.get(0).getPrecioUnitario()), "comp1 gratis");
        assertEquals(0, BigDecimal.ZERO.compareTo(comps.get(1).getPrecioUnitario()), "comp2 gratis");
        assertEquals(0, BigDecimal.ZERO.compareTo(comps.get(2).getPrecioUnitario()), "comp3 gratis");
        assertEquals(0, BigDecimal.valueOf(3).compareTo(comps.get(3).getPrecioUnitario()), "comp4 cobrado");
        System.out.println("[OK] límite=3: comp1,2,3=0, comp4=3");
    }

    @Test
    @DisplayName("Escenario 4 — cobrarSiempre primero NO consume slot: el no-cobrar siguiente es gratis")
    public void agregarComidas_cobrarSiempre_noConsumeSlotsGratuitos() {
        // cobrarSiempre viene PRIMERO, no-cobrar viene DESPUÉS
        // Con límite=1 el no-cobrar debe quedar gratis igualmente
        when(comidaService.findById(1)).thenReturn(comida(1));
        when(complementoService.findById(1)).thenReturn(complemento(1, true,  BigDecimal.valueOf(5)));
        when(complementoService.findById(2)).thenReturn(complemento(2, false, BigDecimal.valueOf(3)));

        Pedido pedido = Pedido.builder().build();
        assertDoesNotThrow(() ->
                catalogoPedidoService.agregarComidas(pedido,
                        List.of(lineaCon(List.of(
                                compDto(1, BigDecimal.valueOf(5)),   // cobrarSiempre primero → cobrado, no toca slots
                                compDto(2, null))))));               // no-cobrar → gratis (slot 1 sigue libre)

        var comps = pedido.getComidasPedido().get(0).getComplementos();
        assertEquals(0, BigDecimal.valueOf(5).compareTo(comps.get(0).getPrecioUnitario()), "cobrarSiempre cobrado");
        assertEquals(0, BigDecimal.ZERO.compareTo(comps.get(1).getPrecioUnitario()), "no-cobrar gratis aunque cobrarSiempre vino primero");
        System.out.println("[OK] cobrarSiempre primero no consume slot: cobrarSiempre=5, no-cobrar=0");
    }

    // ── Tests agregarPaquetes ─────────────────────────────────────────────────

    @Test
    @DisplayName("agregarCombos - agrega línea al pedido cuando el combo existe y está DISPONIBLE")
    public void agregarCombos_comboDisponible_agregaLineaAlPedido() {
        Combo combo = Combo.builder()
                .idCombo(7)
                .descripcion("Promo Familiar")
                .precio(new BigDecimal("150.00"))
                .estatus(Estatus.DISPONIBLE)
                .build();
        when(comboService.findEntityById(7)).thenReturn(combo);

        ComboPedidoDTO dto = new ComboPedidoDTO(7, new BigDecimal("140.00"), 2);
        Pedido pedido = Pedido.builder().build();
        catalogoPedidoService.agregarCombos(pedido, List.of(dto));

        assertEquals(1, pedido.getCombosPedido().size());
        assertEquals(7, pedido.getCombosPedido().get(0).getCombo().getIdCombo());
        assertEquals(0, new BigDecimal("140.00").compareTo(pedido.getCombosPedido().get(0).getPrecioUnitario()));
        assertEquals(2, pedido.getCombosPedido().get(0).getCantidad());
        // El helper addComboPedido debe haber seteado la relación bidireccional
        assertSame(pedido, pedido.getCombosPedido().get(0).getPedido());
        System.out.println("[OK] agregarCombos agregó 1 línea con id=7, cantidad=2, precio=140.00");
    }

    @Test
    @DisplayName("agregarCombos - lanza BusinessException cuando el combo NO_DISPONIBLE")
    public void agregarCombos_comboNoDisponible_lanzaError() {
        Combo combo = Combo.builder()
                .idCombo(8)
                .descripcion("Promo Cerrada")
                .precio(new BigDecimal("90.00"))
                .estatus(Estatus.NO_DISPONIBLE)
                .build();
        when(comboService.findEntityById(8)).thenReturn(combo);

        ComboPedidoDTO dto = new ComboPedidoDTO(8, new BigDecimal("90.00"), 1);
        Pedido pedido = Pedido.builder().build();
        BusinessException ex = assertThrows(BusinessException.class,
                () -> catalogoPedidoService.agregarCombos(pedido, List.of(dto)));
        assertTrue(ex.getMessage().contains("no está disponible"));
        assertTrue(pedido.getCombosPedido().isEmpty());
        System.out.println("[OK] agregarCombos lanzó error por combo NO_DISPONIBLE");
    }

    @Test
    @DisplayName("agregarCombos - lanza BusinessException cuando el combo AGOTADO")
    public void agregarCombos_comboAgotado_lanzaError() {
        Combo combo = Combo.builder()
                .idCombo(9)
                .descripcion("Promo Fin de Semana")
                .precio(new BigDecimal("80.00"))
                .estatus(Estatus.AGOTADO)
                .build();
        when(comboService.findEntityById(9)).thenReturn(combo);

        ComboPedidoDTO dto = new ComboPedidoDTO(9, new BigDecimal("80.00"), 1);
        Pedido pedido = Pedido.builder().build();
        assertThrows(BusinessException.class,
                () -> catalogoPedidoService.agregarCombos(pedido, List.of(dto)));
        System.out.println("[OK] agregarCombos lanzó error por combo AGOTADO");
    }

    @Test
    @DisplayName("agregarCombos - lista vacía no toca el pedido")
    public void agregarCombos_listaVacia_noHaceNada() {
        Pedido pedido = Pedido.builder().build();
        catalogoPedidoService.agregarCombos(pedido, List.of());
        assertTrue(pedido.getCombosPedido().isEmpty());
        verifyNoInteractions(comboService);
        System.out.println("[OK] agregarCombos con lista vacía no interactúa con ComboService");
    }

    // ── Helpers de fixture para descuento de volumen ──────────────────────────

    /** Crea un ComidaPedido listo para usar en pruebas de descuento de volumen. */
    private ComidaPedido lineaComida(TamanoPorcion porcion, TipoComida tipo) {
        Comida comida = new Comida();
        comida.setIdComida(1);
        comida.setTipoComida(tipo);
        return ComidaPedido.builder()
                .comida(comida)
                .tamanoPorcion(porcion)
                .precioUnitario(BigDecimal.valueOf(45))
                .build();
    }

    /** Construye un Pedido con las líneas de ComidaPedido dadas ya asociadas. */
    private Pedido pedidoCon(List<ComidaPedido> items) {
        Pedido pedido = Pedido.builder().build();
        items.forEach(pedido::addComidaPedido);
        return pedido;
    }

    // ── Tests aplicarDescuentoVolumen ─────────────────────────────────────────

    @Test
    @DisplayName("aplicarDescuentoVolumen - exactamente 12 MEDIA FIJA activa el descuento en todas")
    public void descuentoVolumen_12MediaFija_aplicaDescuento() {
        List<ComidaPedido> items = java.util.Collections.nCopies(12,
                lineaComida(TamanoPorcion.MEDIA, TipoComida.FIJA));
        Pedido pedido = pedidoCon(new java.util.ArrayList<>(items));

        catalogoPedidoService.aplicarDescuentoVolumen(pedido);

        assertEquals(TipoDescuento.COMIDAS_DIEZ, pedido.getTipoDescuento());
        pedido.getComidasPedido().forEach(cp ->
                assertEquals(0, BigDecimal.TEN.compareTo(cp.getDescuentoAplicado()),
                        "Cada línea debe tener descuentoAplicado=$10"));
        System.out.println("[OK] 12 MEDIA FIJA → descuento aplicado a todas");
    }

    @Test
    @DisplayName("aplicarDescuentoVolumen - 15 MEDIA FIJA aplica $10 en cada una ($150 total)")
    public void descuentoVolumen_15MediaFija_aplicaDescuentoEnTodas() {
        List<ComidaPedido> items = new java.util.ArrayList<>();
        for (int i = 0; i < 15; i++) items.add(lineaComida(TamanoPorcion.MEDIA, TipoComida.FIJA));
        Pedido pedido = pedidoCon(items);

        catalogoPedidoService.aplicarDescuentoVolumen(pedido);

        assertEquals(TipoDescuento.COMIDAS_DIEZ, pedido.getTipoDescuento());
        long conDescuento = pedido.getComidasPedido().stream()
                .filter(cp -> BigDecimal.TEN.compareTo(cp.getDescuentoAplicado()) == 0)
                .count();
        assertEquals(15, conDescuento);
        System.out.println("[OK] 15 MEDIA FIJA → $10 en las 15 = $150 total descontado");
    }

    @Test
    @DisplayName("aplicarDescuentoVolumen - 11 MEDIA FIJA + 1 ENTERA FIJA no alcanza el mínimo")
    public void descuentoVolumen_11MediaFija_1EnteraFija_sinDescuento() {
        List<ComidaPedido> items = new java.util.ArrayList<>();
        for (int i = 0; i < 11; i++) items.add(lineaComida(TamanoPorcion.MEDIA,  TipoComida.FIJA));
        items.add(lineaComida(TamanoPorcion.ENTERA, TipoComida.FIJA));
        Pedido pedido = pedidoCon(items);

        catalogoPedidoService.aplicarDescuentoVolumen(pedido);

        assertNull(pedido.getTipoDescuento());
        pedido.getComidasPedido().forEach(cp ->
                assertEquals(0, BigDecimal.ZERO.compareTo(cp.getDescuentoAplicado()),
                        "Sin descuento, descuentoAplicado debe ser $0"));
        System.out.println("[OK] 11 MEDIA + 1 ENTERA → count(MEDIA+FIJA)=11 < 12 → sin descuento");
    }

    @Test
    @DisplayName("aplicarDescuentoVolumen - 11 MEDIA FIJA + 1 MEDIA ESPECIAL: count(MEDIA+FIJA)=11 → sin descuento")
    public void descuentoVolumen_11MediaFija_1MediaEspecial_sinDescuento() {
        List<ComidaPedido> items = new java.util.ArrayList<>();
        for (int i = 0; i < 11; i++) items.add(lineaComida(TamanoPorcion.MEDIA, TipoComida.FIJA));
        items.add(lineaComida(TamanoPorcion.MEDIA, TipoComida.ESPECIAL));
        Pedido pedido = pedidoCon(items);

        catalogoPedidoService.aplicarDescuentoVolumen(pedido);

        assertNull(pedido.getTipoDescuento());
        System.out.println("[OK] 11 MEDIA FIJA + 1 MEDIA ESPECIAL → count=11 → sin descuento");
    }

    @Test
    @DisplayName("aplicarDescuentoVolumen - 12 medias (11 FIJA + 1 ESPECIAL): count(MEDIA+FIJA)=11 → sin descuento")
    public void descuentoVolumen_12MediaCon1Especial_sinDescuento() {
        List<ComidaPedido> items = new java.util.ArrayList<>();
        for (int i = 0; i < 11; i++) items.add(lineaComida(TamanoPorcion.MEDIA, TipoComida.FIJA));
        items.add(lineaComida(TamanoPorcion.MEDIA, TipoComida.ESPECIAL));
        Pedido pedido = pedidoCon(items);

        catalogoPedidoService.aplicarDescuentoVolumen(pedido);

        assertNull(pedido.getTipoDescuento());
        System.out.println("[OK] 12 medias (11 FIJA + 1 ESPECIAL) → count(MEDIA+FIJA)=11 → sin descuento");
    }

    @Test
    @DisplayName("aplicarDescuentoVolumen - 13 medias (12 FIJA + 1 ESPECIAL): descuento solo en las 12 FIJA")
    public void descuentoVolumen_13MediaCon1Especial_descuentoSoloEnFija() {
        List<ComidaPedido> items = new java.util.ArrayList<>();
        for (int i = 0; i < 12; i++) items.add(lineaComida(TamanoPorcion.MEDIA, TipoComida.FIJA));
        ComidaPedido especial = lineaComida(TamanoPorcion.MEDIA, TipoComida.ESPECIAL);
        items.add(especial);
        Pedido pedido = pedidoCon(items);

        catalogoPedidoService.aplicarDescuentoVolumen(pedido);

        assertEquals(TipoDescuento.COMIDAS_DIEZ, pedido.getTipoDescuento());
        long conDescuento = pedido.getComidasPedido().stream()
                .filter(cp -> BigDecimal.TEN.compareTo(cp.getDescuentoAplicado()) == 0)
                .count();
        assertEquals(12, conDescuento, "Solo las 12 FIJA deben tener descuento; la ESPECIAL no");
        assertEquals(0, BigDecimal.ZERO.compareTo(especial.getDescuentoAplicado()),
                "La comida ESPECIAL no debe tener descuento");
        System.out.println("[OK] 13 medias (12 FIJA + 1 ESPECIAL) → descuento en las 12 FIJA, ESPECIAL intacta");
    }

    @Test
    @DisplayName("aplicarDescuentoVolumen - re-evaluar en update resetea tipoDescuento si ya no aplica")
    public void descuentoVolumen_updateSinDescuento_resetaTipoDescuento() {
        // Simulación: pedido que antes tenía descuento, ahora solo tiene 5 comidas MEDIA FIJA
        List<ComidaPedido> items = new java.util.ArrayList<>();
        for (int i = 0; i < 5; i++) items.add(lineaComida(TamanoPorcion.MEDIA, TipoComida.FIJA));
        Pedido pedido = pedidoCon(items);
        pedido.setTipoDescuento(TipoDescuento.COMIDAS_DIEZ); // estado previo

        catalogoPedidoService.aplicarDescuentoVolumen(pedido);

        assertNull(pedido.getTipoDescuento(), "Después de re-evaluar, tipoDescuento debe ser null");
        System.out.println("[OK] update con 5 comidas resetea tipoDescuento a null");
    }
}
