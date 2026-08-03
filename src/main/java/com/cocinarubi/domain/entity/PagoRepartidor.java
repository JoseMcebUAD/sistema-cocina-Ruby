package com.cocinarubi.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Registro del pago diario al repartidor por zona de reparto (RF020).
 *
 * <p>Se utiliza en el resumen de caja diario para descontar del ingreso total
 * el monto pagado a cada repartidor por su ruta. Es un registro contable simple
 * que el operador genera al cerrar el turno.</p>
 *
 * <p>Reglas de negocio (aplicadas en {@code PagoRepartidorValidationImp}):
 * un único pago por día calendario, solo días hábiles (Lun–Vie) al crear, y el
 * monto no puede superar el ingreso total del día. El endpoint DELETE no está
 * expuesto: los registros se corrigen mediante PUT.</p>
 *
 * <p>⚠️ Bug conocido [BUG-1]: el DDL original referencia {@code id_ruta} en la
 * FK {@code fk_pago_repartidor_ruta}, pero esa columna <strong>no está declarada</strong>
 * en el CREATE TABLE. Por esta razón, la entidad no mapea relación a {@link Ruta};
 * la vinculación debe resolverse primero en el esquema SQL.</p>
 *
 * <p>Relaciones: ninguna (pendiente de corrección en el esquema).</p>
 */
@Entity
@Table(name = "pago_repartidor")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoRepartidor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago_repartidor")
    private Integer idPagoRepartidor;

    @Column(name = "pago")
    private BigDecimal pago;

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    @Column(name = "nombre_repartidor")
    private String nombreRepartidor;
}
