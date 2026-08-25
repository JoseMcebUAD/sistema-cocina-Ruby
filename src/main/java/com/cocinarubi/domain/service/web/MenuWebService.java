package com.cocinarubi.domain.service.web;

import com.cocinarubi.DBConstants.Estatus;
import com.cocinarubi.dao.BasicoRepository;
import com.cocinarubi.dao.ComidaRepository;
import com.cocinarubi.dao.DesayunoRepository;
import com.cocinarubi.dao.ProductoCocinaRepository;
import com.cocinarubi.domain.entity.Basico;
import com.cocinarubi.domain.entity.Categoria;
import com.cocinarubi.domain.entity.Comida;
import com.cocinarubi.domain.entity.Desayuno;
import com.cocinarubi.domain.entity.ProductoCocina;
import com.cocinarubi.domain.interfaces.IMenuWebService;
import com.cocinarubi.domain.service.PaqueteService;
import com.cocinarubi.presentation.dto.response.BasicoResponseDTO;
import com.cocinarubi.presentation.dto.response.CategoriaMenuDTO;
import com.cocinarubi.presentation.dto.response.ComidaMenuItemDTO;
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

    public MenuWebService(ComidaRepository comidaRepository,
                          BasicoRepository basicoRepository,
                          DesayunoRepository desayunoRepository,
                          PaqueteService paqueteService,
                          ProductoCocinaRepository productoCocinaRepository) {
        this.comidaRepository = comidaRepository;
        this.basicoRepository = basicoRepository;
        this.desayunoRepository = desayunoRepository;
        this.paqueteService = paqueteService;
        this.productoCocinaRepository = productoCocinaRepository;
    }

    @Override
    @Cacheable(value = "menu-web", key = "'all'")
    public MenuWebResponseDTO getMenu() {
        List<ComidaMenuItemDTO> comidas = comidaRepository
                .findDisponiblesOrdenados(Estatus.DISPONIBLE)
                .stream().map(this::toComidaDTO).collect(Collectors.toList());

        // BasicoRepository.findDisponiblesOrdenados ya incluye JOIN FETCH de comida y complementos.
        List<BasicoResponseDTO> basicos = basicoRepository
                .findDisponiblesOrdenados(Estatus.DISPONIBLE)
                .stream().map(this::toBasicoDTO).collect(Collectors.toList());

        List<DesayunoMenuItemDTO> desayunos = desayunoRepository
                .findDisponiblesOrdenados(Estatus.DISPONIBLE)
                .stream().map(this::toDesayunoDTO).collect(Collectors.toList());

        // PaqueteService.findDisponibles aplica resolverNombres internamente (evita N+1).
        List<PaqueteResponseDTO> paquetes = paqueteService.findDisponibles();

        List<CategoriaMenuDTO> categorias = buildCategorias();

        return new MenuWebResponseDTO(comidas, basicos, desayunos, paquetes, categorias);
    }

    /**
     * Agrupa los productos disponibles por categoría usando JOIN FETCH para evitar N+1.
     * El orden del LinkedHashMap respeta el orden ASC por nombre de categoría de la query.
     */
    private List<CategoriaMenuDTO> buildCategorias() {
        List<ProductoCocina> productos = productoCocinaRepository
                .findDisponiblesOrdenadosConCategoria(Estatus.DISPONIBLE);
        Map<Integer, CategoriaMenuDTO> byCategoria = new LinkedHashMap<>();
        for (ProductoCocina pc : productos) {
            Categoria cat = pc.getCategoria();
            byCategoria
                    .computeIfAbsent(cat.getIdCategoria(),
                            id -> new CategoriaMenuDTO(id, cat.getNombre(), new ArrayList<>()))
                    .getProductos().add(toProductoCocinaDTO(pc));
        }
        return new ArrayList<>(byCategoria.values());
    }

    private ComidaMenuItemDTO toComidaDTO(Comida c) {
        return new ComidaMenuItemDTO(
                c.getUuidComida(),
                c.getNombreComida(),
                c.getDescripcion(),
                c.getPrecioMedia(),
                c.getPrecioEntera(),
                c.isDestacado(),
                c.getLimiteComplemento());
    }

    private BasicoResponseDTO toBasicoDTO(Basico b) {
        List<ComplementoResponseDTO> complementos = b.getComplementos().stream()
                .map(bc -> new ComplementoResponseDTO(
                        bc.getComplemento().getIdComplemento(),
                        bc.getComplemento().getNombreComplemento(),
                        bc.getComplemento().getPrecioExtra()))
                .collect(Collectors.toList());
        return new BasicoResponseDTO(
                b.getIdBasico(),
                b.getUuidBasico(),
                b.getComida().getIdComida(),
                b.getComida().getNombreComida(),
                b.getDescripcion(),
                b.isDestacado(),
                b.getPrecioBasico(),
                b.getEstatus(),
                complementos);
    }

    private DesayunoMenuItemDTO toDesayunoDTO(Desayuno d) {
        return new DesayunoMenuItemDTO(
                d.getUuidDesayuno(),
                d.getNombreDesayuno(),
                d.getDescripcion(),
                d.getPrecioMedia(),
                d.getPrecioEntera(),
                d.isDestacado());
    }

    private ProductoCocinaMenuItemDTO toProductoCocinaDTO(ProductoCocina pc) {
        return new ProductoCocinaMenuItemDTO(
                pc.getUuidProductoCocina(),
                pc.getNombreProducto(),
                pc.getDescripcion(),
                pc.getPrecioDomicilio(),
                pc.getPrecioNormal(),
                pc.isDestacado());
    }
}
