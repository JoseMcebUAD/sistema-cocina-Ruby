package com.Controller.order;

import com.Model.ModeloOrden;

/**
 * Interfaz para que las ordenes puedan ser mas maleables en el controlador de orden
*/
public interface OrderSelectedInterface {
    //mustra la barra que se necesita usar
    public void showOrderBar(boolean show);
    //calcula el total de la orden,
    //1- con los items
    //2- con los items y con la tarifa
    public double calculateOrderTotal();
    //regresa el tipo de orden actual, si es domicilio,mostrador o mesa
    public String currentTypeOrder();
    //modifica los precios, y campos que ya no se deben de usar
    public void barChanged();
    //validar los campos de las ordenes
    public MSGValidacion validateOrderRequirements();

    //crea la orden en la bd
    public ModeloOrden createOrden();

    //setea los atributos necesarios
    public void setOrderAtributes();

    public String getConfirmOrderName();

    public record MSGValidacion(Boolean hasError,String fieldValues,String errorMesagge) {}
}
