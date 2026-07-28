package com.cocinarubi.presentation.strategy.strategyImplementation;

import com.cocinarubi.DBConstants;
import com.cocinarubi.dao.ComidaRepository;
import com.cocinarubi.presentation.strategy.BusquedaProductoStrategy;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Busca comidas DISPONIBLE cuyo nombre contenga el término.
 * Retorna List<Comida> — misma entidad que ComidaController.
 */
@Component
public class ComidaBusquedaImp implements BusquedaProductoStrategy {

    private final ComidaRepository comidaRepository;

    public ComidaBusquedaImp(ComidaRepository comidaRepository) {
        this.comidaRepository = comidaRepository;
    }

    @Override
    public String getNombreCategoria() {
        return "Comidas";
    }

    @Override
    public List<?> buscarTodos(String termino) {
        return comidaRepository.buscarDisponiblesPorNombre(termino, DBConstants.Estatus.DISPONIBLE);
    }
}
