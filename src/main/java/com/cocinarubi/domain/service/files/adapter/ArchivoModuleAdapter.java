package com.cocinarubi.domain.service.files.adapter;

import com.cocinarubi.presentation.dto.response.ArchivoResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Operaciones sobre archivos ligadas a un módulo concreto — sea un enum estático
 * ({@code BASICO}/{@code COMIDA}/{@code DESAYUNO}) o una {@code Categoria} dinámica.
 * Cada implementación conoce su discriminador y encapsula la resolución del
 * handler, la validación de existencia de la entidad y los queries al repositorio.
 * Capa: Service — se obtiene vía {@link ArchivoModuleAdapterFactory}.
 */
public interface ArchivoModuleAdapter {

    List<ArchivoResponseDTO> upload(Integer idEntidad, MultipartFile[] files);

    List<ArchivoResponseDTO> getAll(Integer idEntidad);

    Map<Integer, List<ArchivoResponseDTO>> getAllBatch(List<Integer> ids);

    /**
     * Devuelve la imagen de portada (primer archivo según orden) de cada entidad.
     * El Map resultante es {@code idEntidad → primer ArchivoResponseDTO}; si una
     * entidad no tiene archivos simplemente no aparece en el Map.
     */
    Map<Integer, ArchivoResponseDTO> getPortadaBatch(List<Integer> ids);

    /**
     * Reposiciona {@code idArchivo} al {@code nuevoOrden} dentro de la galería de
     * su entidad, desplazando los demás archivos para mantener el orden contiguo.
     * Lanza 400 si el archivo no pertenece a este módulo o si el orden queda fuera
     * del rango [1, maxOrden].
     */
    ArchivoResponseDTO actualizarOrden(Integer idArchivo, Integer nuevoOrden);
}
