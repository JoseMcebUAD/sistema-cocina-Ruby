package com.subcategoria;

import com.cocinarubi.dao.SubcategoriaRepository;
import com.cocinarubi.domain.entity.Categoria;
import com.cocinarubi.domain.entity.Subcategoria;
import com.cocinarubi.domain.service.CategoriaService;
import com.cocinarubi.domain.service.SubcategoriaService;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.SubcategoriaRequestDTO;
import com.cocinarubi.presentation.dto.response.SubcategoriaResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubcategoriaServiceTest {

    @Mock
    private SubcategoriaRepository subcategoriaRepository;

    @Mock
    private CategoriaService categoriaService;

    @InjectMocks
    private SubcategoriaService subcategoriaService;

    private final Categoria BEBIDA = Categoria.builder().idCategoria(1).nombre("BEBIDA").build();
    private final Categoria SNACK  = Categoria.builder().idCategoria(2).nombre("SNACK").build();

    private final Subcategoria FRIA = Subcategoria.builder()
            .idSubcategoria(10).categoria(BEBIDA).nombre("Fría").build();

    private SubcategoriaRequestDTO dto(String nombre, Integer idCategoria) {
        SubcategoriaRequestDTO d = new SubcategoriaRequestDTO();
        d.setNombre(nombre);
        d.setIdCategoria(idCategoria);
        return d;
    }

    @Test
    @DisplayName("findAll - retorna la lista mapeada a DTO con nombreCategoria")
    public void findAll() {
        when(subcategoriaRepository.findAll(any(Sort.class))).thenReturn(List.of(FRIA));

        List<SubcategoriaResponseDTO> result = subcategoriaService.findAll();

        assertEquals(1, result.size());
        assertEquals("Fría", result.get(0).getNombre());
        assertEquals("BEBIDA", result.get(0).getNombreCategoria());
        System.out.println("[OK] findAll retornó subcategoría con nombreCategoria=" +
                result.get(0).getNombreCategoria());
    }

    @Test
    @DisplayName("findById - lanza NOT_FOUND cuando no existe")
    public void findById_noEncontrado() {
        when(subcategoriaRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> subcategoriaService.findById(99));
        System.out.println("[OK] findById lanzó 404 para id=99");
    }

    @Test
    @DisplayName("save - guarda cuando la categoría existe y el nombre es único en la categoría")
    public void save_exitoso() {
        when(categoriaService.findEntityById(1)).thenReturn(BEBIDA);
        when(subcategoriaRepository.existsByCategoria_IdCategoriaAndNombreIgnoreCase(1, "Fría"))
                .thenReturn(false);
        when(subcategoriaRepository.save(any(Subcategoria.class))).thenReturn(FRIA);

        SubcategoriaResponseDTO result = subcategoriaService.save(dto("Fría", 1));

        assertEquals(10, result.getIdSubcategoria());
        assertEquals("Fría", result.getNombre());
        assertEquals(1, result.getIdCategoria());
        System.out.println("[OK] save creó subcategoría id=" + result.getIdSubcategoria());
    }

    @Test
    @DisplayName("save - propaga NOT_FOUND cuando la categoría no existe")
    public void save_categoriaInexistente() {
        when(categoriaService.findEntityById(99))
                .thenThrow(new BusinessException("Categoría no encontrada con id: 99", HttpStatus.NOT_FOUND));

        assertThrows(BusinessException.class, () -> subcategoriaService.save(dto("Fría", 99)));
        verify(subcategoriaRepository, never()).save(any(Subcategoria.class));
        System.out.println("[OK] save propagó 404 cuando la categoría no existe");
    }

    @Test
    @DisplayName("save - lanza CONFLICT cuando el nombre ya existe en la misma categoría")
    public void save_nombreDuplicadoEnCategoria() {
        when(categoriaService.findEntityById(1)).thenReturn(BEBIDA);
        when(subcategoriaRepository.existsByCategoria_IdCategoriaAndNombreIgnoreCase(1, "fría"))
                .thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> subcategoriaService.save(dto("fría", 1)));
        assertEquals(409, ex.getHttpStatus().value());
        verify(subcategoriaRepository, never()).save(any(Subcategoria.class));
        System.out.println("[OK] save lanzó 409 por nombre duplicado en la misma categoría");
    }

    @Test
    @DisplayName("update - permite cambiar de categoría respetando la unicidad excluyendo el propio id")
    public void update_cambioDeCategoria() {
        Subcategoria existente = Subcategoria.builder()
                .idSubcategoria(10).categoria(BEBIDA).nombre("Fría").build();
        when(subcategoriaRepository.findById(10)).thenReturn(Optional.of(existente));
        when(categoriaService.findEntityById(2)).thenReturn(SNACK);
        when(subcategoriaRepository
                .existsByCategoria_IdCategoriaAndNombreIgnoreCaseAndIdSubcategoriaNot(2, "Salado", 10))
                .thenReturn(false);
        when(subcategoriaRepository.save(any(Subcategoria.class))).thenAnswer(inv -> inv.getArgument(0));

        SubcategoriaResponseDTO result = subcategoriaService.update(10, dto("Salado", 2));

        assertEquals("Salado", result.getNombre());
        assertEquals(2, result.getIdCategoria());
        assertEquals("SNACK", result.getNombreCategoria());
        System.out.println("[OK] update movió la subcategoría de BEBIDA a SNACK con nombre 'Salado'");
    }

    @Test
    @DisplayName("delete - elimina cuando existe (sin validación de pedidos en Fase 1)")
    public void delete_exitoso() {
        when(subcategoriaRepository.existsById(10)).thenReturn(true);

        assertDoesNotThrow(() -> subcategoriaService.delete(10));
        verify(subcategoriaRepository).deleteById(10);
        System.out.println("[OK] delete eliminó subcategoría id=10");
    }

    @Test
    @DisplayName("delete - lanza NOT_FOUND cuando el id no existe")
    public void delete_noEncontrado() {
        when(subcategoriaRepository.existsById(99)).thenReturn(false);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> subcategoriaService.delete(99));
        assertEquals(404, ex.getHttpStatus().value());
        System.out.println("[OK] delete lanzó 404 para id=99");
    }
}
