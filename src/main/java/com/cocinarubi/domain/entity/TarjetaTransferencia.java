package com.cocinarubi.domain.entity;

import com.cocinarubi.DBConstants.Estatus;
import jakarta.persistence.*;
import lombok.*;

/**
 * Tarjeta bancaria registrada para recibir transferencias desde el checkout web.
 * Solo puede existir una tarjeta con estatus DISPONIBLE al mismo tiempo;
 * la invariante es garantizada por {@code TarjetaTransferenciaService}.
 * Capa: Entity — persistencia JPA.
 */
@Entity
@Table(name = "tarjeta_transferencia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TarjetaTransferencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tarjeta")
    private Integer idTarjeta;

    @Column(name = "clabe", length = 18, nullable = false)
    private String clabe;

    @Column(name = "numero_tarjeta", length = 19, nullable = false)
    private String numeroTarjeta;

    @Column(name = "nombre_destinatario", length = 100, nullable = false)
    private String nombreDestinatario;

    @Column(name = "banco", length = 60, nullable = false)
    private String banco;

    @Enumerated(EnumType.STRING)
    @Column(name = "estatus", nullable = false)
    private Estatus estatus;
}
