package com.cocinarubi.presentation.strategy.strategyImplementation;

import com.cocinarubi.DBConstants;
import com.cocinarubi.dao.ProductoCocinaRepository;
import com.cocinarubi.domain.entity.Categoria;
import com.cocinarubi.domain.entity.ProductoCocina;
import com.cocinarubi.presentation.dto.response.ProductoCocinaResponseDTO;
import com.cocinarubi.presentation.strategy.BusquedaProductoStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Busca productos de cocina DISPONIBLE cuyo nombre contenga el término.
 * Retorna List<ProductoCocinaResponseDTO> — mismo tipo que ProductoCocinaController.
 */
@Component
public class ProductoCocinaBusquedaImp implements BusquedaProductoStrategy {

    private final ProductoCocinaRepository productoCocinaRepository;

    public ProductoCocinaBusquedaImp(ProductoCocinaRepository productoCocinaRepository) {
        this.productoCocinaRepository = productoCocinaRepository;
    }

    @Override
    public String getNombreCategoria() {
        return "Productos de Cocina";
    }

    @Override
    public List<?> buscarTodos(String termino) {
        return productoCocinaRepository
                .buscarDisponiblesPorNombre(termino, DBConstants.Estatus.DISPONIBLE)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    private ProductoCocinaResponseDTO toResponseDTO(ProductoCocina p) {
        Categoria cat = p.getCategoria();
        return new ProductoCocinaResponseDTO(
                p.getIdProductoCocina(),
                p.getNombreProducto(),
                p.getDescripcion(),
                p.getPrecioDomicilio(),
                p.getPrecioNormal(),
                p.getEstatus(),
                p.isDestacado(),
                cat != null ? cat.getIdCategoria() : 0,
                cat != null ? cat.getNombre() : null,
                // Búsqueda de catálogo no necesita subcategorías en el resultado
                List.of()
        );
    }
}
