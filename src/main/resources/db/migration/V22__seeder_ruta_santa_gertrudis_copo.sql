SET NAMES utf8mb4;

-- Nuevas rutas: Santa Gertrudis Copó y General
-- Polígonos aproximados; reemplazar con boundaries reales cuando estén disponibles.
INSERT INTO ruta (nombre, boundary, is_active, tarifa_envio, tiempo_estimado_min, orden)
VALUES
(
    'Santa Gertrudis Copó',
    ST_GeomFromText('POLYGON((
        -89.614580 21.040870,
        -89.600764 21.041344,
        -89.600350 21.032236,
        -89.607637 21.030550,
        -89.614894 21.031978,
        -89.614580 21.040870
    ))'),
    1, 0, 0, 16
),
(
    'General',
    ST_GeomFromText('POLYGON((
        -89.625580 21.055870,
        -89.611764 21.056344,
        -89.611350 21.047236,
        -89.618637 21.045550,
        -89.625894 21.046978,
        -89.625580 21.055870
    ))'),
    1, 0, 0, 17
);
