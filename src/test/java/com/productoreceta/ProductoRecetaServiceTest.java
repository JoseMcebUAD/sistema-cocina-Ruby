package com.productoreceta;

import com.cocinarubi.DBConstants.TipoMedida;
import com.cocinarubi.dao.ComidaRepository;
import com.cocinarubi.dao.ProductoRecetaRepository;
import com.cocinarubi.dao.ProductoRepository;
import com.cocinarubi.domain.entity.Comida;
import com.cocinarubi.domain.entity.Producto;
import com.cocinarubi.domain.entity.ProductoReceta;
import com.cocinarubi.domain.mapper.ProductoRecetaMapper;
import com.cocinarubi.domain.service.ProductoRecetaService;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.ProductoRecetaRequestDTO;
import com.cocinarubi.presentation.dto.response.ProductoRecetaResponseDTO;
import com.cocinarubi.presentation.strategy.strategyImplementation.ProductoRecetaValidationImp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductoRecetaServiceTest {

    @Mock private ProductoRecetaRepository productoRecetaRepository;
    @Mock private ComidaRepository comidaRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private ProductoRecetaValidationImp productoRecetaValidation;

    // Mapper real: su cálculo del snapshot es parte del comportamiento a verificar.
    private final ProductoRecetaMapper productoRecetaMapper = new ProductoRecetaMapper();

    private ProductoRecetaService service() {
        return new ProductoRecetaService(
                productoRecetaRepository, comidaRepository, productoRepository,
                productoRecetaMapper, productoRecetaValidation);
    }

    private Comida comida10() {
        return Comida.builder().idComida(10).nombreComida("Caldo").porcionesBase(1).build();
    }

    private Producto sal() {
        return Producto.builder()
                .idProducto(1)
                .nombre("Sal fina")
                .tipoMedida(TipoMedida.PESO_EXACTO)
                .unidadBase("g")
                .contenidoPresentacion(new BigDecimal("500"))
                .unidadPresentacion("g")
                .costoPresentacion(new BigDecimal("25.00"))
                .costoUnitarioBase(new BigDecimal("0.0500"))
                .build();
    }

    private ProductoRecetaRequestDTO reqSal(int idProducto, String cantidad) {
        return ProductoRecetaRequestDTO.builder()
                .idProducto(idProducto)
                .cantidadUsada(new BigDecimal(cantidad))
                .unidadUsada("g")
                .esAproximado(false)
                .build();
    }

    @Test
    @DisplayName("findByIdComida - Debe lanzar 404 si la comida no existe")
    public void findByIdComida_comidaInexistente() {
        when(comidaRepository.existsById(99)).thenReturn(false);
        assertThrows(BusinessException.class, () -> service().findByIdComida(99));
    }

    @Test
    @DisplayName("findByIdComida - Debe retornar ingredientes con fetch join del producto")
    public void findByIdComida_ok() {
        when(comidaRepository.existsById(10)).thenReturn(true);
        ProductoReceta pr = ProductoReceta.builder()
                .idProductoReceta(100)
                .comida(comida10())
                .producto(sal())
                .cantidadUsada(new BigDecimal("650.000"))
                .unidadUsada("g")
                .esAproximado(false)
                .costoUnitario(new BigDecimal("0.0500"))
                .costoTotal(new BigDecimal("32.50"))
                .build();
        when(productoRecetaRepository.findByIdComidaWithProducto(10)).thenReturn(List.of(pr));

        List<ProductoRecetaResponseDTO> result = service().findByIdComida(10);

        assertEquals(1, result.size());
        assertEquals("Sal fina", result.get(0).getNombreProducto());
        assertEquals(0, new BigDecimal("32.50").compareTo(result.get(0).getCostoTotalSnapshot()));
    }

    @Test
    @DisplayName("addIngrediente - Debe persistir snapshot del costoUnitario y calcular costoTotal")
    public void addIngrediente_guardaSnapshot() {
        when(comidaRepository.findById(10)).thenReturn(Optional.of(comida10()));
        when(productoRepository.findById(1)).thenReturn(Optional.of(sal()));
        when(productoRecetaRepository.save(any(ProductoReceta.class))).thenAnswer(inv -> {
            ProductoReceta pr = inv.getArgument(0);
            pr.setIdProductoReceta(100);
            return pr;
        });

        ProductoRecetaResponseDTO res = service().addIngrediente(10, reqSal(1, "650"));

        // Snapshot debe ser el costoUnitarioBase vigente del producto (0.0500)
        assertEquals(0, new BigDecimal("0.0500").compareTo(res.getCostoUnitarioSnapshot()));
        // Total = 650 * 0.05 = 32.50
        assertEquals(0, new BigDecimal("32.50").compareTo(res.getCostoTotalSnapshot()));
        verify(productoRecetaValidation).validarPost(eq(10), any(ProductoRecetaRequestDTO.class));
    }

    @Test
    @DisplayName("addIngrediente - Debe propagar excepción si el producto no existe")
    public void addIngrediente_productoNoExiste() {
        // La strategy también validaría esto, pero verifico que el service maneje bien el 404 interno
        doNothing().when(productoRecetaValidation).validarPost(eq(10), any());
        when(comidaRepository.findById(10)).thenReturn(Optional.of(comida10()));
        when(productoRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> service().addIngrediente(10, reqSal(1, "650")));
    }

    @Test
    @DisplayName("updateIngrediente - Debe lanzar 400 si el ingrediente no pertenece a la comida")
    public void updateIngrediente_noPerteneceAEstaComida() {
        Comida otraComida = Comida.builder().idComida(99).porcionesBase(1).build();
        ProductoReceta pr = ProductoReceta.builder()
                .idProductoReceta(100)
                .comida(otraComida)
                .producto(sal())
                .cantidadUsada(new BigDecimal("650.000"))
                .unidadUsada("g")
                .costoUnitario(new BigDecimal("0.0500"))
                .costoTotal(new BigDecimal("32.50"))
                .build();
        when(productoRecetaRepository.findById(100)).thenReturn(Optional.of(pr));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service().updateIngrediente(10, 100, reqSal(1, "700")));
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("deleteIngrediente - Debe eliminar cuando pertenece a la comida")
    public void deleteIngrediente_ok() {
        ProductoReceta pr = ProductoReceta.builder()
                .idProductoReceta(100)
                .comida(comida10())
                .producto(sal())
                .cantidadUsada(new BigDecimal("650.000"))
                .unidadUsada("g")
                .costoUnitario(new BigDecimal("0.0500"))
                .costoTotal(new BigDecimal("32.50"))
                .build();
        when(productoRecetaRepository.findById(100)).thenReturn(Optional.of(pr));

        assertDoesNotThrow(() -> service().deleteIngrediente(10, 100));
        verify(productoRecetaRepository).deleteById(100);
    }
}
