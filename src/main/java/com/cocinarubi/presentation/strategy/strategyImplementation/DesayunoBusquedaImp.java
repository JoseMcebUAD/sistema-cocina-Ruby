package com.cocinarubi.presentation.strategy.strategyImplementation;

import com.cocinarubi.DBConstants;
import com.cocinarubi.dao.DesayunoRepository;
import com.cocinarubi.presentation.strategy.BusquedaProductoStrategy;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Busca desayunos DISPONIBLE cuyo nombre contenga el término.
 * Retorna List<Desayuno> — misma entidad que DesayunoController.
 */
@Component
public class DesayunoBusquedaImp implements BusquedaProductoStrategy {

    private final DesayunoRepository desayunoRepository;

    public DesayunoBusquedaImp(DesayunoRepository desayunoRepository) {
        this.desayunoRepository = desayunoRepository;
    }

    @Override
    public String getNombreCategoria() {
        return "Desayunos";
    }

    @Override
    public List<?> buscarTodos(String termino) {
        return desayunoRepository.buscarDisponiblesPorNombre(termino, DBConstants.Estatus.DISPONIBLE);
    }
}
