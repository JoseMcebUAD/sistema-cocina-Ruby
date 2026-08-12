package com.cocinarubi.presentation.dto.response.graficas;

import java.math.BigDecimal;

public record DatoGraficaRecord(
    BigDecimal valor, 
    String leyenda
    
) {}
