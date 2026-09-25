package com.cocinarubi.domain.service.web;

import com.cocinarubi.Constants;
import com.cocinarubi.DBConstants.Estatus;
import com.cocinarubi.DBConstants.PedidoCreadoDesde;
import com.cocinarubi.DBConstants.TipoHorario;
import com.cocinarubi.DBConstants.TipoPedido;
import com.cocinarubi.dao.ClienteRepository;
import com.cocinarubi.dao.CodigoClienteRepository;
import com.cocinarubi.dao.HorarioAtencionRepository;
import com.cocinarubi.dao.PedidoRepository;
import com.cocinarubi.dao.TarifaEspecialRepository;
import com.cocinarubi.domain.entity.Cliente;
import com.cocinarubi.domain.entity.CodigoCliente;
import com.cocinarubi.domain.entity.HorarioAtencion;
import com.cocinarubi.domain.entity.Pedido;
import com.cocinarubi.domain.entity.Ruta;
import com.cocinarubi.domain.mapper.PedidoMapper;
import com.cocinarubi.domain.service.CatalogoPedidoService;
import com.cocinarubi.domain.service.PedidoService;
import com.cocinarubi.domain.service.RutaService;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.PedidoDomicilioDTO;
import com.cocinarubi.presentation.dto.request.PedidoRequestDTO;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import com.cocinarubi.presentation.dto.response.PedidoResponseDTO;
import com.cocinarubi.presentation.filter.ClienteSessionFilter;
import com.cocinarubi.presentation.strategy.strategyImplementation.PedidoConfirmationImp;
import com.cocinarubi.presentation.strategy.strategyImplementation.PedidoValidationImp;
import com.cocinarubi.util.HashUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Variante de {@link PedidoService} para pedidos originados desde la web del cliente.
 * Agrega validaciones de sesión, ventana de edición y horario de modalidad (domicilio / pick-up).
 * Capa: Service — lógica de negocio específica del canal WEB.
 */
@Service
public class PedidoWebService extends PedidoService {

    private static final Map<DayOfWeek, String> DIA_SEMANA = Map.of(
            DayOfWeek.MONDAY,    "L",
            DayOfWeek.TUESDAY,   "M",
            DayOfWeek.WEDNESDAY, "X",
            DayOfWeek.THURSDAY,  "J",
            DayOfWeek.FRIDAY,    "V",
            DayOfWeek.SATURDAY,  "S",
            DayOfWeek.SUNDAY,    "D"
    );

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final HorarioAtencionRepository horarioRepo;
    private final RutaService rutaService;
    private final HttpServletRequest httpRequest;
    private final CodigoClienteRepository codigoClienteRepository;

    public PedidoWebService(PedidoRepository pedidoRepository,
                            PedidoValidationImp pedidoValidation,
                            PedidoConfirmationImp pedidoConfirmation,
                            PedidoMapper pedidoMapper,
                            CatalogoPedidoService catalogoPedido,
                            ApplicationEventPublisher eventPublisher,
                            TarifaEspecialRepository tarifaEspecialRepository,
                            ClienteRepository clienteRepository,
                            HorarioAtencionRepository horarioRepo,
                            RutaService rutaService,
                            HttpServletRequest httpRequest,
                            CodigoClienteRepository codigoClienteRepository) {
        super(pedidoRepository, pedidoValidation, pedidoConfirmation, pedidoMapper,
                catalogoPedido, eventPublisher, tarifaEspecialRepository);
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.horarioRepo = horarioRepo;
        this.rutaService = rutaService;
        this.httpRequest = httpRequest;
        this.codigoClienteRepository = codigoClienteRepository;
    }

    @Override
    @Transactional
    public PedidoResponseDTO save(PedidoRequestDTO dto) {
        String uuidAutenticado = verificarTokenWeb(dto);
        // Evita que el cliente cree pedidos a nombre de otro cliente enviando uuidCliente en el body
        dto.setUuidCliente(uuidAutenticado);
        sincronizarNombreCliente(dto);
        verificarHorarioModalidad(dto);
        verificarUbicacionDomicilio(dto);
        aplicarTarifaCodigoCliente(dto);
        return super.save(dto);
    }

    @Override
    @Transactional
    public PedidoResponseDTO update(int id, PedidoRequestDTO dto) {
        String uuidAutenticado = verificarTokenWeb(dto);
        verificarOwnership(id, uuidAutenticado);
        // Evita que el cliente cambie el uuidCliente del pedido desde el body
        dto.setUuidCliente(uuidAutenticado);
        sincronizarNombreCliente(dto);
        verificarVentanaEdicion(id);
        verificarHorarioModalidad(dto);
        verificarUbicacionDomicilio(dto);
        aplicarTarifaCodigoCliente(dto);
        return super.update(id, dto);
    }

    private void sincronizarNombreCliente(PedidoRequestDTO dto) {
        String nombre = dto.getNombreCliente();
        if (nombre == null || nombre.isBlank()) return;
        clienteRepository.findByUuidCliente(dto.getUuidCliente())
                .ifPresent(c -> c.setNombre(nombre));
    }

    /**
     * Valida el horario de atención según los tipos de ítem que contiene el pedido.
     * Si el pedido lleva desayunos verifica el turno DESAYUNO; si lleva comidas verifica
     * COMIDAS; si lleva ambos verifica los dos turnos. Ítems neutros (básicos, productos
     * cocina, paquetes) sin desayunos ni comidas se validan contra COMIDAS por defecto.
     */
    private void verificarHorarioModalidad(PedidoRequestDTO dto) {
        ZonedDateTime ahora = ZonedDateTime.now(Constants.ZONA_MERIDA);
        String diaSemana = DIA_SEMANA.get(ahora.getDayOfWeek());
        LocalTime horaActual = ahora.toLocalTime();

        boolean tieneDesayunos = !dto.getDesayunos().isEmpty();
        boolean tieneComidas = !dto.getComidas().isEmpty();

        if (tieneDesayunos) {
            verificarTurno(TipoHorario.DESAYUNO, diaSemana, horaActual, dto.getTipoPedido());
        }
        if (tieneComidas || !tieneDesayunos) {
            verificarTurno(TipoHorario.COMIDAS, diaSemana, horaActual, dto.getTipoPedido());
        }
    }

    /**
     * Consulta el registro {@code HorarioAtencion} para el tipo y día dados y lanza
     * {@link BusinessException} si el turno no existe, está cerrado o la hora actual
     * cae fuera de la ventana configurada.
     */
    private void verificarTurno(TipoHorario tipoHorario, String diaSemana,
                                 LocalTime horaActual, TipoPedido tipoPedido) {
        // HorarioAtencionRepository: busca el turno del día para el tipo de horario indicado
        HorarioAtencion horario = horarioRepo
                .findByTipoHorarioAndDiaSemana(tipoHorario, diaSemana)
                .orElseThrow(() -> new BusinessException(
                        "No hay servicio de " + nombreLegible(tipoPedido) + " disponible hoy",
                        HttpStatus.UNPROCESSABLE_ENTITY));

        if (!horario.isAtendiendo()) {
            throw new BusinessException(
                    "El servicio no está disponible en este momento",
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }

        LocalTime inicio = horario.getHoraInicioAtencionComidas();
        LocalTime cierre = horario.getHoraCierreAtencionComidas();

        if (horaActual.isBefore(inicio) || !horaActual.isBefore(cierre)) {
            throw new BusinessException(
                    "La modalidad para " + nombreLegible(tipoPedido)
                            + " no está disponible fuera del horario de atención ("
                            + inicio + " – " + cierre + ")",
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    /**
     * Si el pedido es DOMICILIO y el DTO incluye coordenadas, verifica que el punto
     * esté dentro del polígono de la ruta declarada. Evita que el cliente envíe una
     * idRuta que no corresponde a su ubicación real.
     */
    private void verificarUbicacionDomicilio(PedidoRequestDTO dto) {
        if (!TipoPedido.DOMICILIO.equals(dto.getTipoPedido())) return;
        PedidoDomicilioDTO dom = dto.getDomicilio();
        if (dom == null || dom.getLatitud() == null || dom.getLongitud() == null) return;
        // Clientes con código especial no están sujetos a validación geográfica por polígono
        if (dom.getCodigo() != null && !dom.getCodigo().isBlank()) return;

        Ruta ruta = rutaService.findEntityById(dom.getIdRuta());
        // JTS: X = longitud, Y = latitud
        Point punto = new GeometryFactory().createPoint(
                new Coordinate(dom.getLongitud().doubleValue(), dom.getLatitud().doubleValue()));

        if (!ruta.getBoundary().covers(punto)) {
            throw new BusinessException(
                    "La ubicación indicada no se encuentra dentro de la zona de reparto seleccionada",
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    /**
     * Si el pedido es DOMICILIO y el DTO incluye un código de cliente,
     * verifica que exista y esté DISPONIBLE, luego sustituye la tarifa del domicilio
     * por la tarifaEspecial registrada en el código.
     */
    private void aplicarTarifaCodigoCliente(PedidoRequestDTO dto) {
        if (!TipoPedido.DOMICILIO.equals(dto.getTipoPedido())) return;
        PedidoDomicilioDTO dom = dto.getDomicilio();
        if (dom == null || dom.getCodigo() == null || dom.getCodigo().isBlank()) return;

        CodigoCliente codigo = codigoClienteRepository.findByCodigoCliente(dom.getCodigo())
                .orElseThrow(() -> new BusinessException(
                        "El código de cliente '" + dom.getCodigo() + "' no existe",
                        HttpStatus.NOT_FOUND));

        if (!Estatus.DISPONIBLE.equals(codigo.getEstatus())) {
            throw new BusinessException(
                    "El código de cliente '" + dom.getCodigo() + "' no está disponible",
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // CodigoCliente: sustituye la tarifa enviada por el frontend con la tarifa especial del código
        dom.setTarifa(codigo.getTarifaEspecial());
    }

    private void verificarVentanaEdicion(int id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Pedido no encontrado", HttpStatus.NOT_FOUND));

        LocalDateTime ahora = LocalDateTime.now(Constants.ZONA_MERIDA);
        LocalDateTime creacion = pedido.getFechaExpedicionPedido();
        long minutosTranscurridos = java.time.Duration.between(creacion, ahora).toMinutes();

        if (minutosTranscurridos >= 5) {
            throw new BusinessException(
                    "No es posible modificar el pedido: han pasado " + minutosTranscurridos
                            + " minutos desde su creación (límite: 5 minutos)",
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private String nombreLegible(TipoPedido tipo) {
        return switch (tipo) {
            case PICK_UP -> "recoger";
            default -> tipo.name().toLowerCase();
        };
    }

    /** Valida que el token sea de origen WEB y no esté expirado. Devuelve el UUID del cliente autenticado. */
    private String verificarTokenWeb(PedidoRequestDTO dto) {
        if (!PedidoCreadoDesde.WEB.equals(dto.getPedidoCreadoDesde())) {
            throw new BusinessException(
                    "Este endpoint solo acepta pedidos de origen WEB", HttpStatus.BAD_REQUEST);
        }

        // ClienteSessionFilter ya autentico y expuso el Cliente en el request attribute.
        // Si esta presente lo usamos directamente y evitamos un lookup adicional.
        Object attr = httpRequest.getAttribute(ClienteSessionFilter.CLIENTE_ATTR);
        if (attr instanceof Cliente c && c.getTokenExpiracion() != null
                && !c.getTokenExpiracion().isBefore(LocalDateTime.now())) {
            return c.getUuidCliente();
        }

        // Fallback: si el filtro no corrio (paths cambian, tests), autenticamos aqui.
        String tokenPlano = extraerTokenPlano();
        if (tokenPlano == null) {
            throw new BusinessException("Token de sesión requerido", HttpStatus.UNAUTHORIZED);
        }
        // HashUtils: el token en el request es plano; en BD solo vive su hash SHA-256
        Optional<Cliente> clienteOpt = clienteRepository.findBySessionTokenHash(HashUtils.sha256Hex(tokenPlano));

        if (clienteOpt.isEmpty()
                || clienteOpt.get().getTokenExpiracion() == null
                || clienteOpt.get().getTokenExpiracion().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Token de sesión inválido o expirado", HttpStatus.UNAUTHORIZED);
        }

        return clienteOpt.get().getUuidCliente();
    }

    /** Extrae el token plano desde la cookie {@code session_token} o el header {@code Authorization}. */
    private String extraerTokenPlano() {
        if (httpRequest.getCookies() != null) {
            for (Cookie c : httpRequest.getCookies()) {
                if (ClienteSessionFilter.SESSION_COOKIE.equals(c.getName())
                        && c.getValue() != null && !c.getValue().isBlank()) {
                    return c.getValue();
                }
            }
        }
        String header = httpRequest.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String bearer = header.substring(7).trim();
            if (!bearer.isEmpty()) return bearer;
        }
        return null;
    }

    /** Verifica que el pedido pertenezca al cliente autenticado antes de permitir la modificación. */
    private void verificarOwnership(int id, String uuidAutenticado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Pedido no encontrado", HttpStatus.NOT_FOUND));
        if (!uuidAutenticado.equals(pedido.getUuidCliente())) {
            throw new BusinessException("No tienes permiso para modificar este pedido", HttpStatus.FORBIDDEN);
        }
    }
}
