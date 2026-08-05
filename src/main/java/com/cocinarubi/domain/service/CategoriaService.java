package com.cocinarubi.domain.service;

import com.cocinarubi.dao.CategoriaRepository;
import com.cocinarubi.dao.SubcategoriaRepository;
import com.cocinarubi.domain.entity.Categoria;
import com.cocinarubi.domain.entity.Subcategoria;
import com.cocinarubi.exception.BusinessException;
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
 * <p>Capa: Service — lógica de negocio de catálogo de productoCocina.</p>
 */
@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final SubcategoriaRepository subcategoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository,
                            SubcategoriaRepository subcategoriaRepository) {
        this.categoriaRepository = categoriaRepository;
        this.subcategoriaRepository = subcategoriaRepository;
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
    public CategoriaResponseDTO save(CategoriaRequestDTO dto) {
        String nombre = normalizar(dto.getNombre());
        validarNombreDisponible(nombre, null);
        Categoria entidad = Categoria.builder().nombre(nombre).build();
        return toResponseDTO(categoriaRepository.save(entidad));
    }

    @Transactional
    public CategoriaResponseDTO update(int id, CategoriaRequestDTO dto) {
        Categoria existente = findEntityById(id);
        String nombre = normalizar(dto.getNombre());
        validarNombreDisponible(nombre, id);
        existente.setNombre(nombre);
        return toResponseDTO(categoriaRepository.save(existente));
    }

    @Transactional
    public void delete(int id) {
        if (!categoriaRepository.existsById(id)) {
            throw new BusinessException(
                    "Categoría no encontrada con id: " + id, HttpStatus.NOT_FOUND);
        }
        // Fase 1: bloquea eliminación si existen subcategorías asociadas.
        // Fase 2 agregará la validación de productos asociados.
        if (subcategoriaRepository.countByCategoria_IdCategoria(id) > 0) {
            throw new BusinessException(
                    "No se puede eliminar la categoría porque tiene subcategorías asociadas",
                    HttpStatus.CONFLICT);
        }
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
