package com.cocinarubi.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
        lineas.add(construirLineaConPrecio(basico.getBasico().getNombreComida(), precio, anchoEfectivo));
        return lineas;
    }
}
