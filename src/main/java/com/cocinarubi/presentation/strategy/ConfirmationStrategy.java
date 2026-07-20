package com.cocinarubi.presentation.strategy;

//strategia para confirmaciones en el front
//para que en el front salga "Estas seguro de continuar?"
public interface ConfirmationStrategy<T> {
 void validarPost(T entidad);
}
