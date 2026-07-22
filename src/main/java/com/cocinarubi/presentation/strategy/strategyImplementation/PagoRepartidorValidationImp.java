package com.cocinarubi.presentation.strategy.strategyImplementation;

import com.cocinarubi.dao.PagoRepartidorRepository;
import com.cocinarubi.domain.service.VistaResumenPedidoService;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.exception.ErrorCode;
import com.cocinarubi.presentation.dto.request.PagoRepartidorRequestDTO;
import com.cocinarubi.presentation.dto.response.VistaResumenPedidoConMetricasResponseDTO;
import com.cocinarubi.presentation.strategy.ValidationStrategy;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Validaciones de negocio para el registro y actualización de {@link com.cocinarubi.domain.entity.PagoRepartidor}.
 * Capa: Strategy — reglas invocadas desde {@code PagoRepartidorService} antes de persistir.
 *
 * <p>Reglas cubiertas: día hábil (Lun–Vie), unicidad diaria y tope contra el ingreso total del día.</p>
 */
@Component
public class PagoRepartidorValidationImp implements ValidationStrategy<PagoRepartidorRequestDTO> {

    private static final DateTimeFormatter FECHA_LEGIBLE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final PagoRepartidorRepository pagoRepartidorRepository;
    private final VistaResumenPedidoService vistaResumenPedidoService;

    public PagoRepartidorValidationImp(PagoRepartidorRepository pagoRepartidorRepository,
                                       VistaResumenPedidoService vistaResumenPedidoService) {
        this.pagoRepartidorRepository = pagoRepartidorRepository;
        this.vistaResumenPedidoService = vistaResumenPedidoService;
    }

    /**
     * Reglas para POST: campos requeridos, día hábil, no duplicado, monto ≤ ingreso del día.
     */
    @Override
    public void validarPost(PagoRepartidorRequestDTO dto) {
        validarCamposBasicos(dto);
        validarDiaHabil(dto.getFechaPago());

        LocalDate dia = dto.getFechaPago().toLocalDate();
        // Repository: garantiza unicidad por día calendario antes de insertar.
        if (pagoRepartidorRepository.existsByFecha(dia)) {
            throw new BusinessException(
                    "Ya existe un pago para la fecha " + dia.format(FECHA_LEGIBLE),
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.VALIDACION);
        }

        validarNoSobrepasaIngreso(dto.getPago(), dia);
    }

    /**
     * Reglas para PUT: campos requeridos y monto ≤ ingreso del día. No revalida
     * día hábil (registro ya existente) ni unicidad (mismo id).
     */
    public void validarPut(PagoRepartidorRequestDTO dto) {
        validarCamposBasicos(dto);
        if (dto.getIdPagoRepartidor() == null) {
            throw new BusinessException(
                    "El id del pago es requerido para actualizar",
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.VALIDACION);
        }
        validarNoSobrepasaIngreso(dto.getPago(), dto.getFechaPago().toLocalDate());
    }

    private void validarCamposBasicos(PagoRepartidorRequestDTO dto) {
        if (dto.getFechaPago() == null) {
            throw new BusinessException(
                    "La fecha del pago es requerida",
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.VALIDACION);
        }
        if (dto.getPago() == null || dto.getPago().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "El pago debe ser mayor a cero",
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.VALIDACION);
        }
    }

    private void validarDiaHabil(LocalDateTime fechaPago) {
        DayOfWeek dia = fechaPago.getDayOfWeek();
        if (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY) {
            throw new BusinessException(
                    "Solo se permiten pagos de lunes a viernes",
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.VALIDACION);
        }
    }

    private void validarNoSobrepasaIngreso(BigDecimal pago, LocalDate dia) {
        LocalDateTime desde = dia.atStartOfDay();
        LocalDateTime hasta = dia.atTime(LocalTime.MAX);
        // VistaResumenPedidoService: retorna métricas del día; solo se consume ingresoTotal.
        VistaResumenPedidoConMetricasResponseDTO metricas = vistaResumenPedidoService
                .findVistaConMetricas(desde, hasta, null, null, PageRequest.of(0, 1));

        BigDecimal ingresoTotal = metricas.getIngresoTotal();
        if (pago.compareTo(ingresoTotal) > 0) {
            throw new BusinessException(
                    "El pago (" + pago + ") supera el ingreso del día " +
                            dia.format(FECHA_LEGIBLE) + " (" + ingresoTotal + ")",
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.VALIDACION);
        }
    }
}
