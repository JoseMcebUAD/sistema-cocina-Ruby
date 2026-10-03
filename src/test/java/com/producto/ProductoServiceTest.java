package com.producto;

import com.cocinarubi.DBConstants.TipoMedida;
import com.cocinarubi.dao.ProductoRepository;
import com.cocinarubi.domain.entity.Producto;
import com.cocinarubi.domain.mapper.ProductoMapper;
import com.cocinarubi.domain.service.ProductoService;
import com.cocinarubi.exception.AdvertenciaEliminacionException;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.ProductoRequestDTO;
import com.cocinarubi.presentation.dto.response.ProductoResponseDTO;
import com.cocinarubi.presentation.strategy.strategyImplementation.ProductoValidationImp;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductoServiceTest {

    @Mock private ProductoRepository productoRepository;
    @Mock private ProductoValidationImp productoValidation;

    // Mapper real: sin lógica de negocio, derivar costos aquí mismo es exactamente lo que queremos verificar.
    private final ProductoMapper productoMapper = new ProductoMapper();

    private ProductoService service() {
        return new ProductoService(productoRepository, productoMapper, productoValidation);
    }

    private ProductoRequestDTO salDTO() {
        return ProductoRequestDTO.builder()
                .nombre("Sal fina")
                .tipoMedida(TipoMedida.PESO_EXACTO)
                .unidadBase("g")
                .contenidoPresentacion(new BigDecimal("500"))
                .unidadPresentacion("g")
                .costoPresentacion(new BigDecimal("25.00"))
                .build();
    }

    private Producto salEntity() {
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

    @Test
    @DisplayName("findAll - Debe retornar la lista de productos como DTOs")
    public void findAll() {
        when(productoRepository.findAll()).thenReturn(List.of(salEntity()));

        List<ProductoResponseDTO> result = service().findAll();

        assertEquals(1, result.size());
        assertEquals("Sal fina", result.get(0).getNombre());
        assertEquals(0, new BigDecimal("0.0500").compareTo(result.get(0).getCostoUnitarioBase()));
    }

    @Test
    @DisplayName("findById - Debe lanzar excepción cuando el ID no existe")
    public void findById_noEncontrado() {
        when(productoRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> service().findById(99));
    }

    @Test
    @DisplayName("save - Debe calcular costoUnitarioBase como costoPresentacion/contenidoPresentacion")
    public void save_calculaCostoUnitarioBase() {
        ProductoRequestDTO dto = salDTO();
        when(productoRepository.existsByNombreIgnoreCase("Sal fina")).thenReturn(false);
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> {
            Producto p = inv.getArgument(0);
            p.setIdProducto(1);
            return p;
        });

        ProductoResponseDTO res = service().save(dto);

        // 25 / 500 = 0.05 con escala 4
        assertEquals(0, new BigDecimal("0.0500").compareTo(res.getCostoUnitarioBase()));
        verify(productoValidation).validarPost(dto);
    }

    @Test
    @DisplayName("save - Debe lanzar conflicto cuando el nombre ya existe")
    public void save_nombreDuplicado() {
        ProductoRequestDTO dto = salDTO();
        when(productoRepository.existsByNombreIgnoreCase("Sal fina")).thenReturn(true);

        assertThrows(BusinessException.class, () -> service().save(dto));
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("update - Debe recalcular costoUnitarioBase cuando cambia contenido o costo")
    public void update_recalculaCostoUnitarioBase() {
        Producto existente = salEntity();
        when(productoRepository.findById(1)).thenReturn(Optional.of(existente));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductoRequestDTO dto = salDTO();
        dto.setContenidoPresentacion(new BigDecimal("1000"));
        dto.setCostoPresentacion(new BigDecimal("60.00"));

        ProductoResponseDTO res = service().update(1, dto);

        // 60 / 1000 = 0.06
        assertEquals(0, new BigDecimal("0.0600").compareTo(res.getCostoUnitarioBase()));
    }

    @Test
    @DisplayName("delete - Debe lanzar AdvertenciaEliminacionException cuando el producto está en receta y no se saltó la confirmación")
    public void delete_enReceta_sinSaltar() {
        when(productoRepository.existsById(1)).thenReturn(true);
        when(productoRepository.existsEnReceta(1)).thenReturn(true);

        assertThrows(AdvertenciaEliminacionException.class, () -> service().delete(1, false));
        verify(productoRepository, never()).deleteById(anyInt());
    }

    @Test
    @DisplayName("delete - Debe eliminar cuando se saltó la confirmación aunque esté en receta")
    public void delete_enReceta_conSaltar() {
        when(productoRepository.existsById(1)).thenReturn(true);

        assertDoesNotThrow(() -> service().delete(1, true));
        verify(productoRepository).deleteById(1);
    }

    @Test
    @DisplayName("delete - Debe eliminar producto sin referencias")
    public void delete_libre() {
        when(productoRepository.existsById(1)).thenReturn(true);
        when(productoRepository.existsEnReceta(1)).thenReturn(false);

        assertDoesNotThrow(() -> service().delete(1, false));
        verify(productoRepository).deleteById(1);
    }
}
