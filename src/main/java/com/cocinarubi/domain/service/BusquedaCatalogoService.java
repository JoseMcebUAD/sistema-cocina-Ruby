package com.cocinarubi.domain.service;

import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.response.busqueda.CategoriaResultadoResponseDTO;
import com.cocinarubi.presentation.dto.response.busqueda.ResultadoBusquedaResponseDTO;
import com.cocinarubi.presentation.strategy.BusquedaProductoStrategy;
import com.cocinarubi.presentation.strategy.strategyImplementation.BasicoBusquedaImp;
import com.cocinarubi.presentation.strategy.strategyImplementation.ComidaBusquedaImp;
import com.cocinarubi.presentation.strategy.strategyImplementation.DesayunoBusquedaImp;
import com.cocinarubi.presentation.strategy.strategyImplementation.ProductoCocinaBusquedaImp;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Orquesta la búsqueda cross-catálogo ejecutando cada estrategia y aplicando
 * paginación global en memoria sobre la lista plana resultante.
 * Capa: Service — lógica de negocio de búsqueda.
 */
@Service
public class BusquedaCatalogoService {

    private final List<BusquedaProductoStrategy> estrategias;

    public BusquedaCatalogoService(ComidaBusquedaImp comidaBusqueda,
                                   DesayunoBusquedaImp desayunoBusqueda,
                                   BasicoBusquedaImp basicoBusqueda,
                                   ProductoCocinaBusquedaImp productoCocinasBusqueda) {
        // El orden define la secuencia: Comidas → Desayunos → Básicos → Productos de Cocina
        this.estrategias = List.of(comidaBusqueda, desayunoBusqueda, basicoBusqueda, productoCocinasBusqueda);
    }

    /**
     * Busca en todos los catálogos y aplica paginación global en memoria.
     * Las categorías sin resultados en la página solicitada no aparecen en la respuesta.
     */
    @Transactional(readOnly = true)
    public ResultadoBusquedaResponseDTO buscar(String termino, int page, int size) {
        if (termino == null || termino.trim().length() < 3)
            throw new BusinessException("El término de búsqueda debe tener al menos 3 caracteres", HttpStatus.BAD_REQUEST);
        if (size < 1)
            throw new BusinessException("El tamaño de página debe ser al menos 1", HttpStatus.BAD_REQUEST);

        String t = termino.trim();

        // 1. Lista plana ordenada: todos los ítems de cada estrategia en secuencia
        record Entrada(String categoria, Object dato) {}
        List<Entrada> todos = new ArrayList<>();
        for (BusquedaProductoStrategy estrategia : estrategias) {
            String cat = estrategia.getNombreCategoria();
            estrategia.buscarTodos(t).forEach(item -> todos.add(new Entrada(cat, item)));
        }

        // 2. Paginación global: skip y limit sobre la lista plana
        long total = todos.size();
        int offset = page * size;
        List<Entrada> pagina = todos.stream().skip(offset).limit(size).collect(Collectors.toList());

        // 3. Reagrupar los ítems de esta página por categoría (LinkedHashMap preserva el orden de inserción)
        Map<String, List<Object>> agrupado = new LinkedHashMap<>();
        for (Entrada e : pagina) {
            agrupado.computeIfAbsent(e.categoria(), k -> new ArrayList<>()).add(e.dato());
        }

        // 4. Construir la respuesta
        List<CategoriaResultadoResponseDTO> cats = agrupado.entrySet().stream()
                .map(e -> new CategoriaResultadoResponseDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        int totalPaginas = (int) Math.ceil((double) total / size);
        return new ResultadoBusquedaResponseDTO(cats, total, totalPaginas, page, size);
    }
}
