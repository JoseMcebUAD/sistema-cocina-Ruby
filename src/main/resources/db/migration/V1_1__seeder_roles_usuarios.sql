SET NAMES utf8mb4;

-- =============================================================================
-- ROLES DE USUARIO
-- =============================================================================

INSERT INTO rol_usuario (nombre_rol, descripcion) VALUES
    ('JEFA_COCINA', 'Acceso total al sistema de cocina'),
    ('COCINA',      'Acceso operativo a pedidos y productos');

-- =============================================================================
-- USUARIOS INICIALES
-- =============================================================================

INSERT INTO usuario (id_rol, nombre_usuario, contrasena, creado_en, ultimo_login) VALUES
    (
        (SELECT id_rol FROM rol_usuario WHERE nombre_rol = 'JEFA_COCINA'),
        'rubi',
        '$2a$12$jhJxhT5qA94MHl9t46roJOo383JyrXwUhMA6fl6buvK7wZDbqrDYm',
        NOW(),
        NULL
    ),
    (
        (SELECT id_rol FROM rol_usuario WHERE nombre_rol = 'COCINA'),
        'ana',
        '$2a$12$Dy.8lP4pBCJw3YcxQbTZk.V.tKdAB..1dry71PNPYTixOP/9k51A6',
        NOW(),
        NULL
    ),
    (
        (SELECT id_rol FROM rol_usuario WHERE nombre_rol = 'COCINA'),
        'pedro',
        '$2a$12$krAj7xGqBqE9jq9ZbqFa3./3FekVWDbvO3JD7/js/RLvXbEi2WIPS',
        NOW(),
        NULL
    );
