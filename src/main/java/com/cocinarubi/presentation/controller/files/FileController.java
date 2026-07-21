package com.cocinarubi.presentation.controller.files;

import com.cocinarubi.DBConstants.TipoCatalogoProducto;
import com.cocinarubi.domain.interfaces.FileUploadService;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.FileUploadRequestDTO;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.presentation.dto.response.ArchivoResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
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
 */
@RestController
@RequestMapping("/files")
@Tag(name = "Archivos", description = "Subir, eliminar o consultar los archivos alojados en Cloudinary")
public class FileController {

    private final FileUploadService fileUploadService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public FileController(FileUploadService fileUploadService,
                          ObjectMapper objectMapper,
                          Validator validator) {
        this.fileUploadService = fileUploadService;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<ArchivoResponseDTO>>> upload(
            @RequestPart("meta") String metaJson,
            @RequestPart("files") MultipartFile[] files) {

        // Deserializa el JSON de metadata y valida con Jakarta Validation de forma manual
        FileUploadRequestDTO meta = parseMeta(metaJson);
        validateMeta(meta);

        // Delega al servicio la validación, subida a Cloudinary y persistencia
        List<ArchivoResponseDTO> archivos = fileUploadService.upload(meta, files);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito(201, "Archivos subidos correctamente", archivos));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ArchivoResponseDTO>>> getAll(
            @RequestParam("entityType") TipoCatalogoProducto entityType,
            @RequestParam("idEntidad") Integer idEntidad) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Archivos obtenidos correctamente",
                fileUploadService.getAll(entityType, idEntidad)));
    }

    @GetMapping("/batch")
    public ResponseEntity<ApiResponse<Map<Integer, List<ArchivoResponseDTO>>>> getBatch(
            @RequestParam("entityType") TipoCatalogoProducto entityType,
            @RequestParam("ids") List<Integer> ids) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Archivos obtenidos correctamente",
                fileUploadService.getAllBatch(entityType, ids)));
    }

    @GetMapping("/portada")
    public ResponseEntity<ApiResponse<Map<Integer, ArchivoResponseDTO>>> getPortada(
            @RequestParam("entityType") TipoCatalogoProducto entityType,
            @RequestParam("ids") List<Integer> ids) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Portadas obtenidas correctamente",
                fileUploadService.getPortadaBatch(entityType, ids)));
    }

    @GetMapping("/{idArchivo}")
    public ResponseEntity<ApiResponse<ArchivoResponseDTO>> getOne(@PathVariable Integer idArchivo) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Archivo encontrado",
                fileUploadService.getOne(idArchivo)));
    }

    @DeleteMapping("/{idArchivo}")
    public ResponseEntity<Void> delete(@PathVariable Integer idArchivo) {
        fileUploadService.delete(idArchivo);
        return ResponseEntity.noContent().build();
    }

    private FileUploadRequestDTO parseMeta(String metaJson) {
        try {
            // El @RequestPart como String evita conflictos de content-type en multipart
            return objectMapper.readValue(metaJson, FileUploadRequestDTO.class);
        } catch (JsonProcessingException e) {
            throw new BusinessException(
                    "Metadata inválida en la petición: " + e.getOriginalMessage(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    private void validateMeta(FileUploadRequestDTO meta) {
        // Reemplaza @Valid porque el DTO llega como String y no como objeto tipado en multipart
        Set<ConstraintViolation<FileUploadRequestDTO>> violations = validator.validate(meta);
        if (!violations.isEmpty()) {
            // Concatena todos los mensajes de violación en uno solo para la respuesta de error
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining("; "));
            throw new BusinessException(message, HttpStatus.BAD_REQUEST);
        }
    }
}
