package com.cocinarubi.domain.entity;

import com.cocinarubi.util.StringListConverter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Directorio reutilizable de clientes para pedidos creados desde COCINA.
 *
 * <p>A diferencia de {@link Cliente} (visitante anónimo del menú web), un
 * {@code RegistroCliente} es creado manualmente por un operador del dashboard.
 * Puede asociarse a múltiples pedidos y permite autocompletar datos de entrega
 * buscando por número de teléfono.</p>
 *
 * <p>El campo {@code id_ruta} es la ruta habitual del cliente. {@code direcciones}
 * es la lista de direcciones guardadas; cada elemento es un string libre.
 * Pueden diferir de los datos reales de cada entrega, que quedan
 * registrados en {@link PedidoDomicilioCocina}.</p>
 */
@Entity
@Table(name = "registro_cliente", indexes = {
        @Index(name = "idx_registro_cliente_nombre", columnList = "nombre")
})
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

    @Convert(converter = StringListConverter.class)
    @Column(name = "direccion", columnDefinition = "JSON")
    private List<String> direcciones;
}
