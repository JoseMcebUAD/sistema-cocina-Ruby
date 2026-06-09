package com.cocinarubi.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

/**
 * Horario de atención de un turno del negocio para un día de la semana.
 *
 * <p>Define cuándo está abierto el servicio de comidas (L-V 8:30–15:30) y el
 * de desayunos (L-S 7:00–11:00). El campo {@code atendiendo} actúa como
 * override manual: el operador puede abrir o cerrar el turno fuera del horario
 * configurado sin modificar el registro base.</p>
 *
 * <p>Relaciones: ninguna FK saliente ni entrante.</p>
 */
@Entity
@Table(name = "horario_atencion")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HorarioAtencion {

    /** Tipo de turno: menú de comidas del día o menú de desayunos. */
    public enum TipoHorario { DESAYUNO, COMIDAS }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_horario_atencion_comidas")
    private Integer idHorarioAtencionComidas;

    @Column(name = "hora_inicio_atencion_comidas", nullable = false)
    private LocalTime horaInicioAtencionComidas;

    @Column(name = "hora_cierre_atencion_comidas", nullable = false)
    private LocalTime horaCierreAtencionComidas;

    /**
     * Código de día: L=lunes, M=martes, X=miércoles, J=jueves, V=viernes, S=sábado.
     * Se usa CHAR(1) en la DB; se mapea como String de longitud 1.
     */
    @Column(name = "dia_semana", nullable = false, length = 1)
    private String diaSemana;

    /** Identifica si este registro corresponde al turno de comidas o de desayunos. */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_horario", nullable = false)
    private TipoHorario tipoHorario;

    /**
     * Override manual de apertura. 1 = abierto ahora mismo, independientemente
     * del horario configurado. El operador lo activa/desactiva desde el dashboard.
     */
    @Column(name = "atendiendo", nullable = false)
    private boolean atendiendo;
}
