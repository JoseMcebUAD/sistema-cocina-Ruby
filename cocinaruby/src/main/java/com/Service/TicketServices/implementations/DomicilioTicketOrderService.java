package com.Service.TicketServices.implementations;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.DAO.Daos.ClienteDAO;
import com.DAO.Daos.Orden.OrdenDomicilioDAO;
import com.Model.ModeloCliente;
import com.Model.DTO.ModeloRecibo;
import com.Model.Orden.ModeloOrdenDomicilio;
import com.Service.TicketServices.factories.AbstractTicketTemplateService;
import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.escpos.Style;

public final class DomicilioTicketOrderService extends AbstractTicketTemplateService {

    private static final DecimalFormat FORMATO_PRECIO = new DecimalFormat("$#,##0.00");
    private static final Logger LOGGER = Logger.getLogger(DomicilioTicketOrderService.class.getName());

    public DomicilioTicketOrderService(ModeloRecibo recibo) {
        super(recibo);
    }

    @Override
    protected void printTypeInfo(EscPos escpos, Style subtitle) throws IOException {
        escpos.write("Tipo: ").writeLF(subtitle, "DOMICILIO");

        ModeloOrdenDomicilio ordenDom = resolveOrderWithFallback();
        if (ordenDom != null) {
            if (hasText(ordenDom.getNombreCliente())) {
                escpos.write("Nombre: ").writeLF(ordenDom.getNombreCliente());
            }
            if (hasText(ordenDom.getDireccionCliente())) {
                escpos.write("Direccion: ").writeLF(ordenDom.getDireccionCliente());
            }
            if (hasText(ordenDom.getTelefonoCliente())) {
                escpos.write("Telefono: ").writeLF(ordenDom.getTelefonoCliente());
            }
            if (ordenDom.getTarifaDomicilio() > 0) {
                escpos.write("Tarifa: ").writeLF(FORMATO_PRECIO.format(ordenDom.getTarifaDomicilio()));
            }
        }
        escpos.feed(1);
    }

    private ModeloOrdenDomicilio resolveOrderWithFallback() {
        ModeloRecibo factura = getRecibo();
        if (factura == null || factura.getOrdenCompleta() == null || !(factura.getOrdenCompleta().getOrden() instanceof ModeloOrdenDomicilio)) {
            return null;
        }

        ModeloOrdenDomicilio ordenDom = (ModeloOrdenDomicilio) factura.getOrdenCompleta().getOrden();
        if (needsDbFallback(ordenDom)) {
            hydrateFromDatabase(ordenDom);
        }
        return ordenDom;
    }

    private boolean needsDbFallback(ModeloOrdenDomicilio ordenDom) {
        return !hasText(ordenDom.getNombreCliente())
                || !hasText(ordenDom.getDireccionCliente())
                || !hasText(ordenDom.getTelefonoCliente())
                || ordenDom.getTarifaDomicilio() <= 0;
    }

    private void hydrateFromDatabase(ModeloOrdenDomicilio target) {
        if (target.getIdOrden() <= 0) {
            return;
        }

        try {
            OrdenDomicilioDAO ordenDomicilioDAO = new OrdenDomicilioDAO();
            ModeloOrdenDomicilio dbOrder = ordenDomicilioDAO.read(target.getIdOrden());

            if (dbOrder != null) {
                if (!hasText(target.getDireccionCliente()) && hasText(dbOrder.getDireccionCliente())) {
                    target.setDireccionCliente(dbOrder.getDireccionCliente());
                }
                if (target.getTarifaDomicilio() <= 0 && dbOrder.getTarifaDomicilio() > 0) {
                    target.setTarifaDomicilio(dbOrder.getTarifaDomicilio());
                }
                if (target.getIdRelCliente() == null && dbOrder.getIdRelCliente() != null) {
                    target.setIdRelCliente(dbOrder.getIdRelCliente());
                }
            }

            if (target.getIdRelCliente() != null
                    && (!hasText(target.getNombreCliente())
                    || !hasText(target.getTelefonoCliente())
                    || target.getTarifaDomicilio() <= 0)) {
                ClienteDAO clienteDAO = new ClienteDAO();
                ModeloCliente cliente = clienteDAO.read(target.getIdRelCliente());
                if (cliente != null) {
                    if (!hasText(target.getNombreCliente()) && hasText(cliente.getNombreCliente())) {
                        target.setNombreCliente(cliente.getNombreCliente());
                    }
                    if (!hasText(target.getTelefonoCliente()) && hasText(cliente.getTelefono())) {
                        target.setTelefonoCliente(cliente.getTelefono());
                    }
                    if (!hasText(target.getDireccionCliente()) && hasText(cliente.getDirecciones())) {
                        target.setDireccionCliente(cliente.getDirecciones());
                    }
                    if (target.getTarifaDomicilio() <= 0 && cliente.getTarifaDomicilio() > 0) {
                        target.setTarifaDomicilio(cliente.getTarifaDomicilio());
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "No se pudo completar datos de domicilio para impresión. Orden id: " + target.getIdOrden(), ex);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
