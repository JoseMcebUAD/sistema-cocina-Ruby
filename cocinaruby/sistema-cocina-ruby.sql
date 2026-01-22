

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

CREATE TABLE IF NOT EXISTS `tipo_usuario` (
  `id_tipo_usuario` int(11) NOT NULL PRIMARY KEY,
  `nombre_tipo_usuario` varchar(50) NOT NULL,
  `permisos_usuario` varchar(1000) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `usuario`
--

CREATE TABLE IF NOT EXISTS `usuario` (
  `id_usuario` int(11) NOT NULL PRIMARY KEY,
  `idRel_tipo_usuario` int(11) NOT NULL,
  `nombre_usuario` varchar(50) NOT NULL,
  `contrasena_usuario` varchar(200) NOT NULL,
  FOREIGN KEY (`idRel_tipo_usuario`) REFERENCES `tipo_usuario` (`id_tipo_usuario`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `cliente`
--

CREATE TABLE IF NOT EXISTS `cliente` (
  `id_cliente` int(11) NOT NULL PRIMARY KEY,
  `nombre_cliente` varchar(50) NOT NULL,
  `direcciones` varchar(400) DEFAULT NULL,
  `telefono` varchar(25) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `tipo_pago`
--

CREATE TABLE IF NOT EXISTS `tipo_pago` (
  `id_tipo_pago` int(11) NOT NULL PRIMARY KEY,
  `nombre_tipo_pago` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `orden`
--

CREATE TABLE IF NOT EXISTS `orden` (
  `id_orden` int(11) NOT NULL PRIMARY KEY,
  `idRel_tipo_pago` int(11) NOT NULL,
  `tipo_cliente` ENUM('Mostrador','Domicilio','Mesa') NOT NULL,
  `fecha_expedicion_orden` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `precio_orden` decimal(10,2) NOT NULL DEFAULT 0.00,
  `pago_cliente` decimal(10,2) NULL DEFAULT 0.00,
  `facturado` tinyint(1) NOT NULL DEFAULT 0,
  FOREIGN KEY (`idRel_tipo_pago`) REFERENCES `tipo_pago` (`id_tipo_pago`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `orden_mostrador`
--

CREATE TABLE IF NOT EXISTS `orden_mostrador` (
  `id_orden` int(11) PRIMARY KEY,
  `nombre` varchar(100) DEFAULT NULL,
  FOREIGN KEY (`id_orden`) REFERENCES `orden` (`id_orden`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `orden_domicilio`
--

CREATE TABLE IF NOT EXISTS `orden_domicilio` (
  `id_orden` int(11) PRIMARY KEY,
  `idRel_cliente` int(11) DEFAULT NULL,
  `direccion` varchar(400) DEFAULT NULL,
  FOREIGN KEY (`id_orden`) REFERENCES `orden` (`id_orden`) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (`idRel_cliente`) REFERENCES `cliente` (`id_cliente`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `orden_mesa`
--

CREATE TABLE IF NOT EXISTS `orden_mesa` (
  `id_orden` int(11) PRIMARY KEY,
  `numero_mesa` varchar(30) DEFAULT NULL,
  FOREIGN KEY (`id_orden`) REFERENCES `orden` (`id_orden`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `detalle_orden`
--

CREATE TABLE IF NOT EXISTS `detalle_orden` (
  `id_detalle_orden` int(11) NOT NULL PRIMARY KEY,
  `idRel_orden` int(11) NOT NULL,
  `especificaciones_detalle_orden` varchar(100) DEFAULT NULL,
  `precio_detalle_orden` decimal(10,2) NOT NULL,
  `cantidad` int(3) NOT NULL DEFAULT 1,
  FOREIGN KEY (`idRel_orden`) REFERENCES `orden` (`id_orden`) ON DELETE CASCADE ON UPDATE CASCADE
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


COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
