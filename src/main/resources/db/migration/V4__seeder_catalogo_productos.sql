SET NAMES utf8mb4;

-- =============================================================================
-- COMIDAS
-- =============================================================================

INSERT INTO comida (uuid_comida, nombre_comida, descripcion, precio_media, precio_entera, estatus, destacado) VALUES
    ('c1a2b3c4-d5e6-7890-abcd-ef1234567890', 'Pollo a la plancha',    'Pechuga de pollo a la plancha con limón y especias',          55.00,  95.00, 'DISPONIBLE',    1),
    ('a1b2c3d4-e5f6-7890-abcd-111222333444', 'Arroz con camarones',   'Arroz frito con camarones y verduras salteadas',              70.00, 120.00, 'DISPONIBLE',    0),
    ('b2c3d4e5-f6a7-8901-bcde-222333444555', 'Milanesa de res',       'Filete de res empanizado con puré de papa',                   65.00, 110.00, 'DISPONIBLE',    1),
    ('c3d4e5f6-a7b8-9012-cdef-333444555666', 'Pasta alfredo',         'Pasta con salsa cremosa alfredo y champiñones',               60.00, 100.00, 'NO_DISPONIBLE', 0),
    ('d4e5f6a7-b8c9-0123-defa-444555666777', 'Enchiladas verdes',     'Tres enchiladas con salsa verde y pollo deshebrado',          50.00,  85.00, 'DISPONIBLE',    0);

-- =============================================================================
-- COMPLEMENTOS
-- =============================================================================

INSERT INTO complemento (uuid_complemento, nombre_complemento, descripcion, precio_extra, estatus, destacado, cobrar_siempre) VALUES
    ('e5f6a7b8-c9d0-1234-efab-555666777888', 'Frijoles de la olla', 'Porción de frijoles negros de la olla',    0.00, 'DISPONIBLE', 0, 0),
    ('f6a7b8c9-d0e1-2345-fabc-666777888999', 'Aguacate',            'Medio aguacate en rodajas',               15.00, 'DISPONIBLE', 1, 1),
    ('a7b8c9d0-e1f2-3456-abcd-777888999000', 'Arroz blanco',        'Porción de arroz blanco al vapor',         0.00, 'DISPONIBLE', 0, 0),
    ('b8c9d0e1-f2a3-4567-bcde-888999000111', 'Crema y queso',       'Crema fresca con queso cotija rallado',   10.00, 'DISPONIBLE', 0, 1),
    ('c9d0e1f2-a3b4-5678-cdef-999000111222', 'Tortillas de maíz',   'Paquete de 4 tortillas hechas a mano',    0.00, 'DISPONIBLE', 0, 0);

-- =============================================================================
-- PRODUCTOS DE COCINA
-- =============================================================================

INSERT INTO producto_cocina (uuid_producto_cocina, nombre_producto, descripcion, precio_domicilio, precio_normal, estatus, destacado, tipo_producto) VALUES
    ('d0e1f2a3-b4c5-6789-defa-000111222333', 'Refresco de cola 600ml',   'Refresco de cola en presentación de 600ml',    25.00, 20.00, 'DISPONIBLE', 0, 'BEBIDA'),
    ('e1f2a3b4-c5d6-7890-efab-111222333444', 'Agua mineral 500ml',       'Agua mineral sin sabor 500ml',                 18.00, 15.00, 'DISPONIBLE', 0, 'BEBIDA'),
    ('f2a3b4c5-d6e7-8901-fabc-222333444555', 'Papas fritas pequeñas',    'Porción pequeña de papas fritas con sal',      30.00, 30.00, 'DISPONIBLE', 1, 'SNACK'),
    ('a3b4c5d6-e7f8-9012-abcd-333444555666', 'Charola individual',       'Charola con comida, arroz, frijoles y agua',   95.00, 85.00, 'DISPONIBLE', 1, 'CHAROLA'),
    ('b4c5d6e7-f8a9-0123-bcde-444555666777', 'Jugo de naranja natural',  'Jugo de naranja recién exprimido 350ml',       35.00, 30.00, 'AGOTADO',    0, 'BEBIDA');

-- =============================================================================
-- BÁSICOS
-- =============================================================================

INSERT INTO basico (id_comida, descripcion, destacado, precio_basico, estatus) VALUES
    (
        (SELECT id_comida FROM comida WHERE uuid_comida = 'c1a2b3c4-d5e6-7890-abcd-ef1234567890'),
        'Básico de pollo: pollo a la plancha con arroz y frijoles',
        1, 75.00, 'DISPONIBLE'
    ),
    (
        (SELECT id_comida FROM comida WHERE uuid_comida = 'a1b2c3d4-e5f6-7890-abcd-111222333444'),
        'Básico de camarones: arroz con camarones, frijoles y tortillas',
        0, 110.00, 'DISPONIBLE'
    );

-- =============================================================================
-- BÁSICO - COMPLEMENTOS
-- básico 1 (pollo)     -> frijoles de la olla + arroz blanco
-- básico 2 (camarones) -> arroz blanco + tortillas de maíz
-- =============================================================================

INSERT INTO basico_complemento (id_basico, id_complemento) VALUES
    (
        (SELECT id_basico FROM basico WHERE descripcion LIKE 'Básico de pollo%'),
        (SELECT id_complemento FROM complemento WHERE uuid_complemento = 'e5f6a7b8-c9d0-1234-efab-555666777888')
    ),
    (
        (SELECT id_basico FROM basico WHERE descripcion LIKE 'Básico de pollo%'),
        (SELECT id_complemento FROM complemento WHERE uuid_complemento = 'a7b8c9d0-e1f2-3456-abcd-777888999000')
    ),
    (
        (SELECT id_basico FROM basico WHERE descripcion LIKE 'Básico de camarones%'),
        (SELECT id_complemento FROM complemento WHERE uuid_complemento = 'a7b8c9d0-e1f2-3456-abcd-777888999000')
    ),
    (
        (SELECT id_basico FROM basico WHERE descripcion LIKE 'Básico de camarones%'),
        (SELECT id_complemento FROM complemento WHERE uuid_complemento = 'c9d0e1f2-a3b4-5678-cdef-999000111222')
    );
