package com.cocinarubi.domain.service;

import com.cocinarubi.dao.CategoriaRepository;
import com.cocinarubi.dao.ProductoCocinaRepository;
import com.cocinarubi.dao.SubcategoriaRepository;
import com.cocinarubi.domain.entity.Categoria;
import com.cocinarubi.domain.entity.Subcategoria;
import com.cocinarubi.domain.service.files.ArchivoModuloService;
import com.cocinarubi.exception.BusinessException;
import org.springframework.cache.annotation.CacheEvict;
import com.cocinarubi.presentation.dto.request.CategoriaRequestDTO;
import com.cocinarubi.presentation.dto.response.CategoriaResponseDTO;
import com.cocinarubi.presentation.dto.response.SubcategoriaResponseDTO;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de dominio para {@link Categoria}. CRUD + endpoint especial que arma
 * el árbol categoría → subcategorías para el catálogo de configuración.
 *
 * <p>Fase 2: cada POST /categoria dispara la creación de un {@code archivo_modulo}
 * asociado (vía {@link ArchivoModuloService}), y el DELETE se bloquea con 409 si
 * la categoría tiene subcategorías <b>o</b> productos_cocina asociados.</p>
 *
 * <p>Capa: Service — lógica de negocio de catálogo de productoCocina.</p>
 */
@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final SubcategoriaRepository subcategoriaRepository;
    private final ProductoCocinaRepository productoCocinaRepository;
    private final ArchivoModuloService archivoModuloService;

    public CategoriaService(CategoriaRepository categoriaRepository,
                            SubcategoriaRepository subcategoriaRepository,
                            ProductoCocinaRepository productoCocinaRepository,
                            ArchivoModuloService archivoModuloService) {
        this.categoriaRepository = categoriaRepository;
        this.subcategoriaRepository = subcategoriaRepository;
        this.productoCocinaRepository = productoCocinaRepository;
        this.archivoModuloService = archivoModuloService;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> findAll() {
        return categoriaRepository.findAll(Sort.by(Sort.Direction.ASC, "nombre")).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Devuelve el árbol categoría → subcategorías en una sola consulta
     * (LEFT JOIN FETCH ordenado en el repositorio).
     */
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> findAllConSubcategorias() {
        return categoriaRepository.findAllConSubcategorias().stream()
                .map(c -> new CategoriaResponseDTO(
                        c.getIdCategoria(),
                        c.getNombre(),
                        c.getSubcategorias().stream()
                                .map(this::toSubResponseDTO)
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoriaResponseDTO findById(int id) {
        return toResponseDTO(findEntityById(id));
    }

    @Transactional
    @CacheEvict(value = "menu-web", allEntries = true)
    public CategoriaResponseDTO save(CategoriaRequestDTO dto) {
        String nombre = normalizar(dto.getNombre());
        validarNombreDisponible(nombre, null);
        Categoria entidad = Categoria.builder().nombre(nombre).build();
        Categoria persistida = categoriaRepository.save(entidad);
        // Genera automáticamente la carpeta Cloudinary y la fila archivo_modulo
        // para que el usuario pueda subir imágenes de productos de esta categoría.
        archivoModuloService.crearParaCategoria(persistida);
        return toResponseDTO(persistida);
    }

    @Transactional
    @CacheEvict(value = "menu-web", allEntries = true)
    public CategoriaResponseDTO update(int id, CategoriaRequestDTO dto) {
        Categoria existente = findEntityById(id);
        String nombre = normalizar(dto.getNombre());
        validarNombreDisponible(nombre, id);
        boolean cambioNombre = !nombre.equalsIgnoreCase(existente.getNombre());
        existente.setNombre(nombre);
        Categoria actualizada = categoriaRepository.save(existente);
        if (cambioNombre) {
            // Ajusta la ruta del archivo_modulo (cocina_rubi/<nuevo-nombre-lower>)
            archivoModuloService.actualizarRutaParaCategoria(actualizada);
        }
        return toResponseDTO(actualizada);
    }

    @Transactional
    @CacheEvict(value = "menu-web", allEntries = true)
    public void delete(int id) {
        if (!categoriaRepository.existsById(id)) {
            throw new BusinessException(
                    "Categoría no encontrada con id: " + id, HttpStatus.NOT_FOUND);
        }
        // Bloquea si tiene subcategorías (evita orfandad) o productos asociados.
        if (subcategoriaRepository.existsByCategoria_IdCategoria(id)) {
            throw new BusinessException(
                    "No se puede eliminar la categoría porque tiene subcategorías asociadas",
                    HttpStatus.CONFLICT);
        }
        if (productoCocinaRepository.existsByCategoria_IdCategoria(id)) {
            throw new BusinessException(
                    "No se puede eliminar la categoría porque tiene productos de cocina asociados",
                    HttpStatus.CONFLICT);
        }
        // ArchivoModuloService borra la fila archivo_modulo; archivos hijos caen por CASCADE.
        archivoModuloService.eliminarParaCategoria(id);
        categoriaRepository.deleteById(id);
    }

    /** Utilidad interna reusada por {@link SubcategoriaService} para resolver la FK. */
    public Categoria findEntityById(int id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Categoría no encontrada con id: " + id, HttpStatus.NOT_FOUND));
    }

    private void validarNombreDisponible(String nombre, Integer idAExcluir) {
        boolean duplicado = idAExcluir == null
                ? categoriaRepository.existsByNombreIgnoreCase(nombre)
                : categoriaRepository.existsByNombreIgnoreCaseAndIdCategoriaNot(nombre, idAExcluir);
        if (duplicado) {
            throw new BusinessException(
                    "Ya existe una categoría con el nombre '" + nombre + "'",
                    HttpStatus.CONFLICT);
        }
    }

    private String normalizar(String valor) {
        return valor == null ? null : valor.trim();
    }

    private CategoriaResponseDTO toResponseDTO(Categoria c) {
        return new CategoriaResponseDTO(c.getIdCategoria(), c.getNombre());
    }

    private SubcategoriaResponseDTO toSubResponseDTO(Subcategoria s) {
        return new SubcategoriaResponseDTO(
                s.getIdSubcategoria(),
                s.getNombre(),
                s.getCategoria().getIdCategoria(),
                s.getCategoria().getNombre());
    }
}
