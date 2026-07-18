**ERS Dashboard Administrativo Cocina Rubí**

**Especificación de Requisitos del Sistema**

Dashboard Administrativo — Cocina Rubí (Cocina Económica)

Versión: 1.0

Fecha: 2026-05-24

Estado: Borrador Inicial

## **Control de Versiones**

| Versión | Fecha | Autor | Descripción de cambio |
| ----- | ----- | ----- | ----- |
| 1.0 | 2026-04-30 | José Ceballos y Ángel Uribe | Creación inicial del documento: Introducción, Propósito, Alcance, Actores y Educción de requisitos basados en el diseño del carrito de compras. |
| 1.1 | 2026-05-24 | Equipo Re-Q-Soft | Corrección de numeración de requisitos, eliminación de duplicados, reintegración de RF omitido (actualización en tiempo real de caja) y adición de sección 1.3 Definiciones. |

# **1\. Introducción**

## **1.1 Propósito**

Este documento describe las especificaciones funcionales y no funcionales para el dashboard administrativo de Cocina Rubí, una cocina económica. El sistema busca digitalizar y centralizar el proceso de registro de comidas, ingresos, cierre de caja, rutas y pedidos de los clientes, así como la impresión de los pedidos, con el objetivo de evitar la dispersión de información en múltiples entornos. El documento está dirigido al equipo de desarrollo y al cliente como guía base para el diseño, implementación y pruebas del sistema.

## **1.2 Ámbito del Sistema**

El sistema recibirá el nombre de Dashboard Administrativo Cocina Rubí. El sistema comprende el desarrollo de una aplicación de escritorio (frontend) que centraliza la gestión operativa de la cocina económica e incluye los siguientes módulos:

* Configuración de comidas: visualización de comidas con precios, imágenes, estatus de disponibilidad y conteo de vendidos del día.

* Configurador de complementos: visualización y gestión de complementos asociados a comidas y platillos específicos.

* Configurador de charolas: visualización y gestión de charolas.

* Configurador de desayunos: visualización y gestión de desayunos.

* Configuración de rutas: configuración de rutas de reparto y costo de envío.

* Configuración de snacks y bebidas: visualización y gestión de bebidas y snacks.

* Configuración de tarifas especiales: visualización y gestión de tarifas especiales, como la tarifa de lluvia.

* Configuración de horarios de atención:

  * Desayunos: lunes a sábado, 7:00 AM – 11:00 AM.

  * Almuerzos: lunes a viernes, 8:30 AM – 3:30 PM.

* Módulo de impresión: impresión de tickets de todos los pedidos entrantes.

* Módulo de anuncios: gestión de anuncios para la cocina

* Gestión de códigos especiales para clientes del carrito de compras

* Módulo de ingresos: resumen de ventas totales del día con desglose por método de pago, ruta y comisiones.

* Módulo de pedidos: gestión para crear pedidos de pick-up, comedor o domicilio.

* Resumen de comidas: visualización de resumen de comidas con estadísticas de ventas.

El sistema no incluye pasarelas de pago bancarias directas ni sistemas de seguimiento de repartidores en tiempo real.

## **1.3 Definiciones, Acrónimos y Abreviaturas**

| Término / Acrónimo | Definición |
| ----- | ----- |
| **ERS** | Especificación de Requisitos Software. |
| **RF** | Requisito Funcional. |
| **RNF** | Requisito No Funcional. |
| **Pick-up** | Modalidad de pedido donde el cliente recoge su orden en el establecimiento. |
| **Domicilio** | Modalidad de pedido con entrega en la dirección del cliente. |
| **Mostrador** | Modalidad de pedido atendido directamente en el establecimiento sin ruta de entrega. |
| **Complemento** | Acompañamiento asociado a una comida (por ejemplo: arroz, frijoles, ensalada). |
| **Charola** | Artículo extra de servicio que puede añadirse a un pedido. |
| **Básico** | Combinación predefinida de una comida con al menos un complemento predeterminado, nombre, precio e imagen. |
| **Desayuno** | Producto de catálogo disponible únicamente en el horario de desayunos (L-S 7:00–11:00 AM). |
| **Favorito** | Producto marcado para visualización prioritaria en el carrito de compras del menú web. |
| **Ruta** | Zona de reparto predefinida con nombre, tarifa y orden de salida. |
| **Tarifa lluvia** | Cargo adicional aplicado al costo de envío cuando se activa manualmente por condiciones climáticas, con nombre y monto configurable. |
| **Tarifa extraordinaria** | Cargo especial para clientes fuera del rango de las rutas predefinidas, registrado mediante un código y monto específico. |
| **Corte de caja** | Resumen financiero diario con desglose de ingresos por método de pago, ruta y pago al repartidor. |
| **Token** | Credencial digital de sesión utilizada para autenticar las peticiones del usuario al sistema. |
| **Lazy load** | Técnica de carga diferida de imágenes para optimizar el rendimiento. |

## **1.4 Referencias**

* IEEE Std. 830-1998. IEEE Recommended Practice for Software Requirements Specifications.

* Entrevistas y sesiones de educción de requisitos realizadas con el cliente de Cocina Rubí, abril–mayo 2026\.

* Documento de educción inicial: "ERS dashboard administrativo cocina rubí — Propuesta AJ" (versión 1.0, 2026-04-30).

## **1.5 Visión General del Documento**

El presente documento se organiza en tres secciones principales. La Sección 1 (esta sección) presenta la introducción general. La Sección 2 describe el contexto general del producto. La Sección 3 contiene los requisitos específicos, tanto funcionales como no funcionales, identificados mediante códigos únicos con el formato RF-XXX o RNF-XXX.

# **2\. Descripción General**

## **2.1 Perspectiva del Producto**

El Dashboard Administrativo Cocina Rubí es un producto de software nuevo, desarrollado a medida para una cocina económica. No forma parte de un sistema mayor preexistente ni se integra con plataformas externas de terceros en su versión inicial.

Actualmente, los procesos de registro de pedidos, control de ingresos y gestión de comidas se realizan de forma manual o mediante herramientas dispersas. El sistema reemplazará estos procesos con módulos digitales centralizados, accesibles desde una computadora o tablet en el área operativa del establecimiento.

El sistema operará como aplicación de escritorio y se comunicará con un backend mediante red local o internet, garantizando disponibilidad durante el horario operativo del establecimiento.

## **2.2 Funciones del Producto**

A grandes rasgos, el sistema ofrecerá las siguientes funciones principales:

* Gestión del catálogo de comidas, complementos, charolas, snacks, bebidas y desayunos.

* Creación y administración de pedidos (domicilio, pick-up y mostrador).

* Visualización y gestión en tiempo real de pedidos entrantes desde el menú web.

* Impresión automática y manual de tickets de pedido.

* Control de rutas de reparto con tarifas configurables y tarifas especiales.

* Módulo de corte de caja con desglose de ingresos por método de pago, ruta y comisiones.

* Gestión de avisos para el carrito de compras.

* Gestión de códigos especiales para los clientes del carrito de compras

* Gestión de tarifas especiales y horarios de atención.

* Visualización de inventario y conteo de comidas.

* Autenticación de usuario mediante nombre de usuario y PIN.

## **2.3 Características de los Usuarios**

| Tipo de usuario | Nivel educativo | Experiencia técnica | Función en el sistema |
| ----- | ----- | ----- | ----- |
| **Operador de cocina** | Educación media-superior | Baja-media — uso cotidiano de smartphone | Usuario principal del dashboard: gestión de todos los módulos del sistema (pedidos, catálogo, ingresos, impresión). |

Dado el bajo nivel de experiencia técnica del usuario, el sistema deberá ser intuitivo, con navegación simple, mensajes claros y retroalimentación visual en cada acción.

*NOTA: de momento solo se tiene un usuario con todos los permisos del sistema. si en un futuro se tienen más usuarios ponerlos en esta vista.*

## **2.4 Restricciones**

* El sistema deberá operar como aplicación de escritorio en hardware existente o de bajo costo (computadora o tablet con resolución de 320 px a 768 px).

* La interfaz debe estar completamente en español.

* El sistema no incluye pasarelas de pago bancarias directas ni seguimiento de repartidores en tiempo real en esta versión.

* La autenticación se realizará mediante nombre de usuario y PIN de 5 dígitos; no se contemplan métodos biométricos ni de doble factor.

* La gestión de pagos con tarjeta (Clip) o transferencia bancaria es externa al sistema; el dashboard únicamente registrará el tipo de pago utilizado.


## **2.5 Suposiciones y Dependencias**

* Se asume que el establecimiento contará con al menos una tablet operativa durante el horario de servicio.

* Se asume que el catálogo de rutas de reparto está predefinido por el repartidor y que el número máximo de rutas no se modificará sin diálogo entre desarrolladores y dueña de la cocina.

* El sistema depende de la disponibilidad de una impresora térmica de tickets conectada a la misma red WiFi que el dispositivo donde corre el dashboard.

* Se asume que los precios, tarifas y configuración del catálogo pueden ser actualizados por el operador desde el sistema sin modificar el código fuente.

* Se asume que el personal operativo recibirá capacitación básica antes de la puesta en marcha, apoyada por el manual de usuario entregado con el software.

## **2.6 Requisitos Futuros**

* Integración con dispositivo Clip para registro automático de pagos con tarjeta.

* Sistema de seguimiento de repartidores en tiempo real.

* Módulo de reservaciones en línea accesible desde dispositivos externos.

* Notificaciones automáticas al operador cuando un pedido lleve más de X minutos sin atenderse.

# **3\. Requisitos Específicos**

En esta sección se encuentran los requisitos del Dashboard Administrativo Cocina Rubí a un nivel de detalle suficiente para que los diseñadores puedan diseñar un sistema que los satisfaga y el equipo de pruebas pueda planificar y ejecutar las verificaciones correspondientes. Todo requisito está identificado mediante un código único con el formato RF-XXX o RNF-XXX.

## **3.1 Interfaces Externas**

### **3.1.1 Interfaz de Usuario**

* La interfaz de usuario será una aplicación de escritorio con diseño responsivo que permita su uso en pantallas con resolución de 320 px a 768 px (tablet).

* Todos los textos, mensajes, etiquetas y botones estarán completamente en español.

* El sistema mostrará retroalimentación visual mediante pop-ups de color verde para acciones exitosas (POST, PUT, PATCH, DELETE e impresión) y de color rojo para errores, con mensajes descriptivos.

* Las imágenes del catálogo se cargarán mediante lazy load para optimizar el rendimiento.

**⚠ PENDIENTE:** *Definir el diseño y ubicación de la sección de avisos, tanto en el dashboard como en el menú web de pick-up.*

### **3.1.2 Interfaz con Otros Sistemas**

* El sistema recibirá pedidos en tiempo real desde el menú web del cliente mediante integración con el backend.

* El sistema no contempla interfaces con plataformas de pago bancarias directas en esta versión.

### **3.1.3 Interfaces de Comunicación**

* El sistema operará conectado al backend mediante red local o internet.

* La comunicación con la impresora térmica se realizará mediante red WiFi; el dispositivo del operador deberá estar conectado a la misma red que la impresora y a una distancia máxima de 5 metros de ella.

## **3.2 Funciones**

Los requisitos funcionales se organizan por módulo. Cada requisito describe una acción que el sistema debe ser capaz de realizar.

### **3.2.1 Módulo de Catálogo de Comidas**

| ID | Descripción del Requisito |
| ----- | ----- |
| **RF-001** | El sistema deberá mostrar la lista de comidas disponibles con su imagen principal, dos imágenes adicionales, nombre, precio (media o entera), identificador, estatus de disponibilidad y conteo de vendidos del día. |
| **RF-002** | El sistema deberá permitir al operador registrar una nueva comida capturando los siguientes datos obligatorios: nombre, descripción, precio de media porción, precio de entera, disponibilidad, identificador, fecha y al menos una imagen de un máximo de tres imágenes. |
| **RF-003** | El sistema deberá permitir al operador editar los datos de una comida existente en cualquier momento. |
| **RF-004** | El sistema deberá permitir al operador eliminar una comida del catálogo. |
| **RF-005** | El sistema deberá restringir la eliminación de una comida del catálogo cuando dicha comida tenga pedidos históricos registrados, mostrando un mensaje informativo al operador. |

### **3.2.2 Módulo de Complementos**

| ID | Descripción del Requisito |
| ----- | ----- |
| **RF-006** | El sistema deberá permitir al operador registrar un complemento capturando los siguientes campos: nombre, identificador, precio (si aplica), disponibilidad, precio extra por complemento adicional e imagen. |
| **RF-007** | El sistema deberá permitir al operador editar los datos de un complemento existente en cualquier momento. |
| **RF-008** | El sistema deberá permitir al operador eliminar un complemento del catálogo. |
| **RF-009** | El sistema deberá impedir la eliminación de un complemento del catálogo cuando dicho complemento tenga pedidos históricos registrados, mostrando un mensaje informativo al operador. |

### **3.2.3 Módulo de Snacks, Charolas y Desayunos**

| ID | Descripción del Requisito |
| ----- | ----- |
| **RF-010** | El sistema deberá permitir al operador registrar un snack capturando los siguientes campos: nombre, precio, imagen y disponibilidad. |
| **RF-011** | El sistema deberá permitir al operador registrar una charola capturando los siguientes campos: nombre, precio, imagen y disponibilidad. |
| **RF-012** | El sistema deberá permitir al operador registrar un desayuno capturando los siguientes campos: nombre, precio, imagen y disponibilidad. |
| **RF-013** | El sistema deberá permitir al operador editar y eliminar snacks, charolas y desayunos del catálogo. |
| **RF-014** | El sistema deberá impedir la eliminación de un snack, charola o desayuno cuando el elemento tenga pedidos históricos registrados, mostrando un mensaje informativo al operador. |

### **3.2.4 Módulo de Bebidas**

| ID | Descripción del Requisito |
| ----- | ----- |
| **RF-015** | El sistema deberá permitir al operador registrar una bebida capturando los siguientes campos: nombre, precio estándar, precio de domicilio, precio de pick-up, imagen y disponibilidad. |
| **RF-016** | El sistema deberá permitir al operador editar los datos de una bebida en cualquier momento. |
| **RF-017** | El sistema deberá impedir la eliminación de una bebida cuando dicha bebida tenga pedidos históricos registrados, mostrando un mensaje informativo al operador. |

### **3.2.5 Módulo de Básicos**

| ID | Descripción del Requisito |
| ----- | ----- |
| **RF-018** | El sistema deberá permitir al operador registrar y editar una categoría "básico", compuesta por una comida, al menos un complemento, nombre del básico, precio, descripción e imagen. |
| **RF-019** | El sistema deberá impedir la eliminación de un básico cuando dicho básico tenga pedidos históricos registrados, mostrando un mensaje informativo al operador. |

### **3.2.6 Módulo de Configuración de Rutas**

| ID | Descripción del Requisito |
| ----- | ----- |
| **RF-020** | El sistema deberá permitir al operador editar los campos de nombre, tarifa y orden de salida de las rutas de reparto predefinidas por el equipo de desarrollo. |
| **RF-021** | El sistema deberá tener predefinidas las zonas de reparto configuradas por el equipo de desarrollo; el operador no podrá crear rutas nuevas más allá del número de rutas definidas por defecto. |
| **RF-022** | El sistema deberá mostrar un mapa de las zonas de las rutas de reparto disponibles, correspondientes a la zona norte de Yucatán. |
| **RF-023** | El sistema deberá permitir al operador activar manualmente tarifas adicionales configurando el nombre de la tarifa y el monto adicional aplicable al costo de envío (por ejemplo, tarifa lluvia). |
| **RF-024** | El sistema deberá permitir al operador registrar un código y una tarifa extraordinaria para atender clientes que se encuentren fuera del rango de las rutas predefinidas. |

### **3.2.7 Módulo de Pedidos**

| ID | Descripción del Requisito |
| ----- | ----- |
| **RF-025** | El sistema deberá permitir al operador generar un pedido manual capturando: método de pago, comidas con sus complementos, charolas, snacks, bebidas, desayunos y básicos; tipo de envío (domicilio, pick-up o mostrador); nombre del cliente; y dirección del cliente (obligatoria únicamente cuando el tipo de envío sea domicilio). |
| **RF-026** | El sistema deberá impedir el registro de un desayuno en un pedido pasadas las 11:00 AM, mostrando un mensaje informativo al operador. |
| **RF-027** | El sistema deberá calcular automáticamente el total del pedido con base en los productos, cantidades y tipo de envío seleccionados, sin requerir cálculo manual por parte del operador. |
| **RF-028** | El sistema deberá incluir en el cálculo del total el precio base de los complementos a partir del cuarto complemento por comida; los primeros tres complementos no generarán cargo adicional. |
| **RF-029** | El sistema deberá permitir al operador ingresar el monto del billete entregado por el cliente para calcular y mostrar el cambio a devolver. |
| **RF-030** | El sistema deberá mostrar en tiempo real los pedidos entrantes desde el menú web, ordenados por hora de llegada. |
| **RF-031** | El sistema deberá permitir filtrar los pedidos entrantes por ruta de reparto. |
| **RF-032** | El sistema deberá dar prioridad visual, dentro de cada filtro de la vista de pedidos, a los pedidos que aún no hayan sido impresos. |
| **RF-033** | El sistema deberá permitir al operador editar y eliminar cualquier pedido, tanto los originados desde el menú web como los manuales, independientemente de su estado (impreso o entregado). |

### **3.2.8 Módulo de Impresión de Tickets**

| ID | Descripción del Requisito |
| ----- | ----- |
| **RF-034** | El sistema deberá imprimir automáticamente el ticket de un pedido al momento de ser registrado manualmente por el operador. |
| **RF-035** | El sistema deberá imprimir automáticamente dos tickets de un pedido web una vez transcurridos 5 minutos desde su recepción, si no ha sido impreso antes. El temporizador conservará el tiempo ya consumido en caso de que el pedido sea actualizado. |
| **RF-036** | El sistema deberá impedir la impresión de tickets a menos que el dispositivo del operador esté conectado a la misma red WiFi que la impresora y a una distancia máxima de 7 metros de ella. |
| **RF-037** | El sistema deberá permitir al operador reimprimir el ticket de cualquier pedido registrado durante el día en curso. |
| **RF-038** | El sistema deberá mostrar un aviso visual cuando la impresora se quede sin papel (rollo de tickets). |
| **RF-039** | El sistema deberá encolar los tickets de pedidos web pendientes cuando la impresora no tenga papel y, al reponerse el rollo, imprimir automáticamente todos los tickets en cola. |

### **3.2.9 Módulo de Ingresos y Corte de Caja**

| ID | Descripción del Requisito |
| ----- | ----- |
| **RF-040** | El sistema deberá mostrar en el módulo de caja el resumen del corte diario, incluyendo: ingreso total del día, desglose por método de pago (efectivo, tarjeta, transferencia), desglose por ruta y pago al repartidor. |
| **RF-041** | El sistema deberá actualizar en tiempo real el módulo de caja cada vez que se cree, edite o elimine un pedido. |
| **RF-042** | El sistema deberá permitir al operador filtrar el resumen de caja por rango de fechas para consultar períodos anteriores. |
| **RF-043** | El sistema deberá permitir registrar el pago arbitrario al repartidor, capturando los campos de fecha y monto del pago. |

### **3.2.10 Módulo de Conteo de Comidas**

| ID | Descripción del Requisito |
| ----- | ----- |
| **RF-044** | El sistema deberá permitir registrar el conteo de una comida indicando la cantidad y la unidad de medida (kilogramos, gramos, litros u otras unidades configurables según el tipo de ingrediente). |
| **RF-045** | El sistema deberá mostrar el consumo de porciones medias y enteras de una comida, así como su historial de conteo acumulado. |

### **3.2.11 Requisitos Generales**

| ID | Descripción del Requisito |
| ----- | ----- |
| **RF-046** | El sistema deberá permitir al operador marcar como favorito cualquier producto del catálogo (comidas, complementos, snacks, básicos, bebidas y charolas) desde su módulo correspondiente. |
| **RF-047** | El sistema deberá contar con un catálogo general donde se visualicen todos los productos disponibles: comidas, complementos, bebidas, snacks, charolas y desayunos. |
| **RF-048** | El sistema deberá mostrar el resumen de consumo de un producto del catálogo, incluyendo cantidad total, ganancias (cuando aplique), desglose entre porción media y entera (cuando aplique) y desglose por tipo de pedido (domicilio, mostrador y pick-up). |

### **3.2.12 Códigos especiales**

| ID | Descripción del Requisito |
| ----- | ----- |
| **RF-046** | El sistema deberá permitir al operador registrar un código especial para clientes especiales con los campos de código de 9 caracteres, nombre del código, tarifa especial Y su estatus (activo o no). |
| **RF-047** | El sistema deberá permitir al usuario editar los campos del código especial a excepción de los caracteres de este |
| **RF-048** | El sistema impedirá eliminar un código especial en caso que ya tenga pedidos registrado |

### **3.2.13 Avisos para el carrito de compras**

| ID | Descripción del Requisito |
| ----- | ----- |
| **RF-049** | El sistema deberá permitir al operador registrar avisos para el carrito de compras con los campos de descripción anuncio, fecha de expiración, color y descripción del anuncio |
| **RF-050** | El sistema deberá permitir al operador editar los campos de avisos en cualquier momento, reflejándose en el carrito de compras |
| **RF-051** | El sistema deberá permitir al operador eliminar el aviso en cualquier momento, reflejándose en el carrito de compras |

### **3.2.14 Módulo de Autenticación**

| ID | Descripción del Requisito |
| ----- | ----- |
| **RF-052** | El sistema deberá autenticar al operador mediante nombre de usuario y un PIN de 5 dígitos para acceder a cualquier módulo del sistema. |
| **RF-053** | El sistema deberá cerrar la sesión del operador automáticamente tras 3 días sin realizar ninguna petición al sistema. |
| **RF-054** | El sistema deberá renovar el token de sesión del operador automáticamente cada 3 horas mientras la sesión esté activa y no hayan transcurrido los 3 días de inactividad. |

## **3.3 Requisitos No Funcionales**

Los siguientes requisitos establecen las restricciones de calidad, comportamiento y operación del sistema.

| ID | Descripción del Requisito |
| ----- | ----- |
| **RNF-001** | El sistema deberá imprimir un ticket en un tiempo máximo de 4 segundos desde que se envía la orden de impresión. Si transcurren más de 5 segundos sin respuesta, el sistema considerará que la impresora no está disponible o no tiene papel y mostrará la alerta correspondiente. |
| **RNF-002** | El sistema deberá registrar un log de errores que incluya fecha, hora y descripción del error ocurrido. |
| **RNF-003** | El sistema deberá contar con un manual de usuario en español que cubra cada una de las funcionalidades del sistema y los procedimientos de uso de la impresora, entregado junto con el software. |
| **RNF-004** | Un operador sin experiencia técnica previa deberá ser capaz de completar las tareas principales del sistema sin asistencia del equipo de desarrollo, utilizando únicamente el manual de usuario. |
| **RNF-005** | El sistema deberá mostrar un pop-up de color rojo con un mensaje descriptivo cada vez que ocurra un error, indicando claramente la acción que falló y el motivo (por ejemplo: "No se pudo registrar el pedido porque falta el campo de dirección", "Error al conectar con la impresora"). |
| **RNF-006** | El sistema deberá mostrar un pop-up de color verde por cada acción exitosa de tipo POST, PUT, PATCH o DELETE, así como al completarse una impresión de ticket. |
| **RNF-007** | El sistema deberá cargar las imágenes del catálogo mediante lazy load para optimizar el rendimiento en dispositivos con recursos limitados. |
| **RNF-008** | El sistema deberá soportar una resolución mínima de 320 px y una resolución máxima de 768 px de ancho (formato tablet). |

*— Fin del documento ERS v1.1 — Dashboard Administrativo Cocina Rubí —*