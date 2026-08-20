package com.cocinarubi.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Línea de pedido para un paquete básico.
 *
 * <p>Representa una unidad de {@link Basico} dentro de un {@link Pedido}.
 * Almacena el {@code precio_unitario} en el momento de la orden (precio histórico),
 * de modo que cambios futuros al catálogo no alteren registros pasados.</p>
 *
 * <p>Relaciones:
 * <ul>
 *   <li>{@code @ManyToOne} LAZY a {@link Pedido} — orden a la que pertenece.</li>
 *   <li>{@code @ManyToOne} LAZY a {@link Basico} — paquete básico ordenado.</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "basico_pedido")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BasicoPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_basico_pedido")
    private Integer idBasicoPedido;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido")
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_basico")
    private Basico basico;

    @Column(name = "precio_unitario")
    private BigDecimal precioUnitario;

    @Builder.Default
    @OneToMany(mappedBy = "basicoPedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BasicoPedidoExtra> extras = new ArrayList<>();

    public void addExtra(BasicoPedidoExtra item) {
        item.setBasicoPedido(this);
        this.extras.add(item);
    }
}
