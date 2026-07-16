INSERT INTO ruta (nombre, boundary, is_active, tarifa_envio, tiempo_estimado_min,orden) VALUES
    (
        'Ruta general',
        ST_GeomFromText('POLYGON((-103.3520 20.6680, -103.3390 20.6680, -103.3390 20.6590, -103.3520 20.6590, -103.3520 20.6680))'),
        1, 25.00, 20,6
    );