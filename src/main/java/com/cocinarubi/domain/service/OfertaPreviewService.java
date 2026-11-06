package com.cocinarubi.domain.service;

import com.cocinarubi.dao.BasicoRepository;
import com.cocinarubi.dao.ComboRepository;
import com.cocinarubi.dao.ComidaRepository;
import com.cocinarubi.dao.ProductoCocinaRepository;
import com.cocinarubi.domain.entity.*;
import com.cocinarubi.domain.model.OfertaAplicacion;
import com.cocinarubi.presentation.dto.request.*;
import com.cocinarubi.presentation.dto.response.OfertaPedidoLineaResponseDTO;
import com.cocinarubi.presentation.dto.response.OfertaPreviewResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cocinarubi.Constants;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Calcula en memoria qué {@link Oferta}s aplicarían sobre un carrito en construcción,
 * sin persistir ningún dato.
 *
 * <p>Reutiliza exactamente {@link OfertaMatcherService#detectar} para garantizar que
 * el resultado del preview coincida con lo que el save aplicaría al confirmar el pedido.
 * IDs inexistentes en catálogo se ignoran silenciosamente.</p>
 *
 * <p>Capa: Service — lógica de preview de ofertas.</p>
 */
@Service
public class OfertaPreviewService {

    private final OfertaService ofertaService;
    private final OfertaMatcherService ofertaMatcher;
    private final ComidaRepository comidaRepository;
    private final ProductoCocinaRepository productoCocinaRepository;
    private final BasicoRepository basicoRepository;
    private final ComboRepository comboRepository;

    public OfertaPreviewService(OfertaService ofertaService,
                                 OfertaMatcherService ofertaMatcher,
                                 ComidaRepository comidaRepository,
                                 ProductoCocinaRepository productoCocinaRepository,
                                 BasicoRepository basicoRepository,
                                 ComboRepository comboRepository) {
        this.ofertaService = ofertaService;
        this.ofertaMatcher = ofertaMatcher;
        this.comidaRepository = comidaRepository;
        this.productoCocinaRepository = productoCocinaRepository;
        this.basicoRepository = basicoRepository;
        this.comboRepository = comboRepository;
    }

    /**
     * Detecta las ofertas que aplicarían al carrito recibido sin persistir nada.
     * {@code tipoPedido} null = sin filtro por restricción de tipo (muestra todas las activas).
     */
    @Transactional(readOnly = true)
    public List<OfertaPreviewResponseDTO> preview(OfertaPreviewRequestDTO dto) {
        LocalDate hoy = LocalDate.now(Constants.ZONA_MERIDA);
        // ofertaService.findActivasEntidades(): cargadas con FETCH JOIN, cacheadas en Redis con clave fecha
        List<Oferta> activas = ofertaService.findActivasEntidades(hoy);
        if (dto.getTipoPedido() != null) {
            activas = activas.stream()
                    .filter(o -> o.estaDisponiblePara(dto.getTipoPedido()))
                    .toList();
        }
        if (activas.isEmpty()) return List.of();

        List<ComidaPedido>        comidas   = buildComidasPreview(dto.getComidas());
        List<ProductoCocinaPedido> productos = buildProductosPreview(dto.getProductosCocina());
        List<BasicoPedido>         basicos   = buildBasicosPreview(dto.getBasicos());
        List<ComboPedido>          combos    = buildCombosPreview(dto.getCombos());

        List<OfertaAplicacion> aplicaciones = ofertaMatcher.detectar(
                comidas, productos, basicos, combos, activas);

        return aplicaciones.stream()
                .map(this::toPreviewResponse)
                .toList();
    }

    // -------------------------------------------------------------------------
    // Construcción de líneas en memoria — sin Pedido padre, sin persistir
    // -------------------------------------------------------------------------

    private List<ComidaPedido> buildComidasPreview(List<ComidaPedidoDTO> dtos) {
        List<Integer> ids = dtos.stream()
                .map(ComidaPedidoDTO::getIdComida)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (ids.isEmpty()) return List.of();

        Map<Integer, Comida> comidaMap = comidaRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Comida::getIdComida, c -> c));

        List<ComidaPedido> resultado = new ArrayList<>();
        for (ComidaPedidoDTO d : dtos) {
            Comida entidad = comidaMap.get(d.getIdComida());
            if (entidad == null) continue;
            resultado.add(ComidaPedido.builder()
                    .comida(entidad)
                    .precioUnitario(d.getPrecioUnitario() != null
                            ? d.getPrecioUnitario() : BigDecimal.ZERO)
                    .tamanoPorcion(d.getTamanoPorcion())
                    .build());
        }
        return resultado;
    }

    private List<ProductoCocinaPedido> buildProductosPreview(List<ProductoCocinaPedidoDTO> dtos) {
        List<Integer> ids = dtos.stream()
                .map(ProductoCocinaPedidoDTO::getIdProductoCocina)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (ids.isEmpty()) return List.of();

        Map<Integer, ProductoCocina> prodMap = productoCocinaRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(ProductoCocina::getIdProductoCocina, p -> p));

        List<ProductoCocinaPedido> resultado = new ArrayList<>();
        for (ProductoCocinaPedidoDTO d : dtos) {
            ProductoCocina entidad = prodMap.get(d.getIdProductoCocina());
            if (entidad == null) continue;
            resultado.add(ProductoCocinaPedido.builder()
                    .productoCocina(entidad)
                    .precioUnitario(d.getPrecioUnitario() != null
                            ? d.getPrecioUnitario() : BigDecimal.ZERO)
                    .cantidad(d.getCantidad() != null ? d.getCantidad().byteValue() : (byte) 1)
                    .build());
        }
        return resultado;
    }

    private List<BasicoPedido> buildBasicosPreview(List<BasicoPedidoDTO> dtos) {
        List<Integer> ids = dtos.stream()
                .map(BasicoPedidoDTO::getIdBasico)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (ids.isEmpty()) return List.of();

        Map<Integer, Basico> basicoMap = basicoRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Basico::getIdBasico, b -> b));

        List<BasicoPedido> resultado = new ArrayList<>();
        for (BasicoPedidoDTO d : dtos) {
            Basico entidad = basicoMap.get(d.getIdBasico());
            if (entidad == null) continue;
            resultado.add(BasicoPedido.builder()
                    .basico(entidad)
                    .precioUnitario(d.getPrecioUnitario() != null
                            ? d.getPrecioUnitario() : BigDecimal.ZERO)
                    .build());
        }
        return resultado;
    }

    private List<ComboPedido> buildCombosPreview(List<ComboPedidoDTO> dtos) {
        List<Integer> ids = dtos.stream()
                .map(ComboPedidoDTO::getIdCombo)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (ids.isEmpty()) return List.of();

        Map<Integer, Combo> comboMap = comboRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Combo::getIdCombo, c -> c));

        List<ComboPedido> resultado = new ArrayList<>();
        for (ComboPedidoDTO d : dtos) {
            Combo entidad = comboMap.get(d.getIdCombo());
            if (entidad == null) continue;
            resultado.add(ComboPedido.builder()
                    .combo(entidad)
                    .precioUnitario(d.getPrecioUnitario() != null
                            ? d.getPrecioUnitario() : BigDecimal.ZERO)
                    .cantidad(d.getCantidad() != null ? d.getCantidad() : 1)
                    .build());
        }
        return resultado;
    }

    /**
     * Mapea una {@link OfertaAplicacion} al DTO de preview.
     * precioNormalConsumido = precio unitario × cantidad consumida (snapshot del carrito actual).
     */
    private OfertaPreviewResponseDTO toPreviewResponse(OfertaAplicacion aplicacion) {
        OfertaPreviewResponseDTO dto = new OfertaPreviewResponseDTO();
        dto.setIdOferta(aplicacion.getOferta().getIdOferta());
        dto.setNombreOferta(aplicacion.getOferta().getNombre());
        dto.setPrecioAplicado(aplicacion.getPrecioAplicado());
        dto.setAhorro(aplicacion.getAhorro());
        dto.setProductos(aplicacion.getLineas().stream()
                .map(lc -> new OfertaPedidoLineaResponseDTO(
                        lc.getTipoEntidad(),
                        lc.getIdProductoCatalogo() != null ? lc.getIdProductoCatalogo() : 0,
                        lc.getNombreProducto(),
                        lc.getCantidadConsumida(),
                        lc.getPrecioNormal().multiply(BigDecimal.valueOf(lc.getCantidadConsumida()))))
                .toList());
        return dto;
    }
}
