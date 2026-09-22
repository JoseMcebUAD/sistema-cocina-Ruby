package com.cocinarubi.aop.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * MixIn de auditoría para Cliente.
 * Bloquea campos sensibles del snapshot sin modificar la entidad ni las respuestas HTTP.
 * sessionTokenHash: hash SHA-256 del token de sesion (aunque es hash, no aporta valor al log).
 * ipAddress / userAgent: datos de fingerprinting del visitante (PII).
 */
public abstract class ClienteAuditMixin {
    @JsonIgnore abstract String getSessionTokenHash();
    @JsonIgnore abstract String getIpAddress();
    @JsonIgnore abstract String getUserAgent();
}
