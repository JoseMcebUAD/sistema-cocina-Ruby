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

        if (!hayComplementos && !hayPredeterminados) {
            lineas.add(construirLineaConPrecio(lineaComida, precio, anchoEfectivo));
            return lineas;
        }

        lineas.add(lineaComida);
        lineas.add("");

        if (hayComplementos) {
            for (ComplementoResponseDTO compl : complementos) {
                String nombreCompl = compl.getNombreComplemento();
                BigDecimal precioComp = compl.getPrecioExtra();
                if (precioComp != null && precioComp.compareTo(BigDecimal.ZERO) != 0) {
                    lineas.add(construirLineaConPrecio(nombreCompl, FORMATO_PRECIO.format(precioComp), anchoEfectivo));
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

        lineas.add(alinearDerechaCompleto(precio, anchoEfectivo));

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

    /**
     * Formato del bloque Paquete para el ticket:
     * <pre>
     * PAQUETE nombrePaquete
     *   - producto1
     *   - producto2
     *                                 $precio
     * </pre>
     * Si no hay productos (paquete recién creado sin líneas), se imprime en una sola línea
     * con el precio a la derecha, igual que un ComidaPedido sin complementos.
     */
    public List<String> formatPaqueteBlock(PaquetePedidoResponseDTO paquete, String precio, int anchoEfectivo) {
        List<String> lineas = new ArrayList<>();
        String encabezado = "PAQUETE " + (paquete.getDescripcion() != null ? paquete.getDescripcion() : "");
        List<String> nombres = paquete.getNombresProductos();
        boolean hayProductos = nombres != null && !nombres.isEmpty();

        if (!hayProductos) {
            lineas.add(construirLineaConPrecio(encabezado, precio, anchoEfectivo));
            return lineas;
        }

        lineas.add(encabezado);
        for (String nombre : nombres) {
            lineas.add("  - " + nombre);
        }
        lineas.add(alinearDerechaCompleto(precio, anchoEfectivo));
        return lineas;
    }
}
