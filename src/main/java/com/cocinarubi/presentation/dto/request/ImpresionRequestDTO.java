package com.cocinarubi.presentation.dto.request;

import com.cocinarubi.DBConstants.TipoEntidadImpresion;
import jakarta.validation.constraints.NotNull;

public class ImpresionRequestDTO {

    @NotNull(message = "El id es obligatorio")
    private Integer id;

    @NotNull(message = "El tipo de entidad es obligatorio")
    private TipoEntidadImpresion tipo;

    public ImpresionRequestDTO() {}

    public ImpresionRequestDTO(Integer id, TipoEntidadImpresion tipo) {
        this.id = id;
        this.tipo = tipo;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public TipoEntidadImpresion getTipo() { return tipo; }
    public void setTipo(TipoEntidadImpresion tipo) { this.tipo = tipo; }
}
