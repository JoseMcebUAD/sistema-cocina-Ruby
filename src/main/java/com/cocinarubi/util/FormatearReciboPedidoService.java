package com.cocinarubi.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.cocinarubi.presentation.dto.response.BasicoPedidoExtraResponseDTO;
import com.cocinarubi.presentation.dto.response.BasicoPedidoResponseDTO;
import com.cocinarubi.presentation.dto.response.ComidaPedidoResponseDTO;
import com.cocinarubi.presentation.dto.response.ComplementoResponseDTO;

public class FormatearReciboPedidoService extends FormatearReciboService {

    public List<String> formatProductBlock(ComidaPedidoResponseDTO comida, String precio, int anchoEfectivo) {
        List<String> lineas = new ArrayList<>();
        String lineaComida = comida.getTamanoPorcion() + " " + comida.getNombreComida();

        List<ComplementoResponseDTO> complementos = comida.getComplementos();
        boolean hayComplementos = complementos != null && !complementos.isEmpty();

        if (!hayComplementos) {
            lineas.add(construirLineaConPrecio(lineaComida, precio, anchoEfectivo));
            return lineas;
        }

        lineas.add(lineaComida);
        lineas.add("");

        for (ComplementoResponseDTO compl : complementos) {
            String nombreCompl = compl.getNombreComplemento();
            BigDecimal precioComp = compl.getPrecioExtra();
            if (precioComp != null && precioComp.compareTo(BigDecimal.ZERO) != 0) {
                lineas.add(construirLineaConPrecio(nombreCompl, FORMATO_PRECIO.format(precioComp), anchoEfectivo));
            } else {
                lineas.add(nombreCompl);
            }
        }

        lineas.add(alinearDerechaCompleto(precio, anchoEfectivo));

        return lineas;
    }

    public List<String> formatBasicoBlock(BasicoPedidoResponseDTO basico, String precio, int anchoEfectivo) {
        List<String> lineas = new ArrayList<>();

        List<ComplementoResponseDTO> complementos = basico.getBasico().getComplementos();
        List<BasicoPedidoExtraResponseDTO> extras = basico.getExtras();

        boolean hayComplementos = complementos != null && !complementos.isEmpty();
        boolean hayExtras = extras != null && !extras.isEmpty();

        if (!hayComplementos && !hayExtras) {
            lineas.add(construirLineaConPrecio(basico.getBasico().getNombreComida(), precio, anchoEfectivo));
            return lineas;
        }

        lineas.add(basico.getBasico().getNombreComida());
        lineas.add("");

        if (hayComplementos) {
            for (ComplementoResponseDTO compl : complementos) {
                // Complementos del paquete sin precio: ya incluidos en el total
                lineas.add(compl.getNombreComplemento());
            }
        }

        if (hayExtras) {
            for (BasicoPedidoExtraResponseDTO extra : extras) {
                String descripcion = extra.getCantidad() + "x " + extra.getNombreComplemento();
                BigDecimal precioExtra = extra.getPrecio();
                if (precioExtra != null && precioExtra.compareTo(BigDecimal.ZERO) != 0) {
                    lineas.add(construirLineaConPrecio(descripcion, FORMATO_PRECIO.format(precioExtra), anchoEfectivo));
                } else {
                    lineas.add(descripcion);
                }
            }
        }

        lineas.add(alinearDerechaCompleto(precio, anchoEfectivo));
        return lineas;
    }
}
