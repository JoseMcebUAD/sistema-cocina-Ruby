

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `sistema-cocina-ruby`
--
CREATE DATABASE IF NOT EXISTS `sistema-cocina-ruby`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE `sistema-cocina-ruby`;
-- --------------------------------------------------------

--
-- Table structure for table `tipo_usuario`
--

CREATE TABLE IF NOT EXISTS`tipo_usuario` (
  `id_tipo_usuario` int(11) NOT NULL,
  `nombre_tipo_usuario` varchar(50) NOT NULL,
  `permisos_usuario` varchar(1000) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `usuario`
--

CREATE TABLE IF NOT EXISTS `usuario` (
  `id_usuario` int(11) NOT NULL,
  `idRel_tipo_usuario` int(11) NOT NULL,
  `nombre_usuario` varchar(50) NOT NULL,
  `contrasena_usuario` varchar(200) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `tipo_cliente`
--

CREATE TABLE IF NOT EXISTS `tipo_cliente` (
  `id_tipo_cliente` int(11) NOT NULL,
  `nombre_tipo_cliente` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `cliente`
--

CREATE TABLE IF NOT EXISTS `cliente` (
  `id_cliente` int(11) NOT NULL,
  `idRel_tipo_cliente` int(11) NOT NULL,
  `nombre_cliente` varchar(50) DEFAULT NULL,
  `direcciones` varchar(400) DEFAULT NULL,
  `numero_tarjeta` varchar(50) DEFAULT NULL
  `telefono` varchar(25) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `tipo_pago`
--

CREATE TABLE IF NOT EXISTS `tipo_pago` (
  `id_tipo_pago` int(11) NOT NULL,
  `nombre_tipo_pago` varchar(30) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `orden`
--

CREATE TABLE IF NOT EXISTS `orden` (
  `id_orden` int(11) NOT NULL,
  `idRel_cliente` int(11) DEFAULT NULL,
  `idRel_tipo_pago` int(11) NOT NULL,
  `fecha_expedicion_orden` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `notas_orden` varchar(100) DEFAULT NULL,
  `precio_orden` decimal(10,2) NOT NULL DEFAULT 0.00,
  `pago_cliente` float(5,2) NULL DEFAULT 0.00,
  `facturado` tinyint(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `detalle_orden`
--

CREATE TABLE IF NOT EXISTS `detalle_orden` (
  `id_detalle_orden` int(11) NOT NULL,
  `idRel_orden` int(11) NOT NULL,
  `especificaciones_detalle_orden` varchar(100) DEFAULT NULL,
  `precio_detalle_orden` decimal(10,2) NOT NULL,
  'cantidad' int(3) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DELIMITER $$

CREATE TRIGGER trg_detalle_insert
AFTER INSERT ON detalle_orden
FOR EACH ROW
BEGIN
  UPDATE orden
  SET precio_orden = precio_orden + NEW.precio_detalle_orden
  WHERE id_orden = NEW.idRel_orden;
END$$

CREATE TRIGGER trg_detalle_update
AFTER UPDATE ON detalle_orden
FOR EACH ROW
BEGIN
  UPDATE orden
  SET precio_orden = precio_orden
      - OLD.precio_detalle_orden
      + NEW.precio_detalle_orden
  WHERE id_orden = NEW.idRel_orden;
END$$

CREATE TRIGGER trg_detalle_delete
AFTER DELETE ON detalle_orden
FOR EACH ROW
BEGIN
  UPDATE orden
  SET precio_orden = precio_orden - OLD.precio_detalle_orden
  WHERE id_orden = OLD.idRel_orden;
END$$

DELIMITER ;




-- --------------------------------------------------------

--
-- Indexes for dumped tables
--

--
-- Indexes for table `tipo_usuario`
--
ALTER TABLE `tipo_usuario`
  ADD PRIMARY KEY (`id_tipo_usuario`);

--
-- Indexes for table `usuario`
--
ALTER TABLE `usuario`
  ADD PRIMARY KEY (`id_usuario`),
  ADD KEY `fk_usuario_tipo` (`idRel_tipo_usuario`);

--
-- Indexes for table `tipo_cliente`
--
ALTER TABLE `tipo_cliente`
  ADD PRIMARY KEY (`id_tipo_cliente`);

--
-- Indexes for table `cliente`
--
ALTER TABLE `cliente`
  ADD PRIMARY KEY (`id_cliente`),
  ADD KEY `fk_cliente_tipo` (`idRel_tipo_cliente`);

--
-- Indexes for table `tipo_pago`
--
ALTER TABLE `tipo_pago`
  ADD PRIMARY KEY (`id_tipo_pago`);

--
-- Indexes for table `orden`
--
ALTER TABLE `orden`
  ADD PRIMARY KEY (`id_orden`),
  ADD KEY `fk_orden_cliente` (`idRel_cliente`),
  ADD KEY `fk_orden_tipo_pago` (`idRel_tipo_pago`),
  ADD KEY `idx_fecha_expedicion` (`fecha_expedicion_orden`),
  ADD KEY `idx_facturado` (`facturado`);

--
-- Indexes for table `detalle_orden`
--
ALTER TABLE `detalle_orden`
  ADD PRIMARY KEY (`id_detalle_orden`),
  ADD KEY `fk_detalle_orden` (`idRel_orden`);

-- --------------------------------------------------------

--
-- AUTO_INCREMENT for dumped tables
--

ALTER TABLE `tipo_usuario`
  MODIFY `id_tipo_usuario` int(11) NOT NULL AUTO_INCREMENT;

ALTER TABLE `usuario`
  MODIFY `id_usuario` int(11) NOT NULL AUTO_INCREMENT;

ALTER TABLE `tipo_cliente`
  MODIFY `id_tipo_cliente` int(11) NOT NULL AUTO_INCREMENT;

ALTER TABLE `cliente`
  MODIFY `id_cliente` int(11) NOT NULL AUTO_INCREMENT;

ALTER TABLE `tipo_pago`
  MODIFY `id_tipo_pago` int(11) NOT NULL AUTO_INCREMENT;

ALTER TABLE `orden`
  MODIFY `id_orden` int(11) NOT NULL AUTO_INCREMENT;

ALTER TABLE `detalle_orden`
  MODIFY `id_detalle_orden` int(11) NOT NULL AUTO_INCREMENT;

-- --------------------------------------------------------

--
-- Constraints for dumped tables
--

--
-- Constraints for table `usuario`
--
ALTER TABLE `usuario`
  ADD CONSTRAINT `usuario_ibfk_1`
  FOREIGN KEY (`idRel_tipo_usuario`)
  REFERENCES `tipo_usuario` (`id_tipo_usuario`)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;

--
-- Constraints for table `cliente`
--
ALTER TABLE `cliente`
  ADD CONSTRAINT `cliente_ibfk_1`
  FOREIGN KEY (`idRel_tipo_cliente`)
  REFERENCES `tipo_cliente` (`id_tipo_cliente`)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;

--
-- Constraints for table `orden`
--
ALTER TABLE `orden`
  ADD CONSTRAINT `orden_ibfk_1`
  FOREIGN KEY (`idRel_cliente`)
  REFERENCES `cliente` (`id_cliente`)
  ON DELETE SET NULL
  ON UPDATE CASCADE,
  ADD CONSTRAINT `orden_ibfk_2`
  FOREIGN KEY (`idRel_tipo_pago`)
  REFERENCES `tipo_pago` (`id_tipo_pago`)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;

--
-- Constraints for table `detalle_orden`
--
ALTER TABLE `detalle_orden`
  ADD CONSTRAINT `detalle_orden_ibfk_1`
  FOREIGN KEY (`idRel_orden`)
  REFERENCES `orden` (`id_orden`)
  ON DELETE CASCADE
  ON UPDATE CASCADE;

COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
