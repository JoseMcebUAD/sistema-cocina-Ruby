package com.cocinarubi.entity;

import com.cocinarubi.DBConstants.TamanoPorcion;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Línea de pedido para un platillo del menú principal.
 *
 * <p>Representa una unidad de {@link Comida} dentro de un {@link Pedido}.
 * Almacena el {@code precio_unitario} en el momento de la orden (precio histórico),
 * de modo que cambios futuros al catálogo no alteren registros pasados.</p>
 *
 * <p>El cliente puede agregar {@link ComplementoComidaPedido}s específicos para
 * esta línea, independientemente de los complementos de otras líneas del mismo pedido.</p>
 *
 * <p>Relaciones:
 * <ul>
 *   <li>{@code @ManyToOne} LAZY a {@link Pedido} — orden a la que pertenece.</li>
 *   <li>{@code @ManyToOne} LAZY a {@link Comida} — platillo ordenado.</li>
 *   <li>{@code @OneToMany} LAZY a {@link ComplementoComidaPedido} — complementos de esta línea.</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "comida_pedido")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComidaPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comida_pedido")
    private Integer idComidaPedido;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_comida", nullable = false)
    private Comida comida;

    @Column(name = "precio_unitario", nullable = false, precision = 5, scale = 2)
    private BigDecimal precioUnitario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tamano_porcion", nullable = false)
    private TamanoPorcion tamanoPorcion;

    @Builder.Default
    @OneToMany(mappedBy = "comidaPedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ComplementoComidaPedido> complementos = new ArrayList<>();

    public void addComplemento(ComplementoComidaPedido complemento) {
        complemento.setComidaPedido(this);
        this.complementos.add(complemento);
    }
}
