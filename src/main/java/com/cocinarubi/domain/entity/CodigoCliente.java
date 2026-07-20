package com.cocinarubi.domain.entity;

import com.cocinarubi.DBConstants.Estatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Código de acceso especial para clientes que están fuera del área de rutas estándar.
 *
 * <p>El operador crea un código con nombre legible (ej. "Código Susanita") y lo
 * entrega al cliente. Cuando el cliente lo ingresa en el menú web, el sistema
 * le aplica la {@code tarifaEspecial} correspondiente en lugar de calcular la
 * tarifa por ruta geográfica.</p>
 *
 * <p>La relación con {@link Cliente} es <strong>lógica</strong> (validada en la
 * capa de aplicación comparando {@code codigo_cliente} del cliente con
 * {@code codigo_cliente} de esta tabla). No hay FK formal en la base de datos.</p>
 */
@Entity
@Table(name = "codigo_cliente")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodigoCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_codigo_cliente")
    private Integer idCodigoCliente;

    @Column(name = "identificador")
    private String identificador;

    @Column(name = "codigo_cliente")
    private String codigoCliente;

    @Column(name = "tarifa_especial")
    private BigDecimal tarifaEspecial;

    @Enumerated(EnumType.STRING)
    @Column(name = "estatus")
    private Estatus estatus;
}
