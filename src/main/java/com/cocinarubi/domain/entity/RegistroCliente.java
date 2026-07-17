package com.cocinarubi.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

/**
 * Directorio reutilizable de clientes para pedidos creados desde COCINA.
 *
 * <p>A diferencia de {@link Cliente} (visitante anónimo del menú web), un
 * {@code RegistroCliente} es creado manualmente por un operador del dashboard.
 * Puede asociarse a múltiples pedidos y permite autocompletar datos de entrega
 * buscando por número de teléfono.</p>
 *
 * <p>El campo {@code id_ruta} y {@code direccion} son la dirección habitual del
 * cliente. Pueden diferir de los datos reales de cada entrega, que quedan
 * registrados en {@link PedidoDomicilioCocina}.</p>
 */
@Entity
@Table(name = "registro_cliente")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_registro_cliente")
    private Integer idRegistroCliente;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "telefono", nullable = false, length = 16)
    private String telefono;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ruta")
    private Ruta ruta;

    @Column(name = "direccion", length = 255)
    private String direccion;
}
