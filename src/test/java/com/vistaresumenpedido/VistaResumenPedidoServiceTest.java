package com.vistaresumenpedido;

import com.cocinarubi.DBConstants.MetodoPago;
import com.cocinarubi.DBConstants.PedidoCreadoDesde;
import com.cocinarubi.DBConstants.TipoPedido;
import com.cocinarubi.dao.VistaResumenPedidoRepository;
import com.cocinarubi.dao.VistaResumenPedidoRepository.VistaResumenMetricasProjection;
import com.cocinarubi.domain.service.VistaResumenPedidoService;
import com.cocinarubi.presentation.dto.response.VistaResumenPedidoConMetricasResponseDTO;
import com.cocinarubi.presentation.dto.response.VistaResumenPedidoResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VistaResumenPedidoServiceTest {

    @Mock private VistaResumenPedidoRepository repository;
    @InjectMocks private VistaResumenPedidoService service;

    private final Pageable PAGEABLE = PageRequest.of(0, 10);
    private final LocalDateTime DESDE = LocalDateTime.of(2026, 1, 1, 0, 0);
    private final LocalDateTime HASTA = LocalDateTime.of(2026, 12, 31, 23, 59);

    private final VistaResumenPedidoResponseDTO FILA_WEB_DOMICILIO = VistaResumenPedidoResponseDTO.builder()
            .idPedido(1)
            .impreso(true)
            .nombreCliente("Juan Web")
            .metodoPagoPrincipal(MetodoPago.TARJETA)
            .tipoPedido(TipoPedido.DOMICILIO)
            .pedidoCreadoDesde(PedidoCreadoDesde.WEB)
            .fechaExpedicionPedido(LocalDateTime.of(2026, 6, 15, 12, 30))
            .precioFinalOrden(BigDecimal.valueOf(150))
            .pagoClientePrincipal(BigDecimal.valueOf(150))
            .ruta("Zona Centro")
            .domicilio("Calle Falsa 123")
            .precioTarifa(BigDecimal.valueOf(40))
            .build();

    private final VistaResumenPedidoResponseDTO FILA_COCINA_MOSTRADOR = VistaResumenPedidoResponseDTO.builder()
            .idPedido(2)
            .impreso(false)
            .nombreCliente("Ana Cocina")
            .metodoPagoPrincipal(MetodoPago.EFECTIVO)
            .tipoPedido(TipoPedido.MOSTRADOR)
            .pedidoCreadoDesde(PedidoCreadoDesde.COCINA)
            .fechaExpedicionPedido(LocalDateTime.of(2026, 6, 16, 9, 0))
            .precioFinalOrden(BigDecimal.valueOf(80))
            .pagoClientePrincipal(BigDecimal.valueOf(100))
            .build();

    @Test
    @DisplayName("findVista - Debe delegar filtros y pageable al repositorio y retornar la página")
    public void findVista_delegaAlRepositorio() {
        Page<VistaResumenPedidoResponseDTO> page = new PageImpl<>(List.of(FILA_WEB_DOMICILIO, FILA_COCINA_MOSTRADOR));
        when(repository.findVistaConFiltros(DESDE, HASTA, TipoPedido.DOMICILIO, PedidoCreadoDesde.WEB, PAGEABLE))
                .thenReturn(page);

        Page<VistaResumenPedidoResponseDTO> result = service.findVista(
                DESDE, HASTA, TipoPedido.DOMICILIO, PedidoCreadoDesde.WEB, PAGEABLE);

        assertEquals(2, result.getTotalElements());
        assertEquals("Juan Web", result.getContent().get(0).getNombreCliente());
        verify(repository).findVistaConFiltros(DESDE, HASTA, TipoPedido.DOMICILIO, PedidoCreadoDesde.WEB, PAGEABLE);
        System.out.println("[OK] findVista delegó filtros al repositorio: total=" + result.getTotalElements());
    }

    @Test
    @DisplayName("findVista - Sin filtros pasa nulls al repositorio")
    public void findVista_sinFiltros_pasaNullsAlRepositorio() {
        Page<VistaResumenPedidoResponseDTO> page = new PageImpl<>(List.of(FILA_WEB_DOMICILIO));
        when(repository.findVistaConFiltros(null, null, null, null, PAGEABLE)).thenReturn(page);

        Page<VistaResumenPedidoResponseDTO> result = service.findVista(null, null, null, null, PAGEABLE);

        assertEquals(1, result.getTotalElements());
        verify(repository).findVistaConFiltros(null, null, null, null, PAGEABLE);
        System.out.println("[OK] findVista con filtros nulos delegó correctamente");
    }

    @Test
    @DisplayName("findVistaConMetricas - Debe componer el DTO con la página y las métricas")
    public void findVistaConMetricas_componeDto() {
        Page<VistaResumenPedidoResponseDTO> page = new PageImpl<>(
                List.of(FILA_WEB_DOMICILIO, FILA_COCINA_MOSTRADOR),
                PAGEABLE,
                2);
        VistaResumenMetricasProjection proj = proj(1L, 1L,
                BigDecimal.valueOf(230), BigDecimal.valueOf(80),
                BigDecimal.ZERO, BigDecimal.valueOf(150));

        when(repository.findVistaConFiltros(null, null, null, null, PAGEABLE)).thenReturn(page);
        when(repository.findMetricasConFiltros(null, null, null, null)).thenReturn(proj);

        VistaResumenPedidoConMetricasResponseDTO result = service.findVistaConMetricas(
                null, null, null, null, PAGEABLE);

        assertNotNull(result);
        assertEquals(2, result.getPedidos().getTotalElements());
        assertEquals(2, result.getCantidadTotal());
        assertEquals(1, result.getCantidadImpresos());
        assertEquals(1, result.getCantidadNoImpresos());
        assertEquals(0, BigDecimal.valueOf(230).compareTo(result.getIngresoTotal()));
        assertEquals(0, BigDecimal.valueOf(80).compareTo(result.getIngresoEfectivo()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getIngresoTransferencia()));
        assertEquals(0, BigDecimal.valueOf(150).compareTo(result.getIngresoTarjeta()));

        assertEquals(result.getCantidadImpresos() + result.getCantidadNoImpresos(), result.getCantidadTotal());
        BigDecimal sumaMetodos = result.getIngresoEfectivo()
                .add(result.getIngresoTransferencia())
                .add(result.getIngresoTarjeta());
        assertEquals(0, sumaMetodos.compareTo(result.getIngresoTotal()));
        System.out.println("[OK] findVistaConMetricas ingresoTotal=" + result.getIngresoTotal()
                + " (efectivo+transferencia+tarjeta=" + sumaMetodos + ")");
    }

    @Test
    @DisplayName("findVistaConMetricas - Debe soportar proyección con nulls (sin resultados)")
    public void findVistaConMetricas_nullSafe() {
        Page<VistaResumenPedidoResponseDTO> page = new PageImpl<>(List.of(), PAGEABLE, 0);
        VistaResumenMetricasProjection proj = proj(null, null, null, null, null, null);

        when(repository.findVistaConFiltros(DESDE, HASTA, TipoPedido.PICK_UP, PedidoCreadoDesde.COCINA, PAGEABLE))
                .thenReturn(page);
        when(repository.findMetricasConFiltros(DESDE, HASTA, TipoPedido.PICK_UP, PedidoCreadoDesde.COCINA))
                .thenReturn(proj);

        VistaResumenPedidoConMetricasResponseDTO result = service.findVistaConMetricas(
                DESDE, HASTA, TipoPedido.PICK_UP, PedidoCreadoDesde.COCINA, PAGEABLE);

        assertEquals(0, result.getCantidadTotal());
        assertEquals(0, result.getCantidadImpresos());
        assertEquals(0, result.getCantidadNoImpresos());
        assertEquals(BigDecimal.ZERO, result.getIngresoTotal());
        assertEquals(BigDecimal.ZERO, result.getIngresoEfectivo());
        assertEquals(BigDecimal.ZERO, result.getIngresoTransferencia());
        assertEquals(BigDecimal.ZERO, result.getIngresoTarjeta());
        System.out.println("[OK] findVistaConMetricas null-safe: todos los agregados en cero");
    }

    private VistaResumenMetricasProjection proj(Long impresos, Long noImpresos,
                                                BigDecimal total, BigDecimal efectivo,
                                                BigDecimal transferencia, BigDecimal tarjeta) {
        return new VistaResumenMetricasProjection() {
            @Override public Long getCantidadImpresos() { return impresos; }
            @Override public Long getCantidadNoImpresos() { return noImpresos; }
            @Override public BigDecimal getIngresoTotal() { return total; }
            @Override public BigDecimal getIngresoEfectivo() { return efectivo; }
            @Override public BigDecimal getIngresoTransferencia() { return transferencia; }
            @Override public BigDecimal getIngresoTarjeta() { return tarjeta; }
        };
    }
}
