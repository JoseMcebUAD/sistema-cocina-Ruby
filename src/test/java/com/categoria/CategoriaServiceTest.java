package com.categoria;

import com.cocinarubi.dao.CategoriaRepository;
import com.cocinarubi.dao.SubcategoriaRepository;
import com.cocinarubi.domain.entity.Categoria;
import com.cocinarubi.domain.entity.Subcategoria;
import com.cocinarubi.domain.service.CategoriaService;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.CategoriaRequestDTO;
import com.cocinarubi.presentation.dto.response.CategoriaResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private SubcategoriaRepository subcategoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    private final Categoria BEBIDA = Categoria.builder().idCategoria(1).nombre("BEBIDA").build();
    private final Categoria SNACK  = Categoria.builder().idCategoria(2).nombre("SNACK").build();

    private CategoriaRequestDTO dto(String nombre) {
        CategoriaRequestDTO d = new CategoriaRequestDTO();
        d.setNombre(nombre);
        return d;
    }

    @Test
    @DisplayName("findAll - retorna la lista de categorías")
    public void findAll() {
        when(categoriaRepository.findAll(any(Sort.class))).thenReturn(List.of(BEBIDA, SNACK));

        List<CategoriaResponseDTO> result = categoriaService.findAll();

        assertEquals(2, result.size());
        assertEquals("BEBIDA", result.get(0).getNombre());
        System.out.println("[OK] findAll retornó " + result.size() + " categorías");
    }

    @Test
    @DisplayName("findAllConSubcategorias - trae árbol vía LEFT JOIN FETCH con subcategorías hidratadas")
    public void findAllConSubcategorias() {
        Subcategoria sub1 = Subcategoria.builder()
                .idSubcategoria(10).categoria(BEBIDA).nombre("Fría").build();
        Subcategoria sub2 = Subcategoria.builder()
                .idSubcategoria(11).categoria(BEBIDA).nombre("Caliente").build();

        // Hidrata las subcategorías en las entidades
        BEBIDA.setSubcategorias(List.of(sub1, sub2));
        SNACK.setSubcategorias(List.of());

        when(categoriaRepository.findAllConSubcategorias())
                .thenReturn(List.of(BEBIDA, SNACK));

        List<CategoriaResponseDTO> arbol = categoriaService.findAllConSubcategorias();

        assertEquals(2, arbol.size());
        CategoriaResponseDTO bebida = arbol.stream()
                .filter(c -> c.getIdCategoria() == 1).findFirst().orElseThrow();
        assertEquals(2, bebida.getSubcategorias().size());

        CategoriaResponseDTO snack = arbol.stream()
                .filter(c -> c.getIdCategoria() == 2).findFirst().orElseThrow();
        assertTrue(snack.getSubcategorias().isEmpty());
        System.out.println("[OK] findAllConSubcategorias — BEBIDA con " +
                bebida.getSubcategorias().size() + " subs, SNACK sin subs");
    }

    @Test
    @DisplayName("findById - lanza BusinessException cuando no existe")
    public void findById_noEncontrado() {
        when(categoriaRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> categoriaService.findById(99));
        System.out.println("[OK] findById lanzó BusinessException para id=99");
    }

    @Test
    @DisplayName("save - guarda y retorna la categoría cuando el nombre no existe")
    public void save_exitoso() {
        when(categoriaRepository.existsByNombreIgnoreCase("POSTRE")).thenReturn(false);
        Categoria persisted = Categoria.builder().idCategoria(3).nombre("POSTRE").build();
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(persisted);

        CategoriaResponseDTO result = categoriaService.save(dto("POSTRE"));

        assertEquals(3, result.getIdCategoria());
        assertEquals("POSTRE", result.getNombre());
        verify(categoriaRepository).save(any(Categoria.class));
        System.out.println("[OK] save creó categoría id=" + result.getIdCategoria());
    }

    @Test
    @DisplayName("save - lanza CONFLICT cuando el nombre ya existe (case-insensitive)")
    public void save_nombreDuplicado() {
        when(categoriaRepository.existsByNombreIgnoreCase("bebida")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> categoriaService.save(dto("bebida")));
        assertEquals(409, ex.getHttpStatus().value());
        verify(categoriaRepository, never()).save(any(Categoria.class));
        System.out.println("[OK] save lanzó 409 por nombre duplicado");
    }

    @Test
    @DisplayName("update - actualiza el nombre respetando la unicidad excluyendo el propio id")
    public void update_exitoso() {
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(BEBIDA));
        when(categoriaRepository.existsByNombreIgnoreCaseAndIdCategoriaNot("BEBIDAS", 1))
                .thenReturn(false);
        Categoria persisted = Categoria.builder().idCategoria(1).nombre("BEBIDAS").build();
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(persisted);

        CategoriaResponseDTO result = categoriaService.update(1, dto("BEBIDAS"));

        assertEquals("BEBIDAS", result.getNombre());
        System.out.println("[OK] update cambió BEBIDA → BEBIDAS");
    }

    @Test
    @DisplayName("delete - bloquea con CONFLICT cuando la categoría tiene subcategorías")
    public void delete_conSubcategorias() {
        when(categoriaRepository.existsById(1)).thenReturn(true);
        when(subcategoriaRepository.countByCategoria_IdCategoria(1)).thenReturn(3L);

        BusinessException ex = assertThrows(BusinessException.class, () -> categoriaService.delete(1));
        assertEquals(409, ex.getHttpStatus().value());
        verify(categoriaRepository, never()).deleteById(anyInt());
        System.out.println("[OK] delete lanzó 409 por tener subcategorías");
    }

    @Test
    @DisplayName("delete - elimina cuando no hay subcategorías")
    public void delete_exitoso() {
        when(categoriaRepository.existsById(1)).thenReturn(true);
        when(subcategoriaRepository.countByCategoria_IdCategoria(1)).thenReturn(0L);

        assertDoesNotThrow(() -> categoriaService.delete(1));
        verify(categoriaRepository).deleteById(1);
        System.out.println("[OK] delete eliminó categoría id=1");
    }

    @Test
    @DisplayName("delete - lanza NOT_FOUND cuando el id no existe")
    public void delete_noEncontrado() {
        when(categoriaRepository.existsById(99)).thenReturn(false);
        BusinessException ex = assertThrows(BusinessException.class, () -> categoriaService.delete(99));
        assertEquals(404, ex.getHttpStatus().value());
        System.out.println("[OK] delete lanzó 404 para id=99");
    }
}
