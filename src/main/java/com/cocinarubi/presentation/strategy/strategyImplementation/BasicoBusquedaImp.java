package com.cocinarubi.presentation.strategy.strategyImplementation;

import com.cocinarubi.DBConstants;
import com.cocinarubi.dao.BasicoRepository;
import com.cocinarubi.domain.entity.Basico;
import com.cocinarubi.presentation.dto.response.BasicoResponseDTO;
import com.cocinarubi.presentation.dto.response.ComplementoResponseDTO;
import com.cocinarubi.presentation.strategy.BusquedaProductoStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Busca básicos DISPONIBLE cuyo nombre de comida contenga el término.
 * El JOIN FETCH del repositorio inicializa comida y complementos antes del mapeo.
 * Retorna List<BasicoResponseDTO> — mismo tipo que BasicoService.
 */
@Component
public class BasicoBusquedaImp implements BusquedaProductoStrategy {

    private final BasicoRepository basicoRepository;

    public BasicoBusquedaImp(BasicoRepository basicoRepository) {
        this.basicoRepository = basicoRepository;
    }

    @Override
    public String getNombreCategoria() {
        return "Básicos";
    }

    @Override
    public List<?> buscarTodos(String termino) {
        return basicoRepository
                .buscarDisponiblesPorNombreComida(termino, DBConstants.Estatus.DISPONIBLE)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    private BasicoResponseDTO toResponseDTO(Basico b) {
        List<ComplementoResponseDTO> complementos = b.getComplementos().stream()
                .map(bc -> new ComplementoResponseDTO(
                        bc.getComplemento().getIdComplemento(),
                        bc.getComplemento().getNombreComplemento(),
                        bc.getComplemento().getPrecioExtra()))
                .collect(Collectors.toList());
        return new BasicoResponseDTO(
                b.getIdBasico(),
                b.getUuidBasico(),
                b.getComida().getIdComida(),
                b.getComida().getNombreComida(),
                b.getDescripcion(),
                b.isDestacado(),
                b.getPrecioBasico(),
                b.getEstatus(),
                complementos
        );
    }
}
