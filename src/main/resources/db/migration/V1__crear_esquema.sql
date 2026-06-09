SET NAMES utf8mb4;
SET time_zone = '+00:00';

-- =============================================================================
-- TABLAS DE CATÁLOGOS Y CONFIGURACIÓN
-- (sin dependencias externas; se crean primero)
-- =============================================================================

CREATE TABLE IF NOT EXISTS rol_usuario (
    id_rol          INT          NOT NULL AUTO_INCREMENT,
    nombre_rol      VARCHAR(50)  NOT NULL,
    descripcion     VARCHAR(100) NULL,
    PRIMARY KEY (id_rol)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS ruta (
    id_ruta              INT            NOT NULL AUTO_INCREMENT,
    nombre               VARCHAR(45)    NOT NULL,
    boundary             GEOMETRY       NOT NULL,
    is_active            TINYINT        NOT NULL DEFAULT 1,
    tarifa_envio         DECIMAL(6,2)   NOT NULL,
    tiempo_estimado_min  INT            NULL,
    PRIMARY KEY (id_ruta),
    SPATIAL INDEX idx_ruta_boundary (boundary)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tarifa_especial (
    id_tarifa  INT           NOT NULL AUTO_INCREMENT,
    nombre_tarifa     VARCHAR(255)  NOT NULL,
    tarifa            DECIMAL(5,2)  NOT NULL,       
    is_active         TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id_tarifa)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS horario_atencion (
    id_horario_atencion_comidas   INT   NOT NULL AUTO_INCREMENT,
    hora_inicio_atencion_comidas  TIME  NOT NULL,
    hora_cierre_atencion_comidas  TIME  NOT NULL,
    dia_semana                    CHAR(1) NOT NULL  COMMENT 'L=lunes M=martes X=miércoles J=jueves V=viernes S=sábado',
    tipo_horario                  ENUM('DESAYUNO','COMIDAS') NOT NULL
                                  COMMENT 'COMIDAS: L-V 8:30-15:30 | DESAYUNO: L-S 7:00-11:00',
    atendiendo                    TINYINT NOT NULL DEFAULT 0
                                  COMMENT '1=abierto (override manual del horario normal)',
    PRIMARY KEY (id_horario_atencion_comidas)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS anuncio (
    id_anuncio          INT          NOT NULL AUTO_INCREMENT,
    descripcion_anuncio VARCHAR(255) NOT NULL,
    color               VARCHAR(10)  NULL     COMMENT 'Color HEX del anuncio, ej. #FF5733',
    fecha_anuncio       DATETIME     NOT NULL,
    PRIMARY KEY (id_anuncio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS codigo_cliente (
    id_codigo_cliente  INT          NOT NULL AUTO_INCREMENT,
    identificador      VARCHAR(255) NOT NULL COMMENT 'Nombre legible asignado por el operador, ej. Código Susanita',
    codigo_cliente     VARCHAR(255) NOT NULL COMMENT 'Código hasheado que se entrega al cliente',
    tarifa_especial    DECIMAL(5,2) NOT NULL,
    PRIMARY KEY (id_codigo_cliente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- TABLAS DE USUARIOS Y CLIENTES
-- =============================================================================

CREATE TABLE IF NOT EXISTS usuario (
    id_usuario     INT          NOT NULL AUTO_INCREMENT,
    id_rol         INT          NOT NULL,
    nombre_usuario VARCHAR(20)  NOT NULL,
    contrasena     VARCHAR(70)  NOT NULL COMMENT 'PIN hasheado con bcrypt',
    creado_en      DATETIME     NOT NULL,
    ultimo_login   DATETIME     NULL,
    PRIMARY KEY (id_usuario),
    CONSTRAINT fk_usuario_rol
        FOREIGN KEY (id_rol) REFERENCES rol_usuario (id_rol)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS cliente (
    id_cliente         INT           NOT NULL AUTO_INCREMENT,
    id_ruta            INT           NULL     ,
    uuid_cliente       VARCHAR(45)   NOT NULL UNIQUE ,
    session_token      VARCHAR(255)  NOT NULL UNIQUE,
    codigo_cliente     VARCHAR(255)  NULL,     
    user_agent         VARCHAR(255)  NULL,
    ip_address         VARCHAR(45)   NULL,
    ubicacion_latitud  DECIMAL(10,7) NULL,     
    ubicacion_longitud DECIMAL(10,7) NULL,     
    nombre             VARCHAR(255)  NULL,
    direccion_cliente  VARCHAR(255)  NULL,     
    telefono           VARCHAR(16)   NULL,
    PRIMARY KEY (id_cliente),
    CONSTRAINT fk_cliente_ruta
        FOREIGN KEY (id_ruta) REFERENCES ruta (id_ruta)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =============================================================================
-- CATÁLOGO DE PRODUCTOS
-- =============================================================================

CREATE TABLE IF NOT EXISTS comida (
    id_comida       INT           NOT NULL AUTO_INCREMENT,
    uuid_comida     VARCHAR(45)   NOT NULL,
    nombre_comida   VARCHAR(255)  NOT NULL,
    descripcion     VARCHAR(255)  NULL,
    precio_media    DECIMAL(10,2) NOT NULL,
    precio_entera   DECIMAL(10,2) NOT NULL,
    estatus         ENUM('DISPONIBLE','NO_DISPONIBLE','AGOTADO') NOT NULL DEFAULT 'DISPONIBLE',
    destacado       TINYINT       NOT NULL DEFAULT 0 COMMENT '1=mostrar prioritariamente en el carrito web',
    PRIMARY KEY (id_comida)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS desayuno (
    id_desayuno     INT           NOT NULL AUTO_INCREMENT,
    uuid_desayuno   VARCHAR(45)   NOT NULL,
    nombre_desayuno VARCHAR(255)  NOT NULL,
    descripcion     VARCHAR(255)  NULL,
    precio_media    DECIMAL(10,2) NOT NULL,
    precio_entera   DECIMAL(10,2) NOT NULL,
    estatus         ENUM('DISPONIBLE','NO_DISPONIBLE','AGOTADO') NOT NULL DEFAULT 'DISPONIBLE',
    destacado       TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id_desayuno)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS complemento (
    id_complemento     INT          NOT NULL AUTO_INCREMENT,
    uuid_complemento   VARCHAR(45)  NOT NULL,
    nombre_complemento VARCHAR(255) NOT NULL,
    descripcion        VARCHAR(255) NULL,
    precio_extra       DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    estatus            ENUM('DISPONIBLE','NO_DISPONIBLE','AGOTADO') NOT NULL DEFAULT 'DISPONIBLE',
    destacado          TINYINT      NOT NULL DEFAULT 0,
    cobrar_siempre     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id_complemento)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS producto_cocina (
    id_producto_cocina  INT          NOT NULL AUTO_INCREMENT,
    uuid_producto_cocina VARCHAR(45) NOT NULL,
    nombre_producto     VARCHAR(100) NOT NULL,
    descripcion         VARCHAR(255) NULL,
    precio_domicilio    DECIMAL(5,2) NOT NULL, 
    precio_normal       DECIMAL(5,2) NOT NULL, 
    estatus             ENUM('DISPONIBLE','NO_DISPONIBLE','AGOTADO') NOT NULL DEFAULT 'DISPONIBLE',
    destacado           TINYINT      NOT NULL DEFAULT 0,
    tipo_producto       ENUM('SNACK','CHAROLA','BEBIDA') NOT NULL,
    PRIMARY KEY (id_producto_cocina)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- Paquete predefinido: una comida + complementos a precio fijo

CREATE TABLE IF NOT EXISTS basico (
    id_basico     INT          NOT NULL AUTO_INCREMENT UNIQUE,
    id_comida     INT          NOT NULL,
    descripcion   VARCHAR(255) NULL,
    destacado     TINYINT      NOT NULL DEFAULT 0,
    precio_basico DECIMAL(5,2) NOT NULL,
    PRIMARY KEY (id_basico),
    CONSTRAINT fk_basico_comida
        FOREIGN KEY (id_comida) REFERENCES comida (id_comida)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS basico_complemento (
    id_basico_complemento INT NOT NULL AUTO_INCREMENT,
    id_basico             INT NOT NULL,
    id_complemento        INT NOT NULL,
    PRIMARY KEY (id_basico_complemento),
    CONSTRAINT fk_basico_comp_basico
        FOREIGN KEY (id_basico) REFERENCES basico (id_basico)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_basico_comp_complemento
        FOREIGN KEY (id_complemento) REFERENCES complemento (id_complemento)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- PEDIDOS
-- =============================================================================

CREATE TABLE IF NOT EXISTS pedido (
    id_pedido               INT           NOT NULL AUTO_INCREMENT,
    metodo_pago             ENUM('TARJETA','EFECTIVO','TRANSFERENCIA') NOT NULL,
    tipo_pedido             ENUM('PICK_UP','DOMICILIO','MOSTRADOR')    NOT NULL,
    fecha_expedicion_pedido DATETIME      NOT NULL,
    pedido_creado_desde     ENUM('COCINA','WEB') NOT NULL,
    precio_final_orden      DECIMAL(10,2) NOT NULL,
    pago_cliente            DECIMAL(10,2) NULL,     
    uuid_cliente            VARCHAR(45)   NULL,    
    impreso                 TINYINT       NOT NULL DEFAULT 0,
                        
    PRIMARY KEY (id_pedido),
    CONSTRAINT fk_pedido_cliente
        FOREIGN KEY (uuid_cliente) REFERENCES cliente (uuid_cliente)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS pedido_domicilio (
    id_pedido  INT           NOT NULL,
    id_ruta    INT           NOT NULL,
    direccion  VARCHAR(255)  NOT NULL,
    codigo     VARCHAR(255)  NULL,    
    latitud    DECIMAL(10,7) NULL,
    longitud   DECIMAL(10,7) NULL,
    PRIMARY KEY (id_pedido),
    CONSTRAINT fk_ped_dom_pedido
        FOREIGN KEY (id_pedido) REFERENCES pedido (id_pedido)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_ped_dom_ruta
        FOREIGN KEY (id_ruta) REFERENCES ruta (id_ruta)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- LÍNEAS DE PEDIDO
-- =============================================================================

CREATE TABLE IF NOT EXISTS comida_pedido (
    id_comida_pedido INT          NOT NULL AUTO_INCREMENT,
    id_comida        INT          NOT NULL,
    id_pedido        INT          NOT NULL,
    precio_unitario  DECIMAL(5,2) NOT NULL, 
    tamano_porcion   ENUM('MEDIA','ENTERA') NOT NULL,
    PRIMARY KEY (id_comida_pedido),
    CONSTRAINT fk_comida_pedido_pedido
        FOREIGN KEY (id_pedido) REFERENCES pedido (id_pedido)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_comida_pedido_comida
        FOREIGN KEY (id_comida) REFERENCES comida (id_comida)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS complemento_comida_pedido (
    id_complemento_comida_pedido INT          NOT NULL AUTO_INCREMENT,
    id_comida_pedido             INT          NOT NULL,
    id_complemento               INT          NOT NULL,
    precio_unitario              DECIMAL(5,2) NOT NULL COMMENT 'Precio del complemento al momento del pedido',
    PRIMARY KEY (id_complemento_comida_pedido),
    CONSTRAINT fk_comp_com_ped_comida_pedido
        FOREIGN KEY (id_comida_pedido) REFERENCES comida_pedido (id_comida_pedido)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_comp_com_ped_complemento
        FOREIGN KEY (id_complemento) REFERENCES complemento (id_complemento)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS desayuno_pedido (
    id_desayuno_pedido INT          NOT NULL AUTO_INCREMENT,
    id_pedido          INT          NOT NULL,
    id_desayuno        INT          NOT NULL, 
    precio             DECIMAL(5,2) NOT NULL,
    PRIMARY KEY (id_desayuno_pedido),
    CONSTRAINT fk_desayuno_pedido_pedido
        FOREIGN KEY (id_pedido) REFERENCES pedido (id_pedido)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_desayuno_pedido_desayuno
        FOREIGN KEY (id_desayuno) REFERENCES desayuno (id_desayuno)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS producto_cocina_pedido (
    id_producto_cocina_pedido INT          NOT NULL AUTO_INCREMENT,
    id_pedido                 INT          NOT NULL,
    id_producto_cocina        INT          NOT NULL,
    cantidad                  TINYINT      NOT NULL DEFAULT 1,
    precio_unitario           DECIMAL(5,2) NOT NULL,
    PRIMARY KEY (id_producto_cocina_pedido),
    CONSTRAINT fk_prod_coc_ped_pedido
        FOREIGN KEY (id_pedido) REFERENCES pedido (id_pedido)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_prod_coc_ped_producto
        FOREIGN KEY (id_producto_cocina) REFERENCES producto_cocina (id_producto_cocina)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- ARCHIVOS
-- =============================================================================

CREATE TABLE IF NOT EXISTS archivo_modulo (
    id_archivo_modulo      INT         NOT NULL AUTO_INCREMENT,
    nombre_modulo          VARCHAR(50) NOT NULL,
    tipo_catalogo_producto ENUM('BASICO','COMIDA','DESAYUNO','SNACK','CHAROLA','BEBIDA') NULL,
                           
    ruta                   VARCHAR(100) NOT NULL, 
    archivos_aceptados     JSON         NOT NULL, 
    PRIMARY KEY (id_archivo_modulo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS archivo (
    id_archivo        INT          NOT NULL AUTO_INCREMENT,
    id_archivo_modulo INT          NOT NULL,
    path_archivo      VARCHAR(255) NOT NULL COMMENT 'Ruta absoluta del archivo en el servidor',
    mime_type         VARCHAR(50)  NOT NULL,
    extension_archivo VARCHAR(50)  NOT NULL,
    nombre_archivo    VARCHAR(255) NOT NULL,
    creado_en         DATETIME     NOT NULL,
    PRIMARY KEY (id_archivo),
    CONSTRAINT fk_archivo_modulo
        FOREIGN KEY (id_archivo_modulo) REFERENCES archivo_modulo (id_archivo_modulo)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- FAVORITOS, INVENTARIO Y PAGOS
-- =============================================================================

CREATE TABLE IF NOT EXISTS favorito_cliente (
    id_favorito_cliente    INT         NOT NULL AUTO_INCREMENT,
    id_producto            INT         NOT NULL,
    session_token          VARCHAR(255) NOT NULL,
    tipo_catalogo_producto ENUM('BASICO','COMIDA','DESAYUNO','SNACK','CHAROLA','BEBIDA') NOT NULL,
    PRIMARY KEY (id_favorito_cliente),
    CONSTRAINT fk_favorito_cliente_session
        FOREIGN KEY (session_token) REFERENCES cliente (session_token)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- [C4] inventario_comida — FK formal agregada (faltaba en el DBML)

CREATE TABLE IF NOT EXISTS inventario_comida (
    id_inventario_comida INT          NOT NULL AUTO_INCREMENT,
    id_comida            INT          NOT NULL, -- [C4] FK formal a comida
    cantidad             INT          NULL,
    kilogramos           DECIMAL(8,3) NULL, 
    PRIMARY KEY (id_inventario_comida),
    CONSTRAINT fk_inventario_comida
        FOREIGN KEY (id_comida) REFERENCES comida (id_comida)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_inventario_no_vacio
        CHECK (cantidad IS NOT NULL OR kilogramos IS NOT NULL)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pago_repartidor (
    id_pago_repartidor INT          NOT NULL AUTO_INCREMENT,
    pago               DECIMAL(5,2) NOT NULL,
    fecha_pago         DATETIME     NOT NULL,
    PRIMARY KEY (id_pago_repartidor)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- ÍNDICES ADICIONALES RECOMENDADOS
-- (mejoran el rendimiento de las consultas más frecuentes)
-- =============================================================================

CREATE INDEX idx_pedido_uuid_cliente      ON pedido         (uuid_cliente);
CREATE INDEX idx_pedido_fecha             ON pedido         (fecha_expedicion_pedido);
CREATE INDEX idx_pedido_impreso           ON pedido         (impreso);
CREATE INDEX idx_favorito_session         ON favorito_cliente (session_token);
CREATE INDEX idx_archivo_modulo           ON archivo        (id_archivo_modulo);
CREATE INDEX idx_comp_com_ped_comida_ped  ON complemento_comida_pedido (id_comida_pedido);
CREATE INDEX idx_comida_pedido_pedido     ON comida_pedido  (id_pedido);
CREATE INDEX idx_desayuno_pedido_pedido   ON desayuno_pedido (id_pedido);
CREATE INDEX idx_prod_coc_ped_pedido      ON producto_cocina_pedido (id_pedido);

-- =============================================================================
-- FIN DEL SCRIPT
-- =============================================================================

