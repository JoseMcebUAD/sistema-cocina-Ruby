package com.cocinarubi.domain.service;

import com.cocinarubi.Constants;
import com.cocinarubi.DBConstants.PedidoCreadoDesde;
import com.cocinarubi.DBConstants.TipoPedido;
import com.cocinarubi.DBConstants.TipoProducto;
import com.cocinarubi.dao.ResumenProduccionRepository;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.response.DetalleProduccionResponseDTO;
import com.cocinarubi.presentation.dto.response.ItemProduccionDTO;
import com.cocinarubi.presentation.dto.response.ResumenProduccionResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lógica de negocio para el resumen de producción diaria.
 * Agrega los pedidos del día según filtros opcionales (tipoPedido, pedidoCreadoDesde, rutas)
 * y devuelve conteos por categoría o el desglose de una categoría específica.
 * Capa: Service.
 */
@Service
public class ResumenProduccionService {

    private final ResumenProduccionRepository resumenProduccionRepository;

    public ResumenProduccionService(ResumenProduccionRepository resumenProduccionRepository) {
        this.resumenProduccionRepository = resumenProduccionRepository;
    }

    /**
     * Devuelve los conteos totales de cada categoría de alimento para el día de hoy.
     * Aplica los filtros recibidos; los nulos se ignoran (sin filtro para ese campo).
     */
    @Transactional(readOnly = true)
    public ResumenProduccionResponseDTO resumenProduccion(
            TipoPedido tipoPedido,
            PedidoCreadoDesde pedidoCreadoDesde,
            List<Integer> idRutas) {

        LocalDate hoy = LocalDate.now(Constants.ZONA_MERIDA);
        LocalDateTime inicio = hoy.atStartOfDay();
        LocalDateTime fin = hoy.plusDays(1).atStartOfDay();

        boolean filtrarRuta = esFiltrarRuta(idRutas);
        List<Integer> idRutasSafe = idRutasSafe(idRutas);

        long totalComidas = resumenProduccionRepository.countComidas(
                inicio, fin, tipoPedido, pedidoCreadoDesde, filtrarRuta, idRutasSafe);

        long totalDesayunos = resumenProduccionRepository.countDesayunos(
                inicio, fin, tipoPedido, pedidoCreadoDesde, filtrarRuta, idRutasSafe);

        long totalBasicos = resumenProduccionRepository.countBasicos(
                inicio, fin, tipoPedido, pedidoCreadoDesde, filtrarRuta, idRutasSafe);

        // Desglosa productos cocina por tipo desde una sola query agrupada
        List<Object[]> productosPorTipo = resumenProduccionRepository
                .countProductosCocinaAgrupadoPorTipo(
                        inicio, fin, tipoPedido, pedidoCreadoDesde, filtrarRuta, idRutasSafe);

        long totalSnacks = 0, totalBebidas = 0, totalCharolas = 0, totalPostres = 0;
        for (Object[] row : productosPorTipo) {
            TipoProducto tipo = (TipoProducto) row[0];
            long cantidad = ((Number) row[1]).longValue();
            switch (tipo) {
                case SNACK   -> totalSnacks   += cantidad;
                case BEBIDA  -> totalBebidas  += cantidad;
                case CHAROLA -> totalCharolas += cantidad;
                case POSTRE  -> totalPostres  += cantidad;
            }
        }

        return ResumenProduccionResponseDTO.builder()
                .fecha(hoy)
                .totalComidas(totalComidas)
                .totalDesayunos(totalDesayunos)
                .totalSnacks(totalSnacks)
                .totalBebidas(totalBebidas)
                .totalCharolas(totalCharolas)
                .totalPostres(totalPostres)
                .totalBasicos(totalBasicos)
                .build();
    }

    /**
     * Devuelve la lista de productos concretos y su cantidad para una categoría del día de hoy.
     * La categoría debe ser: comidas, desayunos, snacks, bebidas, charolas, postres o basicos.
     */
    @Transactional(readOnly = true)
    public DetalleProduccionResponseDTO detalleProduccion(
            String categoria,
            TipoPedido tipoPedido,
            PedidoCreadoDesde pedidoCreadoDesde,
            List<Integer> idRutas) {

        LocalDate hoy = LocalDate.now(Constants.ZONA_MERIDA);
        LocalDateTime inicio = hoy.atStartOfDay();
        LocalDateTime fin = hoy.plusDays(1).atStartOfDay();

        boolean filtrarRuta = esFiltrarRuta(idRutas);
        List<Integer> idRutasSafe = idRutasSafe(idRutas);

        List<Object[]> rows = switch (categoria.toLowerCase()) {
            case "comidas"  -> resumenProduccionRepository.findDetalleComidas(
                    inicio, fin, tipoPedido, pedidoCreadoDesde, filtrarRuta, idRutasSafe);
            case "desayunos" -> resumenProduccionRepository.findDetalleDesayunos(
                    inicio, fin, tipoPedido, pedidoCreadoDesde, filtrarRuta, idRutasSafe);
            case "basicos"  -> resumenProduccionRepository.findDetalleBasicos(
                    inicio, fin, tipoPedido, pedidoCreadoDesde, filtrarRuta, idRutasSafe);
            case "snacks"   -> resumenProduccionRepository.findDetalleProductosCocina(
                    TipoProducto.SNACK, inicio, fin, tipoPedido, pedidoCreadoDesde, filtrarRuta, idRutasSafe);
            case "bebidas"  -> resumenProduccionRepository.findDetalleProductosCocina(
                    TipoProducto.BEBIDA, inicio, fin, tipoPedido, pedidoCreadoDesde, filtrarRuta, idRutasSafe);
            case "charolas" -> resumenProduccionRepository.findDetalleProductosCocina(
                    TipoProducto.CHAROLA, inicio, fin, tipoPedido, pedidoCreadoDesde, filtrarRuta, idRutasSafe);
            case "postres"  -> resumenProduccionRepository.findDetalleProductosCocina(
                    TipoProducto.POSTRE, inicio, fin, tipoPedido, pedidoCreadoDesde, filtrarRuta, idRutasSafe);
            default -> throw new BusinessException(
                    "Categoría no válida: '" + categoria + "'. Valores permitidos: comidas, desayunos, snacks, bebidas, charolas, postres, basicos",
                    HttpStatus.BAD_REQUEST);
        };

        List<ItemProduccionDTO> items = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            items.add(ItemProduccionDTO.builder()
                    .nombre((String) row[0])
                    .cantidad(((Number) row[1]).intValue())
                    .build());
        }

        return DetalleProduccionResponseDTO.builder()
                .categoria(categoria.toLowerCase())
                .items(items)
                .build();
    }

    // ── Helpers privados ──────────────────────────────────────────────────────

    private boolean esFiltrarRuta(List<Integer> idRutas) {
        return idRutas != null && !idRutas.isEmpty();
    }

    /** Devuelve la lista de rutas o List.of(-1) cuando no hay filtro, evitando IN () vacío en JPQL. */
    private List<Integer> idRutasSafe(List<Integer> idRutas) {
        return esFiltrarRuta(idRutas) ? idRutas : List.of(-1);
    }
}
