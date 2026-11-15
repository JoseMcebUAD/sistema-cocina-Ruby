package com.cocinarubi.domain.service;

import com.cocinarubi.DBConstants.MetodoPago;
import com.cocinarubi.DBConstants.TipoPedido;
import com.cocinarubi.dao.VistaResumenPedidoRepository;
import com.cocinarubi.domain.entity.VistaResumenPedido;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.CorteCajaImpresionRequestDTO;
import com.cocinarubi.presentation.dto.response.pedido.EscPosBytesDTO;
import com.cocinarubi.util.infra.UniversalPrintService;
import com.cocinarubi.util.template.data.CorteCajaTicketData.PedidoDesglose;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CorteCajaImpresionServiceTest {

    @Mock private VistaResumenPedidoRepository vistaRepository;
    @Mock private UniversalPrintService universalPrintService;

    @InjectMocks
    private CorteCajaImpresionService service;

    // ── calcularTotales ───────────────────────────────────────────────────────

    @Test
    @DisplayName("calcularTotales - pago simple EFECTIVO: todo el monto va a efectivo")
    void calcularTotales_pagoSimpleEfectivo() {
        VistaResumenPedido pedido = pedidoSimple(new BigDecimal("200.00"), MetodoPago.EFECTIVO);

        BigDecimal[] totales = service.calcularTotales(List.of(pedido));

        assertEquals(new BigDecimal("200.00"), totales[0], "total");
        assertEquals(new BigDecimal("200.00"), totales[1], "efectivo");
        assertEquals(BigDecimal.ZERO,          totales[2], "transferencia");
        assertEquals(BigDecimal.ZERO,          totales[3], "tarjeta");
        System.out.println("[OK] calcularTotales pago simple EFECTIVO $200 → efectivo=$200");
    }

    @Test
    @DisplayName("calcularTotales - pago mixto EFECTIVO+TRANSFERENCIA: se distribuye correctamente")
    void calcularTotales_pagoMixto() {
        // Precio final $200: cliente pagó $80 en EFECTIVO, resto ($120) en TRANSFERENCIA
        VistaResumenPedido pedido = pedidoMixto(
                new BigDecimal("200.00"),
                MetodoPago.EFECTIVO, new BigDecimal("80.00"),
                MetodoPago.TRANSFERENCIA);

        BigDecimal[] totales = service.calcularTotales(List.of(pedido));

        assertEquals(new BigDecimal("200.00"), totales[0], "total");
        assertEquals(new BigDecimal("80.00"),  totales[1], "efectivo");
        assertEquals(new BigDecimal("120.00"), totales[2], "transferencia");
        assertEquals(BigDecimal.ZERO,          totales[3], "tarjeta");
        System.out.println("[OK] calcularTotales pago mixto EFECTIVO $80 + TRANSFERENCIA $120 → total=$200");
    }

    @Test
    @DisplayName("calcularTotales - lista vacía: todos los totales son cero")
    void calcularTotales_sinVentas() {
        BigDecimal[] totales = service.calcularTotales(List.of());

        assertEquals(BigDecimal.ZERO, totales[0], "total");
        assertEquals(BigDecimal.ZERO, totales[1], "efectivo");
        assertEquals(BigDecimal.ZERO, totales[2], "transferencia");
        assertEquals(BigDecimal.ZERO, totales[3], "tarjeta");
        System.out.println("[OK] calcularTotales lista vacía → todos los totales en $0");
    }

    // ── toDesglose ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("toDesglose - pago doble: tienePagoDoble=true y montos correctos")
    void toDesglose_pagoMixto() {
        VistaResumenPedido pedido = pedidoMixto(
                new BigDecimal("200.00"),
                MetodoPago.TARJETA, new BigDecimal("120.00"),
                MetodoPago.EFECTIVO);
        pedido = withNombre(pedido, "oscar hernandez");

        PedidoDesglose desglose = service.toDesglose(pedido);

        assertTrue(desglose.tienePagoDoble());
        assertEquals("oscar hernandez",        desglose.getNombreCliente());
        assertEquals("TARJETA",                desglose.getMetodoPagoPrincipal());
        assertEquals(new BigDecimal("120.00"), desglose.getMontoPrincipal());
        assertEquals("EFECTIVO",               desglose.getMetodoPagoSecundario());
        assertEquals(new BigDecimal("80.00"),  desglose.getMontoSecundario());
        System.out.println("[OK] toDesglose pago mixto TARJETA+EFECTIVO correcto");
    }

    // ── generarCorte (flujo completo) ─────────────────────────────────────────

    @Test
    @DisplayName("generarCorte DOMICILIO - sin ventas: retorna Base64 válido (no null, no vacío)")
    void generarCorte_domicilio_sinVentas() {
        when(vistaRepository.findAllForCorte(any(), any(), any())).thenReturn(List.of());
        when(universalPrintService.generateEscPosBytes(any())).thenReturn(new byte[]{1, 2, 3});

        EscPosBytesDTO result = service.generarCorte(
                new CorteCajaImpresionRequestDTO(CorteCajaImpresionRequestDTO.TipoCorte.DOMICILIO,
                        LocalDate.of(2026, 11, 11)));

        assertNotNull(result.getEscposData());
        assertFalse(result.getEscposData().isEmpty());
        System.out.println("[OK] generarCorte DOMICILIO sin ventas → escposData no vacío");
    }

    @Test
    @DisplayName("generarCorte COMEDOR - sin ventas: retorna Base64 válido")
    void generarCorte_comedor_sinVentas() {
        when(vistaRepository.findAllForCorte(any(), any(), any())).thenReturn(List.of());
        when(universalPrintService.generateEscPosBytes(any())).thenReturn(new byte[]{1, 2, 3});

        EscPosBytesDTO result = service.generarCorte(
                new CorteCajaImpresionRequestDTO(CorteCajaImpresionRequestDTO.TipoCorte.COMEDOR,
                        LocalDate.of(2026, 11, 11)));

        assertNotNull(result.getEscposData());
        assertFalse(result.getEscposData().isEmpty());
        System.out.println("[OK] generarCorte COMEDOR sin ventas → escposData no vacío");
    }

    @Test
    @DisplayName("generarCorte - error ESC/POS: lanza BusinessException cuando generateEscPosBytes retorna vacío")
    void generarCorte_errorEscPos_lanzaExcepcion() {
        when(vistaRepository.findAllForCorte(any(), any(), any())).thenReturn(List.of());
        when(universalPrintService.generateEscPosBytes(any())).thenReturn(new byte[0]);

        assertThrows(BusinessException.class, () ->
                service.generarCorte(new CorteCajaImpresionRequestDTO(
                        CorteCajaImpresionRequestDTO.TipoCorte.DOMICILIO,
                        LocalDate.of(2026, 11, 11))));
        System.out.println("[OK] generarCorte lanzó BusinessException cuando ESC/POS retornó bytes vacíos");
    }

    // ── Helpers para construir entidades de prueba ────────────────────────────

    private VistaResumenPedido pedidoSimple(BigDecimal precio, MetodoPago metodoPpal) {
        VistaResumenPedido v = new VistaResumenPedido();
        setField(v, "precioFinalOrden",      precio);
        setField(v, "metodoPagoPrincipal",   metodoPpal);
        setField(v, "metodoPagoSecundario",  null);
        setField(v, "pagoClientePrincipal",  null);
        setField(v, "tipoPedido",            TipoPedido.DOMICILIO);
        setField(v, "nombreCliente",         "Cliente Test");
        return v;
    }

    private VistaResumenPedido pedidoMixto(BigDecimal precio,
                                            MetodoPago metodoPpal,
                                            BigDecimal pagoPpal,
                                            MetodoPago metodoSec) {
        VistaResumenPedido v = new VistaResumenPedido();
        setField(v, "precioFinalOrden",      precio);
        setField(v, "metodoPagoPrincipal",   metodoPpal);
        setField(v, "pagoClientePrincipal",  pagoPpal);
        setField(v, "metodoPagoSecundario",  metodoSec);
        setField(v, "tipoPedido",            TipoPedido.DOMICILIO);
        setField(v, "nombreCliente",         "Cliente Test");
        return v;
    }

    private VistaResumenPedido withNombre(VistaResumenPedido v, String nombre) {
        setField(v, "nombreCliente", nombre);
        return v;
    }

    /** Asigna un campo privado por reflexión — solo para construcción de fixtures de test. */
    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = VistaResumenPedido.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("No se pudo asignar el campo de test: " + fieldName, e);
        }
    }
}
