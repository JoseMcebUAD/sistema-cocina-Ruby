package com.cocinarubi.aop.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * MixIn de auditoría para Pedido.
 * uuidCliente: identificador anónimo del cliente web (PII).
 */
public abstract class PedidoAuditMixin {
    @JsonIgnore abstract String getUuidCliente();
}
