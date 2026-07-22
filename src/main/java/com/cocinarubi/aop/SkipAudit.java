package com.cocinarubi.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excluye un método o clase completa del registro automático de auditoría en AuditAspect.
 * <p>Nivel clase: todos los métodos POST/PUT/PATCH/DELETE del Controller son ignorados.</p>
 * <p>Nivel método: solo ese método específico es ignorado.</p>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SkipAudit {}
