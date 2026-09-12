package com.productococina;

import com.cocinarubi.DBConstants.Estatus;
import com.cocinarubi.dao.ProductoCocinaRepository;
import com.cocinarubi.dao.SubcategoriaRepository;
import com.cocinarubi.domain.entity.Categoria;
import com.cocinarubi.domain.entity.ProductoCocina;
import com.cocinarubi.domain.entity.Subcategoria;
import com.cocinarubi.domain.service.CategoriaService;
import com.cocinarubi.domain.service.ProductoCocinaService;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.ProductoCocinaRequestDTO;
import com.cocinarubi.presentation.dto.response.ProductoCocinaResponseDTO;
import com.cocinarubi.presentation.strategy.strategyImplementation.ProductoCocinaConfirmationImp;
import com.cocinarubi.presentation.strategy.strategyImplementation.ProductoCocinaValidationImp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductoCocinaServiceTest {

    @Mock
    private ProductoCocinaRepository productoCocinaRepository;

    @Mock
    private ProductoCocinaValidationImp productoCocinaValidation;

    @Mock
    private ProductoCocinaConfirmationImp productoCocinaConfirmation;

    @Mock
    private CategoriaService categoriaService;

    @Mock
    private SubcategoriaRepository subcategoriaRepository;

    @InjectMocks
    private ProductoCocinaService productoCocinaService;

    // Ids alineados con V23: BEBIDA=1, CHAROLA=2, SNACK=3, POSTRE=4
    private final Categoria SNACK  = Categoria.builder().idCategoria(3).nombre("SNACK").build();
    private final Categoria POSTRE = Categoria.builder().idCategoria(4).nombre("POSTRE").build();

    public ProductoCocina PREPARED = ProductoCocina.builder()
            .idProductoCocina(10)
            .uuidProductoCocina("uuid-prod-cocina-10")
            .nombreProducto("Snack Test")
            .descripcion("Snack de prueba")
            .precioDomicilio(BigDecimal.valueOf(35.00))
            .precioNormal(BigDecimal.valueOf(25.00))
            .estatus(Estatus.DISPONIBLE)
            .destacado(false)
            .categoria(SNACK)
            .build();

    public ProductoCocinaRequestDTO DTO = crearDto("Snack Test", "Snack de prueba",
            BigDecimal.valueOf(35.00), BigDecimal.valueOf(25.00), Estatus.DISPONIBLE, false, 3, List.of());

    public ProductoCocinaRequestDTO DTO_MODIFIED = crearDto("Snack Test Actualizado", "Descripción actualizada",
            BigDecimal.valueOf(40.00), BigDecimal.valueOf(30.00), Estatus.DISPONIBLE, true, 3, List.of());

    private ProductoCocinaRequestDTO crearDto(String nombre, String descripcion, BigDecimal precioDomicilio,
                                               BigDecimal precioNormal, Estatus estatus, boolean destacado,
                                               Integer idCategoria, List<Integer> idSubcategorias) {
        ProductoCocinaRequestDTO dto = new ProductoCocinaRequestDTO();
        dto.setNombreProducto(nombre);
        dto.setDescripcion(descripcion);
        dto.setPrecioDomicilio(precioDomicilio);
        dto.setPrecioNormal(precioNormal);
        dto.setEstatus(estatus);
        dto.setDestacado(destacado);
        dto.setIdCategoria(idCategoria);
        dto.setIdSubcategorias(idSubcategorias);
        dto.setSaltarConfirmacion(true);
        return dto;
    }

    @Test
    @DisplayName("findAll - Debe retornar la lista de productos de cocina registrados")
    public void findAll() {
        when(productoCocinaRepository.findAll()).thenReturn(List.of(PREPARED));

        List<ProductoCocinaResponseDTO> result = productoCocinaService.findAll();

        assertEquals(1, result.size());
        assertEquals("Snack Test", result.get(0).getNombreProducto());
        assertEquals("SNACK", result.get(0).getNombreCategoria());
        assertEquals(3, result.get(0).getIdCategoria());
        System.out.println("[OK] findAll retornó " + result.size() + " producto(s): " + result.get(0).getNombreProducto());
    }

    @Test
    @DisplayName("findById - Debe retornar el producto cuando el ID existe")
    public void findById_encontrado() {
        when(productoCocinaRepository.findByIdConSubcategorias(10)).thenReturn(Optional.of(PREPARED));

        ProductoCocinaResponseDTO result = productoCocinaService.findById(10);

        assertNotNull(result);
        assertEquals(10, result.getIdProductoCocina());
        assertEquals("Snack Test", result.getNombreProducto());
        System.out.println("[OK] findById retornó producto id=" + result.getIdProductoCocina());
    }

    @Test
    @DisplayName("findById - Debe lanzar excepción cuando el ID no existe")
    public void findById_noEncontrado() {
        when(productoCocinaRepository.findByIdConSubcategorias(99)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> productoCocinaService.findById(99));
        System.out.println("[OK] findById lanzó BusinessException para id=99");
    }

    @Test
    @DisplayName("save - Debe guardar sin subcategorías (0..N permitido)")
    public void save_exitoso_sinSubcategorias() {
        when(categoriaService.findEntityById(3)).thenReturn(SNACK);
        when(productoCocinaRepository.save(any(ProductoCocina.class))).thenReturn(PREPARED);

        ProductoCocinaResponseDTO result = productoCocinaService.save(DTO);

        assertNotNull(result);
        assertEquals("Snack Test", result.getNombreProducto());
        assertEquals("SNACK", result.getNombreCategoria());
        assertTrue(result.getSubcategorias().isEmpty());
        verify(productoCocinaRepository).save(any(ProductoCocina.class));
        System.out.println("[OK] save guardó producto sin subcategorías");
    }

    @Test
    @DisplayName("save - CONFLICT si alguna subcategoría pertenece a otra categoría")
    public void save_conflictSubcategoriaDeOtraCategoria() {
        Subcategoria subFueraDeCat = Subcategoria.builder()
                .idSubcategoria(99).categoria(POSTRE).nombre("Frío").build();
        ProductoCocinaRequestDTO dto = crearDto("Snack Test", "d",
                BigDecimal.valueOf(30), BigDecimal.valueOf(25), Estatus.DISPONIBLE, false, 3, List.of(99));

        when(categoriaService.findEntityById(3)).thenReturn(SNACK);
        when(subcategoriaRepository.findAllById(List.of(99))).thenReturn(List.of(subFueraDeCat));

        BusinessException ex = assertThrows(BusinessException.class, () -> productoCocinaService.save(dto));
        assertEquals(409, ex.getHttpStatus().value());
        verify(productoCocinaRepository, never()).save(any(ProductoCocina.class));
        System.out.println("[OK] save lanzó 409 por subcategoría de otra categoría");
    }

    @Test
    @DisplayName("update - Debe actualizar y retornar el producto correctamente")
    public void update_exitoso() {
        when(productoCocinaRepository.findById(10)).thenReturn(Optional.of(PREPARED));
        when(categoriaService.findEntityById(3)).thenReturn(SNACK);
        ProductoCocina actualizado = ProductoCocina.builder()
                .idProductoCocina(10)
                .uuidProductoCocina("uuid-prod-cocina-10")
                .nombreProducto("Snack Test Actualizado")
                .descripcion("Descripción actualizada")
                .precioDomicilio(BigDecimal.valueOf(40.00))
                .precioNormal(BigDecimal.valueOf(30.00))
                .estatus(Estatus.DISPONIBLE)
                .destacado(true)
                .categoria(SNACK)
                .build();
        when(productoCocinaRepository.save(any(ProductoCocina.class))).thenReturn(actualizado);

        ProductoCocinaResponseDTO result = productoCocinaService.update(10, DTO_MODIFIED);

        assertNotNull(result);
        assertEquals("Snack Test Actualizado", result.getNombreProducto());
        assertTrue(result.isDestacado());
        System.out.println("[OK] update actualizó producto: " + result.getNombreProducto());
    }

    @Test
    @DisplayName("delete - Debe eliminar el producto cuando el ID existe y no tiene pedidos")
    public void delete_exitoso() {
        when(productoCocinaRepository.existsById(10)).thenReturn(true);
        when(productoCocinaRepository.existsById(10)).thenReturn(false);

        assertDoesNotThrow(() -> productoCocinaService.delete(10,false));
        verify(productoCocinaRepository).deleteById(10);
        System.out.println("[OK] delete eliminó producto id=10");
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción cuando el producto tiene pedidos asociados (RF-014/017)")
    public void delete_conPedidosAsociados() {
        when(productoCocinaRepository.existsById(10)).thenReturn(true);
        when(productoCocinaRepository.existsById(10)).thenReturn(false);

        assertThrows(BusinessException.class, () -> productoCocinaService.delete(10,false));
        verify(productoCocinaRepository, never()).deleteById(anyInt());
        System.out.println("[OK] delete lanzó CONFLICT por pedidos asociados (RF-014/017)");
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción cuando el ID no existe")
    public void delete_noEncontrado() {
        when(productoCocinaRepository.existsById(99)).thenReturn(false);

        assertThrows(BusinessException.class, () -> productoCocinaService.delete(99,false));
        verify(productoCocinaRepository, never()).deleteById(anyInt());
        System.out.println("[OK] delete lanzó BusinessException para id=99");
    }
}
