package com.cocinarubi.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Complemento extra elegido para una línea de paquete básico dentro de un pedido.
 *
 * <p>Nodo hoja: {@link Pedido} → {@link BasicoPedido} → {@code BasicoPedidoExtra}.
 * Almacena el {@code precio} del complemento al momento de la orden (precio histórico),
 * de modo que cambios futuros al catálogo no alteren registros pasados.
 * El campo {@code cobrarSiempre} del complemento no aplica aquí: los extras de un
 * básico siempre se cobran al ser elegidos explícitamente por el cliente.</p>
 *
 * <p>Se elimina en cascada si se borra el {@link BasicoPedido} padre.</p>
 *
 * <p>Relaciones:
 * <ul>
 *   <li>{@code @ManyToOne} LAZY a {@link BasicoPedido} — línea de básico propietaria.</li>
 *   <li>{@code @ManyToOne} LAZY a {@link Complemento} — acompañamiento elegido.</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "basico_pedido_extra")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BasicoPedidoExtra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_basico_pedido_extra")
    private Integer idBasicoPedidoExtra;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_basico_pedido")
    private BasicoPedido basicoPedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_complemento")
    private Complemento complemento;

    @Column(name = "cantidad")
    private byte cantidad;

    @Column(name = "precio")
    private BigDecimal precio;
}
