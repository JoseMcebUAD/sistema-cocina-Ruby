package com.cocinarubi.util.template.data;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Value object con los datos calculados para el ticket de corte de caja.
 * Capa: util/template — modelo de datos inmutable pasado a los templates ESC/POS.
 */
@Getter
@Builder
public class CorteCajaTicketData {

    private final LocalDate fecha;

    /** Texto centrado bajo el nombre del negocio (ej. "DOMICILIO" o "COMEDOR + PICK-UP"). */
    private final String tituloTipo;

    /** Suma bruta de todos los pedidos incluidos en este corte. */
    private final BigDecimal total;

    private final BigDecimal totalEfectivo;
    private final BigDecimal totalTransferencia;
    private final BigDecimal totalTarjeta;

    /**
     * Desglose por cliente. Para tipo COMEDOR esta lista es vacía;
     * para DOMICILIO contiene una entrada por pedido.
     */
    private final List<PedidoDesglose> pedidos;

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Fila del desglose de domicilio: un pedido con su cliente y pagos.
     * Soporta pago simple y pago doble (dos métodos en el mismo pedido).
     */
    @Getter
    @Builder
    public static class PedidoDesglose {

        private final String nombreCliente;

        /** Nombre del método de pago principal (ej. "EFECTIVO"). */
        private final String metodoPagoPrincipal;
        private final BigDecimal montoPrincipal;

        /** Null cuando el pedido no usa pago doble. */
        private final String metodoPagoSecundario;
        private final BigDecimal montoSecundario;

        /** true cuando el pedido tiene dos métodos de pago. */
        public boolean tienePagoDoble() {
            return metodoPagoSecundario != null;
        }
    }
}
