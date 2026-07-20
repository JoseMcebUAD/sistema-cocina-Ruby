SET NAMES utf8mb4;

-- =============================================================================
-- RUTAS DE REPARTO
-- Polígonos aproximados para zonas de entrega a domicilio.
-- Se usan con ST_Contains(boundary, POINT(lng, lat)) en queries nativas.
-- =============================================================================

INSERT INTO ruta (nombre, boundary, is_active, tarifa_envio, tiempo_estimado_min) VALUES
    (
        'Zona Centro',
        ST_GeomFromText('POLYGON((-103.3520 20.6680, -103.3390 20.6680, -103.3390 20.6590, -103.3520 20.6590, -103.3520 20.6680))'),
        1, 25.00, 20
    ),
    (
        'Zona Norte',
        ST_GeomFromText('POLYGON((-103.3560 20.6780, -103.3390 20.6780, -103.3390 20.6680, -103.3560 20.6680, -103.3560 20.6780))'),
        1, 35.00, 30
    ),
    (
        'Zona Sur',
        ST_GeomFromText('POLYGON((-103.3520 20.6590, -103.3390 20.6590, -103.3390 20.6490, -103.3520 20.6490, -103.3520 20.6590))'),
        1, 35.00, 30
    ),
    (
        'Zona Oriente',
        ST_GeomFromText('POLYGON((-103.3390 20.6720, -103.3250 20.6720, -103.3250 20.6590, -103.3390 20.6590, -103.3390 20.6720))'),
        1, 45.00, 40
    ),
    (
        'Zona Poniente',
        ST_GeomFromText('POLYGON((-103.3680 20.6720, -103.3520 20.6720, -103.3520 20.6590, -103.3680 20.6590, -103.3680 20.6720))'),
        0, 50.00, 45
    );

-- =============================================================================
-- DESAYUNOS
-- Disponibles en modalidad PICK_UP, L-S de 7:00 a 11:00 h.
-- =============================================================================

INSERT INTO desayuno (uuid_desayuno, nombre_desayuno, descripcion, precio_media, precio_entera, estatus, destacado) VALUES
    ('d1a2b3c4-e5f6-7890-abcd-ef1234567801', 'Chilaquiles rojos',      'Chilaquiles con salsa roja, crema, queso y pollo deshebrado',     42.00,  72.00, 'DISPONIBLE',    1),
    ('d2b3c4d5-f6a7-8901-bcde-ef2345678902', 'Huevos rancheros',       'Dos huevos estrellados sobre tortilla con salsa ranchera',        38.00,  65.00, 'DISPONIBLE',    1),
    ('d3c4d5e6-a7b8-9012-cdef-ef3456789003', 'Molletes con pico',      'Bolillo tostado con frijoles, queso gratinado y pico de gallo',   35.00,  60.00, 'DISPONIBLE',    0),
    ('d4d5e6f7-b8c9-0123-defa-ef4567890104', 'Hotcakes con fruta',     'Torre de hotcakes con miel de maple y fruta de temporada',        45.00,  78.00, 'DISPONIBLE',    1),
    ('d5e6f7a8-c9d0-1234-efab-ef5678901205', 'Quesadillas de huitlacoche', 'Dos quesadillas de maíz rellenas de huitlacoche y queso',     40.00,  68.00, 'NO_DISPONIBLE', 0);
