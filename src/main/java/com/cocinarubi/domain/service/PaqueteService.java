package com.cocinarubi.domain.service;

import com.cocinarubi.DBConstants.Estatus;
import com.cocinarubi.DBConstants.TipoLineaPaquete;
import com.cocinarubi.dao.ComidaRepository;
import com.cocinarubi.dao.ComplementoRepository;
import com.cocinarubi.dao.DesayunoRepository;
import com.cocinarubi.dao.PaquetePedidoRepository;
import com.cocinarubi.dao.PaqueteRepository;
import com.cocinarubi.dao.ProductoCocinaRepository;
import com.cocinarubi.domain.entity.Comida;
import com.cocinarubi.domain.entity.Complemento;
import com.cocinarubi.domain.entity.Desayuno;
import com.cocinarubi.domain.entity.Paquete;
import com.cocinarubi.domain.entity.PaqueteProducto;
import com.cocinarubi.domain.entity.ProductoCocina;
import com.cocinarubi.domain.mapper.PaqueteMapper;
import com.cocinarubi.exception.AdvertenciaEliminacionException;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.PaqueteLineaRequestDTO;
import com.cocinarubi.presentation.dto.request.PaqueteRequestDTO;
import com.cocinarubi.presentation.dto.response.PaqueteResponseDTO;
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
 * Servicio de dominio para {@link Paquete}. Gestiona el CRUD del catálogo de
 * promociones y valida la integridad polimórfica de las líneas contra las tablas
 * maestras (comida/desayuno/complemento/producto_cocina).
 *
 * <p>Capa: Service — lógica de negocio de promociones.</p>
 */
@Service
public class PaqueteService {

    private final PaqueteRepository paqueteRepository;
    private final PaquetePedidoRepository paquetePedidoRepository;
    private final ComidaRepository comidaRepository;
    private final DesayunoRepository desayunoRepository;
    private final ComplementoRepository complementoRepository;
    private final ProductoCocinaRepository productoCocinaRepository;
    private final PaqueteMapper paqueteMapper;

    public PaqueteService(PaqueteRepository paqueteRepository,
                          PaquetePedidoRepository paquetePedidoRepository,
                          ComidaRepository comidaRepository,
                          DesayunoRepository desayunoRepository,
                          ComplementoRepository complementoRepository,
                          ProductoCocinaRepository productoCocinaRepository,
                          PaqueteMapper paqueteMapper) {
        this.paqueteRepository = paqueteRepository;
        this.paquetePedidoRepository = paquetePedidoRepository;
        this.comidaRepository = comidaRepository;
        this.desayunoRepository = desayunoRepository;
        this.complementoRepository = complementoRepository;
        this.productoCocinaRepository = productoCocinaRepository;
        this.paqueteMapper = paqueteMapper;
    }

    @Transactional(readOnly = true)
    public List<PaqueteResponseDTO> findAll() {
        List<Paquete> paquetes = paqueteRepository.findAllWithProductos();
        Map<TipoLineaPaquete, Map<Integer, String>> nombres = resolverNombres(paquetes);
        return paquetes.stream()
                .map(p -> paqueteMapper.toResponse(p, nombres))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<PaqueteResponseDTO> findAllPaginado(Pageable pageable) {
        Page<Paquete> pagina = paqueteRepository.findAllWithProductosPaginado(pageable);
        Map<TipoLineaPaquete, Map<Integer, String>> nombres = resolverNombres(pagina.getContent());
        return pagina.map(p -> paqueteMapper.toResponse(p, nombres));
    }

    @Transactional(readOnly = true)
    public Page<PaqueteResponseDTO> findByEstatusPaginado(Estatus estatus, Pageable pageable) {
        Page<Paquete> pagina = paqueteRepository.findByEstatusWithProductosPaginado(estatus, pageable);
        Map<TipoLineaPaquete, Map<Integer, String>> nombres = resolverNombres(pagina.getContent());
        return pagina.map(p -> paqueteMapper.toResponse(p, nombres));
    }

    @Transactional(readOnly = true)
    public PaqueteResponseDTO findById(int id) {
        Paquete paquete = paqueteRepository.findByIdWithProductos(id)
                .orElseThrow(() -> new BusinessException(
                        "Paquete no encontrado con id: " + id, HttpStatus.NOT_FOUND));
        Map<TipoLineaPaquete, Map<Integer, String>> nombres = resolverNombres(List.of(paquete));
        return paqueteMapper.toResponse(paquete, nombres);
    }

    @Transactional
    public PaqueteResponseDTO save(PaqueteRequestDTO dto) {
        validarLineas(dto.getProductos());
        Paquete paquete = paqueteMapper.toEntity(dto);
        Paquete persistido = paqueteRepository.save(paquete);
        Map<TipoLineaPaquete, Map<Integer, String>> nombres = resolverNombres(List.of(persistido));
        return paqueteMapper.toResponse(persistido, nombres);
    }

    @Transactional
    public PaqueteResponseDTO update(int id, PaqueteRequestDTO dto) {
        validarLineas(dto.getProductos());
        Paquete existente = paqueteRepository.findByIdWithProductos(id)
                .orElseThrow(() -> new BusinessException(
                        "Paquete no encontrado con id: " + id, HttpStatus.NOT_FOUND));
        existente.setPrecio(dto.getPrecio());
        existente.setDescripcion(dto.getDescripcion());
        existente.setEstatus(dto.getEstatus());
        existente.getProductos().clear();
        existente.setDestacado(dto.getDestacado());
        dto.getProductos().forEach(l -> existente.addProducto(PaqueteProducto.builder()
                .tipoProducto(l.getTipoProducto())
                .idProducto(l.getIdProducto())
                .cantidad(l.getCantidad())
                .build()));
        Paquete actualizado = paqueteRepository.save(existente);
        Map<TipoLineaPaquete, Map<Integer, String>> nombres = resolverNombres(List.of(actualizado));
        return paqueteMapper.toResponse(actualizado, nombres);
    }

    @Transactional
    public void delete(int id, boolean saltarConfirmacion) {
        if (!paqueteRepository.existsById(id)) {
            throw new BusinessException(
                    "Paquete no encontrado con id: " + id, HttpStatus.NOT_FOUND);
        }
        if (!saltarConfirmacion && paquetePedidoRepository.existsByPaquete_IdPaquete(id)) {
            throw new AdvertenciaEliminacionException(
                    "Este paquete forma parte de uno o más pedidos. ¿Desea continuar con la eliminación?");
        }
        paqueteRepository.deleteById(id);
    }

    /** Utilidad interna reusable por {@code CatalogoPedidoService} al agregar líneas de paquete. */
    public Paquete findEntityById(int id) {
        return paqueteRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Paquete no encontrado con id: " + id, HttpStatus.NOT_FOUND));
    }

    /**
     * Resuelve los nombres de los productos que componen una lista de paquetes en un solo batch
     * (máx 4 queries, una por tipo). Usado por {@code PedidoMapper} para poblar el detalle del
     * ticket sin recorrer producto por producto.
     */
    public Map<TipoLineaPaquete, Map<Integer, String>> resolverNombresPara(List<Paquete> paquetes) {
        return resolverNombres(paquetes);
    }

    /**
     * Valida que todos los pares (tipo, id) de las líneas existan en su tabla maestra.
     * Agrupa por tipo y hace UNA consulta por tipo, comparando tamaños del set devuelto
     * contra el set solicitado para detectar faltantes.
     */
    private void validarLineas(List<PaqueteLineaRequestDTO> lineas) {
        Map<TipoLineaPaquete, Set<Integer>> idsPorTipo = new EnumMap<>(TipoLineaPaquete.class);
        for (PaqueteLineaRequestDTO linea : lineas) {
            idsPorTipo.computeIfAbsent(linea.getTipoProducto(), k -> new HashSet<>())
                    .add(linea.getIdProducto());
        }
        for (Map.Entry<TipoLineaPaquete, Set<Integer>> e : idsPorTipo.entrySet()) {
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

    private Set<Integer> idsExistentes(TipoLineaPaquete tipo, Set<Integer> ids) {
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
    private Map<TipoLineaPaquete, Map<Integer, String>> resolverNombres(List<Paquete> paquetes) {
        Map<TipoLineaPaquete, Set<Integer>> idsPorTipo = new EnumMap<>(TipoLineaPaquete.class);
        for (Paquete p : paquetes) {
            for (PaqueteProducto pp : p.getProductos()) {
                idsPorTipo.computeIfAbsent(pp.getTipoProducto(), k -> new HashSet<>())
                        .add(pp.getIdProducto());
            }
        }
        Map<TipoLineaPaquete, Map<Integer, String>> resultado = new EnumMap<>(TipoLineaPaquete.class);
        for (Map.Entry<TipoLineaPaquete, Set<Integer>> e : idsPorTipo.entrySet()) {
            resultado.put(e.getKey(), cargarNombres(e.getKey(), e.getValue()));
        }
        return resultado;
    }

    private Map<Integer, String> cargarNombres(TipoLineaPaquete tipo, Set<Integer> ids) {
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
