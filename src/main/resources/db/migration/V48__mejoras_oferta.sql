-- ── Precio normal snapshot por línea consumida en una oferta ─────────────────
ALTER TABLE oferta_pedido_linea
    ADD COLUMN precio_normal_consumido DECIMAL(7,2) NOT NULL DEFAULT 0.00
        COMMENT 'Precio normal total de la línea (precioUnitario * cantidadConsumida) al momento de la orden';

-- ── Restricción de tipos de pedido por oferta ─────────────────────────────────
-- Vacío = sin restricción (aplica a todos los tipos de pedido).
-- Con valores = la oferta solo aplica cuando pedido.tipo_pedido está en este set.
-- Ejemplo: ("MOSTRADOR", "PICK_UP") = solo pedidos locales; ("DOMICILIO") = solo a domicilio.
CREATE TABLE oferta_tipo_pedido (
    id_oferta   INT         NOT NULL,
    tipo_pedido VARCHAR(20) NOT NULL,
    PRIMARY KEY (id_oferta, tipo_pedido),
    CONSTRAINT fk_otp_oferta
        FOREIGN KEY (id_oferta) REFERENCES oferta (id_oferta) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT 'Restricción de tipos de pedido en los que aplica la oferta. Vacío = sin restricción.';
