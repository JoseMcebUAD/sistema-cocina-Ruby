package com.cocinarubi.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.cocinarubi.presentation.dto.response.BasicoPedidoExtraResponseDTO;
import com.cocinarubi.presentation.dto.response.BasicoPedidoResponseDTO;
import com.cocinarubi.presentation.dto.response.ComidaPedidoResponseDTO;
import com.cocinarubi.presentation.dto.response.ComplementoPredeterminadoComidaResponseDTO;
import com.cocinarubi.presentation.dto.response.ComplementoResponseDTO;
import com.cocinarubi.presentation.dto.response.PaquetePedidoResponseDTO;

public class FormatearReciboPedidoService extends FormatearReciboService {

    public List<String> formatProductBlock(ComidaPedidoResponseDTO comida, String precio, int anchoEfectivo) {
        List<String> lineas = new ArrayList<>();
        String lineaComida = comida.getTamanoPorcion() + " " + comida.getNombreComida();

        List<ComplementoResponseDTO> complementos = comida.getComplementos();
        List<ComplementoPredeterminadoComidaResponseDTO> predeterminados = comida.getComplementosPredeterminados();

        boolean hayComplementos = complementos != null && !complementos.isEmpty();
        boolean hayPredeterminados = predeterminados != null && !predeterminados.isEmpty();

        lineas.addAll(formatearItemConPrecio(lineaComida, precio, anchoEfectivo));

        if (hayComplementos || hayPredeterminados) {
            lineas.add("");
        }

        if (hayComplementos) {
            for (ComplementoResponseDTO compl : complementos) {
                String nombreCompl = compl.getNombreComplemento();
                BigDecimal precioComp = compl.getPrecioExtra();
                if (precioComp != null && precioComp.compareTo(BigDecimal.ZERO) != 0) {
                    lineas.addAll(formatearItemConPrecio(nombreCompl, FORMATO_PRECIO.format(precioComp), anchoEfectivo));
                } else {
                    lineas.add(nombreCompl);
                }
            }
        }

        if (hayPredeterminados) {
            for (ComplementoPredeterminadoComidaResponseDTO pred : predeterminados) {
                String nombre = pred.getCantidad() > 1
                        ? pred.getCantidad() + "x " + pred.getNombreComplemento()
                        : pred.getNombreComplemento();
                lineas.add(nombre);
            }
        }

        return lineas;
    }

    public List<String> formatBasicoBlock(BasicoPedidoResponseDTO basico, String precio, int anchoEfectivo) {
        List<String> lineas = new ArrayList<>();

        // basicoDTO puede ser null si id_basico fue SET NULL (básico eliminado)
        if (basico.getBasico() == null) {
            lineas.add(construirLineaConPrecio("(básico eliminado)", precio, anchoEfectivo));
            return lineas;
        }

        List<ComplementoResponseDTO> complementos = basico.getBasico().getComplementos();
        List<BasicoPedidoExtraResponseDTO> extras = basico.getExtras();

        boolean hayComplementos = complementos != null && !complementos.isEmpty();
        boolean hayExtras = extras != null && !extras.isEmpty();

        lineas.addAll(formatearItemConPrecio(basico.getBasico().getNombreComida(), precio, anchoEfectivo));

        if (hayComplementos || hayExtras) {
            lineas.add("");
        }

        if (hayComplementos) {
            for (ComplementoResponseDTO compl : complementos) {
                lineas.add(compl.getNombreComplemento());
            }
        }

        if (hayExtras) {
            for (BasicoPedidoExtraResponseDTO extra : extras) {
                String descripcion = extra.getCantidad() + "x " + extra.getNombreComplemento();
                BigDecimal precioExtra = extra.getPrecio();
                if (precioExtra != null && precioExtra.compareTo(BigDecimal.ZERO) != 0) {
                    lineas.addAll(formatearItemConPrecio(descripcion, FORMATO_PRECIO.format(precioExtra), anchoEfectivo));
                } else {
                    lineas.add(descripcion);
                }
            }
        }

        return lineas;
    }

    /**
     * Formato del bloque Paquete para el ticket:
     * <pre>
     * PAQUETE nombrePaquete   $precio
     *   - producto1
     *   - producto2
     * </pre>
     */
    public List<String> formatPaqueteBlock(PaquetePedidoResponseDTO paquete, String precio, int anchoEfectivo) {
        List<String> lineas = new ArrayList<>();
        String encabezado = "PAQUETE " + (paquete.getDescripcion() != null ? paquete.getDescripcion() : "");
        List<String> nombres = paquete.getNombresProductos();
        boolean hayProductos = nombres != null && !nombres.isEmpty();

        lineas.addAll(formatearItemConPrecio(encabezado, precio, anchoEfectivo));

        if (hayProductos) {
            for (String nombre : nombres) {
                lineas.add("  - " + nombre);
            }
        }

        return lineas;
    }
}
