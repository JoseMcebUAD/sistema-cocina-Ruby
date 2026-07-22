package com.cocinarubi.dao;

import com.cocinarubi.DBConstants;
import com.cocinarubi.domain.entity.Auditoria;
import com.cocinarubi.presentation.dto.response.AuditoriaResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Integer> {

    @Query(value = """
            SELECT new com.cocinarubi.presentation.dto.response.AuditoriaResponseDTO(
                a.idAuditoria, u.idUsuario, u.nombreUsuario,
                CASE
                  WHEN a.tabla = 'pedido'             THEN 'Pedidos'
                  WHEN a.tabla = 'comida'             THEN 'Comidas'
                  WHEN a.tabla = 'complemento'        THEN 'Complementos'
                  WHEN a.tabla = 'basico'             THEN 'Básicos'
                  WHEN a.tabla = 'desayuno'           THEN 'Desayunos'
                  WHEN a.tabla = 'cliente'            THEN 'Clientes'
                  WHEN a.tabla = 'producto_cocina'    THEN 'Productos de cocina'
                  WHEN a.tabla = 'inventario_comida'  THEN 'Contador de comidas'
                  WHEN a.tabla = 'usuario'            THEN 'Usuarios'
                  WHEN a.tabla = 'anuncio'            THEN 'Anuncios'
                  WHEN a.tabla = 'ruta'               THEN 'Rutas'
                  WHEN a.tabla = 'tarifa_especial'    THEN 'Tarifas especiales'
                  WHEN a.tabla = 'favorito_cliente'   THEN 'Favoritos'
                  WHEN a.tabla = 'codigo_cliente'     THEN 'Códigos de cliente'
                  WHEN a.tabla = 'pago_repartidor'    THEN 'Pagos a repartidor'
                  WHEN a.tabla = 'horario_atencion'   THEN 'Horarios'
                  ELSE 'Desconocido'
                END,
                a.creadoEn,
                a.tipoOperacion,
                a.datosDespues,
                a.idRegistro,
                a.tabla,
                a.datosAntes,
                a.datosDespues
            )
            FROM Auditoria a
            JOIN Usuario u ON u.idUsuario = a.idUsuario
            WHERE (:desde IS NULL OR a.creadoEn >= :desde)
              AND (:hasta IS NULL OR a.creadoEn <= :hasta)
              AND (:idUsuario IS NULL OR a.idUsuario = :idUsuario)
              AND (:tipoOperacion IS NULL OR a.tipoOperacion = :tipoOperacion)
            ORDER BY a.creadoEn DESC
            """,
            countQuery = """
            SELECT COUNT(a)
            FROM Auditoria a
            JOIN Usuario u ON u.idUsuario = a.idUsuario
            WHERE (:desde IS NULL OR a.creadoEn >= :desde)
              AND (:hasta IS NULL OR a.creadoEn <= :hasta)
              AND (:idUsuario IS NULL OR a.idUsuario = :idUsuario)
              AND (:tipoOperacion IS NULL OR a.tipoOperacion = :tipoOperacion)
            """)
    Page<AuditoriaResponseDTO> findConFiltros(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("idUsuario") Integer idUsuario,
            @Param("tipoOperacion") DBConstants.TipoOperacion tipoOperacion,
            Pageable pageable
    );
}
