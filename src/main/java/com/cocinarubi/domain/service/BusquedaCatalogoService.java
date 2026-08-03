package com.cocinarubi.domain.service;

import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.response.busqueda.ItemBusquedaResponseDTO;
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
import java.util.List;
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
     * Cada ítem del resultado incluye su categoría para que el frontend pueda distinguir el tipo.
     */
    @Transactional(readOnly = true)
    public ResultadoBusquedaResponseDTO buscar(String termino, int page, int size) {
        if (termino == null || termino.trim().length() < 3)
            throw new BusinessException("El término de búsqueda debe tener al menos 3 caracteres", HttpStatus.BAD_REQUEST);
        if (size < 1)
            throw new BusinessException("El tamaño de página debe ser al menos 1", HttpStatus.BAD_REQUEST);

        String t = termino.trim();

        // 1. Lista plana ordenada: todos los ítems de cada estrategia en secuencia
        List<ItemBusquedaResponseDTO> todos = new ArrayList<>();
        for (BusquedaProductoStrategy estrategia : estrategias) {
            String cat = estrategia.getNombreCategoria();
            estrategia.buscarTodos(t).forEach(item -> todos.add(new ItemBusquedaResponseDTO(cat, item)));
        }

        // 2. Paginación global: skip y limit sobre la lista plana
        long total = todos.size();
        int offset = page * size;
        List<ItemBusquedaResponseDTO> content = todos.stream()
                .skip(offset)
                .limit(size)
                .collect(Collectors.toList());

        // 3. Construir metadatos de paginación equivalentes a Spring Page
        int totalPaginas = (int) Math.ceil((double) total / size);
        boolean isFirst = page == 0;
        boolean isLast = totalPaginas == 0 || page >= totalPaginas - 1;

        ResultadoBusquedaResponseDTO.SortInfo sort =
                new ResultadoBusquedaResponseDTO.SortInfo(true, false, true);
        ResultadoBusquedaResponseDTO.PageableInfo pageable =
                new ResultadoBusquedaResponseDTO.PageableInfo(page, size, sort, (long) page * size, false, true);

        return new ResultadoBusquedaResponseDTO(
                content, pageable, total, totalPaginas,
                isFirst, isLast, size, page, sort,
                content.size(), content.isEmpty()
        );
    }
}
