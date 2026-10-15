package com.cocinarubi.domain.service;

import com.cocinarubi.DBConstants.TipoEntidadOferta;
import com.cocinarubi.domain.entity.BasicoPedido;
import com.cocinarubi.domain.entity.ComboPedido;
import com.cocinarubi.domain.entity.ComidaPedido;
import com.cocinarubi.domain.entity.Oferta;
import com.cocinarubi.domain.entity.OfertaLinea;
import com.cocinarubi.domain.entity.ProductoCocinaPedido;
import com.cocinarubi.domain.model.OfertaAplicacion;
import com.cocinarubi.domain.model.OfertaAplicacion.LineaConsumo;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.LineaCocinaPedidoDTO;
import com.cocinarubi.presentation.dto.request.OfertaCocinaPedidoDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Algoritmo de matching de {@link Oferta}s sobre las líneas de un pedido en construcción.
 *
 * <p>Este servicio es <strong>stateless</strong>: recibe las entidades ya cargadas en memoria
 * (Phase 1 del guardado) y devuelve una lista de {@link OfertaAplicacion} sin tocar la BD.
 * No debe inyectar ningún repositorio; la carga de datos es responsabilidad del llamador.</p>
 *
 * <p>Algoritmo greedy: las ofertas se prueban en orden de {@code prioridad DESC}. Por cada
 * oferta, se intenta aplicarla el mayor número de veces posible antes de pasar a la siguiente.
 * Un producto consumido por una aplicación no puede ser consumido por otra.</p>
 *
 * <p>Capa: Service — lógica de negocio.</p>
 */
@Service
public class OfertaMatcherService {

    /**
     * Detecta automáticamente las ofertas aplicables (canal WEB).
     *
     * @param comidas    líneas de comida del pedido en construcción (sin IDs aún)
     * @param productos  líneas de ProductoCocina del pedido
     * @param basicos    líneas de Basico del pedido
     * @param combos     líneas de Combo del pedido
     * @param ofertasActivas ofertas activas ordenadas por prioridad DESC (ya cargadas con lineas)
     * @return lista de aplicaciones exitosas; vacía si ninguna oferta aplica
     */
    public List<OfertaAplicacion> detectar(
            List<ComidaPedido> comidas,
            List<ProductoCocinaPedido> productos,
            List<BasicoPedido> basicos,
            List<ComboPedido> combos,
            List<Oferta> ofertasActivas) {

        List<LineaConsumo> pool = buildPool(comidas, productos, basicos, combos);
        List<OfertaAplicacion> aplicaciones = new ArrayList<>();

        for (Oferta oferta : ofertasActivas) {
            boolean seguir = true;
            while (seguir) {
                OfertaAplicacion resultado = tryMatch(oferta, pool);
                if (resultado != null) {
                    aplicaciones.add(resultado);
                    // Remueve las líneas consumidas del pool para que no puedan usarse de nuevo
                    pool.removeAll(resultado.getLineas());
                } else {
                    seguir = false;
                }
            }
        }
        return aplicaciones;
    }

    /**
     * Valida y construye las aplicaciones para el canal COCINA (explícito).
     *
     * <p>Para cada {@link OfertaCocinaPedidoDTO}, busca las líneas indicadas en el pool,
     * verifica que satisfagan las reglas de la oferta y construye el {@link OfertaAplicacion}.</p>
     */
    public List<OfertaAplicacion> aplicarCocina(
            List<OfertaCocinaPedidoDTO> ofertasDtos,
            List<Oferta> ofertasById,
            List<ComidaPedido> comidas,
            List<ProductoCocinaPedido> productos,
            List<BasicoPedido> basicos,
            List<ComboPedido> combos) {

        List<LineaConsumo> pool = buildPool(comidas, productos, basicos, combos);
        List<OfertaAplicacion> aplicaciones = new ArrayList<>();

        for (int i = 0; i < ofertasDtos.size(); i++) {
            OfertaCocinaPedidoDTO dto = ofertasDtos.get(i);
            Oferta oferta = ofertasById.get(i);

            List<LineaConsumo> consumidas = new ArrayList<>();
            BigDecimal sumaNormal = BigDecimal.ZERO;

            for (LineaCocinaPedidoDTO lineaDto : dto.getLineas()) {
                LineaConsumo encontrada = buscarEnPool(lineaDto.getTipoEntidad(), lineaDto.getIdProducto(), pool, consumidas);
                if (encontrada == null) {
                    throw new BusinessException(
                            "La línea (" + lineaDto.getTipoEntidad() + ", idProducto=" + lineaDto.getIdProducto()
                                    + ") de la oferta #" + oferta.getIdOferta()
                                    + " no se encontró en el pedido o ya fue consumida por otra oferta",
                            HttpStatus.BAD_REQUEST);
                }
                consumidas.add(encontrada);
                sumaNormal = sumaNormal.add(encontrada.getPrecioNormal()
                        .multiply(BigDecimal.valueOf(encontrada.getCantidadConsumida())));
            }

            // Verifica que las líneas seleccionadas satisfagan las reglas de la oferta
            validarOfertaMatch(oferta, consumidas);

            BigDecimal ahorro = sumaNormal.subtract(oferta.getPrecioOferta());
            aplicaciones.add(new OfertaAplicacion(oferta, oferta.getPrecioOferta(), ahorro, consumidas));
            pool.removeAll(consumidas);
        }
        return aplicaciones;
    }

    /**
     * Verifica que el conjunto de {@link LineaConsumo}s satisfaga todas las reglas de {@link OfertaLinea}.
     * Reutilizado tanto en la validación COCINA como en pruebas unitarias.
     *
     * @throws BusinessException 400 si las líneas no satisfacen alguna regla de la oferta
     */
    public void validarOfertaMatch(Oferta oferta, List<LineaConsumo> consumidas) {
        List<LineaConsumo> disponibles = new ArrayList<>(consumidas);
        for (OfertaLinea regla : oferta.getLineas()) {
            List<LineaConsumo> matched = findMatches(regla, disponibles, regla.getCantidad());
            if (matched.size() < regla.getCantidad()) {
                String detalle = regla.getModoMatch().name()
                        + (regla.getTipoComidaFiltro() != null ? "(" + regla.getTipoComidaFiltro() + ")" : "")
                        + ", cantidad requerida: " + regla.getCantidad()
                        + ", satisfecha: " + matched.size();
                throw new BusinessException(
                        "La oferta '" + oferta.getNombre() + "' requiere " + detalle,
                        HttpStatus.BAD_REQUEST);
            }
            disponibles.removeAll(matched);
        }
    }

    // -------------------------------------------------------------------------
    // Métodos privados
    // -------------------------------------------------------------------------

    private OfertaAplicacion tryMatch(Oferta oferta, List<LineaConsumo> pool) {
        List<LineaConsumo> tentativo = new ArrayList<>();
        BigDecimal sumaNormal = BigDecimal.ZERO;

        for (OfertaLinea regla : oferta.getLineas()) {
            // Excluye del pool las líneas ya reservadas en este intento
            List<LineaConsumo> disponibles = pool.stream()
                    .filter(lc -> !tentativo.contains(lc))
                    .toList();
            List<LineaConsumo> matched = findMatches(regla, disponibles, regla.getCantidad());
            if (matched.size() < regla.getCantidad()) {
                return null; // slot insatisfecho → esta oferta no puede aplicarse
            }
            tentativo.addAll(matched);
            for (LineaConsumo lc : matched) {
                sumaNormal = sumaNormal.add(
                        lc.getPrecioNormal().multiply(BigDecimal.valueOf(lc.getCantidadConsumida())));
            }
        }

        BigDecimal ahorro = sumaNormal.subtract(oferta.getPrecioOferta());
        return new OfertaAplicacion(oferta, oferta.getPrecioOferta(), ahorro, tentativo);
    }

    /**
     * Encuentra hasta {@code cantidad} líneas del pool que satisfagan la regla dada.
     * El orden del pool determina qué líneas se consumen primero (FIFO).
     */
    private List<LineaConsumo> findMatches(OfertaLinea regla, List<LineaConsumo> disponibles, int cantidad) {
        List<LineaConsumo> resultado = new ArrayList<>();
        for (LineaConsumo candidato : disponibles) {
            if (resultado.size() >= cantidad) break;
            if (matches(regla, candidato)) {
                resultado.add(candidato);
            }
        }
        return resultado;
    }

    private LineaConsumo buscarEnPool(TipoEntidadOferta tipo, int idProducto,
                                      List<LineaConsumo> pool, List<LineaConsumo> yaConsumidas) {
        for (LineaConsumo lc : pool) {
            if (!yaConsumidas.contains(lc)
                    && lc.getTipoEntidad() == tipo
                    && lc.getIdProductoCatalogo() != null
                    && lc.getIdProductoCatalogo() == idProducto) {
                return lc;
            }
        }
        return null;
    }

    /**
     * Evalúa si un candidato del pool satisface una regla de oferta según su {@code modoMatch}.
     */
    boolean matches(OfertaLinea regla, LineaConsumo candidato) {
        return switch (regla.getModoMatch()) {
            case COMIDA_ESPECIFICA -> candidato.getTipoEntidad() == TipoEntidadOferta.COMIDA
                    && regla.getIdReferencia() != null
                    && regla.getIdReferencia().equals(candidato.getIdProductoCatalogo());

            case COMIDA_POR_TIPO -> {
                if (candidato.getTipoEntidad() != TipoEntidadOferta.COMIDA) yield false;
                if (!(candidato.getLineaEntity() instanceof ComidaPedido cp)) yield false;
                yield cp.getComida() != null
                        && cp.getComida().getTipoComida() == regla.getTipoComidaFiltro();
            }

            case PRODUCTO_ESPECIFICO -> candidato.getTipoEntidad() == TipoEntidadOferta.PRODUCTO_COCINA
                    && regla.getIdReferencia() != null
                    && regla.getIdReferencia().equals(candidato.getIdProductoCatalogo());

            case PRODUCTO_POR_CATEGORIA -> {
                if (candidato.getTipoEntidad() != TipoEntidadOferta.PRODUCTO_COCINA) yield false;
                if (!(candidato.getLineaEntity() instanceof ProductoCocinaPedido pcp)) yield false;
                yield pcp.getProductoCocina() != null
                        && pcp.getProductoCocina().getCategoria() != null
                        && regla.getIdReferencia() != null
                        && regla.getIdReferencia().equals(pcp.getProductoCocina().getCategoria().getIdCategoria());
            }

            case PRODUCTO_POR_SUBCATEGORIA -> {
                if (candidato.getTipoEntidad() != TipoEntidadOferta.PRODUCTO_COCINA) yield false;
                if (!(candidato.getLineaEntity() instanceof ProductoCocinaPedido pcp)) yield false;
                if (pcp.getProductoCocina() == null || regla.getIdReferencia() == null) yield false;
                yield pcp.getProductoCocina().getSubcategorias().stream()
                        .anyMatch(s -> regla.getIdReferencia().equals(s.getIdSubcategoria()));
            }

            case PRODUCTO_GENERAL -> candidato.getTipoEntidad() == TipoEntidadOferta.PRODUCTO_COCINA;

            case BASICO_ESPECIFICO -> candidato.getTipoEntidad() == TipoEntidadOferta.BASICO
                    && regla.getIdReferencia() != null
                    && regla.getIdReferencia().equals(candidato.getIdProductoCatalogo());

            case BASICO_GENERAL -> candidato.getTipoEntidad() == TipoEntidadOferta.BASICO;

            case COMBO_ESPECIFICO -> candidato.getTipoEntidad() == TipoEntidadOferta.COMBO
                    && regla.getIdReferencia() != null
                    && regla.getIdReferencia().equals(candidato.getIdProductoCatalogo());

            case COMBO_GENERAL -> candidato.getTipoEntidad() == TipoEntidadOferta.COMBO;
        };
    }

    /**
     * Construye el pool de candidatos a partir de las líneas en memoria del pedido.
     * Cada {@link ComidaPedido} genera una entrada; cada {@link ProductoCocinaPedido} con
     * cantidad > 1 genera UNA entrada con {@code cantidadConsumida = cantidad} para simplificar
     * el matching (una oferta consume la línea completa o no la consume).
     */
    private List<LineaConsumo> buildPool(List<ComidaPedido> comidas,
                                          List<ProductoCocinaPedido> productos,
                                          List<BasicoPedido> basicos,
                                          List<ComboPedido> combos) {
        List<LineaConsumo> pool = new ArrayList<>();

        for (ComidaPedido cp : comidas) {
            if (cp.getComida() == null) continue;
            pool.add(new LineaConsumo(
                    cp,
                    TipoEntidadOferta.COMIDA,
                    cp.getComida().getIdComida(),
                    cp.getComida().getNombreComida(),
                    cp.getPrecioUnitario() != null ? cp.getPrecioUnitario() : BigDecimal.ZERO,
                    1));
        }

        for (ProductoCocinaPedido pcp : productos) {
            if (pcp.getProductoCocina() == null) continue;
            // Cada línea de ProductoCocina se trata como un candidato individual.
            // cantidadConsumida refleja la cantidad total de la línea.
            pool.add(new LineaConsumo(
                    pcp,
                    TipoEntidadOferta.PRODUCTO_COCINA,
                    pcp.getProductoCocina().getIdProductoCocina(),
                    pcp.getProductoCocina().getNombreProducto(),
                    pcp.getPrecioUnitario() != null ? pcp.getPrecioUnitario() : BigDecimal.ZERO,
                    pcp.getCantidad()));
        }

        for (BasicoPedido bp : basicos) {
            if (bp.getBasico() == null) continue;
            pool.add(new LineaConsumo(
                    bp,
                    TipoEntidadOferta.BASICO,
                    bp.getBasico().getIdBasico(),
                    bp.getBasico().getDescripcion() != null ? bp.getBasico().getDescripcion() : "Básico #" + bp.getBasico().getIdBasico(),
                    bp.getPrecioUnitario() != null ? bp.getPrecioUnitario() : BigDecimal.ZERO,
                    1));
        }

        for (ComboPedido cp : combos) {
            if (cp.getCombo() == null) continue;
            pool.add(new LineaConsumo(
                    cp,
                    TipoEntidadOferta.COMBO,
                    cp.getCombo().getIdCombo(),
                    cp.getCombo().getDescripcion() != null ? cp.getCombo().getDescripcion() : "Combo #" + cp.getCombo().getIdCombo(),
                    cp.getPrecioUnitario() != null ? cp.getPrecioUnitario() : BigDecimal.ZERO,
                    cp.getCantidad() != null ? cp.getCantidad() : 1));
        }

        return pool;
    }
}
