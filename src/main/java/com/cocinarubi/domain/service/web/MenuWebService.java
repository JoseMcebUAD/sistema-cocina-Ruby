package com.cocinarubi.domain.service.web;

import com.cocinarubi.DBConstants.Estatus;
import com.cocinarubi.DBConstants.TipoCatalogoProducto;
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
import com.cocinarubi.domain.service.PaqueteService;
import com.cocinarubi.presentation.dto.response.BasicoResponseDTO;
import com.cocinarubi.presentation.dto.response.CategoriaMenuDTO;
import com.cocinarubi.presentation.dto.response.ComidaMenuItemDTO;
import com.cocinarubi.presentation.dto.response.ComplementoPredeterminadoResponseDTO;
import com.cocinarubi.presentation.dto.response.ComplementoResponseDTO;
import com.cocinarubi.presentation.dto.response.DesayunoMenuItemDTO;
import com.cocinarubi.presentation.dto.response.MenuWebResponseDTO;
import com.cocinarubi.presentation.dto.response.PaqueteResponseDTO;
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
    private final PaqueteService paqueteService;
    private final ProductoCocinaRepository productoCocinaRepository;
    private final ComplementoRepository complementoRepository;
    private final ArchivoRepository archivoRepository;

    public MenuWebService(ComidaRepository comidaRepository,
                          BasicoRepository basicoRepository,
                          DesayunoRepository desayunoRepository,
                          PaqueteService paqueteService,
                          ProductoCocinaRepository productoCocinaRepository,
                          ComplementoRepository complementoRepository,
                          ArchivoRepository archivoRepository) {
        this.comidaRepository = comidaRepository;
        this.basicoRepository = basicoRepository;
        this.desayunoRepository = desayunoRepository;
        this.paqueteService = paqueteService;
        this.productoCocinaRepository = productoCocinaRepository;
        this.complementoRepository = complementoRepository;
        this.archivoRepository = archivoRepository;
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

        // PaqueteService.findDisponibles aplica resolverNombres internamente (evita N+1).
        List<PaqueteResponseDTO> paquetes = paqueteService.findDisponibles();

        List<CategoriaMenuDTO> categorias = buildCategorias(pcEntities, portadasPc);

        // ComplementoRepository: lista todos los complementos DISPONIBLE ordenados alfabéticamente
        List<ComplementoResponseDTO> complementos = complementoRepository
                .findDisponiblesOrdenados(Estatus.DISPONIBLE)
                .stream().map(this::toComplementoDTO).collect(Collectors.toList());

        return new MenuWebResponseDTO(comidas, basicos, desayunos, paquetes, categorias, complementos);
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
        return dto;
    }
}
