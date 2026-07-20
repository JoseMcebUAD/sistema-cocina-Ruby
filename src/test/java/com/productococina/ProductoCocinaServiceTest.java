package com.productococina;

import com.cocinarubi.DBConstants.Estatus;
import com.cocinarubi.DBConstants.TipoProducto;
import com.cocinarubi.dao.ProductoCocinaRepository;
import com.cocinarubi.domain.entity.ProductoCocina;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductoCocinaServiceTest {

    @Mock
    private ProductoCocinaRepository productoCocinaRepository;

    @Mock
    private ProductoCocinaValidationImp productoCocinaValidation;

    @Mock
    private ProductoCocinaConfirmationImp productoCocinaConfirmation;

    @InjectMocks
    private ProductoCocinaService productoCocinaService;

    public ProductoCocina PREPARED = ProductoCocina.builder()
            .idProductoCocina(10)
            .uuidProductoCocina("uuid-prod-cocina-10")
            .nombreProducto("Snack Test")
            .descripcion("Snack de prueba")
            .precioDomicilio(BigDecimal.valueOf(35.00))
            .precioNormal(BigDecimal.valueOf(25.00))
            .estatus(Estatus.DISPONIBLE)
            .destacado(false)
            .tipoProducto(TipoProducto.SNACK)
            .build();

    public ProductoCocinaRequestDTO DTO = crearDto("Snack Test", "Snack de prueba",
            BigDecimal.valueOf(35.00), BigDecimal.valueOf(25.00), Estatus.DISPONIBLE, false, TipoProducto.SNACK);

    public ProductoCocinaRequestDTO DTO_MODIFIED = crearDto("Snack Test Actualizado", "Descripción actualizada",
            BigDecimal.valueOf(40.00), BigDecimal.valueOf(30.00), Estatus.DISPONIBLE, true, TipoProducto.SNACK);

    private ProductoCocinaRequestDTO crearDto(String nombre, String descripcion, BigDecimal precioDomicilio,
                                               BigDecimal precioNormal, Estatus estatus, boolean destacado,
                                               TipoProducto tipo) {
        ProductoCocinaRequestDTO dto = new ProductoCocinaRequestDTO();
        dto.setNombreProducto(nombre);
        dto.setDescripcion(descripcion);
        dto.setPrecioDomicilio(precioDomicilio);
        dto.setPrecioNormal(precioNormal);
        dto.setEstatus(estatus);
        dto.setDestacado(destacado);
        dto.setTipoProducto(tipo);
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
        assertEquals(TipoProducto.SNACK, result.get(0).getTipoProducto());
        System.out.println("[OK] findAll retornó " + result.size() + " producto(s): " + result.get(0).getNombreProducto());
    }

    @Test
    @DisplayName("findById - Debe retornar el producto cuando el ID existe")
    public void findById_encontrado() {
        when(productoCocinaRepository.findById(10)).thenReturn(Optional.of(PREPARED));

        ProductoCocinaResponseDTO result = productoCocinaService.findById(10);

        assertNotNull(result);
        assertEquals(10, result.getIdProductoCocina());
        assertEquals("Snack Test", result.getNombreProducto());
        System.out.println("[OK] findById retornó producto id=" + result.getIdProductoCocina());
    }

    @Test
    @DisplayName("findById - Debe lanzar excepción cuando el ID no existe")
    public void findById_noEncontrado() {
        when(productoCocinaRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> productoCocinaService.findById(99));
        System.out.println("[OK] findById lanzó BusinessException para id=99");
    }

    @Test
    @DisplayName("save - Debe guardar y retornar el producto correctamente")
    public void save_exitoso() {
        when(productoCocinaRepository.save(any(ProductoCocina.class))).thenReturn(PREPARED);

        ProductoCocinaResponseDTO result = productoCocinaService.save(DTO);

        assertNotNull(result);
        assertEquals("Snack Test", result.getNombreProducto());
        assertEquals(TipoProducto.SNACK, result.getTipoProducto());
        verify(productoCocinaRepository).save(any(ProductoCocina.class));
        System.out.println("[OK] save guardó producto: " + result.getNombreProducto());
    }

    @Test
    @DisplayName("update - Debe actualizar y retornar el producto correctamente")
    public void update_exitoso() {
        when(productoCocinaRepository.findById(10)).thenReturn(Optional.of(PREPARED));
        ProductoCocina actualizado = ProductoCocina.builder()
                .idProductoCocina(10)
                .uuidProductoCocina("uuid-prod-cocina-10")
                .nombreProducto("Snack Test Actualizado")
                .descripcion("Descripción actualizada")
                .precioDomicilio(BigDecimal.valueOf(40.00))
                .precioNormal(BigDecimal.valueOf(30.00))
                .estatus(Estatus.DISPONIBLE)
                .destacado(true)
                .tipoProducto(TipoProducto.SNACK)
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
        when(productoCocinaRepository.countEnPedidos(10)).thenReturn(0);

        assertDoesNotThrow(() -> productoCocinaService.delete(10));
        verify(productoCocinaRepository).deleteById(10);
        System.out.println("[OK] delete eliminó producto id=10");
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción cuando el producto tiene pedidos asociados (RF-014/017)")
    public void delete_conPedidosAsociados() {
        when(productoCocinaRepository.existsById(10)).thenReturn(true);
        when(productoCocinaRepository.countEnPedidos(10)).thenReturn(3);

        assertThrows(BusinessException.class, () -> productoCocinaService.delete(10));
        verify(productoCocinaRepository, never()).deleteById(anyInt());
        System.out.println("[OK] delete lanzó CONFLICT por pedidos asociados (RF-014/017)");
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción cuando el ID no existe")
    public void delete_noEncontrado() {
        when(productoCocinaRepository.existsById(99)).thenReturn(false);

        assertThrows(BusinessException.class, () -> productoCocinaService.delete(99));
        verify(productoCocinaRepository, never()).deleteById(anyInt());
        System.out.println("[OK] delete lanzó BusinessException para id=99");
    }
}
