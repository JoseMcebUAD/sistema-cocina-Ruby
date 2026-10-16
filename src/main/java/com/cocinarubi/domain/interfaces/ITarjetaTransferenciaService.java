package com.cocinarubi.domain.interfaces;

import com.cocinarubi.presentation.dto.request.TarjetaTransferenciaEstatusRequestDTO;
import com.cocinarubi.presentation.dto.request.TarjetaTransferenciaRequestDTO;
import com.cocinarubi.presentation.dto.response.TarjetaTransferenciaActivaDTO;
import com.cocinarubi.presentation.dto.response.TarjetaTransferenciaResponseDTO;

import java.util.List;

/**
 * Contrato del servicio de tarjetas de transferencia bancaria.
 * Capa: Service.
 */
public interface ITarjetaTransferenciaService {

    List<TarjetaTransferenciaResponseDTO> findAll();

    TarjetaTransferenciaResponseDTO save(TarjetaTransferenciaRequestDTO dto);

    /**
     * Cambia el estatus de la tarjeta indicada.
     * Garantiza la invariante: solo una tarjeta puede tener estatus DISPONIBLE.
     */
    TarjetaTransferenciaResponseDTO cambiarEstatus(int id, TarjetaTransferenciaEstatusRequestDTO dto);

    void delete(int id);

    /** Retorna la tarjeta con estatus DISPONIBLE para mostrar en el checkout web. */
    TarjetaTransferenciaActivaDTO obtenerActiva();
}
