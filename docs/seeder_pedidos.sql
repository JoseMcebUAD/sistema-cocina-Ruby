-- seeder_pedidos.sql
-- 42 pedidos del 2026-07-21 (martes) al 2026-07-25 (sábado).
-- El sábado sólo contiene pedidos de desayuno.
-- Todos creados desde COCINA (sin origen WEB).
--
-- Referencias de precios:
--   comida 1 Pollo a la plancha        media=55  entera=95
--   comida 2 Arroz con camarones       media=70  entera=120
--   comida 3 Milanesa de res           media=65  entera=110
--   comida 5 Enchiladas verdes         media=50  entera=85
--   desayuno 1 Chilaquiles rojos       media=42  entera=72
--   desayuno 2 Huevos rancheros        media=38  entera=65
--   desayuno 3 Molletes con pico       media=35  entera=60
--   desayuno 4 Hotcakes con fruta      media=45  entera=78
--   basico 1 Básico de pollo           75
--   basico 2 Básico de camarones       110
--   producto 1 Refresco (normal=20 / domicilio=25)
--   producto 2 Agua mineral (normal=15 / domicilio=18)
--   producto 3 Papas fritas            30
--   producto 4 Charola individual      normal=85 / domicilio=95
--   producto 6 Flan napolitano         normal=18 / domicilio=22
--   producto 7 Gelatina de fresa       normal=15 / domicilio=18
--   complemento 2 Aguacate             +15
--   complemento 4 Crema y queso        +10
--   ruta 1 Zona Centro  tarifa=25
--   ruta 2 Zona Norte   tarifa=35
--   ruta 3 Zona Sur     tarifa=35
--   ruta 4 Zona Oriente tarifa=45

-- ============================================================
-- registro_cliente  (clientes usados en pedidos de domicilio COCINA)
-- ============================================================
INSERT INTO registro_cliente (id_registro_cliente, nombre, telefono, id_ruta, direccion) VALUES
(1, 'Juan García',     '3310001111', 1, 'Calle Hidalgo 123, Centro'),
(2, 'María López',     '3310002222', 2, 'Av. Revolución 456, Norte'),
(3, 'Carlos Ramírez',  '3310003333', 3, 'Blvd. Independencia 789, Sur'),
(4, 'Ana Martínez',    '3310004444', 1, 'Calle Morelos 321, Centro'),
(5, 'Roberto Sánchez', '3310005555', 4, 'Av. Juárez 654, Oriente');

-- ============================================================
-- pedido  (42 registros)
--
-- precio_final_orden = suma de líneas de producto + tarifa si DOMICILIO
-- pago_cliente_principal:
--   EFECTIVO sin secundario  → billete con cambio (>= total)
--   EFECTIVO con secundario  → pago parcial en efectivo (< total)
--   TARJETA / TRANSFERENCIA  → NULL
-- ============================================================
INSERT INTO pedido (id_pedido, metodo_pago_principal, metodo_pago_secundario, tipo_pedido,
                    fecha_expedicion_pedido, pedido_creado_desde, precio_final_orden,
                    pago_cliente_principal, uuid_cliente, impreso) VALUES
-- Martes 2026-07-21 ──────────────────────────────────────────────────────────
( 1, 'EFECTIVO',      NULL,            'MOSTRADOR', '2026-07-21 09:15:00', 'COCINA',  95.00, 100.00, NULL, 1),
( 2, 'EFECTIVO',      NULL,            'PICK_UP',   '2026-07-21 08:30:00', 'COCINA',  72.00, 100.00, NULL, 1),
( 3, 'TRANSFERENCIA', NULL,            'DOMICILIO', '2026-07-21 11:30:00', 'COCINA', 115.00,   NULL, NULL, 1),
( 4, 'TARJETA',       NULL,            'MOSTRADOR', '2026-07-21 12:00:00', 'COCINA',  90.00,   NULL, NULL, 1),
( 5, 'EFECTIVO',      'TRANSFERENCIA', 'PICK_UP',   '2026-07-21 07:45:00', 'COCINA', 110.00,  70.00, NULL, 1),
( 6, 'EFECTIVO',      NULL,            'DOMICILIO', '2026-07-21 13:00:00', 'COCINA', 145.00, 150.00, NULL, 1),
( 7, 'EFECTIVO',      NULL,            'MOSTRADOR', '2026-07-21 14:15:00', 'COCINA', 103.00, 110.00, NULL, 1),
( 8, 'TRANSFERENCIA', NULL,            'PICK_UP',   '2026-07-21 10:30:00', 'COCINA',  85.00,   NULL, NULL, 1),
-- Miércoles 2026-07-22 ───────────────────────────────────────────────────────
( 9, 'EFECTIVO',      NULL,            'MOSTRADOR', '2026-07-22 09:45:00', 'COCINA', 120.00, 150.00, NULL, 1),
(10, 'TARJETA',       NULL,            'PICK_UP',   '2026-07-22 08:00:00', 'COCINA',  80.00,   NULL, NULL, 1),
(11, 'EFECTIVO',      'TARJETA',       'DOMICILIO', '2026-07-22 12:30:00', 'COCINA', 108.00,  60.00, NULL, 1),
(12, 'TARJETA',       NULL,            'MOSTRADOR', '2026-07-22 11:00:00', 'COCINA',  90.00,   NULL, NULL, 1),
(13, 'EFECTIVO',      NULL,            'PICK_UP',   '2026-07-22 13:45:00', 'COCINA', 125.00, 150.00, NULL, 1),
(14, 'EFECTIVO',      NULL,            'DOMICILIO', '2026-07-22 09:00:00', 'COCINA',  97.00, 100.00, NULL, 1),
(15, 'EFECTIVO',      NULL,            'MOSTRADOR', '2026-07-22 14:30:00', 'COCINA', 105.00, 110.00, NULL, 1),
(16, 'TARJETA',       NULL,            'PICK_UP',   '2026-07-22 10:15:00', 'COCINA',  65.00,   NULL, NULL, 1),
-- Jueves 2026-07-23 ──────────────────────────────────────────────────────────
(17, 'EFECTIVO',      NULL,            'MOSTRADOR', '2026-07-23 08:15:00', 'COCINA',  73.00, 100.00, NULL, 1),
(18, 'EFECTIVO',      'TRANSFERENCIA', 'PICK_UP',   '2026-07-23 13:00:00', 'COCINA',  80.00,  50.00, NULL, 1),
(19, 'TRANSFERENCIA', NULL,            'DOMICILIO', '2026-07-23 12:00:00', 'COCINA', 180.00,   NULL, NULL, 1),
(20, 'TARJETA',       NULL,            'MOSTRADOR', '2026-07-23 11:30:00', 'COCINA', 128.00,   NULL, NULL, 1),
(21, 'EFECTIVO',      NULL,            'PICK_UP',   '2026-07-23 09:30:00', 'COCINA',  65.00, 100.00, NULL, 1),
(22, 'EFECTIVO',      'TARJETA',       'MOSTRADOR', '2026-07-23 14:00:00', 'COCINA', 120.00, 100.00, NULL, 1),
(23, 'EFECTIVO',      NULL,            'DOMICILIO', '2026-07-23 13:30:00', 'COCINA', 120.00, 150.00, NULL, 1),
(24, 'TARJETA',       NULL,            'PICK_UP',   '2026-07-23 08:45:00', 'COCINA', 107.00,   NULL, NULL, 1),
-- Viernes 2026-07-24 ─────────────────────────────────────────────────────────
(25, 'EFECTIVO',      NULL,            'MOSTRADOR', '2026-07-24 09:30:00', 'COCINA', 120.00, 150.00, NULL, 1),
(26, 'EFECTIVO',      NULL,            'PICK_UP',   '2026-07-24 07:30:00', 'COCINA', 138.00, 150.00, NULL, 1),
(27, 'TRANSFERENCIA', NULL,            'DOMICILIO', '2026-07-24 11:45:00', 'COCINA',  98.00,   NULL, NULL, 1),
(28, 'EFECTIVO',      'TARJETA',       'MOSTRADOR', '2026-07-24 13:15:00', 'COCINA', 130.00,  50.00, NULL, 1),
(29, 'TARJETA',       NULL,            'PICK_UP',   '2026-07-24 10:00:00', 'COCINA', 110.00,   NULL, NULL, 1),
(30, 'EFECTIVO',      NULL,            'DOMICILIO', '2026-07-24 12:45:00', 'COCINA', 152.00, 160.00, NULL, 1),
(31, 'EFECTIVO',      NULL,            'MOSTRADOR', '2026-07-24 08:30:00', 'COCINA',  85.00, 100.00, NULL, 1),
(32, 'EFECTIVO',      NULL,            'PICK_UP',   '2026-07-24 14:15:00', 'COCINA', 125.00, 150.00, NULL, 1),
-- Sábado 2026-07-25 (sólo desayunos) ────────────────────────────────────────
(33, 'EFECTIVO',      NULL,            'MOSTRADOR', '2026-07-25 07:45:00', 'COCINA', 110.00, 150.00, NULL, 1),
(34, 'TARJETA',       NULL,            'PICK_UP',   '2026-07-25 08:30:00', 'COCINA',  78.00,   NULL, NULL, 1),
(35, 'EFECTIVO',      NULL,            'MOSTRADOR', '2026-07-25 09:00:00', 'COCINA',  55.00,  60.00, NULL, 1),
(36, 'TRANSFERENCIA', NULL,            'DOMICILIO', '2026-07-25 08:15:00', 'COCINA',  77.00,   NULL, NULL, 1),
(37, 'EFECTIVO',      NULL,            'PICK_UP',   '2026-07-25 07:30:00', 'COCINA',  83.00, 100.00, NULL, 1),
(38, 'TARJETA',       NULL,            'MOSTRADOR', '2026-07-25 09:45:00', 'COCINA',  96.00,   NULL, NULL, 1),
(39, 'EFECTIVO',      'TARJETA',       'PICK_UP',   '2026-07-25 10:15:00', 'COCINA',  87.00,  40.00, NULL, 1),
(40, 'EFECTIVO',      NULL,            'DOMICILIO', '2026-07-25 08:00:00', 'COCINA', 123.00, 150.00, NULL, 1),
(41, 'EFECTIVO',      NULL,            'MOSTRADOR', '2026-07-25 09:30:00', 'COCINA',  80.00, 100.00, NULL, 1),
(42, 'TARJETA',       NULL,            'PICK_UP',   '2026-07-25 10:00:00', 'COCINA',  95.00,   NULL, NULL, 1);

-- ============================================================
-- pedido_cocina  (PICK_UP y MOSTRADOR desde COCINA)
-- ============================================================
INSERT INTO pedido_cocina (id_pedido, nombre_cliente) VALUES
( 1, 'Pedro'),
( 2, 'Sofía'),
( 4, 'Laura'),
( 5, 'Miguel'),
( 7, 'Carmen'),
( 8, 'Arturo'),
( 9, 'Elena'),
(10, 'Daniel'),
(12, 'Fernanda'),
(13, 'Héctor'),
(15, 'Verónica'),
(16, 'Rodrigo'),
(17, 'Patricia'),
(18, 'Ramón'),
(20, 'Gloria'),
(21, 'Tomás'),
(22, 'Leticia'),
(24, 'Ernesto'),
(25, 'Consuelo'),
(26, 'Benjamín'),
(28, 'Irma'),
(29, 'Alejandro'),
(31, 'Norma'),
(32, 'Osvaldo'),
(33, 'Dolores'),
(34, 'Salvador'),
(35, 'Esperanza'),
(37, 'Adriana'),
(38, 'Aurelio'),
(39, 'Rebeca'),
(41, 'Francisco'),
(42, 'Graciela');

-- ============================================================
-- pedido_domicilio_cocina  (DOMICILIO desde COCINA)
-- precio_final_orden = líneas de producto + precio_tarifa
-- ============================================================
INSERT INTO pedido_domicilio_cocina (id_pedido, id_registro_cliente, id_ruta, domicilio, precio_tarifa) VALUES
( 3, 1, 1, 'Calle Hidalgo 123, Centro',       25.00),
( 6, 2, 2, 'Av. Revolución 456, Norte',        35.00),
(11, 3, 3, 'Blvd. Independencia 789, Sur',     35.00),
(14, 4, 1, 'Calle Morelos 321, Centro',        25.00),
(19, 5, 4, 'Av. Juárez 654, Oriente',          45.00),
(23, 2, 2, 'Av. Revolución 456, Norte',        35.00),
(27, 1, 1, 'Calle Hidalgo 123, Centro',        25.00),
(30, 3, 3, 'Blvd. Independencia 789, Sur',     35.00),
(36, 2, 2, 'Av. Revolución 456, Norte',        35.00),
(40, 1, 1, 'Calle Hidalgo 123, Centro',        25.00);

-- ============================================================
-- comida_pedido
-- Verificación de totales por pedido:
--   P1 =  95 (pollo entera)
--   P3 = 115 (milanesa media 65 + refresco dom. 25 + tarifa 25)
--   P9 = 120 (camarones entera)
--   P11= 108 (pollo media 55 + gelatina dom. 18 + tarifa 35)
--   P13= 125 (milanesa entera 110 + aguacate 15)
--   P16=  65 (enchiladas media 50 + agua 15)
--   P18=  80 (camarones media 70 + crema/queso 10)
--   P20= 128 (pollo entera 95 + aguacate 15 + flan 18)
--   P21=  65 (milanesa media)
--   P23= 120 (enchiladas entera 85 + tarifa 35)
--   P25= 120 (camarones entera)
--   P27=  98 (pollo media 55 + agua dom. 18 + tarifa 25)
--   P29= 110 (milanesa entera)
--   P32= 125 (enchiladas media 50 + básico pollo 75)
-- ============================================================
INSERT INTO comida_pedido (id_comida_pedido, id_comida, id_pedido, precio_unitario, tamano_porcion) VALUES
( 1, 1,  1,  95.00, 'ENTERA'),
( 2, 3,  3,  65.00, 'MEDIA'),
( 3, 2,  9, 120.00, 'ENTERA'),
( 4, 1, 11,  55.00, 'MEDIA'),
( 5, 3, 13, 110.00, 'ENTERA'),
( 6, 5, 16,  50.00, 'MEDIA'),
( 7, 2, 18,  70.00, 'MEDIA'),
( 8, 1, 20,  95.00, 'ENTERA'),
( 9, 3, 21,  65.00, 'MEDIA'),
(10, 5, 23,  85.00, 'ENTERA'),
(11, 2, 25, 120.00, 'ENTERA'),
(12, 1, 27,  55.00, 'MEDIA'),
(13, 3, 29, 110.00, 'ENTERA'),
(14, 5, 32,  50.00, 'MEDIA');

-- ============================================================
-- complemento_comida_pedido
-- ============================================================
INSERT INTO complemento_comida_pedido (id_complemento_comida_pedido, id_comida_pedido, id_complemento, precio_unitario) VALUES
(1, 5, 2, 15.00),  -- P13 milanesa entera + aguacate
(2, 7, 4, 10.00),  -- P18 camarones media + crema y queso
(3, 8, 2, 15.00);  -- P20 pollo entera + aguacate

-- ============================================================
-- desayuno_pedido
-- ============================================================
INSERT INTO desayuno_pedido (id_desayuno_pedido, id_pedido, id_desayuno, precio) VALUES
( 1,  2, 1, 72.00),  -- P2  chilaquiles entera
( 2,  5, 2, 65.00),  -- P5  huevos entera
( 3,  5, 4, 45.00),  -- P5  hotcakes media
( 4, 10, 3, 35.00),  -- P10 molletes media
( 5, 10, 4, 45.00),  -- P10 hotcakes media
( 6, 14, 1, 72.00),  -- P14 chilaquiles entera (dom.)
( 7, 17, 2, 38.00),  -- P17 huevos media
( 8, 17, 3, 35.00),  -- P17 molletes media
( 9, 24, 1, 42.00),  -- P24 chilaquiles media
(10, 24, 2, 65.00),  -- P24 huevos entera
(11, 26, 3, 60.00),  -- P26 molletes entera
(12, 26, 4, 78.00),  -- P26 hotcakes entera
(13, 31, 2, 65.00),  -- P31 huevos entera
-- Sábado ─────────────────────────────────────────────────────
(14, 33, 1, 72.00),  -- P33 chilaquiles entera
(15, 33, 2, 38.00),  -- P33 huevos media
(16, 34, 4, 78.00),  -- P34 hotcakes entera
(17, 35, 3, 35.00),  -- P35 molletes media
(18, 36, 1, 42.00),  -- P36 chilaquiles media (dom.)
(19, 37, 2, 38.00),  -- P37 huevos media
(20, 37, 4, 45.00),  -- P37 hotcakes media
(21, 38, 4, 78.00),  -- P38 hotcakes entera
(22, 39, 1, 72.00),  -- P39 chilaquiles entera
(23, 40, 3, 60.00),  -- P40 molletes entera (dom.)
(24, 40, 2, 38.00),  -- P40 huevos media (dom.)
(25, 41, 2, 65.00),  -- P41 huevos entera
(26, 42, 3, 35.00),  -- P42 molletes media
(27, 42, 4, 45.00);  -- P42 hotcakes media

-- ============================================================
-- basico_pedido
-- ============================================================
INSERT INTO basico_pedido (id_basico_pedido, id_basico, id_pedido, precio_unitario) VALUES
(1, 1,  4,  75.00),  -- P4  básico pollo
(2, 2,  6, 110.00),  -- P6  básico camarones
(3, 1, 12,  75.00),  -- P12 básico pollo
(4, 2, 19, 110.00),  -- P19 básico camarones
(5, 1, 22,  75.00),  -- P22 básico pollo
(6, 2, 28, 110.00),  -- P28 básico camarones
(7, 1, 32,  75.00);  -- P32 básico pollo

-- ============================================================
-- producto_cocina_pedido
-- DOMICILIO → precio_domicilio del catálogo
-- PICK_UP / MOSTRADOR → precio_normal
-- ============================================================
INSERT INTO producto_cocina_pedido (id_producto_cocina_pedido, id_pedido, id_producto_cocina, cantidad, precio_unitario) VALUES
( 1,  3, 1, 1, 25.00),  -- P3  refresco (domicilio)
( 2,  4, 2, 1, 15.00),  -- P4  agua (normal)
( 3,  7, 6, 1, 18.00),  -- P7  flan (normal)
( 4,  8, 4, 1, 85.00),  -- P8  charola (normal)
( 5, 11, 7, 1, 18.00),  -- P11 gelatina (domicilio)
( 6, 12, 2, 1, 15.00),  -- P12 agua (normal)
( 7, 15, 4, 1, 85.00),  -- P15 charola (normal)
( 8, 15, 1, 1, 20.00),  -- P15 refresco (normal)
( 9, 16, 2, 1, 15.00),  -- P16 agua (normal)
(10, 19, 1, 1, 25.00),  -- P19 refresco (domicilio)
(11, 20, 6, 1, 18.00),  -- P20 flan (normal)
(12, 22, 3, 1, 30.00),  -- P22 papas fritas
(13, 22, 7, 1, 15.00),  -- P22 gelatina (normal)
(14, 27, 2, 1, 18.00),  -- P27 agua (domicilio)
(15, 28, 1, 1, 20.00),  -- P28 refresco (normal)
(16, 30, 4, 1, 95.00),  -- P30 charola (domicilio)
(17, 30, 6, 1, 22.00),  -- P30 flan (domicilio)
(18, 31, 1, 1, 20.00),  -- P31 refresco (normal)
(19, 35, 1, 1, 20.00),  -- P35 refresco (normal)
(20, 38, 6, 1, 18.00),  -- P38 flan (normal)
(21, 39, 2, 1, 15.00),  -- P39 agua (normal)
(22, 41, 7, 1, 15.00),  -- P41 gelatina (normal)
(23, 42, 2, 1, 15.00);  -- P42 agua (normal)
