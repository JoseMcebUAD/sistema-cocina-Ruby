package com.cocinarubi.domain.entity;

import com.cocinarubi.DBConstants.TipoCatalogoProducto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

/**
 * Producto marcado como favorito por un cliente en su sesión web.
 *
 * <p>El campo {@code id_producto} es una <strong>FK lógica polimórfica</strong>:
 * su tabla destino depende de {@code tipo_catalogo_producto}:</p>
 * <ul>
 *   <li>{@code COMIDA} → {@link Comida#idComida}</li>
 *   <li>{@code DESAYUNO} → {@link Desayuno#idDesayuno}</li>
 *   <li>{@code BASICO} → {@link Basico#idBasico}</li>
 *   <li>{@code SNACK / CHAROLA / BEBIDA} → {@link ProductoCocina#idProductoCocina}</li>
 * </ul>
 * <p>No existe FK formal en la base de datos; la integridad se valida en la
 * capa de servicio antes de persistir. Este patrón evita múltiples columnas
 * de FK nullable y tablas de unión adicionales.</p>
 *
 * <p>Si el cliente es eliminado ({@code session_token} desaparece), sus favoritos
 * también se eliminan en cascada (ON DELETE CASCADE).</p>
 *
 * <p>Relaciones: {@code @ManyToOne} LAZY a {@link Cliente} vía {@code session_token}.</p>
 */
@Entity
@Table(name = "favorito_cliente")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoritoCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_favorito_cliente")
    private Integer idFavoritoCliente;

    @Column(name = "id_producto", nullable = false)
    private Integer idProducto;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_token", referencedColumnName = "session_token", nullable = false)
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_catalogo_producto", nullable = false)
    private TipoCatalogoProducto tipoCatalogoProducto;
}
