package com.cocinarubi.domain.service;

import com.cocinarubi.Constants;
import com.cocinarubi.DBConstants.PedidoCreadoDesde;
import com.cocinarubi.DBConstants.TipoPedido;
import com.cocinarubi.dao.CategoriaRepository;
import com.cocinarubi.dao.ResumenProduccionRepository;
import com.cocinarubi.domain.entity.Categoria;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.response.DetalleProduccionResponseDTO;
import com.cocinarubi.presentation.dto.response.ItemProduccionDTO;
import com.cocinarubi.presentation.dto.response.ResumenProduccionResponseDTO;
import com.cocinarubi.presentation.dto.response.TotalPorCategoriaDTO;
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
    private final CategoriaRepository categoriaRepository;

    public ResumenProduccionService(ResumenProduccionRepository resumenProduccionRepository,
                                    CategoriaRepository categoriaRepository) {
        this.resumenProduccionRepository = resumenProduccionRepository;
        this.categoriaRepository = categoriaRepository;
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

        // Una fila por categoría con al menos un producto pedido; ordenado por nombre en el repo.
        List<Object[]> productosPorCategoria = resumenProduccionRepository
                .countProductosCocinaAgrupadoPorCategoria(
                        inicio, fin, tipoPedido, pedidoCreadoDesde, filtrarRuta, idRutasSafe);

        List<TotalPorCategoriaDTO> totales = new ArrayList<>(productosPorCategoria.size());
        for (Object[] row : productosPorCategoria) {
            totales.add(TotalPorCategoriaDTO.builder()
                    .idCategoria(((Number) row[0]).intValue())
                    .nombreCategoria((String) row[1])
                    .total(((Number) row[2]).longValue())
                    .build());
        }

        return ResumenProduccionResponseDTO.builder()
                .fecha(hoy)
                .totalComidas(totalComidas)
                .totalDesayunos(totalDesayunos)
                .totalBasicos(totalBasicos)
                .totalesProductosCocina(totales)
                .build();
    }

    /**
     * Devuelve la lista de productos concretos y su cantidad para una categoría del día.
     * La categoría puede ser: {@code comidas}, {@code desayunos}, {@code basicos}, o
     * el nombre libre de cualquier {@link Categoria} de {@code ProductoCocina}.
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

        List<Object[]> rows = resolverDetalle(
                categoria, inicio, fin, tipoPedido, pedidoCreadoDesde, filtrarRuta, idRutasSafe);

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

    /**
     * Decide qué query ejecutar. Los tres nombres reservados
     * ({@code comidas}, {@code desayunos}, {@code basicos}) siguen usando los
     * repositorios existentes; cualquier otro nombre se resuelve como
     * {@link Categoria} vía {@code findByNombreIgnoreCase}.
     */
    private List<Object[]> resolverDetalle(String categoria,
                                           LocalDateTime inicio, LocalDateTime fin,
                                           TipoPedido tipoPedido, PedidoCreadoDesde pedidoCreadoDesde,
                                           boolean filtrarRuta, List<Integer> idRutasSafe) {
        switch (categoria.toLowerCase()) {
            case "comidas":
                return resumenProduccionRepository.findDetalleComidas(
                        inicio, fin, tipoPedido, pedidoCreadoDesde, filtrarRuta, idRutasSafe);
            case "desayunos":
                return resumenProduccionRepository.findDetalleDesayunos(
                        inicio, fin, tipoPedido, pedidoCreadoDesde, filtrarRuta, idRutasSafe);
            case "basicos":
                return resumenProduccionRepository.findDetalleBasicos(
                        inicio, fin, tipoPedido, pedidoCreadoDesde, filtrarRuta, idRutasSafe);
            default:
                Categoria cat = categoriaRepository.findByNombreIgnoreCase(categoria)
                        .orElseThrow(() -> new BusinessException(
                                "Categoría no válida: '" + categoria + "'. Debe ser 'comidas', 'desayunos', 'basicos' o el nombre de una categoría existente.",
                                HttpStatus.BAD_REQUEST));
                return resumenProduccionRepository.findDetalleProductosCocinaPorCategoria(
                        cat.getIdCategoria(), inicio, fin, tipoPedido, pedidoCreadoDesde,
                        filtrarRuta, idRutasSafe);
        }
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
