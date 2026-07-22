package com.cocinarubi.domain.service.impresion;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.cocinarubi.DBConstants.TipoEntidadImpresion;
import com.cocinarubi.exception.BusinessException;

/**
 * Registro de estrategias de impresión indexado por {@link TipoEntidadImpresion}.
 * Spring inyecta automáticamente todas las implementaciones de {@link ImpresionStrategy},
 * por lo que agregar un nuevo tipo requiere solo crear un nuevo {@code @Component}.
 */
@Component
public class ImpresionRegistry {

    private final Map<TipoEntidadImpresion, ImpresionStrategy> strategies;

    public ImpresionRegistry(List<ImpresionStrategy> strategies) {
        this.strategies = strategies.stream()
                .collect(Collectors.toMap(ImpresionStrategy::getTipo, Function.identity()));
    }

    public ImpresionStrategy get(TipoEntidadImpresion tipo) {
        return Optional.ofNullable(strategies.get(tipo))
                .orElseThrow(() -> new BusinessException(
                        "Sin estrategia de impresión para el tipo: " + tipo,
                        HttpStatus.BAD_REQUEST));
    }
}
