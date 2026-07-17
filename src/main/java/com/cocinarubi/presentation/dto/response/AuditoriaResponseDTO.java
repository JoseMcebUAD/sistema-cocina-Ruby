package com.cocinarubi.presentation.dto.response;

import com.cocinarubi.DBConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditoriaResponseDTO {

    private int idAuditoria;
    private int idUsuario;
    private String nombreUsuario;
    private String nombreTabla;

    @JsonFormat(pattern = "eee yyyy/M/d HH:mm", locale = "es")
    private LocalDateTime fecha;

    private DBConstants.TipoOperacion accion;
    private String descripcion;
    private Integer idRegistro;

    @JsonIgnore private String tabla;
    @JsonIgnore private String datosAntes;
    @JsonIgnore private String datosDespues;

    public AuditoriaResponseDTO(int idAuditoria,
                                Integer idUsuario,
                                String nombreUsuario,
                                String nombreTabla,
                                LocalDateTime fecha,
                                DBConstants.TipoOperacion accion,
                                String descripcion,
                                Integer idRegistro,
                                String tabla,
                                String datosAntes,
                                String datosDespues) {
        this.idAuditoria = idAuditoria;
        this.idUsuario = idUsuario != null ? idUsuario : 0;
        this.nombreUsuario = nombreUsuario;
        this.nombreTabla = nombreTabla;
        this.fecha = fecha;
        this.accion = accion;
        this.descripcion = descripcion;
        this.idRegistro = idRegistro;
        this.tabla = tabla;
        this.datosAntes = datosAntes;
        this.datosDespues = datosDespues;
    }
}
