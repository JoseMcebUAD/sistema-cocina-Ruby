package com.cocinarubi.domain.service;

import com.cocinarubi.DBConstants.Estatus;
import com.cocinarubi.dao.TarjetaTransferenciaRepository;
import com.cocinarubi.domain.entity.TarjetaTransferencia;
import com.cocinarubi.domain.interfaces.ITarjetaTransferenciaService;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.TarjetaTransferenciaEstatusRequestDTO;
import com.cocinarubi.presentation.dto.request.TarjetaTransferenciaRequestDTO;
import com.cocinarubi.presentation.dto.response.TarjetaTransferenciaActivaDTO;
import com.cocinarubi.presentation.dto.response.TarjetaTransferenciaResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Gestiona el catálogo de tarjetas bancarias para transferencias del checkout web.
 * Garantiza que solo una tarjeta tenga estatus DISPONIBLE en todo momento.
 * Capa: Service — lógica de negocio de tarjetas de transferencia.
 */
@Service
public class TarjetaTransferenciaService implements ITarjetaTransferenciaService {

    private final TarjetaTransferenciaRepository tarjetaRepository;

    public TarjetaTransferenciaService(TarjetaTransferenciaRepository tarjetaRepository) {
        this.tarjetaRepository = tarjetaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TarjetaTransferenciaResponseDTO> findAll() {
        return tarjetaRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TarjetaTransferenciaResponseDTO save(TarjetaTransferenciaRequestDTO dto) {
        TarjetaTransferencia tarjeta = TarjetaTransferencia.builder()
                .clabe(dto.getClabe())
                .numeroTarjeta(dto.getNumeroTarjeta())
                .nombreDestinatario(dto.getNombreDestinatario())
                .banco(dto.getBanco())
                .estatus(Estatus.NO_DISPONIBLE)
                .build();
        return toResponseDTO(tarjetaRepository.save(tarjeta));
    }

    /**
     * Cambia el estatus de la tarjeta indicada.
     * Si el nuevo estatus es DISPONIBLE, desactiva todas las demás en la misma transacción
     * para garantizar la invariante "solo una activa". Si se intenta desactivar la única
     * tarjeta activa, lanza un error 409.
     */
    @Override
    @Transactional
    public TarjetaTransferenciaResponseDTO cambiarEstatus(int id, TarjetaTransferenciaEstatusRequestDTO dto) {
        if (dto.getEstatus() == Estatus.AGOTADO) {
            throw new BusinessException(
                    "El estatus AGOTADO no es válido para tarjetas de transferencia",
                    HttpStatus.BAD_REQUEST);
        }

        TarjetaTransferencia tarjeta = findEntityById(id);

        if (dto.getEstatus() == Estatus.NO_DISPONIBLE) {
            boolean esLaUnicaActiva = tarjeta.getEstatus() == Estatus.DISPONIBLE
                    && tarjetaRepository.countByEstatus(Estatus.DISPONIBLE) == 1;
            if (esLaUnicaActiva) {
                throw new BusinessException(
                        "No es posible desactivar la única tarjeta disponible. Activa otra tarjeta primero.",
                        HttpStatus.CONFLICT);
            }
        }

        if (dto.getEstatus() == Estatus.DISPONIBLE) {
            // Desactiva todas (incluyendo esta) y luego activa solo la indicada
            tarjetaRepository.updateAllEstatus(Estatus.NO_DISPONIBLE);
        }

        tarjeta.setEstatus(dto.getEstatus());
        return toResponseDTO(tarjetaRepository.save(tarjeta));
    }

    /**
     * Elimina la tarjeta. Rechaza la operación si es la única con estatus DISPONIBLE,
     * para no dejar el sistema sin tarjeta activa.
     */
    @Override
    @Transactional
    public void delete(int id) {
        TarjetaTransferencia tarjeta = findEntityById(id);
        boolean esLaUnicaActiva = tarjeta.getEstatus() == Estatus.DISPONIBLE
                && tarjetaRepository.countByEstatus(Estatus.DISPONIBLE) == 1;
        if (esLaUnicaActiva) {
            throw new BusinessException(
                    "No puedes eliminar la única tarjeta disponible. Activa otra tarjeta primero.",
                    HttpStatus.CONFLICT);
        }
        tarjetaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public TarjetaTransferenciaActivaDTO obtenerActiva() {
        TarjetaTransferencia tarjeta = tarjetaRepository.findFirstByEstatus(Estatus.DISPONIBLE)
                .orElseThrow(() -> new BusinessException(
                        "No hay una tarjeta de transferencia disponible en este momento.",
                        HttpStatus.NOT_FOUND));
        return toActivaDTO(tarjeta);
    }

    private TarjetaTransferencia findEntityById(int id) {
        return tarjetaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Tarjeta de transferencia no encontrada con id: " + id,
                        HttpStatus.NOT_FOUND));
    }

    private TarjetaTransferenciaResponseDTO toResponseDTO(TarjetaTransferencia t) {
        return new TarjetaTransferenciaResponseDTO(
                t.getIdTarjeta(),
                t.getClabe(),
                t.getNumeroTarjeta(),
                t.getNombreDestinatario(),
                t.getBanco(),
                t.getEstatus()
        );
    }

    private TarjetaTransferenciaActivaDTO toActivaDTO(TarjetaTransferencia t) {
        return new TarjetaTransferenciaActivaDTO(
                t.getClabe(),
                t.getNumeroTarjeta(),
                t.getNombreDestinatario(),
                t.getBanco()
        );
    }
}
