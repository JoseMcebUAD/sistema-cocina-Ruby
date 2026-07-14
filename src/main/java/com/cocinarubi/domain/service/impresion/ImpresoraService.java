package com.cocinarubi.domain.service.impresion;

import java.util.Base64;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.ImpresionRequestDTO;
import com.cocinarubi.presentation.dto.response.pedido.EscPosBytesDTO;
import com.cocinarubi.presentation.dto.response.pedido.PedidoEscResponseDTO;
import com.cocinarubi.util.infra.UniversalPrintService;
import com.cocinarubi.util.template.BaseTicketTemplate;

/**
 * Orquestador del módulo de impresión. Selecciona la estrategia por tipo,
 * genera los bytes ESC/POS, los codifica en base64 para transporte JSON,
 * y dispara el hook post-impresión (ej. marcar impreso=true).
 */
@Service
public class ImpresoraService {

    private final ImpresionRegistry registry;
    private final UniversalPrintService universalPrintService;

    public ImpresoraService(ImpresionRegistry registry, UniversalPrintService universalPrintService) {
        this.registry = registry;
        this.universalPrintService = universalPrintService;
    }

    public EscPosBytesDTO imprimir(ImpresionRequestDTO req) {
        ImpresionStrategy strategy = registry.get(req.getTipo());
        BaseTicketTemplate<?> template = strategy.buildTemplate(req.getId());

        byte[] bytes = universalPrintService.generateEscPosBytes(template);
        if (bytes.length == 0) {
            throw new BusinessException(
                    "Error generando bytes ESC/POS", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        strategy.onPrintSuccess(req.getId());

        String base64 = Base64.getEncoder().encodeToString(bytes);
        return new PedidoEscResponseDTO(req.getId(), base64);
    }
}
