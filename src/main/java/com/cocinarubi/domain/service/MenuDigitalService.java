package com.cocinarubi.domain.service;

import com.cocinarubi.DBConstants.Estatus;
import com.cocinarubi.DBConstants.TipoProducto;
import com.cocinarubi.dao.BasicoRepository;
import com.cocinarubi.dao.ComidaRepository;
import com.cocinarubi.dao.ComplementoRepository;
import com.cocinarubi.dao.ProductoCocinaRepository;
import com.cocinarubi.domain.entity.Basico;
import com.cocinarubi.domain.entity.BasicoComplemento;
import com.cocinarubi.domain.entity.Comida;
import com.cocinarubi.domain.entity.Complemento;
import com.cocinarubi.domain.entity.ProductoCocina;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Genera el texto del menú diario de Cocina Rubi listo para copiar y enviar por WhatsApp.
 * Capa: Service — lógica de composición del menú a partir de ítems con estatus DISPONIBLE.
 */
@Service
@Transactional(readOnly = true)
public class MenuDigitalService {

    private static final String HEADER =
            "☀️ Buen día ☀️\n" +
            "👩🏻‍🍳👩🏻‍🍳👩🏻‍🍳\n" +
            "Cocina Rubi\n" +
            "Un gusto del paladar al corazón \n" +
            "❤❤❤❤❤\n" +
            "Estimado cliente \n" +
            "🔺 le pedimos de antemano anticipar su pedido, de lo contrario, sus encargos están sujetos a disponibilidad de ruta. \n" +
            "\n" +
            "🚫🚫🚫🚫🚫🚫\n" +
            "Ojo importante para los que trasfieren ⭕⭕⭕\n" +
            "estimado cliente comidas trasferidas seran entregadas al realizar su pedido mandar comprobante para poder realizar la nota para que salga su pedido a tiempo \n" +
            "\n" +
            "☀️☀️☀️☀️☀️☀️\n" +
            "MENÚ DEL DIA \n" +
            "Cocina Rubi \n" +
            "🍝🥗🥘🍲🍛🍽️\n" +
            "❤️❤️‼️🥰🥰\n" +
            "\n";

    private static final String OTROS =
            "\n" +
            "*Otros:*\n" +
            "*$32kilo de tortilla*\n" +
            "*$16 ½ kilo de tortilla*\n" +
            "$10 pesos ¼ kilo de tortilla\n" +
            "$6 pieza de limón \n" +
            "$4 pieza de chile habanero \n" +
            "$5 Paquete de cubiertos\n";

    private static final String FOOTER = "\n🔺precios solo para envíos";

    private final ComidaRepository comidaRepository;
    private final BasicoRepository basicoRepository;
    private final ComplementoRepository complementoRepository;
    private final ProductoCocinaRepository productoCocinaRepository;

    public MenuDigitalService(ComidaRepository comidaRepository,
                              BasicoRepository basicoRepository,
                              ComplementoRepository complementoRepository,
                              ProductoCocinaRepository productoCocinaRepository) {
        this.comidaRepository = comidaRepository;
        this.basicoRepository = basicoRepository;
        this.complementoRepository = complementoRepository;
        this.productoCocinaRepository = productoCocinaRepository;
    }

    /**
     * Construye el texto completo del menú para WhatsApp.
     * Carga todos los datos en una sola transacción y compone el texto sección por sección.
     */
    public String generarMenu() {
        List<Comida> comidas = comidaRepository.findDisponiblesOrdenados(Estatus.DISPONIBLE);
        // BasicoRepository.findDisponiblesOrdenados ya hace JOIN FETCH de bc.complemento
        List<Basico> basicos = basicoRepository.findDisponiblesOrdenados(Estatus.DISPONIBLE);
        List<Complemento> complementos = complementoRepository.findDisponiblesOrdenados(Estatus.DISPONIBLE);
        List<ProductoCocina> productos = productoCocinaRepository.findDisponiblesOrdenados(Estatus.DISPONIBLE);

        Map<Integer, List<Basico>> basicosPorComida = basicos.stream()
                .collect(Collectors.groupingBy(b -> b.getComida().getIdComida()));

        List<ProductoCocina> bebidas = filtrarPorTipo(productos, TipoProducto.BEBIDA);
        List<ProductoCocina> snacks = filtrarPorTipo(productos, TipoProducto.SNACK);
        List<ProductoCocina> charolas = filtrarPorTipo(productos, TipoProducto.CHAROLA);
        List<ProductoCocina> postres = filtrarPorTipo(productos, TipoProducto.POSTRE);

        StringBuilder sb = new StringBuilder(HEADER);

        appendComidas(sb, comidas, basicosPorComida);
        appendBebidas(sb, bebidas);
        appendSnacks(sb, snacks);
        appendComplementos(sb, complementos);

        sb.append(OTROS);

        appendCharolas(sb, charolas);
        appendPostres(sb, postres);

        sb.append(FOOTER);

        return sb.toString();
    }

    private void appendComidas(StringBuilder sb,
                               List<Comida> comidas,
                               Map<Integer, List<Basico>> basicosPorComida) {
        for (Comida comida : comidas) {
            List<Basico> basicosComida = basicosPorComida.getOrDefault(comida.getIdComida(), List.of());
            if (!basicosComida.isEmpty()) {
                appendComidaConBasico(sb, comida, basicosComida);
            } else {
                appendComidaSinBasico(sb, comida);
            }
        }
    }

    private void appendComidaConBasico(StringBuilder sb, Comida comida, List<Basico> basicos) {
        sb.append("◼️").append(comida.getNombreComida()).append("\n");

        for (Basico basico : basicos) {
            sb.append("🔳Basico $").append(formatPrecio(basico.getPrecioBasico())).append(" pesos\n");

            List<String> nombresComp = basico.getComplementos().stream()
                    .map(bc -> bc.getComplemento().getNombreComplemento())
                    .collect(Collectors.toList());

            sb.append("(");
            if (!nombresComp.isEmpty()) {
                sb.append("Complemento ").append(String.join(" y ", nombresComp));
            }
            if (basico.getDescripcion() != null && !basico.getDescripcion().isBlank()) {
                if (!nombresComp.isEmpty()) sb.append("\n");
                sb.append(basico.getDescripcion());
            }
            sb.append(")\n");
        }

        sb.append("🔳Media $").append(formatPrecio(comida.getPrecioMedia())).append("\n");
        if (comida.getLimiteComplemento() != null) {
            sb.append(comida.getLimiteComplemento()).append(" complementos a elegir\n");
        }
        sb.append("🔳Entera $").append(formatPrecio(comida.getPrecioEntera())).append("\n");
        if (comida.getLimiteComplemento() != null) {
            sb.append(comida.getLimiteComplemento()).append(" complementos a elegir\n");
        }
        sb.append("\n");
    }

    private void appendComidaSinBasico(StringBuilder sb, Comida comida) {
        sb.append("◼️").append(comida.getNombreComida()).append("\n");
        if (comida.getDescripcion() != null && !comida.getDescripcion().isBlank()) {
            sb.append(comida.getDescripcion()).append("\n");
        }
        if (comida.getLimiteComplemento() != null) {
            sb.append(comida.getLimiteComplemento()).append(" complementos a elegir\n");
        }
        sb.append("Media $").append(formatPrecio(comida.getPrecioMedia())).append("\n");
        sb.append("Entera $").append(formatPrecio(comida.getPrecioEntera())).append("\n");
        sb.append("\n");
    }

    private void appendBebidas(StringBuilder sb, List<ProductoCocina> bebidas) {
        if (bebidas.isEmpty()) return;
        sb.append("Bebidas\n");
        for (ProductoCocina b : bebidas) {
            sb.append(b.getNombreProducto()).append(" $").append(formatPrecio(getPrecioEfectivo(b))).append("\n");
        }
        sb.append("\n");
    }

    private void appendSnacks(StringBuilder sb, List<ProductoCocina> snacks) {
        if (snacks.isEmpty()) return;

        // Agrupar por precio efectivo usando TreeMap para orden ascendente de precio
        Map<BigDecimal, List<ProductoCocina>> porPrecio = new TreeMap<>(
                snacks.stream().collect(Collectors.groupingBy(this::getPrecioEfectivo))
        );

        for (Map.Entry<BigDecimal, List<ProductoCocina>> entry : porPrecio.entrySet()) {
            sb.append("*SNACK $").append(formatPrecio(entry.getKey())).append("*\n");
            String nombres = entry.getValue().stream()
                    .map(ProductoCocina::getNombreProducto)
                    .collect(Collectors.joining(", "));
            sb.append(nombres).append("\n");
        }
        sb.append("\n");
    }

    private void appendComplementos(StringBuilder sb, List<Complemento> complementos) {
        if (complementos.isEmpty()) return;
        sb.append("*Complementos a elegir*\n");
        sb.append("🥑🍅🥒🥝🥬\n\n");
        for (Complemento c : complementos) {
            sb.append("▪️").append(c.getNombreComplemento()).append("\n");
            if (c.isCobrarSiempre()) {
                sb.append("(").append(formatPrecio(c.getPrecioExtra())).append(" pesos adicionales)\n");
            }
        }
        sb.append("\n");
    }

    private void appendCharolas(StringBuilder sb, List<ProductoCocina> charolas) {
        if (charolas.isEmpty()) return;
        sb.append("*Complementos por charola o bolsita*\n");
        for (ProductoCocina c : charolas) {
            sb.append("▪️").append(c.getNombreProducto()).append(" $").append(formatPrecio(getPrecioEfectivo(c))).append("\n");
        }
    }

    private void appendPostres(StringBuilder sb, List<ProductoCocina> postres) {
        if (postres.isEmpty()) return;
        sb.append("\nPostres\n");
        for (ProductoCocina p : postres) {
            sb.append("◼️").append(p.getNombreProducto()).append(" $").append(formatPrecio(getPrecioEfectivo(p))).append("\n");
        }
    }

    private List<ProductoCocina> filtrarPorTipo(List<ProductoCocina> productos, TipoProducto tipo) {
        return productos.stream()
                .filter(p -> p.getTipoProducto() == tipo)
                .collect(Collectors.toList());
    }

    private BigDecimal getPrecioEfectivo(ProductoCocina producto) {
        BigDecimal domicilio = producto.getPrecioDomicilio();
        if (domicilio == null || domicilio.compareTo(BigDecimal.ZERO) == 0) {
            return producto.getPrecioNormal();
        }
        return domicilio;
    }

    private String formatPrecio(BigDecimal precio) {
        if (precio == null) return "0";
        if (precio.stripTrailingZeros().scale() <= 0) {
            return String.valueOf(precio.intValue());
        }
        return precio.stripTrailingZeros().toPlainString();
    }
}
