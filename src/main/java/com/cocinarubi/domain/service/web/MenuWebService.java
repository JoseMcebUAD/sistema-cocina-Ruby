package com.cocinarubi.domain.service.web;

import com.cocinarubi.DBConstants.Estatus;
import com.cocinarubi.DBConstants.TipoCatalogoProducto;
import com.cocinarubi.DBConstants.TipoComida;
import com.cocinarubi.dao.ArchivoRepository;
import com.cocinarubi.dao.BasicoRepository;
import com.cocinarubi.dao.ComidaRepository;
import com.cocinarubi.dao.ComplementoRepository;
import com.cocinarubi.dao.DesayunoRepository;
import com.cocinarubi.dao.ProductoCocinaRepository;
import com.cocinarubi.domain.entity.Archivo;
import com.cocinarubi.domain.entity.Basico;
import com.cocinarubi.domain.entity.Categoria;
import com.cocinarubi.domain.entity.Comida;
import com.cocinarubi.domain.entity.Complemento;
import com.cocinarubi.domain.entity.Desayuno;
import com.cocinarubi.domain.entity.ProductoCocina;
import com.cocinarubi.domain.interfaces.IMenuWebService;
import com.cocinarubi.domain.service.AnuncioService;
import com.cocinarubi.domain.service.ComboService;
import com.cocinarubi.domain.service.OfertaService;
import com.cocinarubi.presentation.dto.response.AnuncioResponseDTO;
import com.cocinarubi.presentation.dto.response.BasicoResponseDTO;
import com.cocinarubi.presentation.dto.response.OfertaLineaMenuDTO;
import com.cocinarubi.presentation.dto.response.OfertaLineaResponseDTO;
import com.cocinarubi.presentation.dto.response.OfertaMenuDTO;
import com.cocinarubi.presentation.dto.response.OfertaResponseDTO;
import com.cocinarubi.presentation.dto.response.CategoriaMenuDTO;
import com.cocinarubi.presentation.dto.response.ComboResponseDTO;
import com.cocinarubi.presentation.dto.response.ComidaMenuItemDTO;
import com.cocinarubi.presentation.dto.response.ComplementoPredeterminadoResponseDTO;
import com.cocinarubi.presentation.dto.response.ComplementoResponseDTO;
import com.cocinarubi.presentation.dto.response.DesayunoMenuItemDTO;
import com.cocinarubi.presentation.dto.response.MenuWebResponseDTO;
import com.cocinarubi.presentation.dto.response.ProductoCocinaMenuItemDTO;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Agrega en una sola transacción todos los ítems DISPONIBLE del menú web:
 * comidas, básicos, desayunos, paquetes y productos agrupados por categoría.
 *
 * <p>Reutiliza las queries con JOIN FETCH de los repositorios existentes para evitar N+1.
 * Los productos de cocina se cargan con JOIN FETCH de su categoría mediante
 * {@link ProductoCocinaRepository#findDisponiblesOrdenadosConCategoria}.</p>
 *
 * <p>Las imágenes de portada se obtienen con una sola query batch por sección:
 * tipos estáticos (COMIDA/DESAYUNO/BASICO) usan el enum {@link TipoCatalogoProducto};
 * ProductoCocina usa módulos dinámicos y se resuelve con
 * {@link ArchivoRepository#findByCategoriaNotNullAndIdEntidadIn}.</p>
 *
 * Capa: Service — lógica de agregación del menú web.
 */
@Service
@Transactional(readOnly = true)
public class MenuWebService implements IMenuWebService {

    private final ComidaRepository comidaRepository;
    private final BasicoRepository basicoRepository;
    private final DesayunoRepository desayunoRepository;
    private final ComboService comboService;
    private final ProductoCocinaRepository productoCocinaRepository;
    private final ComplementoRepository complementoRepository;
    private final ArchivoRepository archivoRepository;
    private final AnuncioService anuncioService;
    private final OfertaService ofertaService;

    public MenuWebService(ComidaRepository comidaRepository,
                          BasicoRepository basicoRepository,
                          DesayunoRepository desayunoRepository,
                          ComboService comboService,
                          ProductoCocinaRepository productoCocinaRepository,
                          ComplementoRepository complementoRepository,
                          ArchivoRepository archivoRepository,
                          AnuncioService anuncioService,
                          OfertaService ofertaService) {
        this.comidaRepository = comidaRepository;
        this.basicoRepository = basicoRepository;
        this.desayunoRepository = desayunoRepository;
        this.comboService = comboService;
        this.productoCocinaRepository = productoCocinaRepository;
        this.complementoRepository = complementoRepository;
        this.archivoRepository = archivoRepository;
        this.anuncioService = anuncioService;
        this.ofertaService = ofertaService;
    }

    @Override
    @Cacheable(value = "menu-web", key = "'all'")
    public MenuWebResponseDTO getMenu() {
        // ── Cargar entidades ──────────────────────────────────────────────────────
        List<Comida> comidaEntities = comidaRepository.findDisponiblesOrdenados(Estatus.DISPONIBLE);
        List<Basico> basicoEntities = basicoRepository.findDisponiblesOrdenados(Estatus.DISPONIBLE);
        List<Desayuno> desayunoEntities = desayunoRepository.findDisponiblesOrdenados(Estatus.DISPONIBLE);

        // ProductoCocinaRepository: JOIN FETCH de categoria para evitar N+1 en buildCategorias
        List<ProductoCocina> pcEntities = productoCocinaRepository
                .findDisponiblesOrdenadosConCategoria(Estatus.DISPONIBLE);

        // ── Batch portadas: 1 query por sección ──────────────────────────────────
        List<Integer> comidaIds   = comidaEntities.stream().map(Comida::getIdComida).toList();
        List<Integer> basicoIds   = basicoEntities.stream().map(Basico::getIdBasico).toList();
        List<Integer> desayunoIds = desayunoEntities.stream().map(Desayuno::getIdDesayuno).toList();
        List<Integer> pcIds       = pcEntities.stream().map(ProductoCocina::getIdProductoCocina).toList();

        Map<Integer, String> portadasComida   = portadasEstaticas(TipoCatalogoProducto.COMIDA, comidaIds);
        Map<Integer, String> portadasBasico   = portadasEstaticas(TipoCatalogoProducto.BASICO, basicoIds);
        Map<Integer, String> portadasDesayuno = portadasEstaticas(TipoCatalogoProducto.DESAYUNO, desayunoIds);
        // ArchivoRepository: portadas de ProductoCocina sin discriminar por idCategoria
        Map<Integer, String> portadasPc       = portadasDinamicas(pcIds);

        // ── Mapear a DTOs ─────────────────────────────────────────────────────────
        List<ComidaMenuItemDTO> comidas = comidaEntities.stream()
                .map(c -> toComidaDTO(c, portadasComida))
                .collect(Collectors.toList());

        // BasicoRepository.findDisponiblesOrdenados ya incluye JOIN FETCH de comida y complementos.
        List<BasicoResponseDTO> basicos = basicoEntities.stream()
                .map(b -> toBasicoDTO(b, portadasBasico))
                .collect(Collectors.toList());

        List<DesayunoMenuItemDTO> desayunos = desayunoEntities.stream()
                .map(d -> toDesayunoDTO(d, portadasDesayuno))
                .collect(Collectors.toList());

        // ComboService.findDisponibles aplica resolverNombres internamente (evita N+1).
        List<ComboResponseDTO> combos = comboService.findDisponibles();

        List<CategoriaMenuDTO> categorias = buildCategorias(pcEntities, portadasPc);

        // ComplementoRepository: lista todos los complementos DISPONIBLE ordenados alfabéticamente
        List<ComplementoResponseDTO> complementos = complementoRepository
                .findDisponiblesOrdenados(Estatus.DISPONIBLE)
                .stream().map(this::toComplementoDTO).collect(Collectors.toList());

        // AnuncioService: solo anuncios con activo=true, ordenados por prioridad y fecha
        List<AnuncioResponseDTO> anuncios = anuncioService.findActivos();

        // ── Mapas id→nombre para resolver líneas de oferta sin queries extra ─────
        Map<Integer, String> nombrePorComida = comidaEntities.stream()
                .collect(Collectors.toMap(Comida::getIdComida, Comida::getNombreComida));
        Map<Integer, String> nombrePorBasico = basicoEntities.stream()
                .collect(Collectors.toMap(Basico::getIdBasico, b -> b.getComida().getNombreComida()));
        // ComboResponseDTO.getDescripcion() es el nombre visible del paquete
        Map<Integer, String> nombrePorCombo = combos.stream()
                .collect(Collectors.toMap(ComboResponseDTO::getIdCombo, ComboResponseDTO::getDescripcion));
        Map<Integer, String> nombrePorProducto = pcEntities.stream()
                .collect(Collectors.toMap(ProductoCocina::getIdProductoCocina, ProductoCocina::getNombreProducto));
        // Dedup: si dos productos comparten categoría, el primero gana (misma categoría → mismo nombre)
        Map<Integer, String> nombrePorCategoria = pcEntities.stream()
                .collect(Collectors.toMap(
                        pc -> pc.getCategoria().getIdCategoria(),
                        pc -> pc.getCategoria().getNombre(),
                        (a, b) -> a));

        // OfertaService: ofertas activas con líneas de matching traducidas a texto legible
        List<OfertaMenuDTO> ofertas = ofertaService.findActivas().stream()
                .map(o -> toOfertaMenuDTO(o, nombrePorComida, nombrePorBasico, nombrePorCombo,
                        nombrePorProducto, nombrePorCategoria))
                .collect(Collectors.toList());

        return new MenuWebResponseDTO(comidas, basicos, desayunos, combos, categorias, complementos, anuncios, ofertas);
    }

    // ── Helpers de portadas ───────────────────────────────────────────────────────

    /**
     * Obtiene las portadas (imagen de orden más bajo) para un lote de entidades estáticas
     * (COMIDA, DESAYUNO o BASICO) en una sola query.
     */
    private Map<Integer, String> portadasEstaticas(TipoCatalogoProducto tipo, List<Integer> ids) {
        if (ids.isEmpty()) return Map.of();
        return archivoRepository.findByEntityTypeAndIdEntidadIn(tipo, ids)
                .stream()
                .collect(Collectors.toMap(
                        Archivo::getIdEntidad,
                        Archivo::getPathArchivo,
                        (primero, siguiente) -> primero)); // orden ASC en la query → primero = portada
    }

    /**
     * Obtiene las portadas de ProductoCocina en una sola query, sin requerir idCategoria.
     * Usa la variante que filtra por {@code categoria IS NOT NULL}.
     */
    private Map<Integer, String> portadasDinamicas(List<Integer> ids) {
        if (ids.isEmpty()) return Map.of();
        return archivoRepository.findByCategoriaNotNullAndIdEntidadIn(ids)
                .stream()
                .collect(Collectors.toMap(
                        Archivo::getIdEntidad,
                        Archivo::getPathArchivo,
                        (primero, siguiente) -> primero));
    }

    // ── Helpers de mapeo ─────────────────────────────────────────────────────────

    private ComplementoResponseDTO toComplementoDTO(Complemento c) {
        return new ComplementoResponseDTO(
                c.getIdComplemento(),
                c.getNombreComplemento(),
                c.getPrecioExtra(),
                c.isCobrarSiempre());
    }

    /**
     * Agrupa los productos disponibles por categoría usando la lista ya cargada con JOIN FETCH.
     * El orden del LinkedHashMap respeta el orden ASC por nombre de categoría de la query.
     */
    private List<CategoriaMenuDTO> buildCategorias(List<ProductoCocina> productos,
                                                    Map<Integer, String> portadas) {
        Map<Integer, CategoriaMenuDTO> byCategoria = new LinkedHashMap<>();
        for (ProductoCocina pc : productos) {
            Categoria cat = pc.getCategoria();
            byCategoria
                    .computeIfAbsent(cat.getIdCategoria(),
                            id -> new CategoriaMenuDTO(id, cat.getNombre(), new ArrayList<>()))
                    .getProductos().add(toProductoCocinaDTO(pc, portadas));
        }
        return new ArrayList<>(byCategoria.values());
    }

    private ComidaMenuItemDTO toComidaDTO(Comida c, Map<Integer, String> portadas) {
        ComidaMenuItemDTO dto = new ComidaMenuItemDTO(
                c.getIdComida(),
                c.getUuidComida(),
                c.getNombreComida(),
                c.getDescripcion(),
                c.getPrecioMedia(),
                c.getPrecioEntera(),
                c.isDestacado(),
                c.getLimiteComplemento());

        dto.setUrlImagen(portadas.get(c.getIdComida()));
        dto.setDescuentoActivo(c.isDescuentoActivo());
        dto.setDescripcionDescuento(c.getDescripcionDescuento());
        dto.setPrecioDescuento(c.getPrecioDescuento());

        // Complementos predeterminados ya cargados en memoria por @OneToMany(EAGER) + JOIN FETCH en repo
        dto.setComplementosPredeterminados(
                c.getComplementosPredeterminados().stream()
                        .map(cp -> new ComplementoPredeterminadoResponseDTO(
                                cp.getComplemento().getIdComplemento(),
                                cp.getComplemento().getNombreComplemento(),
                                cp.getComplemento().getPrecioExtra(),
                                cp.getComplemento().isCobrarSiempre(),
                                cp.getCantidad()))
                        .collect(Collectors.toList()));

        return dto;
    }

    private BasicoResponseDTO toBasicoDTO(Basico b, Map<Integer, String> portadas) {
        List<ComplementoResponseDTO> complementos = b.getComplementos().stream()
                .map(bc -> new ComplementoResponseDTO(
                        bc.getComplemento().getIdComplemento(),
                        bc.getComplemento().getNombreComplemento(),
                        bc.getComplemento().getPrecioExtra(),
                        bc.getComplemento().isCobrarSiempre()))
                .collect(Collectors.toList());
        BasicoResponseDTO dto = new BasicoResponseDTO(
                b.getIdBasico(),
                b.getUuidBasico(),
                b.getComida().getIdComida(),
                b.getComida().getNombreComida(),
                b.getDescripcion(),
                b.isDestacado(),
                b.getPrecioBasico(),
                b.getEstatus(),
                complementos);
        dto.setUrlImagen(portadas.get(b.getIdBasico()));
        dto.setDescuentoActivo(b.isDescuentoActivo());
        dto.setDescripcionDescuento(b.getDescripcionDescuento());
        dto.setPrecioDescuento(b.getPrecioDescuento());
        return dto;
    }

    private DesayunoMenuItemDTO toDesayunoDTO(Desayuno d, Map<Integer, String> portadas) {
        DesayunoMenuItemDTO dto = new DesayunoMenuItemDTO(
                d.getIdDesayuno(),
                d.getUuidDesayuno(),
                d.getNombreDesayuno(),
                d.getDescripcion(),
                d.getPrecioMedia(),
                d.getPrecioEntera(),
                d.isDestacado());
        dto.setUrlImagen(portadas.get(d.getIdDesayuno()));
        dto.setDescuentoActivo(d.isDescuentoActivo());
        dto.setDescripcionDescuento(d.getDescripcionDescuento());
        dto.setPrecioDescuento(d.getPrecioDescuento());
        return dto;
    }

    private ProductoCocinaMenuItemDTO toProductoCocinaDTO(ProductoCocina pc,
                                                           Map<Integer, String> portadas) {
        ProductoCocinaMenuItemDTO dto = new ProductoCocinaMenuItemDTO(
                pc.getIdProductoCocina(),
                pc.getUuidProductoCocina(),
                pc.getNombreProducto(),
                pc.getDescripcion(),
                pc.getPrecioDomicilio(),
                pc.getPrecioNormal(),
                pc.isDestacado());
        dto.setUrlImagen(portadas.get(pc.getIdProductoCocina()));
        dto.setDescuentoActivo(pc.isDescuentoActivo());
        dto.setDescripcionDescuento(pc.getDescripcionDescuento());
        dto.setPrecioDescuento(pc.getPrecioDescuento());
        return dto;
    }

    // ── Helpers de ofertas ────────────────────────────────────────────────────────

    /**
     * Convierte una oferta del catálogo a su vista web, resolviendo cada línea de matching
     * a texto legible usando los mapas id→nombre ya construidos en memoria.
     */
    private OfertaMenuDTO toOfertaMenuDTO(OfertaResponseDTO oferta,
                                           Map<Integer, String> nombrePorComida,
                                           Map<Integer, String> nombrePorBasico,
                                           Map<Integer, String> nombrePorCombo,
                                           Map<Integer, String> nombrePorProducto,
                                           Map<Integer, String> nombrePorCategoria) {
        List<OfertaLineaMenuDTO> lineas = oferta.getLineas().stream()
                .map(l -> new OfertaLineaMenuDTO(l.getCantidad(),
                        resolverDescripcion(l, nombrePorComida, nombrePorBasico, nombrePorCombo,
                                nombrePorProducto, nombrePorCategoria)))
                .collect(Collectors.toList());
        return new OfertaMenuDTO(oferta.getIdOferta(), oferta.getNombre(), oferta.getDescripcion(),
                oferta.getPrecioOferta(), oferta.getTiposPedidoRestriccion(), lineas);
    }

    /**
     * Traduce una línea de oferta (enum técnico + idReferencia) a una descripción legible.
     * Para reglas específicas usa el mapa id→nombre del catálogo; para reglas genéricas
     * devuelve una etiqueta fija.
     */
    private String resolverDescripcion(OfertaLineaResponseDTO linea,
                                        Map<Integer, String> nombrePorComida,
                                        Map<Integer, String> nombrePorBasico,
                                        Map<Integer, String> nombrePorCombo,
                                        Map<Integer, String> nombrePorProducto,
                                        Map<Integer, String> nombrePorCategoria) {
        return switch (linea.getModoMatch()) {
            case COMIDA_ESPECIFICA        -> nombrePorComida.getOrDefault(linea.getIdReferencia(), "Comida");
            case COMIDA_POR_TIPO          -> linea.getTipoComidaFiltro() == TipoComida.FIJA
                                                ? "Comida del Día" : "Comida Especial";
            case BASICO_ESPECIFICO        -> nombrePorBasico.getOrDefault(linea.getIdReferencia(), "Básico");
            case BASICO_GENERAL           -> "Básico (cualquiera)";
            case COMBO_ESPECIFICO         -> nombrePorCombo.getOrDefault(linea.getIdReferencia(), "Combo");
            case COMBO_GENERAL            -> "Combo (cualquiera)";
            case PRODUCTO_ESPECIFICO      -> nombrePorProducto.getOrDefault(linea.getIdReferencia(), "Producto");
            case PRODUCTO_POR_CATEGORIA   -> nombrePorCategoria.getOrDefault(linea.getIdReferencia(), "Producto de categoría");
            case PRODUCTO_POR_SUBCATEGORIA -> "Producto de subcategoría";
            case PRODUCTO_GENERAL         -> "Producto (cualquiera)";
        };
    }
}
