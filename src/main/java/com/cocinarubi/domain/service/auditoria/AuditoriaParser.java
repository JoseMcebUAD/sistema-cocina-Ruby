package com.cocinarubi.domain.service.auditoria;

import com.cocinarubi.DBConstants.TipoOperacion;
import com.cocinarubi.presentation.dto.response.AuditoriaResponseDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class AuditoriaParser {

    private final ObjectMapper objectMapper;
    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("eee yyyy/M/d HH:mm", Locale.forLanguageTag("es"));

    public AuditoriaParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Page<AuditoriaResponseDTO> parsear(Page<AuditoriaResponseDTO> pagina) {
        return pagina.map(this::enriquecer);
    }

    private AuditoriaResponseDTO enriquecer(AuditoriaResponseDTO dto) {
        dto.setDescripcion(generarDescripcion(dto));
        return dto;
    }

    private String generarDescripcion(AuditoriaResponseDTO dto) {
        String tabla = dto.getTabla();
        TipoOperacion accion = dto.getAccion();
        Integer id = dto.getIdRegistro();
        LocalDateTime fecha = dto.getFecha();
        JsonNode despues = parsearJson(dto.getDatosDespues());
        JsonNode antes = parsearJson(dto.getDatosAntes());

        if (tabla == null || accion == null) return "Operación registrada";

        return switch (tabla) {
            case "pedido"             -> describirPedido(accion, despues, antes, id, fecha);
            case "comida"             -> describirComida(accion, despues, antes, id, fecha);
            case "complemento"        -> describirComplemento(accion, despues, antes, id, fecha);
            case "basico"             -> describirBasico(accion, despues, antes, id, fecha);
            case "desayuno"           -> describirDesayuno(accion, despues, antes, id, fecha);
            case "cliente"            -> describirCliente(accion, despues, antes, id, fecha);
            case "producto_cocina"    -> describirProductoCocina(accion, despues, antes, id, fecha);
            case "inventario_comida"  -> describirInventarioComida(accion, despues, antes, id, fecha);
            case "usuario"            -> describirUsuario(accion, despues, antes, id, fecha);
            case "anuncio"            -> describirAnuncio(accion, despues, antes, id, fecha);
            case "ruta"               -> describirRuta(accion, despues, antes, id, fecha);
            case "tarifa_especial"    -> describirTarifaEspecial(accion, despues, antes, id, fecha);
            case "favorito_cliente"   -> describirFavoritoCliente(accion, despues, antes, id, fecha);
            case "codigo_cliente"     -> describirCodigoCliente(accion, despues, antes, id, fecha);
            case "pago_repartidor"    -> describirPagoRepartidor(accion, despues, antes, id, fecha);
            case "horario_atencion"   -> describirHorarioAtencion(accion, despues, antes, id, fecha);
            default                   -> accion + " en " + tabla + (id != null ? " #" + id : "");
        };
    }

    private String describirPedido(TipoOperacion accion, JsonNode despues, JsonNode antes, Integer id, LocalDateTime fecha) {
        return switch (accion) {
            case POST -> {
                LocalDateTime dateTime = parseLocalDateTime(textOrElse(despues, "fechaExpedicionPedido", null));
                if (dateTime == null) dateTime = fecha;
                String fechaFormateada = dateTime != null ? dateTime.format(formatter) : "fecha desconocida";
                String tipo = textOrElse(despues, "tipoPedido", "desconocido");
                yield "Se creó el pedido #" + id + " de tipo " + tipo + " el " + fechaFormateada;
            }
            case PUT    -> "Se actualizó el pedido #" + id;
            case DELETE -> "Se eliminó el pedido #" + id;
        };
    }

    private String describirComida(TipoOperacion accion, JsonNode despues, JsonNode antes, Integer id, LocalDateTime fecha) {
        return switch (accion) {
            case POST -> {
                String nombre = textOrElse(despues, "nombreComida", "desconocida");
                yield "Se creó la comida '" + nombre + "'";
            }
            case PUT -> {
                String nombreDespues = textOrElse(despues, "nombreComida", null);
                String nombreAntes = textOrElse(antes, "nombreComida", null);
                String nombre = nombreDespues != null ? nombreDespues : (nombreAntes != null ? nombreAntes : "#" + id);
                yield "Se actualizó la comida '" + nombre + "'";
            }
            case DELETE -> "Se eliminó la comida #" + id;
        };
    }

    private String describirComplemento(TipoOperacion accion, JsonNode despues, JsonNode antes, Integer id, LocalDateTime fecha) {
        return switch (accion) {
            case POST -> {
                String nombre = textOrElse(despues, "nombreComplemento", "desconocido");
                yield "Se creó el complemento '" + nombre + "'";
            }
            case PUT -> {
                String nombreDespues = textOrElse(despues, "nombreComplemento", null);
                String nombreAntes = textOrElse(antes, "nombreComplemento", null);
                String nombre = nombreDespues != null ? nombreDespues : (nombreAntes != null ? nombreAntes : "#" + id);
                yield "Se actualizó el complemento '" + nombre + "'";
            }
            case DELETE -> "Se eliminó el complemento #" + id;
        };
    }

    private String describirBasico(TipoOperacion accion, JsonNode despues, JsonNode antes, Integer id, LocalDateTime fecha) {
        return switch (accion) {
            case POST -> {
                String desc = textOrElse(despues, "descripcion", "sin descripción");
                yield "Se creó el básico '" + desc + "'";
            }
            case PUT    -> "Se actualizó el básico #" + id;
            case DELETE -> "Se eliminó el básico #" + id;
        };
    }

    private String describirDesayuno(TipoOperacion accion, JsonNode despues, JsonNode antes, Integer id, LocalDateTime fecha) {
        return switch (accion) {
            case POST -> {
                String nombre = textOrElse(despues, "nombreDesayuno", "desconocido");
                yield "Se creó el desayuno '" + nombre + "'";
            }
            case PUT -> {
                String nombreDespues = textOrElse(despues, "nombreDesayuno", null);
                String nombreAntes = textOrElse(antes, "nombreDesayuno", null);
                String nombre = nombreDespues != null ? nombreDespues : (nombreAntes != null ? nombreAntes : "#" + id);
                yield "Se actualizó el desayuno '" + nombre + "'";
            }
            case DELETE -> "Se eliminó el desayuno #" + id;
        };
    }

    private String describirCliente(TipoOperacion accion, JsonNode despues, JsonNode antes, Integer id, LocalDateTime fecha) {
        return switch (accion) {
            case POST -> {
                String nombre = textOrElse(despues, "nombre", "desconocido");
                yield "Se registró el cliente '" + nombre + "'";
            }
            case PUT -> {
                String nombreDespues = textOrElse(despues, "nombre", null);
                String nombreAntes = textOrElse(antes, "nombre", null);
                String nombre = nombreDespues != null ? nombreDespues : (nombreAntes != null ? nombreAntes : "#" + id);
                yield "Se actualizó el cliente '" + nombre + "'";
            }
            case DELETE -> "Se eliminó el cliente #" + id;
        };
    }

    private String describirProductoCocina(TipoOperacion accion, JsonNode despues, JsonNode antes, Integer id, LocalDateTime fecha) {
        return switch (accion) {
            case POST -> {
                String nombre = textOrElse(despues, "nombreProducto", "desconocido");
                String tipo = textOrElse(despues, "tipoProducto", "");
                String sufijo = tipo.isBlank() ? "" : " (" + tipo + ")";
                yield "Se creó el producto de cocina '" + nombre + "'" + sufijo;
            }
            case PUT -> {
                String nombreDespues = textOrElse(despues, "nombreProducto", null);
                String nombreAntes = textOrElse(antes, "nombreProducto", null);
                String nombre = nombreDespues != null ? nombreDespues : (nombreAntes != null ? nombreAntes : "#" + id);
                yield "Se actualizó el producto de cocina '" + nombre + "'";
            }
            case DELETE -> "Se eliminó el producto de cocina #" + id;
        };
    }

    private String describirInventarioComida(TipoOperacion accion, JsonNode despues, JsonNode antes, Integer id, LocalDateTime fecha) {
        return switch (accion) {
            case POST   -> "Se creó el contador de comidas #" + id;
            case PUT    -> "Se actualizó el contador de comidas #" + id;
            case DELETE -> "Se eliminó el contador de comidas #" + id;
        };
    }

    private String describirUsuario(TipoOperacion accion, JsonNode despues, JsonNode antes, Integer id, LocalDateTime fecha) {
        return switch (accion) {
            case POST -> {
                String nombre = textOrElse(despues, "nombreUsuario", "desconocido");
                yield "Se creó el usuario '" + nombre + "'";
            }
            case PUT -> {
                String nombreDespues = textOrElse(despues, "nombreUsuario", null);
                String nombreAntes = textOrElse(antes, "nombreUsuario", null);
                String nombre = nombreDespues != null ? nombreDespues : (nombreAntes != null ? nombreAntes : "#" + id);
                yield "Se actualizó el usuario '" + nombre + "'";
            }
            case DELETE -> "Se eliminó el usuario #" + id;
        };
    }

    private String describirAnuncio(TipoOperacion accion, JsonNode despues, JsonNode antes, Integer id, LocalDateTime fecha) {
        return switch (accion) {
            case POST -> {
                String desc = textOrElse(despues, "descripcionAnuncio", "sin descripción");
                yield "Se publicó el anuncio '" + desc + "'";
            }
            case PUT    -> "Se actualizó el anuncio #" + id;
            case DELETE -> "Se eliminó el anuncio #" + id;
        };
    }

    private String describirRuta(TipoOperacion accion, JsonNode despues, JsonNode antes, Integer id, LocalDateTime fecha) {
        return switch (accion) {
            case POST -> {
                String nombre = textOrElse(despues, "nombre", "desconocida");
                yield "Se creó la ruta '" + nombre + "'";
            }
            case PUT -> {
                String nombreDespues = textOrElse(despues, "nombre", null);
                String nombreAntes = textOrElse(antes, "nombre", null);
                String nombre = nombreDespues != null ? nombreDespues : (nombreAntes != null ? nombreAntes : "#" + id);
                yield "Se actualizó la ruta '" + nombre + "'";
            }
            case DELETE -> "Se eliminó la ruta #" + id;
        };
    }

    private String describirTarifaEspecial(TipoOperacion accion, JsonNode despues, JsonNode antes, Integer id, LocalDateTime fecha) {
        return switch (accion) {
            case POST -> {
                String nombre = textOrElse(despues, "nombreTarifa", "sin nombre");
                String monto = textOrElse(despues, "tarifa", "0");
                yield "Se creó la tarifa especial '" + nombre + "' ($" + monto + ")";
            }
            case PUT    -> "Se actualizó la tarifa especial #" + id;
            case DELETE -> "Se eliminó la tarifa especial #" + id;
        };
    }

    private String describirFavoritoCliente(TipoOperacion accion, JsonNode despues, JsonNode antes, Integer id, LocalDateTime fecha) {
        return switch (accion) {
            case POST   -> "Se agregó un favorito del cliente";
            case PUT    -> "Se actualizó el favorito #" + id;
            case DELETE -> "Se eliminó el favorito #" + id;
        };
    }

    private String describirCodigoCliente(TipoOperacion accion, JsonNode despues, JsonNode antes, Integer id, LocalDateTime fecha) {
        return switch (accion) {
            case POST   -> "Se generó un código de cliente";
            case PUT    -> "Se actualizó el código de cliente #" + id;
            case DELETE -> "Se eliminó el código de cliente #" + id;
        };
    }

    private String describirPagoRepartidor(TipoOperacion accion, JsonNode despues, JsonNode antes, Integer id, LocalDateTime fecha) {
        return switch (accion) {
            case POST -> {
                String monto = textOrElse(despues, "pago", "0");
                yield "Se registró un pago al repartidor de $" + monto;
            }
            case PUT    -> "Se actualizó el pago al repartidor #" + id;
            case DELETE -> "Se eliminó el pago al repartidor #" + id;
        };
    }

    private String describirHorarioAtencion(TipoOperacion accion, JsonNode despues, JsonNode antes, Integer id, LocalDateTime fecha) {
        return switch (accion) {
            case POST -> {
                String dia = textOrElse(despues, "diaSemana", "");
                String tipo = textOrElse(despues, "tipoHorario", "");
                yield "Se creó el horario " + tipo + " del día " + dia;
            }
            case PUT    -> "Se actualizó el horario de atención #" + id;
            case DELETE -> "Se eliminó el horario de atención #" + id;
        };
    }

    private JsonNode parsearJson(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDateTime parseLocalDateTime(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return LocalDateTime.parse(valor);
        } catch (Exception e) {
            return null;
        }
    }

    private String textOrElse(JsonNode node, String campo, String defaultVal) {
        if (node == null || !node.has(campo) || node.get(campo).isNull()) return defaultVal;
        return node.get(campo).asText(defaultVal);
    }
}
