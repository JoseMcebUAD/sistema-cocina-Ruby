package com.cocinarubi.dao;

import com.cocinarubi.DBConstants.TipoCatalogoProducto;
import com.cocinarubi.domain.entity.Archivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Archivo. Provee consultas para obtener archivos
 * por entidad y calcular el orden máximo. Cada método viene en dos variantes:
 * por {@code entityType} (módulos estáticos) y por {@code idCategoria}
 * (módulos dinámicos generados a partir de {@code Categoria}).
 */
public interface ArchivoRepository extends JpaRepository<Archivo, Integer> {

    // ── Vía enum estático (COMIDA / DESAYUNO / BASICO) ────────────────────────

    List<Archivo> findByEntityTypeAndIdEntidadOrderByOrdenAsc(
            TipoCatalogoProducto entityType, Integer idEntidad);

    Optional<Archivo> findByPublicId(String publicId);

    @Query("SELECT COALESCE(MAX(a.orden), 0) FROM Archivo a " +
            "WHERE a.entityType = :type AND a.idEntidad = :idEntidad")
    Integer findMaxOrdenForEntity(
            @Param("type") TipoCatalogoProducto type,
            @Param("idEntidad") Integer idEntidad);

    @Query("SELECT a FROM Archivo a WHERE a.entityType = :type AND a.idEntidad IN :ids " +
            "ORDER BY a.idEntidad ASC, a.orden ASC")
    List<Archivo> findByEntityTypeAndIdEntidadIn(
            @Param("type") TipoCatalogoProducto type,
            @Param("ids") List<Integer> ids);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Archivo a SET a.orden = a.orden + 1 " +
            "WHERE a.entityType = :type AND a.idEntidad = :idEntidad AND a.orden BETWEEN :from AND :to")
    void incrementOrdenBetween(
            @Param("type") TipoCatalogoProducto type,
            @Param("idEntidad") Integer idEntidad,
            @Param("from") Integer from,
            @Param("to") Integer to);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Archivo a SET a.orden = a.orden - 1 " +
            "WHERE a.entityType = :type AND a.idEntidad = :idEntidad AND a.orden BETWEEN :from AND :to")
    void decrementOrdenBetween(
            @Param("type") TipoCatalogoProducto type,
            @Param("idEntidad") Integer idEntidad,
            @Param("from") Integer from,
            @Param("to") Integer to);

    // ── Vía FK a categoria (módulos dinámicos) ────────────────────────────────

    @Query("SELECT a FROM Archivo a " +
            "WHERE a.categoria.idCategoria = :idCategoria AND a.idEntidad = :idEntidad " +
            "ORDER BY a.orden ASC")
    List<Archivo> findByCategoriaAndIdEntidadOrderByOrdenAsc(
            @Param("idCategoria") Integer idCategoria,
            @Param("idEntidad") Integer idEntidad);

    @Query("SELECT COALESCE(MAX(a.orden), 0) FROM Archivo a " +
            "WHERE a.categoria.idCategoria = :idCategoria AND a.idEntidad = :idEntidad")
    Integer findMaxOrdenForEntityCategoria(
            @Param("idCategoria") Integer idCategoria,
            @Param("idEntidad") Integer idEntidad);

    @Query("SELECT a FROM Archivo a WHERE a.categoria.idCategoria = :idCategoria AND a.idEntidad IN :ids " +
            "ORDER BY a.idEntidad ASC, a.orden ASC")
    List<Archivo> findByCategoriaAndIdEntidadIn(
            @Param("idCategoria") Integer idCategoria,
            @Param("ids") List<Integer> ids);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Archivo a SET a.orden = a.orden + 1 " +
            "WHERE a.categoria.idCategoria = :idCategoria AND a.idEntidad = :idEntidad " +
            "  AND a.orden BETWEEN :from AND :to")
    void incrementOrdenBetweenCategoria(
            @Param("idCategoria") Integer idCategoria,
            @Param("idEntidad") Integer idEntidad,
            @Param("from") Integer from,
            @Param("to") Integer to);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Archivo a SET a.orden = a.orden - 1 " +
            "WHERE a.categoria.idCategoria = :idCategoria AND a.idEntidad = :idEntidad " +
            "  AND a.orden BETWEEN :from AND :to")
    void decrementOrdenBetweenCategoria(
            @Param("idCategoria") Integer idCategoria,
            @Param("idEntidad") Integer idEntidad,
            @Param("from") Integer from,
            @Param("to") Integer to);
}
