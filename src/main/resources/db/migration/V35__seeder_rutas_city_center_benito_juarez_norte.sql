SET NAMES utf8mb4;

-- Nuevas rutas: City Center y Benito Juárez Norte
-- Polígonos aproximados; reemplazar con boundaries reales cuando estén disponibles.
INSERT INTO ruta (nombre, boundary, is_active, tarifa_envio, id_orden_ruta, uuid_ruta)
VALUES
(
    'City Center',
    ST_GeomFromText('POLYGON((
        -89.623500 21.161500,
        -89.610000 21.161500,
        -89.610000 21.150000,
        -89.623500 21.150000,
        -89.623500 21.161500
    ))'),
    1, 10.00, null, UUID()
),
(
    'Benito Juárez Norte',
    ST_GeomFromText('POLYGON((
        -89.638000 21.175000,
        -89.623500 21.175000,
        -89.623500 21.162000,
        -89.638000 21.162000,
        -89.638000 21.175000
    ))'),
    1, 20.00, null, UUID()
);
