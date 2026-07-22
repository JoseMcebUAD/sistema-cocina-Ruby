package com.cocinarubi.aop.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * MixIn de auditoría para Cliente.
 * Bloquea campos sensibles del snapshot sin modificar la entidad ni las respuestas HTTP.
 * sessionToken: token de sesión criptográfico.
 * ipAddress / userAgent: datos de fingerprinting del visitante (PII).
 */
public abstract class ClienteAuditMixin {
    @JsonIgnore abstract String getSessionToken();
    @JsonIgnore abstract String getIpAddress();
    @JsonIgnore abstract String getUserAgent();
}
