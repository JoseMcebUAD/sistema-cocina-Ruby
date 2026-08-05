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

}
