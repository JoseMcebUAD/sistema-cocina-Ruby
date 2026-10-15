package com.cocinarubi.domain.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.cocinarubi.dao.AnuncioRepository;
import com.cocinarubi.domain.entity.Anuncio;
import com.cocinarubi.domain.mapper.AnuncioMapper;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.AnuncioRequestDTO;
import com.cocinarubi.presentation.dto.response.AnuncioResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Gestiona los anuncios visibles para los clientes (promociones, avisos, productos destacados, etc.).
 * Capa: Service — lógica de negocio de anuncios, incluyendo subida de imagen a Cloudinary.
 */
@Service
public class AnuncioService {

    private static final String CARPETA_CLOUDINARY = "anuncios";

    // Magic bytes para detección real de MIME (misma lógica que ArchivoUploader)
    private static final byte[] FIRMA_JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] FIRMA_PNG  = {(byte) 0x89, 0x50, 0x4E, 0x47};
    private static final byte[] FIRMA_GIF  = {0x47, 0x49, 0x46, 0x38};
    private static final byte[] FIRMA_WEBP = {0x52, 0x49, 0x46, 0x46};

    private final AnuncioRepository anuncioRepository;
    private final AnuncioMapper anuncioMapper;
    // Cloudinary: cliente para subida directa de la imagen del anuncio
    private final Cloudinary cloudinary;

    public AnuncioService(AnuncioRepository anuncioRepository,
                          AnuncioMapper anuncioMapper,
                          Cloudinary cloudinary) {
        this.anuncioRepository = anuncioRepository;
        this.anuncioMapper = anuncioMapper;
        this.cloudinary = cloudinary;
    }

    public List<AnuncioResponseDTO> findAll() {
        return anuncioMapper.toResponseList(anuncioRepository.findAllOrdered());
    }

    /** Retorna solo los anuncios con {@code activo = true}, usado por el menú web del cliente. */
    public List<AnuncioResponseDTO> findActivos() {
        return anuncioMapper.toResponseList(anuncioRepository.findAllActivosOrdered());
    }

    public AnuncioResponseDTO findById(int id) {
        return anuncioMapper.toResponse(findEntityById(id));
    }

    @Transactional
    public AnuncioResponseDTO save(AnuncioRequestDTO dto) {
        Anuncio anuncio = anuncioMapper.toEntity(dto);
        return anuncioMapper.toResponse(anuncioRepository.save(anuncio));
    }

    /**
     * Reemplaza por completo los datos de un anuncio existente.
     * La imagen no se modifica con este método; usar {@link #uploadImagen} para eso.
     */
    @Transactional
    public AnuncioResponseDTO update(int id, AnuncioRequestDTO dto) {
        Anuncio existente = findEntityById(id);
        Anuncio actualizado = anuncioMapper.toEntity(dto);
        // Conservar imagen y PK del anuncio existente
        actualizado.setIdAnuncio(existente.getIdAnuncio());
        actualizado.setImagenUrl(existente.getImagenUrl());
        actualizado.setImagenPublicId(existente.getImagenPublicId());
        return anuncioMapper.toResponse(anuncioRepository.save(actualizado));
    }

    /**
     * Sube o reemplaza la imagen del anuncio en Cloudinary.
     * Valida el tipo real del archivo por magic bytes antes de subir.
     */
    @Transactional
    public AnuncioResponseDTO uploadImagen(int id, MultipartFile file) {
        Anuncio anuncio = findEntityById(id);

        try {
            byte[] bytes = file.getBytes();
            validarMime(bytes);

            // Eliminar imagen anterior si existe
            if (anuncio.getImagenPublicId() != null) {
                cloudinary.uploader().destroy(anuncio.getImagenPublicId(), ObjectUtils.emptyMap());
            }

            // Subir nueva imagen a Cloudinary
            Map<?, ?> resultado = cloudinary.uploader().upload(bytes,
                    ObjectUtils.asMap("folder", CARPETA_CLOUDINARY));

            anuncio.setImagenUrl((String) resultado.get("secure_url"));
            anuncio.setImagenPublicId((String) resultado.get("public_id"));

            return anuncioMapper.toResponse(anuncioRepository.save(anuncio));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Error al subir la imagen: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /** Elimina la imagen del anuncio de Cloudinary y limpia los campos en BD. */
    @Transactional
    public AnuncioResponseDTO deleteImagen(int id) {
        Anuncio anuncio = findEntityById(id);
        if (anuncio.getImagenPublicId() == null) {
            throw new BusinessException("El anuncio no tiene imagen", HttpStatus.BAD_REQUEST);
        }
        try {
            cloudinary.uploader().destroy(anuncio.getImagenPublicId(), ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new BusinessException("Error al eliminar la imagen de Cloudinary: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        anuncio.setImagenUrl(null);
        anuncio.setImagenPublicId(null);
        return anuncioMapper.toResponse(anuncioRepository.save(anuncio));
    }

    /** Elimina el anuncio y su imagen de Cloudinary si la tiene. */
    @Transactional
    public void delete(int id) {
        Anuncio anuncio = findEntityById(id);
        if (anuncio.getImagenPublicId() != null) {
            try {
                cloudinary.uploader().destroy(anuncio.getImagenPublicId(), ObjectUtils.emptyMap());
            } catch (Exception ignored) {
                // La entidad se borra de todos modos aunque Cloudinary falle
            }
        }
        anuncioRepository.delete(anuncio);
    }

    private Anuncio findEntityById(int id) {
        return anuncioRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Anuncio no encontrado con id: " + id, HttpStatus.NOT_FOUND));
    }

    private void validarMime(byte[] bytes) {
        if (bytes.length < 4) {
            throw new BusinessException("Archivo demasiado pequeño", HttpStatus.BAD_REQUEST);
        }
        if (empiezaCon(bytes, FIRMA_JPEG) || empiezaCon(bytes, FIRMA_PNG)
                || empiezaCon(bytes, FIRMA_GIF) || empiezaCon(bytes, FIRMA_WEBP)) {
            return;
        }
        throw new BusinessException("Tipo de imagen no permitido. Use JPEG, PNG, GIF o WebP.",
                HttpStatus.BAD_REQUEST);
    }

    private boolean empiezaCon(byte[] bytes, byte[] firma) {
        if (bytes.length < firma.length) return false;
        for (int i = 0; i < firma.length; i++) {
            if (bytes[i] != firma[i]) return false;
        }
        return true;
    }
}
