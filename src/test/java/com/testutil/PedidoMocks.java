package com.testutil;

import com.cocinarubi.DBConstants.Estatus;
import com.cocinarubi.DBConstants.MetodoPago;
import com.cocinarubi.DBConstants.PedidoCreadoDesde;
import com.cocinarubi.DBConstants.TamanoPorcion;
import com.cocinarubi.DBConstants.TipoPedido;
import com.cocinarubi.domain.entity.Basico;
import com.cocinarubi.domain.entity.BasicoComplemento;
import com.cocinarubi.domain.entity.Categoria;
import com.cocinarubi.domain.entity.BasicoPedido;
import com.cocinarubi.domain.entity.Complemento;
import com.cocinarubi.presentation.dto.response.PaqueteResponseDTO;
import com.cocinarubi.domain.entity.Comida;
import com.cocinarubi.domain.entity.ComidaPedido;
import com.cocinarubi.domain.entity.Desayuno;
import com.cocinarubi.domain.entity.DesayunoPedido;
import com.cocinarubi.domain.entity.Pedido;
import com.cocinarubi.domain.entity.PedidoDomicilio;
import com.cocinarubi.domain.entity.PedidoDomicilioCocina;
import com.cocinarubi.domain.entity.ProductoCocina;
import com.cocinarubi.domain.entity.ProductoCocinaPedido;
import com.cocinarubi.domain.entity.OrdenRuta;
import com.cocinarubi.domain.entity.Ruta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Fábrica estática de entidades mock para tests unitarios.
 * Reutilizable en cualquier *ServiceTest que necesite entidades JPA pre-configuradas.
 * No depende de Spring ni de base de datos.
 */
public class PedidoMocks {

    // ── Entidades base (catálogo) ─────────────────────────────────────────────

    public static Comida comida() {
        return Comida.builder()
                .idComida(10)
                .uuidComida("uuid-comida-10")
                .nombreComida("Pollo en salsa roja")
                .descripcion("Con arroz y frijoles")
                .precioMedia(BigDecimal.valueOf(55))
                .precioEntera(BigDecimal.valueOf(90))
                .estatus(Estatus.DISPONIBLE)
                .destacado(true)
                .build();
    }

    public static Desayuno desayuno() {
        return Desayuno.builder()
                .idDesayuno(10)
                .uuidDesayuno("uuid-desayuno-10")
                .nombreDesayuno("Huevos con jamón")
                .descripcion("Con tortillas y frijoles")
                .precioMedia(BigDecimal.valueOf(45))
                .precioEntera(BigDecimal.valueOf(70))
                .estatus(Estatus.DISPONIBLE)
                .destacado(false)
                .build();
    }

    public static Basico basico() {
        return Basico.builder()
                .idBasico(10)
                .descripcion("Básico completo")
                .destacado(false)
                .precioBasico(BigDecimal.valueOf(80))
                .estatus(Estatus.DISPONIBLE)
                .build();
    }

    // IDs de categoría alineados con el seeder de V23:
    //   ('BEBIDA')=1, ('CHAROLA')=2, ('SNACK')=3, ('POSTRE')=4.
    public static final Categoria CATEGORIA_BEBIDA  =
            Categoria.builder().idCategoria(1).nombre("BEBIDA").build();
    public static final Categoria CATEGORIA_CHAROLA =
            Categoria.builder().idCategoria(2).nombre("CHAROLA").build();
    public static final Categoria CATEGORIA_SNACK   =
            Categoria.builder().idCategoria(3).nombre("SNACK").build();
    public static final Categoria CATEGORIA_POSTRE  =
            Categoria.builder().idCategoria(4).nombre("POSTRE").build();

    public static ProductoCocina snack() {
        return ProductoCocina.builder()
                .idProductoCocina(10)
                .uuidProductoCocina("uuid-snack-10")
                .nombreProducto("Papas")
                .descripcion("Papas fritas")
                .precioDomicilio(BigDecimal.valueOf(30))
                .precioNormal(BigDecimal.valueOf(25))
                .estatus(Estatus.DISPONIBLE)
                .destacado(false)
                .categoria(CATEGORIA_SNACK)
                .build();
    }

    public static ProductoCocina bebida() {
        return ProductoCocina.builder()
                .idProductoCocina(11)
                .uuidProductoCocina("uuid-bebida-11")
                .nombreProducto("Refresco")
                .precioDomicilio(BigDecimal.valueOf(20))
                .precioNormal(BigDecimal.valueOf(15))
                .estatus(Estatus.DISPONIBLE)
                .destacado(false)
                .categoria(CATEGORIA_BEBIDA)
                .build();
    }

    public static ProductoCocina charola() {
        return ProductoCocina.builder()
                .idProductoCocina(12)
                .uuidProductoCocina("uuid-charola-12")
                .nombreProducto("Charola de frutas")
                .precioDomicilio(BigDecimal.valueOf(50))
                .precioNormal(BigDecimal.valueOf(45))
                .estatus(Estatus.DISPONIBLE)
                .destacado(false)
                .categoria(CATEGORIA_CHAROLA)
                .build();
    }

    public static ProductoCocina postre() {
        return ProductoCocina.builder()
                .idProductoCocina(13)
                .uuidProductoCocina("uuid-postre-13")
                .nombreProducto("Flan")
                .precioDomicilio(BigDecimal.valueOf(25))
                .precioNormal(BigDecimal.valueOf(20))
                .estatus(Estatus.DISPONIBLE)
                .destacado(false)
                .categoria(CATEGORIA_POSTRE)
                .build();
    }

    /** Basico DISPONIBLE con comida precargada; complementos vacíos. */
    public static Basico basicoConComida() {
        return Basico.builder()
                .idBasico(10)
                .uuidBasico("uuid-basico-10")
                .comida(comida())
                .descripcion("Básico completo")
                .destacado(false)
                .precioBasico(BigDecimal.valueOf(80))
                .estatus(Estatus.DISPONIBLE)
                .build();
    }

    /** Basico DISPONIBLE con comida y un complemento (Arroz, precioExtra=0). */
    public static Basico basicoConComplemento() {
        Complemento comp = Complemento.builder()
                .idComplemento(1)
                .nombreComplemento("Arroz")
                .precioExtra(BigDecimal.ZERO)
                .build();
        BasicoComplemento bc = BasicoComplemento.builder()
                .idBasicoComplemento(1)
                .complemento(comp)
                .build();
        Basico b = basicoConComida();
        b.addComplemento(bc);
        return b;
    }

    /** PaqueteResponseDTO mínimo para stubbing de PaqueteService.findDisponibles(). */
    public static PaqueteResponseDTO paqueteResponseDTO() {
        return new PaqueteResponseDTO(10, BigDecimal.valueOf(120), "Promo mixta",
                Estatus.DISPONIBLE, false, List.of());
    }

    public static OrdenRuta ordenRuta() {
        return OrdenRuta.builder()
                .idOrdenRuta(1)
                .tiempoEstimadoMin(30)
                .build();
    }

    public static Ruta ruta() {
        return Ruta.builder()
                .idRuta(10)
                .nombre("Ruta Centro")
                .isActive(true)
                .tarifaEnvio(BigDecimal.valueOf(40))
                .build();
    }

    public static Ruta rutaConOrden() {
        return Ruta.builder()
                .idRuta(10)
                .nombre("Ruta Centro")
                .isActive(true)
                .tarifaEnvio(BigDecimal.valueOf(40))
                .ordenRuta(ordenRuta())
                .build();
    }

    // ── Líneas de pedido ─────────────────────────────────────────────────────

    public static ComidaPedido comidaPedido(Pedido pedido) {
        return ComidaPedido.builder()
                .idComidaPedido(10)
                .pedido(pedido)
                .comida(comida())
                .precioUnitario(BigDecimal.valueOf(90))
                .tamanoPorcion(TamanoPorcion.ENTERA)
                .build();
    }

    public static DesayunoPedido desayunoPedido(Pedido pedido) {
        return DesayunoPedido.builder()
                .idDesayunoPedido(10)
                .pedido(pedido)
                .desayuno(desayuno())
                .precio(BigDecimal.valueOf(70))
                .build();
    }

    public static BasicoPedido basicoPedido(Pedido pedido) {
        return BasicoPedido.builder()
                .idBasicoPedido(10)
                .pedido(pedido)
                .basico(basico())
                .precioUnitario(BigDecimal.valueOf(80))
                .build();
    }

    public static ProductoCocinaPedido snackPedido(Pedido pedido) {
        return ProductoCocinaPedido.builder()
                .idProductoCocinaPedido(10)
                .pedido(pedido)
                .productoCocina(snack())
                .cantidad((byte) 2)
                .precioUnitario(BigDecimal.valueOf(25))
                .build();
    }

    public static ProductoCocinaPedido bebidaPedido(Pedido pedido) {
        return ProductoCocinaPedido.builder()
                .idProductoCocinaPedido(11)
                .pedido(pedido)
                .productoCocina(bebida())
                .cantidad((byte) 1)
                .precioUnitario(BigDecimal.valueOf(15))
                .build();
    }

    // ── Pedidos completos ─────────────────────────────────────────────────────

    /** MOSTRADOR + COCINA, sin ruta, con 1 ComidaPedido. */
    public static Pedido pedidoMostrador(LocalDateTime fecha) {
        Pedido p = Pedido.builder()
                .idPedido(10)
                .tipoPedido(TipoPedido.MOSTRADOR)
                .pedidoCreadoDesde(PedidoCreadoDesde.COCINA)
                .metodoPagoPrincipal(MetodoPago.EFECTIVO)
                .fechaExpedicionPedido(fecha)
                .precioFinalOrden(BigDecimal.valueOf(90))
                .impreso(false)
                .build();
        p.addComidaPedido(comidaPedido(p));
        return p;
    }

    /** PICK_UP + COCINA, sin ruta, con 1 DesayunoPedido. */
    public static Pedido pedidoPickUp(LocalDateTime fecha) {
        Pedido p = Pedido.builder()
                .idPedido(11)
                .tipoPedido(TipoPedido.PICK_UP)
                .pedidoCreadoDesde(PedidoCreadoDesde.COCINA)
                .metodoPagoPrincipal(MetodoPago.EFECTIVO)
                .fechaExpedicionPedido(fecha)
                .precioFinalOrden(BigDecimal.valueOf(70))
                .impreso(false)
                .build();
        p.addDesayunoPedido(desayunoPedido(p));
        return p;
    }

    /** DOMICILIO + WEB, con PedidoDomicilio → Ruta, con 1 ComidaPedido y 1 snackPedido. */
    public static Pedido pedidoDomicilioWeb(LocalDateTime fecha) {
        PedidoDomicilio domicilio = PedidoDomicilio.builder()
                .ruta(ruta())
                .direccion("Calle Principal 42")
                .tarifa(BigDecimal.valueOf(40))
                .build();

        Pedido p = Pedido.builder()
                .idPedido(12)
                .tipoPedido(TipoPedido.DOMICILIO)
                .pedidoCreadoDesde(PedidoCreadoDesde.WEB)
                .metodoPagoPrincipal(MetodoPago.TARJETA)
                .fechaExpedicionPedido(fecha)
                .precioFinalOrden(BigDecimal.valueOf(130))
                .impreso(false)
                .pedidoDomicilio(domicilio)
                .build();
        p.addComidaPedido(comidaPedido(p));
        p.addProductoCocinaPedido(snackPedido(p));
        return p;
    }

    /** DOMICILIO + COCINA, con PedidoDomicilioCocina → Ruta, con 1 BasicoPedido. */
    public static Pedido pedidoDomicilioCocina(LocalDateTime fecha) {
        PedidoDomicilioCocina domicilioCocina = PedidoDomicilioCocina.builder()
                .ruta(ruta())
                .domicilio("Calle Falsa 123")
                .precioTarifa(BigDecimal.valueOf(40))
                .build();

        Pedido p = Pedido.builder()
                .idPedido(13)
                .tipoPedido(TipoPedido.DOMICILIO)
                .pedidoCreadoDesde(PedidoCreadoDesde.COCINA)
                .metodoPagoPrincipal(MetodoPago.EFECTIVO)
                .fechaExpedicionPedido(fecha)
                .precioFinalOrden(BigDecimal.valueOf(120))
                .impreso(false)
                .pedidoDomicilioCocina(domicilioCocina)
                .build();
        p.addBasicoPedido(basicoPedido(p));
        return p;
    }
}
