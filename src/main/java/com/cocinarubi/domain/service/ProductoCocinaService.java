package com.cocinarubi.domain.service;

import com.cocinarubi.DBConstants;
import com.cocinarubi.dao.ProductoCocinaRepository;
import com.cocinarubi.dao.SubcategoriaRepository;
import com.cocinarubi.domain.entity.Categoria;
import com.cocinarubi.domain.entity.ProductoCocina;
import com.cocinarubi.domain.entity.Subcategoria;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.ProductoCocinaRequestDTO;
import com.cocinarubi.presentation.dto.response.ProductoCocinaResponseDTO;
import com.cocinarubi.presentation.dto.response.SubcategoriaResponseDTO;
import com.cocinarubi.presentation.strategy.strategyImplementation.ProductoCocinaConfirmationImp;
import com.cocinarubi.presentation.strategy.strategyImplementation.ProductoCocinaValidationImp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio de dominio para {@link ProductoCocina}. Centraliza la lógica de
 * recuperación y la validación de consistencia entre {@link Categoria} y
 * {@link Subcategoria} al persistir un producto.
 *
 * <p>Regla clave (Fase 2): toda subcategoría asignada a un producto debe
 * pertenecer a la misma categoría del producto. Se rechaza con
 * {@code 409 CONFLICT} en caso contrario.</p>
 */
@Service
public class ProductoCocinaService {

    private final ProductoCocinaRepository productoCocinaRepository;
    private final ProductoCocinaValidationImp productoCocinaValidation;
    private final ProductoCocinaConfirmationImp productoCocinaConfirmation;
    private final CategoriaService categoriaService;
    private final SubcategoriaRepository subcategoriaRepository;

    public ProductoCocinaService(ProductoCocinaRepository productoCocinaRepository,
                                 ProductoCocinaValidationImp productoCocinaValidation,
                                 ProductoCocinaConfirmationImp productoCocinaConfirmation,
                                 CategoriaService categoriaService,
                                 SubcategoriaRepository subcategoriaRepository) {
        this.productoCocinaRepository = productoCocinaRepository;
        this.productoCocinaValidation = productoCocinaValidation;
        this.productoCocinaConfirmation = productoCocinaConfirmation;
        this.categoriaService = categoriaService;
        this.subcategoriaRepository = subcategoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductoCocinaResponseDTO> findAll() {
        return productoCocinaRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ProductoCocinaResponseDTO> findAll(Pageable pageable) {
        return productoCocinaRepository.findAllPaginado(pageable).map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<ProductoCocinaResponseDTO> findDisponibles() {
        return productoCocinaRepository.findDisponiblesOrdenados(DBConstants.Estatus.DISPONIBLE)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ProductoCocinaResponseDTO> findDisponibles(Pageable pageable) {
        return productoCocinaRepository.findDisponiblesPaginado(DBConstants.Estatus.DISPONIBLE, pageable)
                .map(this::toResponseDTO);
    }

    @Transactional
    public ProductoCocinaResponseDTO save(ProductoCocinaRequestDTO dto) {
        productoCocinaValidation.validarPost(dto);
        if (!dto.isSaltarConfirmacion()) {
            productoCocinaConfirmation.validarPost(dto);
        }
        Categoria categoria = categoriaService.findEntityById(dto.getIdCategoria());
        List<Subcategoria> subcategorias = resolverSubcategorias(dto.getIdSubcategorias(), categoria);

        ProductoCocina entidad = ProductoCocina.builder()
                .uuidProductoCocina(UUID.randomUUID().toString())
                .nombreProducto(dto.getNombreProducto())
                .descripcion(dto.getDescripcion())
                .precioDomicilio(dto.getPrecioDomicilio())
                .precioNormal(dto.getPrecioNormal())
                .estatus(dto.getEstatus())
                .destacado(dto.isDestacado())
                .categoria(categoria)
                .subcategorias(subcategorias)
                .build();
        return toResponseDTO(productoCocinaRepository.save(entidad));
    }

    @Transactional
    public ProductoCocinaResponseDTO update(int id, ProductoCocinaRequestDTO dto) {
        productoCocinaValidation.validarPost(dto);
        if (!dto.isSaltarConfirmacion()) {
            productoCocinaConfirmation.validarPost(dto);
        }
        ProductoCocina existente = findEntityById(id);
        Categoria categoria = categoriaService.findEntityById(dto.getIdCategoria());
        List<Subcategoria> subcategorias = resolverSubcategorias(dto.getIdSubcategorias(), categoria);

        existente.setNombreProducto(dto.getNombreProducto());
        existente.setDescripcion(dto.getDescripcion());
        existente.setPrecioDomicilio(dto.getPrecioDomicilio());
        existente.setPrecioNormal(dto.getPrecioNormal());
        existente.setEstatus(dto.getEstatus());
        existente.setDestacado(dto.isDestacado());
        existente.setCategoria(categoria);
        existente.setSubcategorias(subcategorias);
        return toResponseDTO(productoCocinaRepository.save(existente));
    }

    public void delete(int id) {
        if (!productoCocinaRepository.existsById(id)) {
            throw new BusinessException(
                    "Producto de cocina no encontrado con id: " + id, HttpStatus.NOT_FOUND);
        }
        // RF-014/017: bloquear eliminación si el producto tiene pedidos asociados
        if (productoCocinaRepository.existsEnPedidos(id)) {
            throw new BusinessException(
                    "Este producto no se puede eliminar ya que tiene pedidos asignados, puede deshabilitarlo mejor",
                    HttpStatus.CONFLICT);
        }
        productoCocinaRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public ProductoCocinaResponseDTO findById(int id) {
        // Hidrata subcategorias con LEFT JOIN FETCH para evitar N+1 al mapear el DTO
        ProductoCocina p = productoCocinaRepository.findByIdConSubcategorias(id)
                .orElseThrow(() -> new BusinessException(
                        "Producto de cocina no encontrado con id: " + id, HttpStatus.NOT_FOUND));
        return toResponseDTO(p);
    }

    public ProductoCocina findEntityById(int id) {
        return productoCocinaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Producto de cocina no encontrado con id: " + id, HttpStatus.NOT_FOUND));
    }

    /** Usado por el handler de archivos universal para validar existencia previa a subir. */
    public boolean existsById(int id) {
        return productoCocinaRepository.existsById(id);
    }

    @Transactional
    public ProductoCocinaResponseDTO toggleDestacado(int id) {
        ProductoCocina entidad = findEntityById(id);
        entidad.setDestacado(!entidad.isDestacado());
        return toResponseDTO(productoCocinaRepository.save(entidad));
    }

    /**
     * Resuelve las subcategorías por id validando: (a) todos los ids existen
     * (404 si alguno falta) y (b) todas pertenecen a la misma categoría del
     * producto (409 si mezclan otras).
     */
    private List<Subcategoria> resolverSubcategorias(List<Integer> ids, Categoria categoria) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        List<Subcategoria> subs = subcategoriaRepository.findAllById(ids);
        if (subs.size() != ids.size()) {
            throw new BusinessException(
                    "Alguna de las subcategorías indicadas no existe", HttpStatus.NOT_FOUND);
        }
        boolean todasDeMismaCategoria = subs.stream()
                .allMatch(s -> s.getCategoria().getIdCategoria().equals(categoria.getIdCategoria()));
        if (!todasDeMismaCategoria) {
            throw new BusinessException(
                    "Todas las subcategorías deben pertenecer a la categoría '"
                            + categoria.getNombre() + "'",
                    HttpStatus.CONFLICT);
        }
        return subs;
    }

    private ProductoCocinaResponseDTO toResponseDTO(ProductoCocina entidad) {
        Categoria cat = entidad.getCategoria();
        List<SubcategoriaResponseDTO> subDtos = entidad.getSubcategorias().stream()
                .map(this::toSubResponseDTO)
                .collect(Collectors.toList());
        return new ProductoCocinaResponseDTO(
                entidad.getIdProductoCocina(),
                entidad.getNombreProducto(),
                entidad.getDescripcion(),
                entidad.getPrecioDomicilio(),
                entidad.getPrecioNormal(),
                entidad.getEstatus(),
                entidad.isDestacado(),
                cat != null ? cat.getIdCategoria() : 0,
                cat != null ? cat.getNombre() : null,
                subDtos
        );
    }

    private SubcategoriaResponseDTO toSubResponseDTO(Subcategoria s) {
        return new SubcategoriaResponseDTO(
                s.getIdSubcategoria(),
                s.getNombre(),
                s.getCategoria().getIdCategoria(),
                s.getCategoria().getNombre());
    }
}
