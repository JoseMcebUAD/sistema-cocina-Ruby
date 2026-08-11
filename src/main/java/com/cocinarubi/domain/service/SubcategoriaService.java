package com.cocinarubi.domain.service;

import com.cocinarubi.dao.SubcategoriaRepository;
import com.cocinarubi.domain.entity.Categoria;
import com.cocinarubi.domain.entity.Subcategoria;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.SubcategoriaRequestDTO;
import com.cocinarubi.presentation.dto.response.SubcategoriaResponseDTO;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de dominio para {@link Subcategoria}. Toda subcategoría vive bajo una
 * {@link Categoria}: el service resuelve la FK vía {@link CategoriaService#findEntityById(int)}
 * y aplica unicidad de {@code nombre} dentro de la categoría.
 *
 * <p>Capa: Service — lógica de negocio del desglose fino de productoCocina.</p>
 */
@Service
public class SubcategoriaService {

    private final SubcategoriaRepository subcategoriaRepository;
    // CategoriaService: resuelve la entidad Categoria por id y lanza 404 si no existe.
    private final CategoriaService categoriaService;

    public SubcategoriaService(SubcategoriaRepository subcategoriaRepository,
                               CategoriaService categoriaService) {
        this.subcategoriaRepository = subcategoriaRepository;
        this.categoriaService = categoriaService;
    }

    @Transactional(readOnly = true)
    public List<SubcategoriaResponseDTO> findAll() {
        return subcategoriaRepository.findAll(Sort.by(Sort.Direction.ASC, "nombre")).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SubcategoriaResponseDTO findById(int id) {
        return toResponseDTO(findEntityById(id));
    }

    @Transactional
    public SubcategoriaResponseDTO save(SubcategoriaRequestDTO dto) {
        Categoria categoria = categoriaService.findEntityById(dto.getIdCategoria());
        String nombre = normalizar(dto.getNombre());
        validarNombreDisponible(categoria.getIdCategoria(), nombre, null);
        Subcategoria entidad = Subcategoria.builder()
                .categoria(categoria)
                .nombre(nombre)
                .build();
        return toResponseDTO(subcategoriaRepository.save(entidad));
    }

    @Transactional
    public SubcategoriaResponseDTO update(int id, SubcategoriaRequestDTO dto) {
        Subcategoria existente = findEntityById(id);
        Categoria categoria = categoriaService.findEntityById(dto.getIdCategoria());
        String nombre = normalizar(dto.getNombre());
        validarNombreDisponible(categoria.getIdCategoria(), nombre, id);
        existente.setCategoria(categoria);
        existente.setNombre(nombre);
        return toResponseDTO(subcategoriaRepository.save(existente));
    }


    @Transactional
    public void delete(int id) {
        if (!subcategoriaRepository.existsById(id)) {
            throw new BusinessException(
                    "Subcategoría no encontrada con id: " + id, HttpStatus.NOT_FOUND);
        }
        // Fase 2/3 agregará la validación de pedidos asociados.
        subcategoriaRepository.deleteById(id);
    }

    /** Utilidad interna para Fase 2 (tabla puente producto_cocina_subcategoria). */
    public Subcategoria findEntityById(int id) {
        return subcategoriaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Subcategoría no encontrada con id: " + id, HttpStatus.NOT_FOUND));
    }

    private void validarNombreDisponible(Integer idCategoria, String nombre, Integer idAExcluir) {
        boolean duplicado = idAExcluir == null
                ? subcategoriaRepository.existsByCategoria_IdCategoriaAndNombreIgnoreCase(
                        idCategoria, nombre)
                : subcategoriaRepository.existsByCategoria_IdCategoriaAndNombreIgnoreCaseAndIdSubcategoriaNot(
                        idCategoria, nombre, idAExcluir);
        if (duplicado) {
            throw new BusinessException(
                    "Ya existe una subcategoría con el nombre '" + nombre +
                            "' en esta categoría",
                    HttpStatus.CONFLICT);
        }
    }

    private String normalizar(String valor) {
        return valor == null ? null : valor.trim();
    }

    private SubcategoriaResponseDTO toResponseDTO(Subcategoria s) {
        return new SubcategoriaResponseDTO(
                s.getIdSubcategoria(),
                s.getNombre(),
                s.getCategoria().getIdCategoria(),
                s.getCategoria().getNombre());
    }
}
