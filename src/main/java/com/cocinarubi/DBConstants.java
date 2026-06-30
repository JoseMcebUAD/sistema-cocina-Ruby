package com.cocinarubi;

/*
    Clase para alojar los ENUM especificados en la base de datos
*/
public class DBConstants {

    /** Catálogo de producto al que puede asociarse un archivo subido en la tabla de ARCHIVOMODULO. */
    public enum TipoCatalogoProducto { BASICO, COMIDA, DESAYUNO, SNACK, CHAROLA, BEBIDA }

    
    /** Estado de disponibilidad de platillos para el menú web. */
    public enum Estatus { DISPONIBLE, NO_DISPONIBLE, AGOTADO }

    /*Tipos de tamaño de porción para la entidad de ComidaPedido */
    public enum TamanoPorcion { MEDIA, ENTERA }

    /** Tipo de turno: menú de comidas del día o menú de desayunos para la tabla HorarioAtencion. */
    public enum TipoHorario { DESAYUNO, COMIDAS }

    /*Tipo de producto genérico con nombre,percio, estatus e imágenes para la tabla ProductoCocina */
    public enum TipoProducto { SNACK, CHAROLA, BEBIDA }

    /***PEDIDO */
    /** Forma de pago utilizada por el cliente. para  */
    public enum MetodoPago { TARJETA, EFECTIVO, TRANSFERENCIA }

    /** Canal por el que se entrega el pedido. */
    public enum TipoPedido { PICK_UP, DOMICILIO, MOSTRADOR }

    /** Indica si el pedido fue generado por el cliente web o por un operador de la cocina. */
    public enum PedidoCreadoDesde { COCINA, WEB }

    /**Se crea el tipo contador de comida, si es por unidad, gramo o kilogramo  */
    public enum TipoContadorComida {UNIDAD, KILOGRAMO, GRAMO }

}
