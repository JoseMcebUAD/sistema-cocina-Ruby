package com.cocinarubi;

/*
    Clase para alojar los ENUM especificados en la base de datos
*/
public class DBConstants {

    /**
     * Catálogo estático de módulos de archivo. Las categorías dinámicas de
     * ProductoCocina (SNACK, CHAROLA, BEBIDA, POSTRE y las creadas por el
     * usuario) ya no viven aquí — se resuelven vía FK a la tabla categoria.
     */
    public enum TipoCatalogoProducto { BASICO, COMIDA, DESAYUNO }

    /**
     * Discriminador polimórfico para {@code favorito_cliente.tipo_catalogo_producto}.
     * Mantiene los 8 valores originales porque la columna en BD sigue soportando
     * los 4 tipos que antes vivían en {@link TipoCatalogoProducto}
     * (SNACK, CHAROLA, BEBIDA, POSTRE) y COMPLEMENTO, sin migración destructiva.
     */
    public enum TipoProductoFavorito {
        BASICO, COMIDA, DESAYUNO, COMPLEMENTO, SNACK, CHAROLA, BEBIDA, POSTRE
    }

    /**
     * Discriminador polimórfico para {@code combo_producto.tipo_producto}.
     * Una línea de combo referencia UNA de estas cuatro tablas maestras según el valor.
     */
    public enum TipoLineaCombo { COMIDA, DESAYUNO, COMPLEMENTO, PRODUCTO_COCINA }


    /** Estado de disponibilidad de platillos para el menú web. */
    public enum Estatus { DISPONIBLE, NO_DISPONIBLE, AGOTADO }

    /*Tipos de tamaño de porción para la entidad de ComidaPedido */
    public enum TamanoPorcion { MEDIA, ENTERA }

    /** Tipo de turno: menú de comidas del día o menú de desayunos para la tabla HorarioAtencion. */
    public enum TipoHorario { DESAYUNO, COMIDAS }

    /***PEDIDO */
    /** Forma de pago utilizada por el cliente. para  */
    public enum MetodoPago { TARJETA, EFECTIVO, TRANSFERENCIA }

    /** Canal por el que se entrega el pedido. */
    public enum TipoPedido { PICK_UP, DOMICILIO, MOSTRADOR }

    /** Indica si el pedido fue generado por el cliente web o por un operador de la cocina. */
    public enum PedidoCreadoDesde { COCINA, WEB }

    /**Se crea el tipo contador de comida, si es por unidad, gramo o kilogramo  */
    public enum TipoContadorComida {UNIDAD, KILOGRAMO, GRAMO }

    /** Tipo de entidad soportada por el módulo de impresión (extensible a futuro). */
    public enum TipoEntidadImpresion { PEDIDO }

    /** Tipo de operación HTTP registrada en la tabla auditoria. PATCH se mapea como PUT. */
    public enum TipoOperacion { POST, PUT, DELETE }

    /** Clasifica si una comida pertenece al menú fijo del día o es un especial temporal. */
    public enum TipoComida { FIJA, ESPECIAL }

    /**
     * Tipo de descuento automático aplicado a un pedido.
     * {@code COMIDAS_DIEZ} se activa cuando el pedido contiene ≥ 12 comidas MEDIA + FIJA,
     * descontando $10 en cada una de esas líneas.
     */
    public enum TipoDescuento { COMIDAS_DIEZ }

    /** Nivel de urgencia/visibilidad de un anuncio en el carrito del cliente. */
    public enum PrioridadAnuncio { ALTA, MEDIA, BAJA }

    /**
     * Discriminador polimórfico para {@code anuncio_producto.tipo_producto}.
     * Indica a qué tabla maestra pertenece el {@code id_producto} referenciado.
     */
    public enum TipoProductoAnuncio { COMIDA, DESAYUNO, COMPLEMENTO, PRODUCTO_COCINA, BASICO, COMBO }

    /**
     * Tipo de entidad de catálogo que puede participar en una {@code Oferta}.
     * Desayuno queda excluido intencionalmente en v1.
     */
    public enum TipoEntidadOferta { COMIDA, PRODUCTO_COCINA, BASICO, COMBO }

    /**
     * Modo de matching de una línea ({@code oferta_linea}) dentro de una {@code Oferta}.
     *
     * <p>El campo {@code id_referencia} en la línea se interpreta según el modo:
     * <ul>
     *   <li>{@code COMIDA_ESPECIFICA} → idComida.</li>
     *   <li>{@code COMIDA_POR_TIPO} → null; el filtro lo da {@code tipo_comida_filtro}.</li>
     *   <li>{@code PRODUCTO_ESPECIFICO} → idProductoCocina.</li>
     *   <li>{@code PRODUCTO_POR_CATEGORIA} → idCategoria.</li>
     *   <li>{@code PRODUCTO_POR_SUBCATEGORIA} → idSubcategoria.</li>
     *   <li>{@code PRODUCTO_GENERAL} → null (cualquier ProductoCocina).</li>
     *   <li>{@code BASICO_ESPECIFICO} → idBasico.</li>
     *   <li>{@code BASICO_GENERAL} → null (cualquier Basico).</li>
     *   <li>{@code COMBO_ESPECIFICO} → idCombo.</li>
     *   <li>{@code COMBO_GENERAL} → null (cualquier Combo).</li>
     * </ul>
     */
    public enum ModoMatchOferta {
        COMIDA_ESPECIFICA,
        COMIDA_POR_TIPO,
        PRODUCTO_ESPECIFICO,
        PRODUCTO_POR_CATEGORIA,
        PRODUCTO_POR_SUBCATEGORIA,
        PRODUCTO_GENERAL,
        BASICO_ESPECIFICO,
        BASICO_GENERAL,
        COMBO_ESPECIFICO,
        COMBO_GENERAL
    }

    /** Indica si una suscripción semanal fue creada por el operador (COCINA) o comprada por el cliente vía web (WEB). */
    public enum OrigenSuscripcion { COCINA, WEB }

}
