package com.cocinarubi.dao;

import com.cocinarubi.DBConstants.TipoCatalogoProducto;
import com.cocinarubi.domain.entity.Archivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Archivo. Provee consultas para obtener archivos
 * por entidad (tipo + id) y calcular el orden máximo para nuevas subidas.
 */
public interface ArchivoRepository extends JpaRepository<Archivo, Integer> {

    // Devuelve los archivos de una entidad ordenados por 'orden' ascendente
    List<Archivo> findByEntityTypeAndIdEntidadOrderByOrdenAsc(
            TipoCatalogoProducto entityType, Integer idEntidad);

    // Permite localizar un archivo directamente por su public_id de Cloudinary
    Optional<Archivo> findByPublicId(String publicId);

    // Retorna el orden más alto registrado para la entidad; 0 si no hay filas (COALESCE)
    @Query("SELECT COALESCE(MAX(a.orden), 0) FROM Archivo a " +
            "WHERE a.entityType = :type AND a.idEntidad = :idEntidad")
    Integer findMaxOrdenForEntity(
            @Param("type") TipoCatalogoProducto type,
            @Param("idEntidad") Integer idEntidad);

    // Batch: archivos de varias entidades del mismo tipo, ordenados por entidad y luego por orden
    @Query("SELECT a FROM Archivo a WHERE a.entityType = :type AND a.idEntidad IN :ids " +
            "ORDER BY a.idEntidad ASC, a.orden ASC")
    List<Archivo> findByEntityTypeAndIdEntidadIn(
            @Param("type") TipoCatalogoProducto type,
            @Param("ids") List<Integer> ids);
}
