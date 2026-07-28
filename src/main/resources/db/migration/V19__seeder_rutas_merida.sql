SET NAMES utf8mb4;

-- =============================================================================
-- RUTAS DE REPARTO — MÉRIDA, YUCATÁN
-- Reutiliza los 7 registros existentes (UPDATE) y agrega los 8 faltantes (INSERT).
-- Polígonos aproximados; reemplazar con boundaries reales cuando estén disponibles.
-- Índice único en `orden`: existentes conservan 1-7, nuevas van de 8 a 15.
-- =============================================================================

-- orden 1: Zona Centro → Altabrisa
UPDATE ruta
SET nombre   = 'Altabrisa',
    boundary = ST_GeomFromText('POLYGON((
        -89.597820 21.029125,
        -89.584612 21.029380,
        -89.584318 21.020642,
        -89.591274 21.018956,
        -89.598104 21.020214,
        -89.597820 21.029125
    ))')
WHERE nombre = 'Zona Centro';

-- orden 2: Zona Norte → Cholul
UPDATE ruta
SET nombre   = 'Cholul',
    boundary = ST_GeomFromText('POLYGON((
        -89.576043 21.087318,
        -89.561527 21.088042,
        -89.560893 21.077214,
        -89.568740 21.075528,
        -89.576381 21.076954,
        -89.576043 21.087318
    ))')
WHERE nombre = 'Zona Norte';

-- orden 3: Zona Sur → Jardines del Norte
UPDATE ruta
SET nombre   = 'Jardines del Norte',
    boundary = ST_GeomFromText('POLYGON((
        -89.631648 21.045438,
        -89.617832 21.045912,
        -89.617418 21.036804,
        -89.624705 21.035118,
        -89.631962 21.036546,
        -89.631648 21.045438
    ))')
WHERE nombre = 'Zona Sur';

-- orden 4: Zona Oriente → José María Iturralde
UPDATE ruta
SET nombre   = 'José María Iturralde',
    boundary = ST_GeomFromText('POLYGON((
        -89.649736 21.022054,
        -89.635920 21.022528,
        -89.635506 21.013420,
        -89.642793 21.011734,
        -89.650050 21.013162,
        -89.649736 21.022054
    ))')
WHERE nombre = 'Zona Oriente';

-- orden 5: Zona Poniente → Maya
UPDATE ruta
SET nombre   = 'Maya',
    boundary = ST_GeomFromText('POLYGON((
        -89.608124 21.033748,
        -89.594308 21.034222,
        -89.593894 21.025114,
        -89.601181 21.023428,
        -89.608438 21.024856,
        -89.608124 21.033748
    ))')
WHERE nombre = 'Zona Poniente';

-- orden 6: Ruta general → México Norte
UPDATE ruta
SET nombre   = 'México Norte',
    boundary = ST_GeomFromText('POLYGON((
        -89.636892 21.051592,
        -89.623076 21.052066,
        -89.622662 21.042958,
        -89.629949 21.041272,
        -89.637206 21.042700,
        -89.636892 21.051592
    ))')
WHERE nombre = 'Ruta general';

-- orden 7: Santa Gertrudis → Santa Gertrudis Copó La Isla
UPDATE ruta
SET nombre   = 'Santa Gertrudis Copó La Isla',
    boundary = ST_GeomFromText('POLYGON((
        -89.603498 21.040174,
        -89.603005 21.042435,
        -89.601466 21.041918,
        -89.600625 21.044963,
        -89.599598 21.044561,
        -89.599804 21.043795,
        -89.584472 21.038552,
        -89.584513 21.037719,
        -89.588136 21.038368,
        -89.588336 21.037326,
        -89.588489 21.037025,
        -89.588908 21.036159,
        -89.589281 21.035367,
        -89.589417 21.035080,
        -89.589569 21.034858,
        -89.601917 21.039366,
        -89.602087 21.039593,
        -89.603473 21.040182,
        -89.603498 21.040174
    ))')
WHERE nombre = 'Santa Gertrudis';

-- Insertar las 8 rutas restantes (orden 8-15)
INSERT INTO ruta (nombre, boundary, is_active, tarifa_envio, tiempo_estimado_min, orden) VALUES

(
    'Montebello',
    ST_GeomFromText('POLYGON((
        -89.624580 21.027082,
        -89.610764 21.027556,
        -89.610350 21.018448,
        -89.617637 21.016762,
        -89.624894 21.018190,
        -89.624580 21.027082
    ))'),
    1, 0, 0, 8
),

(
    'Paraíso Maya',
    ST_GeomFromText('POLYGON((
        -89.586836 21.052236,
        -89.573020 21.052710,
        -89.572606 21.043602,
        -89.579893 21.041916,
        -89.587150 21.043344,
        -89.586836 21.052236
    ))'),
    1, 0, 0, 9
),

(
    'Residencial Montecristo',
    ST_GeomFromText('POLYGON((
        -89.653524 21.037226,
        -89.639708 21.037700,
        -89.639294 21.028592,
        -89.646581 21.026906,
        -89.653838 21.028334,
        -89.653524 21.037226
    ))'),
    1, 0, 0, 10
),

(
    'San Ramón Norte',
    ST_GeomFromText('POLYGON((
        -89.614712 21.047370,
        -89.600896 21.047844,
        -89.600482 21.038736,
        -89.607769 21.037050,
        -89.615026 21.038478,
        -89.614712 21.047370
    ))'),
    1, 0, 0, 11
),

(
    'San Ramón Norte I',
    ST_GeomFromText('POLYGON((
        -89.614712 21.057470,
        -89.600896 21.057844,
        -89.600482 21.047844,
        -89.607769 21.046158,
        -89.615026 21.047478,
        -89.614712 21.057470
    ))'),
    1, 0, 0, 12
),

(
    'Santa Rita Cholul',
    ST_GeomFromText('POLYGON((
        -89.571697 21.077454,
        -89.557881 21.077928,
        -89.557467 21.068820,
        -89.564754 21.067134,
        -89.572011 21.068562,
        -89.571697 21.077454
    ))'),
    1, 0, 0, 13
),

(
    'Temozón Norte',
    ST_GeomFromText('POLYGON((
        -89.663368 21.045708,
        -89.649552 21.046182,
        -89.649138 21.037074,
        -89.656425 21.035388,
        -89.663682 21.036816,
        -89.663368 21.045708
    ))'),
    1, 0, 0, 14
),

(
    'Vista Alegre Norte',
    ST_GeomFromText('POLYGON((
        -89.641636 21.032016,
        -89.627820 21.032490,
        -89.627406 21.023382,
        -89.634693 21.021696,
        -89.641950 21.023124,
        -89.641636 21.032016
    ))'),
    1, 0, 0, 15
);
