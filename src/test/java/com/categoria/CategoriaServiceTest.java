package com.categoria;

import com.cocinarubi.dao.CategoriaRepository;
import com.cocinarubi.dao.ProductoCocinaRepository;
import com.cocinarubi.dao.SubcategoriaRepository;
import com.cocinarubi.domain.entity.Categoria;
import com.cocinarubi.domain.entity.Subcategoria;
import com.cocinarubi.domain.service.CategoriaService;
import com.cocinarubi.domain.service.files.ArchivoModuloService;
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

    @Mock
    private ProductoCocinaRepository productoCocinaRepository;

    @Mock
    private ArchivoModuloService archivoModuloService;

    @InjectMocks
    private CategoriaService categoriaService;

    private final Categoria BEBIDA = Categoria.builder().idCategoria(1).nombre("BEBIDA").build();
    private final Categoria SNACK  = Categoria.builder().idCategoria(3).nombre("SNACK").build();

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
                .filter(c -> c.getIdCategoria() == 3).findFirst().orElseThrow();
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
    @DisplayName("save - guarda y dispara la creación del archivo_modulo asociado")
    public void save_exitoso_creaModulo() {
        when(categoriaRepository.existsByNombreIgnoreCase("POSTRE")).thenReturn(false);
        Categoria persisted = Categoria.builder().idCategoria(4).nombre("POSTRE").build();
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(persisted);

        CategoriaResponseDTO result = categoriaService.save(dto("POSTRE"));

        assertEquals(4, result.getIdCategoria());
        assertEquals("POSTRE", result.getNombre());
        verify(categoriaRepository).save(any(Categoria.class));
        verify(archivoModuloService).crearParaCategoria(persisted);
        System.out.println("[OK] save creó categoría y disparó crearParaCategoria");
    }

    @Test
    @DisplayName("save - lanza CONFLICT cuando el nombre ya existe (case-insensitive)")
    public void save_nombreDuplicado() {
        when(categoriaRepository.existsByNombreIgnoreCase("bebida")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> categoriaService.save(dto("bebida")));
        assertEquals(409, ex.getHttpStatus().value());
        verify(categoriaRepository, never()).save(any(Categoria.class));
        verify(archivoModuloService, never()).crearParaCategoria(any());
        System.out.println("[OK] save lanzó 409 por nombre duplicado");
    }

    @Test
    @DisplayName("update - cambia el nombre y sincroniza la ruta del módulo cuando el nombre difiere")
    public void update_exitoso_sincronizaModulo() {
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(BEBIDA));
        when(categoriaRepository.existsByNombreIgnoreCaseAndIdCategoriaNot("BEBIDAS", 1))
                .thenReturn(false);
        Categoria persisted = Categoria.builder().idCategoria(1).nombre("BEBIDAS").build();
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(persisted);

        CategoriaResponseDTO result = categoriaService.update(1, dto("BEBIDAS"));

        assertEquals("BEBIDAS", result.getNombre());
        verify(archivoModuloService).actualizarRutaParaCategoria(persisted);
        System.out.println("[OK] update cambió BEBIDA → BEBIDAS y actualizó módulo");
    }

    @Test
    @DisplayName("delete - bloquea con CONFLICT cuando la categoría tiene subcategorías")
    public void delete_conSubcategorias() {
        when(categoriaRepository.existsById(1)).thenReturn(true);
        when(subcategoriaRepository.existsByCategoria_IdCategoria(1)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> categoriaService.delete(1));
        assertEquals(409, ex.getHttpStatus().value());
        verify(categoriaRepository, never()).deleteById(anyInt());
        verify(archivoModuloService, never()).eliminarParaCategoria(anyInt());
        System.out.println("[OK] delete lanzó 409 por tener subcategorías");
    }

    @Test
    @DisplayName("delete - bloquea con CONFLICT cuando la categoría tiene productos asociados")
    public void delete_conProductos() {
        when(categoriaRepository.existsById(1)).thenReturn(true);
        when(subcategoriaRepository.existsByCategoria_IdCategoria(1)).thenReturn(false);
        when(productoCocinaRepository.existsByCategoria_IdCategoria(1)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> categoriaService.delete(1));
        assertEquals(409, ex.getHttpStatus().value());
        verify(categoriaRepository, never()).deleteById(anyInt());
        verify(archivoModuloService, never()).eliminarParaCategoria(anyInt());
        System.out.println("[OK] delete lanzó 409 por tener productos asociados");
    }

    @Test
    @DisplayName("delete - elimina cuando no hay subcategorías ni productos y limpia su módulo")
    public void delete_exitoso() {
        when(categoriaRepository.existsById(1)).thenReturn(true);
        when(subcategoriaRepository.existsByCategoria_IdCategoria(1)).thenReturn(false);
        when(productoCocinaRepository.existsByCategoria_IdCategoria(1)).thenReturn(false);

        assertDoesNotThrow(() -> categoriaService.delete(1));
        verify(archivoModuloService).eliminarParaCategoria(1);
        verify(categoriaRepository).deleteById(1);
        System.out.println("[OK] delete eliminó categoría id=1 y limpió módulo");
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
