package com.catalogopedido;

import com.cocinarubi.DBConstants.Estatus;
import com.cocinarubi.DBConstants.TamanoPorcion;
import com.cocinarubi.dao.BasicoRepository;
import com.cocinarubi.dao.RegistroClienteRepository;
import com.cocinarubi.domain.entity.Comida;
import com.cocinarubi.domain.entity.Complemento;
import com.cocinarubi.domain.entity.Paquete;
import com.cocinarubi.domain.entity.Pedido;
import com.cocinarubi.domain.service.CatalogoPedidoService;
import com.cocinarubi.domain.service.ComidaService;
import com.cocinarubi.domain.service.ComplementoService;
import com.cocinarubi.domain.service.DesayunoService;
import com.cocinarubi.domain.service.PaqueteService;
import com.cocinarubi.domain.service.ProductoCocinaService;
import com.cocinarubi.domain.service.RutaService;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.ComidaPedidoDTO;
import com.cocinarubi.presentation.dto.request.ComplementoPedidoDTO;
import com.cocinarubi.presentation.dto.request.PaquetePedidoDTO;
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
    @Mock private PaqueteService paqueteService;

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
    @DisplayName("agregarComidas - Escenario del plan: límite=3, 2 cobrar_siempre + 2 no_cobrar, 1 con precio → ok")
    public void agregarComidas_conLimite_escenario_del_plan_validacionOk() {
        // 2 cobrar_siempre consumen 2 de los 3 slots → slotsLibres=1
        // 2 no_cobrar → exceso=1 → se necesita al menos 1 con precio
        // comp3 lleva precio (el de exceso), comp4 no lleva (el gratuito) → ok
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
                                compDto(3, BigDecimal.valueOf(3)),  // no_cobrar, con precio (el que excede)
                                compDto(4, null))))));              // no_cobrar, sin precio (el gratuito)
        System.out.println("[OK] límite=3, 2 cobrar_siempre + 2 no_cobrar (1 con precio) pasa validación");
    }

    @Test
    @DisplayName("agregarComidas - Exceso con insuficientes precios lanza BusinessException con mensaje 'Al menos N'")
    public void agregarComidas_conLimite_exceso_sinPrecioSuficiente_lanzaError() {
        // Misma config que el escenario del plan pero ningún no-cobrar lleva precio
        when(comidaService.findById(1)).thenReturn(comida(3));
        when(complementoService.findById(1)).thenReturn(complemento(1, true,  BigDecimal.valueOf(5)));
        when(complementoService.findById(2)).thenReturn(complemento(2, true,  BigDecimal.valueOf(5)));
        when(complementoService.findById(3)).thenReturn(complemento(3, false, BigDecimal.valueOf(3)));
        when(complementoService.findById(4)).thenReturn(complemento(4, false, BigDecimal.valueOf(3)));

        Pedido pedido = Pedido.builder().build();
        BusinessException ex = assertThrows(BusinessException.class, () ->
                catalogoPedidoService.agregarComidas(pedido,
                        List.of(lineaCon(List.of(
                                compDto(1, BigDecimal.valueOf(5)),
                                compDto(2, BigDecimal.valueOf(5)),
                                compDto(3, null),
                                compDto(4, null))))));
        assertTrue(ex.getMessage().contains("Al menos 1"));
        System.out.println("[OK] exceso sin precios suficientes lanza: " + ex.getMessage());
    }

    // ── Tests agregarPaquetes ─────────────────────────────────────────────────

    @Test
    @DisplayName("agregarPaquetes - agrega línea al pedido cuando el paquete existe y está DISPONIBLE")
    public void agregarPaquetes_paqueteDisponible_agregaLineaAlPedido() {
        Paquete paquete = Paquete.builder()
                .idPaquete(7)
                .descripcion("Promo Familiar")
                .precio(new BigDecimal("150.00"))
                .estatus(Estatus.DISPONIBLE)
                .build();
        when(paqueteService.findEntityById(7)).thenReturn(paquete);

        PaquetePedidoDTO dto = new PaquetePedidoDTO(7, new BigDecimal("140.00"), 2);
        Pedido pedido = Pedido.builder().build();
        catalogoPedidoService.agregarPaquetes(pedido, List.of(dto));

        assertEquals(1, pedido.getPaquetesPedido().size());
        assertEquals(7, pedido.getPaquetesPedido().get(0).getPaquete().getIdPaquete());
        assertEquals(0, new BigDecimal("140.00").compareTo(pedido.getPaquetesPedido().get(0).getPrecioUnitario()));
        assertEquals(2, pedido.getPaquetesPedido().get(0).getCantidad());
        // El helper addPaquetePedido debe haber seteado la relación bidireccional
        assertSame(pedido, pedido.getPaquetesPedido().get(0).getPedido());
        System.out.println("[OK] agregarPaquetes agregó 1 línea con id=7, cantidad=2, precio=140.00");
    }

    @Test
    @DisplayName("agregarPaquetes - lanza BusinessException cuando el paquete NO_DISPONIBLE")
    public void agregarPaquetes_paqueteNoDisponible_lanzaError() {
        Paquete paquete = Paquete.builder()
                .idPaquete(8)
                .descripcion("Promo Cerrada")
                .precio(new BigDecimal("90.00"))
                .estatus(Estatus.NO_DISPONIBLE)
                .build();
        when(paqueteService.findEntityById(8)).thenReturn(paquete);

        PaquetePedidoDTO dto = new PaquetePedidoDTO(8, new BigDecimal("90.00"), 1);
        Pedido pedido = Pedido.builder().build();
        BusinessException ex = assertThrows(BusinessException.class,
                () -> catalogoPedidoService.agregarPaquetes(pedido, List.of(dto)));
        assertTrue(ex.getMessage().contains("no está disponible"));
        assertTrue(pedido.getPaquetesPedido().isEmpty());
        System.out.println("[OK] agregarPaquetes lanzó error por paquete NO_DISPONIBLE");
    }

    @Test
    @DisplayName("agregarPaquetes - lanza BusinessException cuando el paquete AGOTADO")
    public void agregarPaquetes_paqueteAgotado_lanzaError() {
        Paquete paquete = Paquete.builder()
                .idPaquete(9)
                .descripcion("Promo Fin de Semana")
                .precio(new BigDecimal("80.00"))
                .estatus(Estatus.AGOTADO)
                .build();
        when(paqueteService.findEntityById(9)).thenReturn(paquete);

        PaquetePedidoDTO dto = new PaquetePedidoDTO(9, new BigDecimal("80.00"), 1);
        Pedido pedido = Pedido.builder().build();
        assertThrows(BusinessException.class,
                () -> catalogoPedidoService.agregarPaquetes(pedido, List.of(dto)));
        System.out.println("[OK] agregarPaquetes lanzó error por paquete AGOTADO");
    }

    @Test
    @DisplayName("agregarPaquetes - lista vacía no toca el pedido")
    public void agregarPaquetes_listaVacia_noHaceNada() {
        Pedido pedido = Pedido.builder().build();
        catalogoPedidoService.agregarPaquetes(pedido, List.of());
        assertTrue(pedido.getPaquetesPedido().isEmpty());
        verifyNoInteractions(paqueteService);
        System.out.println("[OK] agregarPaquetes con lista vacía no interactúa con PaqueteService");
    }
}
