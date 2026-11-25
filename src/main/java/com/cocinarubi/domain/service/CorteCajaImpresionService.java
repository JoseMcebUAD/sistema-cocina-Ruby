package com.cocinarubi.domain.service;

import com.cocinarubi.Constants;
import com.cocinarubi.DBConstants.MetodoPago;
import com.cocinarubi.DBConstants.TipoPedido;
import com.cocinarubi.dao.VistaResumenPedidoRepository;
import com.cocinarubi.domain.entity.VistaResumenPedido;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.CorteCajaImpresionRequestDTO;
import com.cocinarubi.presentation.dto.response.pedido.EscPosBytesDTO;
import com.cocinarubi.util.infra.UniversalPrintService;
import com.cocinarubi.util.FormatearReciboService;
import com.cocinarubi.util.template.data.CorteCajaTicketData;
import com.cocinarubi.util.template.data.CorteCajaTicketData.PedidoDesglose;
import com.cocinarubi.util.template.impl.ComedorCorteCajaTemplate;
import com.cocinarubi.util.template.impl.CorteCajaImpresionTemplate;
import com.cocinarubi.util.template.impl.DomicilioCorteCajaTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

/**
 * Orquesta el cálculo del corte de caja y la generación del ticket ESC/POS en Base64.
 * Capa: Service — único archivo que referencia {@link Constants} para los parámetros
 * de impresión (separador, ancho) y la zona horaria del negocio.
 */
@Service
public class CorteCajaImpresionService {

    private static final String[] NOMBRE_NEGOCIO = { "COCINA RUBI" };

    // FormatearReciboService es un POJO (sin @Component), se instancia con new
    private final FormatearReciboService formatearReciboService = new FormatearReciboService();

    private final VistaResumenPedidoRepository vistaRepository;
    private final UniversalPrintService universalPrintService;

    public CorteCajaImpresionService(VistaResumenPedidoRepository vistaRepository,
                                      UniversalPrintService universalPrintService) {
        this.vistaRepository = vistaRepository;
        this.universalPrintService = universalPrintService;
    }

    /**
     * Calcula el corte de caja del día indicado (o hoy si es null) y genera el ticket ESC/POS.
     *
     * @param request tipo de corte (DOMICILIO o COMEDOR) y fecha opcional
     * @return DTO con los bytes ESC/POS codificados en Base64
     */
    @Transactional(readOnly = true)
    public EscPosBytesDTO generarCorte(CorteCajaImpresionRequestDTO request) {
        LocalDate fecha = request.getFecha() != null
                ? request.getFecha()
                : LocalDate.now(Constants.ZONA_MERIDA);

        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin    = fecha.plusDays(1).atStartOfDay();

        CorteCajaImpresionTemplate template = switch (request.getTipo()) {
            case DOMICILIO -> buildDomicilio(fecha, inicio, fin);
            case COMEDOR   -> buildComedor(fecha, inicio, fin);
        };

        byte[] bytes = universalPrintService.generateEscPosBytes(template);
        if (bytes.length == 0) {
            throw new BusinessException("Error al generar el ticket ESC/POS del corte de caja",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new EscPosBytesDTO(Base64.getEncoder().encodeToString(bytes));
    }

    // ── Builders ──────────────────────────────────────────────────────────────

    private DomicilioCorteCajaTemplate buildDomicilio(LocalDate fecha,
                                                       LocalDateTime inicio,
                                                       LocalDateTime fin) {
        List<VistaResumenPedido> pedidos = vistaRepository.findAllForCorte(
                List.of(TipoPedido.DOMICILIO), inicio, fin);

        BigDecimal[] totales = calcularTotales(pedidos);
        List<PedidoDesglose> desglose = pedidos.stream()
                .map(this::toDesglose)
                .toList();

        CorteCajaTicketData data = CorteCajaTicketData.builder()
                .fecha(fecha)
                .tituloTipo("DOMICILIO")
                .total(totales[0])
                .totalEfectivo(totales[1])
                .totalTransferencia(totales[2])
                .totalTarjeta(totales[3])
                .pedidos(desglose)
                .build();

        return new DomicilioCorteCajaTemplate(data, NOMBRE_NEGOCIO,
                Constants.SEPARADOR_TICKET, Constants.ANCHO_TICKET, formatearReciboService);
    }

    private ComedorCorteCajaTemplate buildComedor(LocalDate fecha,
                                                   LocalDateTime inicio,
                                                   LocalDateTime fin) {
        List<VistaResumenPedido> pedidos = vistaRepository.findAllForCorte(
                List.of(TipoPedido.MOSTRADOR, TipoPedido.PICK_UP), inicio, fin);

        BigDecimal[] totales = calcularTotales(pedidos);

        CorteCajaTicketData data = CorteCajaTicketData.builder()
                .fecha(fecha)
                .tituloTipo("COMEDOR + PICK-UP")
                .total(totales[0])
                .totalEfectivo(totales[1])
                .totalTransferencia(totales[2])
                .totalTarjeta(totales[3])
                .pedidos(List.of())
                .build();

        return new ComedorCorteCajaTemplate(data, NOMBRE_NEGOCIO,
                Constants.SEPARADOR_TICKET, Constants.ANCHO_TICKET, formatearReciboService);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Calcula total y desglose por método de pago manejando pagos dobles (mixtos).
     *
     * Cuando hay pago doble:
     *   - método principal recibe {@code pagoClientePrincipal}
     *   - método secundario recibe {@code precioFinalOrden - pagoClientePrincipal}
     * Cuando el pago es simple, el precio completo va al método principal.
     *
     * @return arreglo [total, efectivo, transferencia, tarjeta]
     */
    BigDecimal[] calcularTotales(List<VistaResumenPedido> pedidos) {
        BigDecimal total         = BigDecimal.ZERO;
        BigDecimal efectivo      = BigDecimal.ZERO;
        BigDecimal transferencia = BigDecimal.ZERO;
        BigDecimal tarjeta       = BigDecimal.ZERO;

        for (VistaResumenPedido p : pedidos) {
            BigDecimal precio = p.getPrecioFinalOrden() != null
                    ? p.getPrecioFinalOrden() : BigDecimal.ZERO;
            total = total.add(precio);

            boolean esDoble = p.getMetodoPagoSecundario() != null;
            BigDecimal montoPpal = esDoble && p.getPagoClientePrincipal() != null
                    ? p.getPagoClientePrincipal()
                    : precio;
            BigDecimal montoSec = esDoble
                    ? precio.subtract(montoPpal)
                    : BigDecimal.ZERO;

            efectivo      = efectivo.add(montoParaMetodo(MetodoPago.EFECTIVO,      p.getMetodoPagoPrincipal(), montoPpal, p.getMetodoPagoSecundario(), montoSec));
            transferencia = transferencia.add(montoParaMetodo(MetodoPago.TRANSFERENCIA, p.getMetodoPagoPrincipal(), montoPpal, p.getMetodoPagoSecundario(), montoSec));
            tarjeta       = tarjeta.add(montoParaMetodo(MetodoPago.TARJETA,        p.getMetodoPagoPrincipal(), montoPpal, p.getMetodoPagoSecundario(), montoSec));
        }

        return new BigDecimal[]{ total, efectivo, transferencia, tarjeta };
    }

    private BigDecimal montoParaMetodo(MetodoPago metodoObjetivo,
                                        MetodoPago metodoPpal,
                                        BigDecimal montoPpal,
                                        MetodoPago metodoSec,
                                        BigDecimal montoSec) {
        BigDecimal resultado = BigDecimal.ZERO;
        if (metodoObjetivo == metodoPpal) resultado = resultado.add(montoPpal);
        if (metodoObjetivo == metodoSec)  resultado = resultado.add(montoSec);
        return resultado;
    }

    /** Convierte un registro de la vista en la fila de desglose para el ticket de domicilio. */
    PedidoDesglose toDesglose(VistaResumenPedido v) {
        boolean esDoble = v.getMetodoPagoSecundario() != null;
        BigDecimal precio = v.getPrecioFinalOrden() != null ? v.getPrecioFinalOrden() : BigDecimal.ZERO;
        BigDecimal montoPpal = esDoble && v.getPagoClientePrincipal() != null
                ? v.getPagoClientePrincipal()
                : precio;
        BigDecimal montoSec = esDoble ? precio.subtract(montoPpal) : null;

        return PedidoDesglose.builder()
                .nombreCliente(v.getNombreCliente() != null ? v.getNombreCliente() : "Sin nombre")
                .metodoPagoPrincipal(v.getMetodoPagoPrincipal() != null
                        ? v.getMetodoPagoPrincipal().name() : "")
                .montoPrincipal(montoPpal)
                .metodoPagoSecundario(esDoble ? v.getMetodoPagoSecundario().name() : null)
                .montoSecundario(montoSec)
                .build();
    }
}
