package com.cocinarubi.domain.service;

import com.cocinarubi.DBConstants.Estatus;
import com.cocinarubi.DBConstants.TipoLineaCombo;
import com.cocinarubi.dao.ComidaRepository;
import com.cocinarubi.dao.ComplementoRepository;
import com.cocinarubi.dao.ComboPedidoRepository;
import com.cocinarubi.dao.ComboRepository;
import com.cocinarubi.dao.DesayunoRepository;
import com.cocinarubi.dao.ProductoCocinaRepository;
import com.cocinarubi.domain.entity.Combo;
import com.cocinarubi.domain.entity.ComboProducto;
import com.cocinarubi.domain.entity.Comida;
import com.cocinarubi.domain.entity.Complemento;
import com.cocinarubi.domain.entity.Desayuno;
import com.cocinarubi.domain.entity.ProductoCocina;
import com.cocinarubi.domain.mapper.ComboMapper;
import com.cocinarubi.exception.AdvertenciaEliminacionException;
import org.springframework.cache.annotation.CacheEvict;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.ComboLineaRequestDTO;
import com.cocinarubi.presentation.dto.request.ComboRequestDTO;
import com.cocinarubi.presentation.dto.response.ComboResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio de dominio para {@link Combo}. Gestiona el CRUD del catálogo de
 * promociones y valida la integridad polimórfica de las líneas contra las tablas
 * maestras (comida/desayuno/complemento/producto_cocina).
 *
 * <p>Capa: Service — lógica de negocio de promociones.</p>
 */
@Service
public class ComboService {

    private final ComboRepository comboRepository;
    private final ComboPedidoRepository comboPedidoRepository;
    private final ComidaRepository comidaRepository;
    private final DesayunoRepository desayunoRepository;
    private final ComplementoRepository complementoRepository;
    private final ProductoCocinaRepository productoCocinaRepository;
    private final ComboMapper comboMapper;

    public ComboService(ComboRepository comboRepository,
                        ComboPedidoRepository comboPedidoRepository,
                        ComidaRepository comidaRepository,
                        DesayunoRepository desayunoRepository,
                        ComplementoRepository complementoRepository,
                        ProductoCocinaRepository productoCocinaRepository,
                        ComboMapper comboMapper) {
        this.comboRepository = comboRepository;
        this.comboPedidoRepository = comboPedidoRepository;
        this.comidaRepository = comidaRepository;
        this.desayunoRepository = desayunoRepository;
        this.complementoRepository = complementoRepository;
        this.productoCocinaRepository = productoCocinaRepository;
        this.comboMapper = comboMapper;
    }

    @Transactional(readOnly = true)
    public List<ComboResponseDTO> findAll() {
        List<Combo> combos = comboRepository.findAllWithProductos();
        Map<TipoLineaCombo, Map<Integer, String>> nombres = resolverNombres(combos);
        return combos.stream()
                .map(c -> comboMapper.toResponse(c, nombres))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ComboResponseDTO> findAllPaginado(Pageable pageable) {
        Page<Combo> pagina = comboRepository.findAllWithProductosPaginado(pageable);
        Map<TipoLineaCombo, Map<Integer, String>> nombres = resolverNombres(pagina.getContent());
        return pagina.map(c -> comboMapper.toResponse(c, nombres));
    }

    @Transactional(readOnly = true)
    public List<ComboResponseDTO> findDisponibles() {
        List<Combo> combos = comboRepository.findByEstatusWithProductos(Estatus.DISPONIBLE);
        Map<TipoLineaCombo, Map<Integer, String>> nombres = resolverNombres(combos);
        return combos.stream()
                .map(c -> comboMapper.toResponse(c, nombres))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ComboResponseDTO> findByEstatusPaginado(Estatus estatus, Pageable pageable) {
        Page<Combo> pagina = comboRepository.findByEstatusWithProductosPaginado(estatus, pageable);
        Map<TipoLineaCombo, Map<Integer, String>> nombres = resolverNombres(pagina.getContent());
        return pagina.map(c -> comboMapper.toResponse(c, nombres));
    }

    @Transactional(readOnly = true)
    public ComboResponseDTO findById(int id) {
        Combo combo = comboRepository.findByIdWithProductos(id)
                .orElseThrow(() -> new BusinessException(
                        "Combo no encontrado con id: " + id, HttpStatus.NOT_FOUND));
        Map<TipoLineaCombo, Map<Integer, String>> nombres = resolverNombres(List.of(combo));
        return comboMapper.toResponse(combo, nombres);
    }

    @Transactional
    @CacheEvict(value = "menu-web", allEntries = true)
    public ComboResponseDTO save(ComboRequestDTO dto) {
        validarLineas(dto.getProductos());
        Combo combo = comboMapper.toEntity(dto);
        Combo persistido = comboRepository.save(combo);
        Map<TipoLineaCombo, Map<Integer, String>> nombres = resolverNombres(List.of(persistido));
        return comboMapper.toResponse(persistido, nombres);
    }

    @Transactional
    @CacheEvict(value = "menu-web", allEntries = true)
    public ComboResponseDTO update(int id, ComboRequestDTO dto) {
        validarLineas(dto.getProductos());
        Combo existente = comboRepository.findByIdWithProductos(id)
                .orElseThrow(() -> new BusinessException(
                        "Combo no encontrado con id: " + id, HttpStatus.NOT_FOUND));
        existente.setPrecio(dto.getPrecio());
        existente.setDescripcion(dto.getDescripcion());
        existente.setEstatus(dto.getEstatus());
        existente.getProductos().clear();
        comboRepository.saveAndFlush(existente); // DELETE huérfanos antes del INSERT
        existente.setDestacado(dto.getDestacado());
        dto.getProductos().forEach(l -> existente.addProducto(ComboProducto.builder()
                .tipoProducto(l.getTipoProducto())
                .idProducto(l.getIdProducto())
                .cantidad(l.getCantidad())
                .build()));
        Combo actualizado = comboRepository.save(existente);
        Map<TipoLineaCombo, Map<Integer, String>> nombres = resolverNombres(List.of(actualizado));
        return comboMapper.toResponse(actualizado, nombres);
    }

    @Transactional
    public ComboResponseDTO toggleDestacado(int id) {
        Combo combo = comboRepository.findByIdWithProductos(id)
                .orElseThrow(() -> new BusinessException(
                        "Combo no encontrado con id: " + id, HttpStatus.NOT_FOUND));
        combo.setDestacado(!combo.isDestacado());
        Combo actualizado = comboRepository.save(combo);
        return comboMapper.toResponse(actualizado, resolverNombres(List.of(actualizado)));
    }

    @Transactional
    public ComboResponseDTO updateEstatus(int id, Estatus estatus) {
        Combo combo = comboRepository.findByIdWithProductos(id)
                .orElseThrow(() -> new BusinessException(
                        "Combo no encontrado con id: " + id, HttpStatus.NOT_FOUND));
        combo.setEstatus(estatus);
        Combo actualizado = comboRepository.save(combo);
        return comboMapper.toResponse(actualizado, resolverNombres(List.of(actualizado)));
    }

    @CacheEvict(value = "menu-web", allEntries = true)
    public void delete(int id, boolean saltarConfirmacion) {
        if (!comboRepository.existsById(id)) {
            throw new BusinessException(
                    "Combo no encontrado con id: " + id, HttpStatus.NOT_FOUND);
        }
        if (!saltarConfirmacion && comboPedidoRepository.existsByCombo_IdCombo(id)) {
            throw new AdvertenciaEliminacionException(
                    "Este combo forma parte de uno o más pedidos. ¿Desea continuar con la eliminación?");
        }
        comboRepository.deleteById(id);
    }

    /** Utilidad interna reusable por {@code CatalogoPedidoService} al agregar líneas de combo. */
    public Combo findEntityById(int id) {
        return comboRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Combo no encontrado con id: " + id, HttpStatus.NOT_FOUND));
    }

    /**
     * Resuelve los nombres de los productos que componen una lista de combos en un solo batch
     * (máx 4 queries, una por tipo). Usado por {@code PedidoMapper} para poblar el detalle del
     * ticket sin recorrer producto por producto.
     */
    public Map<TipoLineaCombo, Map<Integer, String>> resolverNombresPara(List<Combo> combos) {
        return resolverNombres(combos);
    }

    /**
     * Valida que todos los pares (tipo, id) de las líneas existan en su tabla maestra.
     * Agrupa por tipo y hace UNA consulta por tipo, comparando tamaños del set devuelto
     * contra el set solicitado para detectar faltantes.
     */
    private void validarLineas(List<ComboLineaRequestDTO> lineas) {
        Map<TipoLineaCombo, Set<Integer>> idsPorTipo = new EnumMap<>(TipoLineaCombo.class);
        for (ComboLineaRequestDTO linea : lineas) {
            idsPorTipo.computeIfAbsent(linea.getTipoProducto(), k -> new HashSet<>())
                    .add(linea.getIdProducto());
        }
        for (Map.Entry<TipoLineaCombo, Set<Integer>> e : idsPorTipo.entrySet()) {
            Set<Integer> encontrados = idsExistentes(e.getKey(), e.getValue());
            Set<Integer> faltantes = new HashSet<>(e.getValue());
            faltantes.removeAll(encontrados);
            if (!faltantes.isEmpty()) {
                throw new BusinessException(
                        "No existen los siguientes " + e.getKey() + " con id: " + faltantes,
                        HttpStatus.NOT_FOUND);
            }
        }
    }

    private Set<Integer> idsExistentes(TipoLineaCombo tipo, Set<Integer> ids) {
        List<Integer> lista = new ArrayList<>(ids);
        return switch (tipo) {
            case COMIDA -> comidaRepository.findAllById(lista).stream()
                    .map(Comida::getIdComida).collect(Collectors.toSet());
            case DESAYUNO -> desayunoRepository.findAllById(lista).stream()
                    .map(Desayuno::getIdDesayuno).collect(Collectors.toSet());
            case COMPLEMENTO -> complementoRepository.findAllById(lista).stream()
                    .map(Complemento::getIdComplemento).collect(Collectors.toSet());
            case PRODUCTO_COCINA -> productoCocinaRepository.findAllById(lista).stream()
                    .map(ProductoCocina::getIdProductoCocina).collect(Collectors.toSet());
        };
    }

    /**
     * Pre-computa los nombres de cada producto referenciado en las líneas para
     * evitar N+1 en la construcción del response. Máximo 4 queries (una por tipo).
     */
    private Map<TipoLineaCombo, Map<Integer, String>> resolverNombres(List<Combo> combos) {
        Map<TipoLineaCombo, Set<Integer>> idsPorTipo = new EnumMap<>(TipoLineaCombo.class);
        for (Combo c : combos) {
            for (ComboProducto cp : c.getProductos()) {
                idsPorTipo.computeIfAbsent(cp.getTipoProducto(), k -> new HashSet<>())
                        .add(cp.getIdProducto());
            }
        }
        Map<TipoLineaCombo, Map<Integer, String>> resultado = new EnumMap<>(TipoLineaCombo.class);
        for (Map.Entry<TipoLineaCombo, Set<Integer>> e : idsPorTipo.entrySet()) {
            resultado.put(e.getKey(), cargarNombres(e.getKey(), e.getValue()));
        }
        return resultado;
    }

    private Map<Integer, String> cargarNombres(TipoLineaCombo tipo, Set<Integer> ids) {
        List<Integer> lista = new ArrayList<>(ids);
        Map<Integer, String> mapa = new HashMap<>();
        switch (tipo) {
            case COMIDA -> comidaRepository.findAllById(lista)
                    .forEach(c -> mapa.put(c.getIdComida(), c.getNombreComida()));
            case DESAYUNO -> desayunoRepository.findAllById(lista)
                    .forEach(d -> mapa.put(d.getIdDesayuno(), d.getNombreDesayuno()));
            case COMPLEMENTO -> complementoRepository.findAllById(lista)
                    .forEach(c -> mapa.put(c.getIdComplemento(), c.getNombreComplemento()));
            case PRODUCTO_COCINA -> productoCocinaRepository.findAllById(lista)
                    .forEach(pc -> mapa.put(pc.getIdProductoCocina(), pc.getNombreProducto()));
        }
        return mapa;
    }
}
