package com.cocinarubi.presentation.strategy.strategyImplementation;

import com.cocinarubi.Constants;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.exception.ErrorCode;
import com.cocinarubi.presentation.strategy.ValidationStrategy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;

@Component
public class HorarioAtencionValidationImp implements ValidationStrategy<Object> {

    private static final LocalTime DESAYUNO_INICIO = LocalTime.of(7, 0);
    private static final LocalTime DESAYUNO_CIERRE = LocalTime.of(11, 0);
    private static final LocalTime COMIDAS_INICIO  = LocalTime.of(8, 30);
    private static final LocalTime COMIDAS_CIERRE  = LocalTime.of(15, 30);

    private static final String MENSAJE_FUERA_DE_HORARIO =
            "Acción fuera de horario de atención, el horario de atención es:\n" +
            "* Desayunos: lunes a sábado, 7:00 AM – 11:00 AM.\n" +
            "* Almuerzos: lunes a viernes, 8:30 AM – 3:30 PM.";

    @Override
    public void validarPost(Object entidad) {
        ZonedDateTime ahora = ZonedDateTime.now(Constants.ZONA_MERIDA);
        DayOfWeek dia = ahora.getDayOfWeek();
        LocalTime hora = ahora.toLocalTime();

        if (dia == DayOfWeek.SUNDAY) {
            throw new BusinessException(MENSAJE_FUERA_DE_HORARIO, HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
        }

        boolean enDesayuno = !hora.isBefore(DESAYUNO_INICIO) && hora.isBefore(DESAYUNO_CIERRE);

        if (dia == DayOfWeek.SATURDAY) {
            if (!enDesayuno) {
                throw new BusinessException(MENSAJE_FUERA_DE_HORARIO, HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
            }
            return;
        }

        boolean enComidas = !hora.isBefore(COMIDAS_INICIO) && hora.isBefore(COMIDAS_CIERRE);

        if (!enDesayuno && !enComidas) {
            throw new BusinessException(MENSAJE_FUERA_DE_HORARIO, HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
        }
    }
}
