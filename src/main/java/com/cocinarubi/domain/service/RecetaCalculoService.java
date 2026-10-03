package com.cocinarubi.domain.service;

import com.cocinarubi.DBConstants.TipoMedida;
import com.cocinarubi.dao.ComidaRepository;
import com.cocinarubi.dao.ProductoRecetaRepository;
import com.cocinarubi.domain.entity.Comida;
import com.cocinarubi.domain.entity.Producto;
import com.cocinarubi.domain.entity.ProductoReceta;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.CalcularFaltanteRequestDTO;
import com.cocinarubi.presentation.dto.request.DisponibilidadIngredienteDTO;
import com.cocinarubi.presentation.dto.response.CalcularFaltanteResponseDTO;
import com.cocinarubi.presentation.dto.response.FaltanteIngredienteDTO;
import com.cocinarubi.presentation.dto.response.IngredienteCosteadoResponseDTO;
import com.cocinarubi.presentation.dto.response.RecetaCosteoResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Encapsula las fórmulas de costeo escalado y cálculo de faltante para la receta
 * de una comida. Fórmulas tomadas literalmente de RECETAS.md sección 3.
 *
 * <p>Usa el precio actual de {@link Producto} (no el snapshot persistido en
 * {@link ProductoReceta}), porque el usuario quiere saber el costo HOY.</p>
 *
 * <p>Capa: Service — lógica de negocio del módulo de recetas (sin persistir cambios).</p>
 */
@Service
public class RecetaCalculoService {

    private static final int SCALE_CANTIDAD = 4;
    private static final int SCALE_DINERO = 2;
    private static final int SCALE_FACTOR = 4;
    private static final RoundingMode MODE = RoundingMode.HALF_UP;

    private final ComidaRepository comidaRepository;
    private final ProductoRecetaRepository productoRecetaRepository;

    public RecetaCalculoService(ComidaRepository comidaRepository,
                                ProductoRecetaRepository productoRecetaRepository) {
        this.comidaRepository = comidaRepository;
        this.productoRecetaRepository = productoRecetaRepository;
    }

    /**
     * Costea la receta de una comida escalada a {@code porcionesDeseadas}.
     * Devuelve ingredientes con cantidad escalada, costo con precio actual y paquetes a comprar.
     */
    public RecetaCosteoResponseDTO costearReceta(int idComida, int porcionesDeseadas) {
        if (porcionesDeseadas <= 0) {
            throw new BusinessException(
                    "porcionesDeseadas debe ser > 0", HttpStatus.BAD_REQUEST);
        }
        Comida comida = comidaRepository.findById(idComida)
                .orElseThrow(() -> new BusinessException(
                        "Comida no encontrada con id: " + idComida, HttpStatus.NOT_FOUND));
        Integer porcionesBase = comida.getPorcionesBase();
        if (porcionesBase == null || porcionesBase <= 0) {
            throw new BusinessException(
                    "La comida " + idComida + " tiene porciones_base inválida", HttpStatus.BAD_REQUEST);
        }

        List<ProductoReceta> ingredientes =
                productoRecetaRepository.findByIdComidaWithProducto(idComida);

        BigDecimal factor = BigDecimal.valueOf(porcionesDeseadas)
                .divide(BigDecimal.valueOf(porcionesBase), SCALE_FACTOR, MODE);

        List<IngredienteCosteadoResponseDTO> filas = new ArrayList<>(ingredientes.size());
        BigDecimal costoTotalReceta = BigDecimal.ZERO.setScale(SCALE_DINERO, MODE);

        for (ProductoReceta pr : ingredientes) {
            IngredienteCosteadoResponseDTO fila = costearIngrediente(pr, factor);
            filas.add(fila);
            costoTotalReceta = costoTotalReceta.add(fila.getCostoTotal());
        }
        costoTotalReceta = costoTotalReceta.setScale(SCALE_DINERO, MODE);

        BigDecimal costoPorPorcion = costoTotalReceta
                .divide(BigDecimal.valueOf(porcionesDeseadas), SCALE_DINERO, MODE);

        return RecetaCosteoResponseDTO.builder()
                .idComida(comida.getIdComida())
                .nombreComida(comida.getNombreComida())
                .porcionesBase(porcionesBase)
                .porcionesDeseadas(porcionesDeseadas)
                .factorEscala(factor)
                .ingredientes(filas)
                .costoTotalReceta(costoTotalReceta)
                .costoPorPorcion(costoPorPorcion)
                .build();
    }

    /**
     * Calcula lo que falta para producir {@code porcionesDeseadas} dado el stock que
     * el usuario trae en el request. No persiste nada: todo se evalúa en memoria.
     */
    public CalcularFaltanteResponseDTO calcularFaltante(int idComida, CalcularFaltanteRequestDTO req) {
        RecetaCosteoResponseDTO costeo = costearReceta(idComida, req.getPorcionesDeseadas());

        Map<Integer, BigDecimal> disponiblesPorProducto = construirMapDisponibles(req.getDisponibles());
        Set<Integer> idsEnReceta = new HashSet<>();
        for (IngredienteCosteadoResponseDTO ing : costeo.getIngredientes()) {
            idsEnReceta.add(ing.getIdProducto());
        }
        for (Integer idDisponible : disponiblesPorProducto.keySet()) {
            if (!idsEnReceta.contains(idDisponible)) {
                throw new BusinessException(
                        "El producto " + idDisponible + " no pertenece a la receta de la comida " + idComida,
                        HttpStatus.BAD_REQUEST);
            }
        }

        List<ProductoReceta> ingredientes =
                productoRecetaRepository.findByIdComidaWithProducto(idComida);
        Map<Integer, ProductoReceta> prPorIdProducto = new HashMap<>();
        for (ProductoReceta pr : ingredientes) {
            prPorIdProducto.put(pr.getProducto().getIdProducto(), pr);
        }

        List<FaltanteIngredienteDTO> faltantes = new ArrayList<>(costeo.getIngredientes().size());
        BigDecimal costoTotalFaltante = BigDecimal.ZERO.setScale(SCALE_DINERO, MODE);
        boolean sePuedeProducir = true;

        for (IngredienteCosteadoResponseDTO ing : costeo.getIngredientes()) {
            BigDecimal necesaria = ing.getCantidadEscalada();
            BigDecimal disponible = disponiblesPorProducto
                    .getOrDefault(ing.getIdProducto(), BigDecimal.ZERO);
            BigDecimal faltanteRaw = necesaria.subtract(disponible);
            BigDecimal faltante = faltanteRaw.compareTo(BigDecimal.ZERO) > 0
                    ? faltanteRaw.setScale(SCALE_CANTIDAD, MODE)
                    : BigDecimal.ZERO.setScale(SCALE_CANTIDAD, MODE);

            ProductoReceta pr = prPorIdProducto.get(ing.getIdProducto());
            Producto producto = pr.getProducto();

            BigDecimal costoFaltante;
            Integer paquetes;
            if (faltante.compareTo(BigDecimal.ZERO) == 0) {
                costoFaltante = BigDecimal.ZERO.setScale(SCALE_DINERO, MODE);
                paquetes = 0;
            } else {
                sePuedeProducir = false;
                costoFaltante = calcularCostoFaltante(producto, faltante);
                paquetes = calcularPaquetes(producto, faltante);
            }
            costoTotalFaltante = costoTotalFaltante.add(costoFaltante);

            faltantes.add(FaltanteIngredienteDTO.builder()
                    .idProducto(ing.getIdProducto())
                    .nombreProducto(ing.getNombreProducto())
                    .cantidadNecesaria(necesaria)
                    .cantidadDisponible(disponible.setScale(SCALE_CANTIDAD, MODE))
                    .cantidadFaltante(faltante)
                    .costoFaltante(costoFaltante)
                    .paquetesACompar(paquetes)
                    .build());
        }

        return CalcularFaltanteResponseDTO.builder()
                .receta(costeo)
                .faltantes(faltantes)
                .costoTotalFaltante(costoTotalFaltante.setScale(SCALE_DINERO, MODE))
                .sePuedeProducir(sePuedeProducir)
                .build();
    }

    private IngredienteCosteadoResponseDTO costearIngrediente(ProductoReceta pr, BigDecimal factor) {
        Producto producto = pr.getProducto();
        BigDecimal cantidadEscalada = pr.getCantidadUsada()
                .multiply(factor)
                .setScale(SCALE_CANTIDAD, MODE);
        BigDecimal costoTotal = calcularCostoPorCantidad(producto, cantidadEscalada);
        Integer paquetes = calcularPaquetes(producto, cantidadEscalada);

        return IngredienteCosteadoResponseDTO.builder()
                .idProducto(producto.getIdProducto())
                .nombreProducto(producto.getNombre())
                .tipoMedida(producto.getTipoMedida())
                .cantidadBase(pr.getCantidadUsada())
                .cantidadEscalada(cantidadEscalada)
                .unidadUsada(pr.getUnidadUsada())
                .costoUnitarioActual(producto.getCostoUnitarioBase())
                .costoTotal(costoTotal)
                .paquetesACompar(paquetes)
                .esAproximado(pr.isEsAproximado())
                .build();
    }

    /**
     * Costo = cantidad * costoUnitarioBase. Si el producto es de pieza variable y la unidad
     * del uso es pieza, convierte piezas → gramos equivalentes antes de multiplicar.
     */
    private BigDecimal calcularCostoPorCantidad(Producto producto, BigDecimal cantidad) {
        BigDecimal equivalenteEnUnidadBase = convertirAUnidadBase(producto, cantidad);
        return equivalenteEnUnidadBase
                .multiply(producto.getCostoUnitarioBase())
                .setScale(SCALE_DINERO, MODE);
    }

    private BigDecimal calcularCostoFaltante(Producto producto, BigDecimal faltante) {
        return calcularCostoPorCantidad(producto, faltante);
    }

    /** Paquetes = CEILING(cantidad_en_unidad_base / contenido_presentacion). */
    private Integer calcularPaquetes(Producto producto, BigDecimal cantidad) {
        if (cantidad.compareTo(BigDecimal.ZERO) <= 0) return 0;
        BigDecimal equivalenteEnUnidadBase = convertirAUnidadBase(producto, cantidad);
        BigDecimal paquetesBD = equivalenteEnUnidadBase
                .divide(producto.getContenidoPresentacion(), 0, RoundingMode.CEILING);
        return paquetesBD.intValueExact();
    }

    /**
     * Si el producto es de pieza variable, la cantidad llega en piezas pero el costo y el
     * contenido_presentacion están en la unidad base del producto (gramos, ml, etc.).
     * Multiplicamos por peso_promedio_pieza para equiparar magnitudes.
     */
    private BigDecimal convertirAUnidadBase(Producto producto, BigDecimal cantidad) {
        if (producto.getTipoMedida() == TipoMedida.PIEZA_VARIABLE
                && producto.getPesoPromedioPieza() != null) {
            return cantidad.multiply(producto.getPesoPromedioPieza());
        }
        return cantidad;
    }

    private Map<Integer, BigDecimal> construirMapDisponibles(List<DisponibilidadIngredienteDTO> items) {
        Map<Integer, BigDecimal> map = new HashMap<>();
        if (items == null) return map;
        for (DisponibilidadIngredienteDTO d : items) {
            if (d.getCantidad().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(
                        "La cantidad disponible del producto " + d.getIdProducto() + " no puede ser negativa",
                        HttpStatus.BAD_REQUEST);
            }
            map.merge(d.getIdProducto(), d.getCantidad(), BigDecimal::add);
        }
        return map;
    }
}
