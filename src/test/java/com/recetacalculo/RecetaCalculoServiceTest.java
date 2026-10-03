package com.recetacalculo;

import com.cocinarubi.DBConstants.TipoMedida;
import com.cocinarubi.dao.ComidaRepository;
import com.cocinarubi.dao.ProductoRecetaRepository;
import com.cocinarubi.domain.entity.Comida;
import com.cocinarubi.domain.entity.Producto;
import com.cocinarubi.domain.entity.ProductoReceta;
import com.cocinarubi.domain.service.RecetaCalculoService;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.CalcularFaltanteRequestDTO;
import com.cocinarubi.presentation.dto.request.DisponibilidadIngredienteDTO;
import com.cocinarubi.presentation.dto.response.CalcularFaltanteResponseDTO;
import com.cocinarubi.presentation.dto.response.FaltanteIngredienteDTO;
import com.cocinarubi.presentation.dto.response.RecetaCosteoResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Cubre los 5 escenarios funcionales que pidió el cliente:
 * 1) Faltante y costo cuando hay stock parcial (10 kg necesarios, 4 kg disponibles).
 * 2) Paquetes a comprar cuando el producto tiene presentación mínima (sal 500 g).
 * 3) Escalado de receta (4 porciones base → 10 porciones).
 * 4) Producto de pieza variable (pechuga de pollo, costo estimado por peso promedio).
 * 5) Validaciones (porciones inválidas, cantidades negativas, ingrediente ajeno a la receta).
 */
@ExtendWith(MockitoExtension.class)
public class RecetaCalculoServiceTest {

    @Mock private ComidaRepository comidaRepository;
    @Mock private ProductoRecetaRepository productoRecetaRepository;

    @InjectMocks private RecetaCalculoService recetaCalculoService;

    // ------- Fixtures -------

    private Producto productoCarne() {
        // 1 kg = $200 por kg
        return Producto.builder()
                .idProducto(1).nombre("Carne")
                .tipoMedida(TipoMedida.PESO_EXACTO)
                .unidadBase("kg")
                .contenidoPresentacion(new BigDecimal("1"))
                .unidadPresentacion("kg")
                .costoPresentacion(new BigDecimal("200.00"))
                .costoUnitarioBase(new BigDecimal("200.0000"))
                .build();
    }

    private Producto productoSal() {
        // Paquete de 500 g a $25 → $0.05 por gramo
        return Producto.builder()
                .idProducto(2).nombre("Sal fina")
                .tipoMedida(TipoMedida.PESO_EXACTO)
                .unidadBase("g")
                .contenidoPresentacion(new BigDecimal("500"))
                .unidadPresentacion("g")
                .costoPresentacion(new BigDecimal("25.00"))
                .costoUnitarioBase(new BigDecimal("0.0500"))
                .build();
    }

    private Producto productoPechuga() {
        // Pieza variable: 1000 g a $90 → $0.09 por gramo, 300 g por pieza en promedio
        return Producto.builder()
                .idProducto(3).nombre("Pechuga de pollo")
                .tipoMedida(TipoMedida.PIEZA_VARIABLE)
                .unidadBase("g")
                .pesoPromedioPieza(new BigDecimal("300"))
                .contenidoPresentacion(new BigDecimal("1000"))
                .unidadPresentacion("g")
                .costoPresentacion(new BigDecimal("90.00"))
                .costoUnitarioBase(new BigDecimal("0.0900"))
                .build();
    }

    private Comida comidaConReceta(int id, int porcionesBase) {
        return Comida.builder().idComida(id).nombreComida("Guisado").porcionesBase(porcionesBase).build();
    }

    private ProductoReceta receta(int idProductoReceta, Comida c, Producto p, String cantidad, String unidad) {
        return ProductoReceta.builder()
                .idProductoReceta(idProductoReceta)
                .comida(c).producto(p)
                .cantidadUsada(new BigDecimal(cantidad))
                .unidadUsada(unidad)
                .esAproximado(false)
                .costoUnitario(p.getCostoUnitarioBase())
                .costoTotal(new BigDecimal("0.00"))
                .build();
    }

    // ------- Caso 1: faltante y costo con stock parcial -------

    @Test
    @DisplayName("Caso 1: Necesito 10 kg de carne, tengo 4 kg → faltan 6 kg con costo proporcional")
    public void calcularFaltante_necesito10kgTengo4kg() {
        Comida comida = comidaConReceta(10, 1);
        Producto carne = productoCarne();
        ProductoReceta pr = receta(100, comida, carne, "10", "kg");

        when(comidaRepository.findById(10)).thenReturn(Optional.of(comida));
        when(productoRecetaRepository.findByIdComidaWithProducto(10)).thenReturn(List.of(pr));

        CalcularFaltanteRequestDTO req = CalcularFaltanteRequestDTO.builder()
                .porcionesDeseadas(1)
                .disponibles(List.of(DisponibilidadIngredienteDTO.builder()
                        .idProducto(1).cantidad(new BigDecimal("4")).build()))
                .build();

        CalcularFaltanteResponseDTO res = recetaCalculoService.calcularFaltante(10, req);

        FaltanteIngredienteDTO f = res.getFaltantes().get(0);
        assertEquals(0, new BigDecimal("6").compareTo(f.getCantidadFaltante()));
        assertEquals(0, new BigDecimal("1200.00").compareTo(f.getCostoFaltante()));
        assertFalse(res.isSePuedeProducir());
    }

    // ------- Caso 2: paquetes a comprar con presentación mínima -------

    @Test
    @DisplayName("Caso 2: Sal 100 g por comida x 7 comidas → 700 g → CEILING(700/500)=2 paquetes")
    public void calcularFaltante_sal100gPor7Comidas_devuelve2Paquetes() {
        Comida comida = comidaConReceta(20, 1);
        Producto sal = productoSal();
        ProductoReceta pr = receta(200, comida, sal, "100", "g"); // 100 g por 1 porción base

        when(comidaRepository.findById(20)).thenReturn(Optional.of(comida));
        when(productoRecetaRepository.findByIdComidaWithProducto(20)).thenReturn(List.of(pr));

        CalcularFaltanteRequestDTO req = CalcularFaltanteRequestDTO.builder()
                .porcionesDeseadas(7)
                .disponibles(List.of()) // sin stock
                .build();

        CalcularFaltanteResponseDTO res = recetaCalculoService.calcularFaltante(20, req);

        FaltanteIngredienteDTO f = res.getFaltantes().get(0);
        // 100 g * 7 = 700 g necesarios
        assertEquals(0, new BigDecimal("700").compareTo(f.getCantidadNecesaria()));
        // CEILING(700 / 500) = 2 paquetes
        assertEquals(2, f.getPaquetesACompar());
        // 700 g * $0.05/g = $35.00
        assertEquals(0, new BigDecimal("35.00").compareTo(f.getCostoFaltante()));
    }

    // ------- Caso 3: escalado de receta -------

    @Test
    @DisplayName("Caso 3: porcionesBase=4, porcionesDeseadas=10 → factor 2.5, 650g * 2.5 = 1625g")
    public void costearReceta_escaladoFactor2_5() {
        Comida comida = comidaConReceta(30, 4);
        Producto sal = productoSal();
        ProductoReceta pr = receta(300, comida, sal, "650", "g");

        when(comidaRepository.findById(30)).thenReturn(Optional.of(comida));
        when(productoRecetaRepository.findByIdComidaWithProducto(30)).thenReturn(List.of(pr));

        RecetaCosteoResponseDTO res = recetaCalculoService.costearReceta(30, 10);

        assertEquals(0, new BigDecimal("2.5000").compareTo(res.getFactorEscala()));
        assertEquals(0, new BigDecimal("1625.0000").compareTo(res.getIngredientes().get(0).getCantidadEscalada()));
        // 1625 * 0.05 = 81.25
        assertEquals(0, new BigDecimal("81.25").compareTo(res.getIngredientes().get(0).getCostoTotal()));
        assertEquals(0, new BigDecimal("81.25").compareTo(res.getCostoTotalReceta()));
    }

    // ------- Caso 4: producto de pieza variable -------

    @Test
    @DisplayName("Caso 4: Pechuga pieza variable, 0.5 pieza * 300 g * 0.09 $/g = 13.50")
    public void costearReceta_piezaVariable_usaPesoPromedio() {
        Comida comida = comidaConReceta(40, 1);
        Producto pechuga = productoPechuga();
        ProductoReceta pr = receta(400, comida, pechuga, "0.5", "pieza");
        pr.setEsAproximado(true);

        when(comidaRepository.findById(40)).thenReturn(Optional.of(comida));
        when(productoRecetaRepository.findByIdComidaWithProducto(40)).thenReturn(List.of(pr));

        RecetaCosteoResponseDTO res = recetaCalculoService.costearReceta(40, 1);

        // 0.5 piezas * 300 g * $0.09/g = $13.50
        assertEquals(0, new BigDecimal("13.50").compareTo(res.getIngredientes().get(0).getCostoTotal()));
        // Gramos equivalentes = 0.5 * 300 = 150 → CEILING(150 / 1000) = 1 paquete
        assertEquals(1, res.getIngredientes().get(0).getPaquetesACompar());
    }

    // ------- Caso 5: validaciones -------

    @Test
    @DisplayName("Caso 5a: porcionesDeseadas = 0 debe lanzar 400")
    public void calcularFaltante_porcionesDeseadasZero() {
        CalcularFaltanteRequestDTO req = CalcularFaltanteRequestDTO.builder()
                .porcionesDeseadas(0)
                .disponibles(List.of())
                .build();
        assertThrows(BusinessException.class,
                () -> recetaCalculoService.calcularFaltante(10, req));
    }

    @Test
    @DisplayName("Caso 5b: cantidadDisponible negativa debe lanzar 400")
    public void calcularFaltante_cantidadNegativa() {
        Comida comida = comidaConReceta(10, 1);
        ProductoReceta pr = receta(100, comida, productoCarne(), "10", "kg");

        when(comidaRepository.findById(10)).thenReturn(Optional.of(comida));
        when(productoRecetaRepository.findByIdComidaWithProducto(10)).thenReturn(List.of(pr));

        CalcularFaltanteRequestDTO req = CalcularFaltanteRequestDTO.builder()
                .porcionesDeseadas(1)
                .disponibles(List.of(DisponibilidadIngredienteDTO.builder()
                        .idProducto(1).cantidad(new BigDecimal("-1")).build()))
                .build();

        assertThrows(BusinessException.class,
                () -> recetaCalculoService.calcularFaltante(10, req));
    }

    @Test
    @DisplayName("Caso 5c: idProducto que no está en la receta debe lanzar 400")
    public void calcularFaltante_productoAjenoALaReceta() {
        Comida comida = comidaConReceta(10, 1);
        ProductoReceta pr = receta(100, comida, productoCarne(), "10", "kg");

        when(comidaRepository.findById(10)).thenReturn(Optional.of(comida));
        when(productoRecetaRepository.findByIdComidaWithProducto(10)).thenReturn(List.of(pr));

        CalcularFaltanteRequestDTO req = CalcularFaltanteRequestDTO.builder()
                .porcionesDeseadas(1)
                .disponibles(List.of(DisponibilidadIngredienteDTO.builder()
                        .idProducto(999).cantidad(new BigDecimal("1")).build()))
                .build();

        assertThrows(BusinessException.class,
                () -> recetaCalculoService.calcularFaltante(10, req));
    }

    @Test
    @DisplayName("Caso 5d: porcionesBase inválido en la comida debe lanzar 400")
    public void costearReceta_porcionesBaseInvalido() {
        Comida comida = Comida.builder().idComida(10).nombreComida("x").porcionesBase(0).build();
        when(comidaRepository.findById(10)).thenReturn(Optional.of(comida));

        assertThrows(BusinessException.class,
                () -> recetaCalculoService.costearReceta(10, 5));
    }
}
