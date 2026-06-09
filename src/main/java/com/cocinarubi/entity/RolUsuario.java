package com.cocinarubi.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

/**
 * Rol de acceso asignado a un operador del dashboard.
 *
 * <p>Catálogo fijo que define los permisos de cada usuario del sistema interno
 * (p.ej. ADMIN, CAJERO, COCINERO). Se usa en {@link Usuario} para determinar
 * qué rutas y acciones están disponibles en el dashboard.</p>
 *
 * <p>Relaciones: ninguna FK saliente. {@link Usuario} referencia esta tabla.</p>
 */
@Entity
@Table(name = "rol_usuario")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Integer idRol;

    @Column(name = "nombre_rol", nullable = false, length = 50)
    private String nombreRol;

    @Column(name = "descripcion", length = 100)
    private String descripcion;
}
