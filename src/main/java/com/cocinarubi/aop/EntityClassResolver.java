package com.cocinarubi.aop;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Resuelve el nombre de una tabla (snake_case) a la clase @Entity correspondiente.
 * Usado por AuditAspect para cargar el snapshot previo (datos_antes) vía
 * EntityManager.find() en operaciones PUT/PATCH. El mapa se construye una sola vez
 * al arrancar la app.
 */
@Component
public class EntityClassResolver {

    private static final Logger log = LoggerFactory.getLogger(EntityClassResolver.class);
    private static final String ENTITY_PACKAGE = "com.cocinarubi.domain.entity";

    private final Map<String, Class<?>> tablaAClase = new HashMap<>();

    @PostConstruct
    void escanear() {
        ClassPathScanningCandidateComponentProvider provider =
                new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(Entity.class));

        provider.findCandidateComponents(ENTITY_PACKAGE).forEach(bd -> {
            try {
                Class<?> clazz = Class.forName(bd.getBeanClassName());
                String tabla = obtenerNombreTabla(clazz);
                tablaAClase.put(tabla, clazz);
            } catch (ClassNotFoundException e) {
                log.warn("EntityClassResolver: no se pudo cargar {}", bd.getBeanClassName());
            }
        });

        log.info("EntityClassResolver: registradas {} entidades", tablaAClase.size());
    }

    public Optional<Class<?>> resolver(String tabla) {
        return Optional.ofNullable(tablaAClase.get(tabla));
    }

    private String obtenerNombreTabla(Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        if (table != null && !table.name().isBlank()) {
            return table.name();
        }
        return clazz.getSimpleName()
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .toLowerCase();
    }
}
