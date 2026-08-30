package com.cocinarubi.domain.mapper;

import com.cocinarubi.DBConstants.TipoLineaPaquete;
import com.cocinarubi.domain.entity.Paquete;
import com.cocinarubi.domain.entity.PaqueteProducto;
import com.cocinarubi.presentation.dto.request.PaqueteLineaRequestDTO;
import com.cocinarubi.presentation.dto.request.PaqueteRequestDTO;
import com.cocinarubi.presentation.dto.response.PaqueteLineaResponseDTO;
import com.cocinarubi.presentation.dto.response.PaqueteResponseDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Convierte entidades del módulo Paquete a sus DTOs de respuesta y viceversa.
 *
 * <p>El discriminador polimórfico ({@code tipoProducto} + {@code idProducto}) impide
 * navegar por relaciones JPA para resolver el nombre — el mapper recibe un mapa
 * pre-computado {@code nombresPorTipo} desde el service para evitar N+1.</p>
 *
 * <p>Capa: Mapper — sin lógica de negocio.</p>
 */
@Component
public class PaqueteMapper {

    /** Construye una nueva entidad Paquete a partir del request; no persiste. */
    public Paquete toEntity(PaqueteRequestDTO dto) {
        Paquete paquete = Paquete.builder()
                .precio(dto.getPrecio())
                .descripcion(dto.getDescripcion())
                .destacado(dto.getDestacado())
                .estatus(dto.getEstatus())
                .build();
        for (PaqueteLineaRequestDTO linea : dto.getProductos()) {
            paquete.addProducto(PaqueteProducto.builder()
                    .tipoProducto(linea.getTipoProducto())
                    .idProducto(linea.getIdProducto())
                    .cantidad(linea.getCantidad())
                    .build());
        }
        return paquete;
    }

    /**
     * Mapea un Paquete a su response DTO resolviendo los nombres de cada línea
     * desde el mapa pre-computado (una entrada por tipo → mapa id→nombre).
     */
    public PaqueteResponseDTO toResponse(Paquete p,
                                         Map<TipoLineaPaquete, Map<Integer, String>> nombresPorTipo) {
        List<PaqueteLineaResponseDTO> lineas = new ArrayList<>(p.getProductos().size());
        for (PaqueteProducto pp : p.getProductos()) {
            String nombre = nombresPorTipo
                    .getOrDefault(pp.getTipoProducto(), Map.of())
                    .getOrDefault(pp.getIdProducto(), "(eliminado)");
            lineas.add(new PaqueteLineaResponseDTO(
                    pp.getIdPaqueteProducto(),
                    pp.getTipoProducto(),
                    pp.getIdProducto(),
                    nombre,
                    pp.getCantidad()));
        }
        return new PaqueteResponseDTO(
                p.getIdPaquete(),
                p.getPrecio(),
                p.getDescripcion(),
                p.getEstatus(),
                p.isDestacado(),
                lineas);
    }

    /** Atajo cuando el llamador no tiene aún el mapa pre-computado (findById). */
    public PaqueteResponseDTO toResponse(Paquete p) {
        return toResponse(p, new EnumMap<>(TipoLineaPaquete.class));
    }
}
