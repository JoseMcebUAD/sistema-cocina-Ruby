package com.cocinarubi.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Visitante anónimo del menú web de Cocina Ruby.
 *
 * <p>No requiere registro formal. El cliente se identifica mediante un
 * {@code uuid_cliente} generado en {@code localStorage} del navegador y
 * un {@code session_token} único. Los campos de ubicación, dirección y nombre
 * son caché de UX para mejorar la experiencia: <strong>no son la fuente de
 * verdad de entrega</strong>. La dirección real siempre se almacena en
 * {@link PedidoDomicilio}.</p>
 *
 * <p>El campo {@code id_ruta} es un caché de la última ruta detectada via cookie.
 * No debe usarse para calcular el costo de envío de un pedido; para eso se usa
 * {@link PedidoDomicilio#idRuta}.</p>
 *
 * <p>Relaciones: {@code @ManyToOne} LAZY a {@link Ruta} (nullable).</p>
 */
@Entity
@Table(name = "cliente")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Integer idCliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ruta")
    private Ruta ruta;

    @Column(name = "uuid_cliente", nullable = false, length = 45)
    private String uuidCliente;

    @Column(name = "session_token", nullable = false, unique = true, length = 255)
    private String sessionToken;

    @Column(name = "codigo_cliente", length = 255)
    private String codigoCliente;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "ubicacion_latitud", precision = 10, scale = 7)
    private BigDecimal ubicacionLatitud;

    @Column(name = "ubicacion_longitud", precision = 10, scale = 7)
    private BigDecimal ubicacionLongitud;

    @Column(name = "nombre", length = 255)
    private String nombre;

    @Column(name = "direccion_cliente", length = 255)
    private String direccionCliente;

    @Column(name = "telefono", length = 16)
    private String telefono;
}
