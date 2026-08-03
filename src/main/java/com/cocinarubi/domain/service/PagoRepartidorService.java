package com.cocinarubi.domain.service;

import com.cocinarubi.dao.PagoRepartidorRepository;
import com.cocinarubi.domain.entity.PagoRepartidor;
import com.cocinarubi.domain.mapper.PagoRepartidorMapper;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.PagoRepartidorRequestDTO;
import com.cocinarubi.presentation.dto.response.PagoRepartidorPorFechaResponseDTO;
import com.cocinarubi.presentation.dto.response.PagoRepartidorResponseDTO;
import com.cocinarubi.presentation.strategy.strategyImplementation.PagoRepartidorValidationImp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Gestiona los registros de pago a repartidores por sus entregas realizadas.
 * Capa: Service — orquesta validaciones, mapeo y persistencia.
 */
@Service
public class PagoRepartidorService {

    private final PagoRepartidorRepository pagoRepartidorRepository;
    private final PagoRepartidorMapper mapper;
    private final PagoRepartidorValidationImp validator;

    public PagoRepartidorService(PagoRepartidorRepository pagoRepartidorRepository,
                                 PagoRepartidorMapper mapper,
                                 PagoRepartidorValidationImp validator) {
        this.pagoRepartidorRepository = pagoRepartidorRepository;
        this.mapper = mapper;
        this.validator = validator;
    }

    public List<PagoRepartidorResponseDTO> findAll() {
        return mapper.toResponseList(pagoRepartidorRepository.findAll());
    }

    public PagoRepartidorResponseDTO findById(int id) {
        PagoRepartidor entity = pagoRepartidorRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Pago de repartidor no encontrado con id: " + id, HttpStatus.NOT_FOUND));
        return mapper.toResponse(entity);
    }

    public PagoRepartidorResponseDTO save(PagoRepartidorRequestDTO dto) {
        // PagoRepartidorValidationImp: reglas de día hábil, unicidad diaria y tope de ingreso.
        validator.validarPost(dto);
        dto.setIdPagoRepartidor(null);
        PagoRepartidor persisted = pagoRepartidorRepository.save(mapper.toEntity(dto));
        return mapper.toResponse(persisted);
    }

    public PagoRepartidorResponseDTO update(PagoRepartidorRequestDTO dto) {
        validator.validarPut(dto);
        if (!pagoRepartidorRepository.existsById(dto.getIdPagoRepartidor())) {
            throw new BusinessException(
                    "Pago de repartidor no encontrado con id: " + dto.getIdPagoRepartidor(),
                    HttpStatus.NOT_FOUND);
        }
        PagoRepartidor persisted = pagoRepartidorRepository.save(mapper.toEntity(dto));
        return mapper.toResponse(persisted);
    }

    /**
     * Devuelve una entrada por cada día hábil (Lun–Vie) dentro del rango
     * {@code [desde, hasta]}. Los días sin pago registrado llevan {@code pagoRepartidor = null}.
     * La paginación se aplica sobre la lista de días hábiles resultante.
     */
    public Page<PagoRepartidorPorFechaResponseDTO> findByRango(LocalDate desde,
                                                               LocalDate hasta,
                                                               Pageable pageable) {
        if (desde == null || hasta == null) {
            throw new BusinessException(
                    "Los parámetros 'desde' y 'hasta' son requeridos",
                    HttpStatus.BAD_REQUEST);
        }
        if (desde.isAfter(hasta)) {
            throw new BusinessException(
                    "La fechas ingresadas son incorrectas, la fecha de inicio debe ser menor a la de fin",
                    HttpStatus.BAD_REQUEST);
        }

        List<LocalDate> diasHabiles = generarDiasHabiles(desde, hasta);
        if (diasHabiles.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // Trae todos los pagos del rango de una sola vez (rango semi-abierto en el repositorio).
        List<PagoRepartidor> pagos = pagoRepartidorRepository.findByRango(
                desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay());

        // Si por datos legacy un mismo día tiene múltiples registros, conservamos el más reciente.
        Map<LocalDate, PagoRepartidorResponseDTO> pagoPorDia = pagos.stream()
                .collect(Collectors.toMap(
                        p -> p.getFechaPago().toLocalDate(),
                        mapper::toResponse,
                        (existente, nuevo) -> nuevo.getFechaPago().isAfter(existente.getFechaPago()) ? nuevo : existente));

        List<PagoRepartidorPorFechaResponseDTO> resultado = diasHabiles.stream()
                .map(fecha -> PagoRepartidorPorFechaResponseDTO.builder()
                        .fecha(fecha)
                        .pagoRepartidor(pagoPorDia.get(fecha))
                        .build())
                .collect(Collectors.toList());

        int total = resultado.size();
        int inicio = (int) pageable.getOffset();
        if (inicio >= total) {
            return new PageImpl<>(List.of(), pageable, total);
        }
        int fin = Math.min(inicio + pageable.getPageSize(), total);
        return new PageImpl<>(resultado.subList(inicio, fin), pageable, total);
    }

    private List<LocalDate> generarDiasHabiles(LocalDate desde, LocalDate hasta) {
        List<LocalDate> dias = new ArrayList<>();
        LocalDate cursor = desde;
        while (!cursor.isAfter(hasta)) {
            DayOfWeek dow = cursor.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                dias.add(cursor);
            }
            cursor = cursor.plusDays(1);
        }
        return dias;
    }
}
