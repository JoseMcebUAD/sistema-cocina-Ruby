package com.cocinarubi.domain.mapper;

import com.cocinarubi.DBConstants.TipoLineaCombo;
import com.cocinarubi.domain.entity.Combo;
import com.cocinarubi.domain.entity.ComboProducto;
import com.cocinarubi.presentation.dto.request.ComboLineaRequestDTO;
import com.cocinarubi.presentation.dto.request.ComboRequestDTO;
import com.cocinarubi.presentation.dto.response.ComboLineaResponseDTO;
import com.cocinarubi.presentation.dto.response.ComboResponseDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Convierte entidades del módulo Combo a sus DTOs de respuesta y viceversa.
 *
 * <p>El discriminador polimórfico ({@code tipoProducto} + {@code idProducto}) impide
 * navegar por relaciones JPA para resolver el nombre — el mapper recibe un mapa
 * pre-computado {@code nombresPorTipo} desde el service para evitar N+1.</p>
 *
 * <p>Capa: Mapper — sin lógica de negocio.</p>
 */
@Component
public class ComboMapper {

    /** Construye una nueva entidad Combo a partir del request; no persiste. */
    public Combo toEntity(ComboRequestDTO dto) {
        Combo combo = Combo.builder()
                .precio(dto.getPrecio())
                .descripcion(dto.getDescripcion())
                .destacado(dto.getDestacado())
                .estatus(dto.getEstatus())
                .build();
        for (ComboLineaRequestDTO linea : dto.getProductos()) {
            combo.addProducto(ComboProducto.builder()
                    .tipoProducto(linea.getTipoProducto())
                    .idProducto(linea.getIdProducto())
                    .cantidad(linea.getCantidad())
                    .build());
        }
        return combo;
    }

    /**
     * Mapea un Combo a su response DTO resolviendo los nombres de cada línea
     * desde el mapa pre-computado (una entrada por tipo → mapa id→nombre).
     */
    public ComboResponseDTO toResponse(Combo c,
                                       Map<TipoLineaCombo, Map<Integer, String>> nombresPorTipo) {
        List<ComboLineaResponseDTO> lineas = new ArrayList<>(c.getProductos().size());
        for (ComboProducto cp : c.getProductos()) {
            String nombre = nombresPorTipo
                    .getOrDefault(cp.getTipoProducto(), Map.of())
                    .getOrDefault(cp.getIdProducto(), "(eliminado)");
            lineas.add(new ComboLineaResponseDTO(
                    cp.getIdComboProducto(),
                    cp.getTipoProducto(),
                    cp.getIdProducto(),
                    nombre,
                    cp.getCantidad()));
        }
        return new ComboResponseDTO(
                c.getIdCombo(),
                c.getPrecio(),
                c.getDescripcion(),
                c.getEstatus(),
                c.isDestacado(),
                lineas);
    }

    /** Atajo cuando el llamador no tiene aún el mapa pre-computado (findById). */
    public ComboResponseDTO toResponse(Combo c) {
        return toResponse(c, new EnumMap<>(TipoLineaCombo.class));
    }
}
