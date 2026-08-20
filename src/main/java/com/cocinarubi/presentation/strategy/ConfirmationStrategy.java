package com.cocinarubi.presentation.strategy;

//strategia para confirmaciones en el front
//para que en el front salga "Estas seguro de continuar?"
public interface ConfirmationStrategy<T> {

    void validarPost(T entidad);

    /** Comprueba si la entidad tiene pedidos relacionados antes de eliminarla.
     *  Lanza {@link com.cocinarubi.exception.AdvertenciaEliminacionException} si los hay. */
    default void validarEliminacion(Integer id) {}
}
