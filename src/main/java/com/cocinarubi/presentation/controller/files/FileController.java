package com.cocinarubi.presentation.controller.files;

import com.cocinarubi.DBConstants.TipoCatalogoProducto;
import com.cocinarubi.domain.interfaces.ArchivoService;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.CambiarOrdenRequestDTO;
import com.cocinarubi.presentation.dto.request.FileUploadRequestDTO;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.presentation.dto.response.ArchivoResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controlador REST para la gestión de archivos en Cloudinary. Recibe la metadata
 * de la entidad como JSON en el @RequestPart "meta" y los archivos en el @RequestPart "files".
 *
 * <p>Los endpoints GET/PATCH aceptan discriminador dual: {@code entityType} para
 * módulos estáticos (BASICO/COMIDA/DESAYUNO/EXTRAS) o {@code idCategoria} para módulos
 * dinámicos generados a partir de {@code Categoria}. Exactamente uno debe venir;
 * la validación XOR vive en {@code ArchivoService.forModule}.</p>
 */
@RestController
@RequestMapping("/files")
@Tag(name = "Archivos", description = "Subir, eliminar o consultar los archivos alojados en Cloudinary")
public class FileController {

    private final ArchivoService archivoService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public FileController(ArchivoService archivoService,
                          ObjectMapper objectMapper,
                          Validator validator) {
        this.archivoService = archivoService;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    /**
     * Sube uno o más archivos a Cloudinary junto con su metadata (entidad destino,
     * módulo, tipo). La metadata llega como JSON en el part "meta" y los binarios
     * en el part "files". Devuelve la lista de archivos creados con sus URLs y orden.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<ArchivoResponseDTO>>> upload(
            @RequestPart("meta") String metaJson,
            @RequestPart("files") MultipartFile[] files) {

        FileUploadRequestDTO meta = parseMeta(metaJson);
        validateMeta(meta);

        List<ArchivoResponseDTO> archivos = archivoService.upload(meta, files);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito(201, "Archivos subidos correctamente", archivos));
    }

    /**
     * Devuelve todos los archivos de una entidad concreta ({@code idEntidad}).
     * El módulo se discrimina con {@code entityType} (módulo estático) o
     * {@code idCategoria} (módulo dinámico); exactamente uno de los dos debe venir.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ArchivoResponseDTO>>> getAll(
            @RequestParam(value = "entityType", required = false) TipoCatalogoProducto entityType,
            @RequestParam(value = "idCategoria", required = false) Integer idCategoria,
            @RequestParam("idEntidad") Integer idEntidad) {
        List<ArchivoResponseDTO> archivos = archivoService
                .forModule(entityType, idCategoria)
                .getAll(idEntidad);
        return ResponseEntity.ok(ApiResponse.exito(200, "Archivos obtenidos correctamente", archivos));
    }

    /**
     * Devuelve todos los archivos de múltiples entidades en una sola llamada.
     * El resultado es un mapa {@code idEntidad → lista de archivos}, útil para
     * poblar galerías de listados sin hacer N peticiones individuales.
     */
    @GetMapping("/batch")
    public ResponseEntity<ApiResponse<Map<Integer, List<ArchivoResponseDTO>>>> getBatch(
            @RequestParam(value = "entityType", required = false) TipoCatalogoProducto entityType,
            @RequestParam(value = "idCategoria", required = false) Integer idCategoria,
            @RequestParam("ids") List<Integer> ids) {
        Map<Integer, List<ArchivoResponseDTO>> archivos = archivoService
                .forModule(entityType, idCategoria)
                .getAllBatch(ids);
        return ResponseEntity.ok(ApiResponse.exito(200, "Archivos obtenidos correctamente", archivos));
    }

    /**
     * Devuelve la imagen de portada (orden más bajo) de cada entidad en {@code ids}.
     * El resultado es un mapa {@code idEntidad → archivo portada}, pensado para
     * tarjetas o miniaturas donde solo se necesita una imagen representativa.
     */
    @GetMapping("/portada")
    public ResponseEntity<ApiResponse<Map<Integer, ArchivoResponseDTO>>> getPortada(
            @RequestParam(value = "entityType", required = false) TipoCatalogoProducto entityType,
            @RequestParam(value = "idCategoria", required = false) Integer idCategoria,
            @RequestParam("ids") List<Integer> ids) {
        Map<Integer, ArchivoResponseDTO> portadas = archivoService
                .forModule(entityType, idCategoria)
                .getPortadaBatch(ids);
        return ResponseEntity.ok(ApiResponse.exito(200, "Portadas obtenidas correctamente", portadas));
    }

    /** Devuelve los datos de un archivo específico por su ID. */
    @GetMapping("/{idArchivo}")
    public ResponseEntity<ApiResponse<ArchivoResponseDTO>> getOne(@PathVariable Integer idArchivo) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Archivo encontrado",
                archivoService.getOne(idArchivo)));
    }

    /**
     * Cambia el número de orden de un archivo dentro de su módulo, lo que controla
     * la posición en que se muestra en galerías (el orden 1 es la portada).
     */
    @PatchMapping("/orden")
    public ResponseEntity<ApiResponse<ArchivoResponseDTO>> actualizarOrden(
            @Valid @RequestBody CambiarOrdenRequestDTO dto) {
        ArchivoResponseDTO actualizado = archivoService
                .forModule(dto.getEntityType(), dto.getIdCategoria())
                .actualizarOrden(dto.getIdArchivo(), dto.getNuevoOrden());
        return ResponseEntity.ok(ApiResponse.exito(200, "Orden actualizado correctamente", actualizado));
    }

    /** Elimina un archivo de Cloudinary y su registro en base de datos. */
    @DeleteMapping("/{idArchivo}")
    public ResponseEntity<Void> delete(@PathVariable Integer idArchivo) {
        archivoService.delete(idArchivo);
        return ResponseEntity.noContent().build();
    }

    private FileUploadRequestDTO parseMeta(String metaJson) {
        try {
            return objectMapper.readValue(metaJson, FileUploadRequestDTO.class);
        } catch (JsonProcessingException e) {
            throw new BusinessException(
                    "Metadata inválida en la petición: " + e.getOriginalMessage(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    private void validateMeta(FileUploadRequestDTO meta) {
        Set<ConstraintViolation<FileUploadRequestDTO>> violations = validator.validate(meta);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining("; "));
            throw new BusinessException(message, HttpStatus.BAD_REQUEST);
        }
    }
}
