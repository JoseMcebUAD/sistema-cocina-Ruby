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
        'ruby',
        '$2a$12$j0S.0y07VUEp78skArDlpOTLYcHkr.c5gW3P./AcY15BabnDlKMhK',
        NOW(),
        NULL
    ),
    (
        (SELECT id_rol FROM rol_usuario WHERE nombre_rol = 'COCINA'),
        'ana',
        '$2a$12$BOe4CQKgy9E8EqLYl0klKuOTa122gozBZRooohLpek8am19QL51Tq',
        NOW(),
        NULL
    ),
    (
        (SELECT id_rol FROM rol_usuario WHERE nombre_rol = 'COCINA'),
        'pedro',
        '$2a$12$cjSdR5p53VLiCbBX9dVmuu1beOzKUFf02BbQR.E7q1TLRHiolJieO',
        NOW(),
        NULL
    );
