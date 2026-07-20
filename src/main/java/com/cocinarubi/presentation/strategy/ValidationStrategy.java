package com.cocinarubi.presentation.strategy;

//Strategy para las validaciones más técnicas, como null, números más grandes que 0, manda errores al front
public interface ValidationStrategy<T> {
 void validarPost(T entidad);
}
