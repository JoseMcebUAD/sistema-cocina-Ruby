package com.cocinarubi.domain.service.web;

import com.cocinarubi.Constants;
import com.cocinarubi.DBConstants.PedidoCreadoDesde;
import com.cocinarubi.DBConstants.TipoHorario;
import com.cocinarubi.DBConstants.TipoPedido;
import com.cocinarubi.dao.ClienteRepository;
import com.cocinarubi.dao.HorarioAtencionRepository;
import com.cocinarubi.dao.PedidoRepository;
import com.cocinarubi.dao.TarifaEspecialRepository;
import com.cocinarubi.domain.entity.Cliente;
import com.cocinarubi.domain.entity.HorarioAtencion;
import com.cocinarubi.domain.entity.Pedido;
import com.cocinarubi.domain.mapper.PedidoMapper;
import com.cocinarubi.domain.service.CatalogoPedidoService;
import com.cocinarubi.domain.service.PedidoService;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.PedidoRequestDTO;
import com.cocinarubi.presentation.dto.response.PedidoResponseDTO;
import com.cocinarubi.presentation.strategy.strategyImplementation.PedidoConfirmationImp;
import com.cocinarubi.presentation.strategy.strategyImplementation.PedidoValidationImp;
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
    private final HttpServletRequest httpRequest;

    public PedidoWebService(PedidoRepository pedidoRepository,
                            PedidoValidationImp pedidoValidation,
                            PedidoConfirmationImp pedidoConfirmation,
                            PedidoMapper pedidoMapper,
                            CatalogoPedidoService catalogoPedido,
                            ApplicationEventPublisher eventPublisher,
                            TarifaEspecialRepository tarifaEspecialRepository,
                            ClienteRepository clienteRepository,
                            HorarioAtencionRepository horarioRepo,
                            HttpServletRequest httpRequest) {
        super(pedidoRepository, pedidoValidation, pedidoConfirmation, pedidoMapper,
                catalogoPedido, eventPublisher, tarifaEspecialRepository);
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.horarioRepo = horarioRepo;
        this.httpRequest = httpRequest;
    }

    @Override
    @Transactional
    public PedidoResponseDTO save(PedidoRequestDTO dto) {
        verificarTokenWeb(dto);
        verificarHorarioModalidad(dto.getTipoPedido());
        return super.save(dto);
    }

    @Override
    @Transactional
    public PedidoResponseDTO update(int id, PedidoRequestDTO dto) {
        verificarTokenWeb(dto);
        verificarVentanaEdicion(id);
        verificarHorarioModalidad(dto.getTipoPedido());
        return super.update(id, dto);
    }

    /**
     * Valida que la modalidad solicitada (domicilio o pick-up) esté dentro del horario COMIDAS
     * configurado en {@code HorarioAtencion} para el día actual en la zona horaria de Mérida.
     * Lanza {@link BusinessException} si el servicio está cerrado, sin horario registrado o
     * la hora actual cae fuera de la ventana configurada.
     */
    private void verificarHorarioModalidad(TipoPedido tipoPedido) {
        ZonedDateTime ahora = ZonedDateTime.now(Constants.ZONA_MERIDA);
        String diaSemana = DIA_SEMANA.get(ahora.getDayOfWeek());
        LocalTime horaActual = ahora.toLocalTime();

        HorarioAtencion horario = horarioRepo
                .findByTipoHorarioAndDiaSemana(TipoHorario.COMIDAS, diaSemana)
                .orElseThrow(() -> new BusinessException(
                        "No hay servicio de " + tipoPedido.name().toLowerCase() + " disponible hoy",
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
                    "La modalidad " + tipoPedido.name().toLowerCase()
                            + " no está disponible fuera del horario de atención ("
                            + inicio + " – " + cierre + ")",
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private void verificarVentanaEdicion(int id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Pedido no encontrado", HttpStatus.NOT_FOUND));
        if (pedido.getFechaExpedicionPedido().isBefore(LocalDateTime.now().minusMinutes(5))) {
            throw new BusinessException(
                    "No es posible modificar el pedido despues de 5 minutos de su creacion",
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private void verificarTokenWeb(PedidoRequestDTO dto) {
        if (!PedidoCreadoDesde.WEB.equals(dto.getPedidoCreadoDesde())) {
            throw new BusinessException(
                    "Este endpoint solo acepta pedidos de origen WEB", HttpStatus.BAD_REQUEST);
        }

        String header = httpRequest.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new BusinessException("Token de sesión requerido", HttpStatus.UNAUTHORIZED);
        }

        String token = header.substring(7);
        Optional<Cliente> clienteOpt = clienteRepository.findBySessionToken(token);

        if (clienteOpt.isEmpty()
                || clienteOpt.get().getTokenExpiracion() == null
                || clienteOpt.get().getTokenExpiracion().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Token de sesión inválido o expirado", HttpStatus.UNAUTHORIZED);
        }
    }
}
