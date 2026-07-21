-- Desayunos: lunes–sábado, 07:00–11:00  (6 filas)
-- Almuerzos: lunes–viernes, 08:30–15:30 (5 filas)
INSERT INTO horario_atencion
    (hora_inicio_atencion_comidas, hora_cierre_atencion_comidas, dia_semana, tipo_horario, atendiendo)
VALUES
    ('07:00:00', '11:00:00', 'L', 'DESAYUNO', 1),
    ('07:00:00', '11:00:00', 'M', 'DESAYUNO', 1),
    ('07:00:00', '11:00:00', 'X', 'DESAYUNO', 1),
    ('07:00:00', '11:00:00', 'J', 'DESAYUNO', 1),
    ('07:00:00', '11:00:00', 'V', 'DESAYUNO', 1),
    ('07:00:00', '11:00:00', 'S', 'DESAYUNO', 1),
    ('08:30:00', '15:30:00', 'L', 'COMIDAS',  1),
    ('08:30:00', '15:30:00', 'M', 'COMIDAS',  1),
    ('08:30:00', '15:30:00', 'X', 'COMIDAS',  1),
    ('08:30:00', '15:30:00', 'J', 'COMIDAS',  1),
    ('08:30:00', '15:30:00', 'V', 'COMIDAS',  1);
