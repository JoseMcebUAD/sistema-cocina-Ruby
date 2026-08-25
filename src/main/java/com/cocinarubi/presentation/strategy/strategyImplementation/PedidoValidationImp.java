package com.cocinarubi.presentation.strategy.strategyImplementation;

import com.cocinarubi.DBConstants.Estatus;
import com.cocinarubi.DBConstants.PedidoCreadoDesde;
import com.cocinarubi.DBConstants.TipoPedido;
import com.cocinarubi.dao.BasicoRepository;
import com.cocinarubi.dao.ComidaRepository;
import com.cocinarubi.dao.ComplementoRepository;
import com.cocinarubi.dao.DesayunoRepository;
import com.cocinarubi.dao.PaqueteRepository;
import com.cocinarubi.dao.ProductoCocinaRepository;
import com.cocinarubi.dao.RegistroClienteRepository;
import com.cocinarubi.dao.RutaRepository;
import com.cocinarubi.domain.entity.Basico;
import com.cocinarubi.domain.entity.Comida;
import com.cocinarubi.domain.entity.Complemento;
import com.cocinarubi.domain.entity.Desayuno;
import com.cocinarubi.domain.entity.Paquete;
import com.cocinarubi.domain.entity.ProductoCocina;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.exception.ErrorCode;
import com.cocinarubi.presentation.dto.request.BasicoPedidoDTO;
import com.cocinarubi.presentation.dto.request.ComidaPedidoDTO;
import com.cocinarubi.presentation.dto.request.ComplementoPedidoDTO;
import com.cocinarubi.presentation.dto.request.DesayunoPedidoDTO;
import com.cocinarubi.presentation.dto.request.PaquetePedidoDTO;
import com.cocinarubi.presentation.dto.request.PedidoRequestDTO;
import com.cocinarubi.presentation.dto.request.ProductoCocinaPedidoDTO;
import com.cocinarubi.presentation.strategy.ValidationStrategy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Valida la integridad estructural y referencial de un {@link com.cocinarubi.presentation.dto.request.PedidoRequestDTO}
 * antes de que el servicio construya la entidad de dominio.
 *
 * <p>Contratos validados:
 * <ul>
 *   <li><b>RF-025</b> – el pedido debe contener al menos una línea de producto.</li>
 *   <li>Coherencia entre {@code pedidoCreadoDesde}, {@code tipoPedido} y los campos de entrega:
 *       COCINA+DOMICILIO exige {@code idRegistroCliente}; COCINA+PICK_UP/MOSTRADOR exige {@code nombreCliente};
 *       WEB+DOMICILIO exige el objeto {@code domicilio}.</li>
 *   <li>Existencia en base de datos de cada referencia de catálogo (Comida, Desayuno, Básico,
 *       ProductoCocina, Complemento, Ruta, RegistroCliente).</li>
 * </ul>
 *
 * <p>Esta validación se ejecuta siempre; la confirmación de negocio ({@link PedidoConfirmationImp})
 * es la que puede saltarse para canales internos.
 */
@Component
public class PedidoValidationImp implements ValidationStrategy<PedidoRequestDTO> {

    private final ComidaRepository comidaRepository;
    private final DesayunoRepository desayunoRepository;
    private final BasicoRepository basicoRepository;
    private final ProductoCocinaRepository productoCocinaRepository;
    private final ComplementoRepository complementoRepository;
    private final RutaRepository rutaRepository;
    private final RegistroClienteRepository registroClienteRepository;
    private final PaqueteRepository paqueteRepository;

    // Virtual threads (Java 21): un hilo virtual por tarea, sin pool fijo — ideal para I/O bloqueante.
    private static final Executor EXECUTOR_VALIDACION = Executors.newVirtualThreadPerTaskExecutor();

    public PedidoValidationImp(ComidaRepository comidaRepository,
                               DesayunoRepository desayunoRepository,
                               BasicoRepository basicoRepository,
                               ProductoCocinaRepository productoCocinaRepository,
                               ComplementoRepository complementoRepository,
                               RutaRepository rutaRepository,
                               RegistroClienteRepository registroClienteRepository,
                               PaqueteRepository paqueteRepository) {
        this.comidaRepository = comidaRepository;
        this.desayunoRepository = desayunoRepository;
        this.basicoRepository = basicoRepository;
        this.productoCocinaRepository = productoCocinaRepository;
        this.complementoRepository = complementoRepository;
        this.rutaRepository = rutaRepository;
        this.registroClienteRepository = registroClienteRepository;
        this.paqueteRepository = paqueteRepository;
    }

    @Override
    public void validarPost(PedidoRequestDTO dto) {
        List<CompletableFuture<Void>> tareas = List.of(
                CompletableFuture.runAsync(() -> validarAlMenosUnProducto(dto),       EXECUTOR_VALIDACION),
                CompletableFuture.runAsync(() -> validarConsistenciaDomicilio(dto),   EXECUTOR_VALIDACION),
                CompletableFuture.runAsync(() -> validarLineasComida(dto),            EXECUTOR_VALIDACION),
                CompletableFuture.runAsync(() -> validarLineasDesayuno(dto),          EXECUTOR_VALIDACION),
                CompletableFuture.runAsync(() -> validarLineasBasico(dto),            EXECUTOR_VALIDACION),
                CompletableFuture.runAsync(() -> validarLineasProductoCocina(dto),    EXECUTOR_VALIDACION),
                CompletableFuture.runAsync(() -> validarPaquetesDisponibles(dto),     EXECUTOR_VALIDACION),
                CompletableFuture.runAsync(() -> validarDomicilioWeb(dto),            EXECUTOR_VALIDACION),
                CompletableFuture.runAsync(() -> validarRutaDomicilioCocina(dto),     EXECUTOR_VALIDACION),
                CompletableFuture.runAsync(() -> validarRegistroCliente(dto),         EXECUTOR_VALIDACION),
                CompletableFuture.runAsync(() -> validarMetodosPagoNoDuplicados(dto), EXECUTOR_VALIDACION),
                CompletableFuture.runAsync(() -> validarPagoClienteNoExcedaTotal(dto),EXECUTOR_VALIDACION),
                CompletableFuture.runAsync(() -> validarSubtotalMinimoWeb(dto),       EXECUTOR_VALIDACION)
        );
        try {
            CompletableFuture.allOf(tareas.toArray(new CompletableFuture[0])).join();
        } catch (CompletionException ex) {
            Throwable causa = ex.getCause();
            if (causa instanceof BusinessException be) {
                throw be;
            }
            throw new BusinessException(
                    "El pedido es incorrecto, por favor verifique su pedido",
                    HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
        }
    }

    /** RF-025: el pedido debe tener al menos una línea de algún tipo. */
    private void validarAlMenosUnProducto(PedidoRequestDTO dto) {
        boolean tieneProductos = !dto.getComidas().isEmpty()
                || !dto.getDesayunos().isEmpty()
                || !dto.getBasicos().isEmpty()
                || !dto.getProductosCocina().isEmpty()
                || !dto.getPaquetes().isEmpty();
        if (!tieneProductos) {
            throw new BusinessException(
                    "El pedido debe incluir al menos un producto",
                    HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
        }
    }

    /**
     * Verifica coherencia entre {@code tipoPedido}, {@code pedidoCreadoDesde} y los campos de entrega.
     *
     * <p>Para COCINA: DOMICILIO exige {@code idRegistroCliente}; PICK_UP/MOSTRADOR exigen {@code nombreCliente}.
     * Para WEB: DOMICILIO exige el objeto {@code domicilio}.
     */
    private void validarConsistenciaDomicilio(PedidoRequestDTO dto) {
        boolean esCocina = dto.getPedidoCreadoDesde() == PedidoCreadoDesde.COCINA;
        boolean esDomicilio = dto.getTipoPedido() == TipoPedido.DOMICILIO;

        if (esCocina) {
            if (esDomicilio) {
                if (dto.getPedidoDomicilioCocina() == null) {
                    throw new BusinessException(
                            "Un pedido COCINA a domicilio requiere los datos de domicilio'",
                            HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
                }

            } 
            else {
                //verfi
                if (dto.getNombreCliente() == null || dto.getNombreCliente().isBlank()) {
                    throw new BusinessException(
                            "Un pedido COCINA de tipo PICK_UP o MOSTRADOR requiere el nombre del cliente ('nombreCliente')",
                            HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
                }
            }
        } else {
            if (dto.getPedidoDomicilioCocina() != null) {
                throw new BusinessException(
                        "Un pedido WEB no puede incluir el campo 'pedidoDomicilioCocina'",
                        HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
            }
            if (esDomicilio) {
                if (dto.getDomicilio() == null) {
                    throw new BusinessException(
                            "Un pedido WEB a domicilio requiere los datos de entrega de domicilio",
                            HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
                }

            } else if(dto.getTipoPedido() == TipoPedido.MOSTRADOR){
                throw new BusinessException(
                            "El pedido de carrito no puede ser por mostrador, por favor modifica el tipo de envío",
                            HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);

            }else {
                if (dto.getDomicilio() != null) {
                    throw new BusinessException(
                            "Solo los pedidos WEB a domicilio pueden incluir el campo 'domicilio'",
                            HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
                }
            }
        }
    }

    private void validarLineasComida(PedidoRequestDTO dto) {
        for (ComidaPedidoDTO linea : dto.getComidas()) {
            Comida comida = comidaRepository.findById(linea.getIdComida())
                    .orElseThrow(() -> new BusinessException(
                            "La comida " + linea.getIdComida() + " no existe",
                            HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION));
            if (comida.getEstatus() != Estatus.DISPONIBLE) {
                throw new BusinessException(
                        "La comida " + linea.getIdComida() + " no está disponible",
                        HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
            }
            for (ComplementoPedidoDTO comp : linea.getComplementos()) {
                Complemento complemento = complementoRepository.findById(comp.getIdComplemento())
                        .orElseThrow(() -> new BusinessException(
                                "El complemento " + comp.getIdComplemento() + " no existe",
                                HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION));
                if (complemento.getEstatus() != Estatus.DISPONIBLE) {
                    throw new BusinessException(
                            "El complemento " + comp.getIdComplemento() + " no está disponible",
                            HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
                }
            }
        }
    }

    private void validarLineasDesayuno(PedidoRequestDTO dto) {
        for (DesayunoPedidoDTO linea : dto.getDesayunos()) {
            Desayuno desayuno = desayunoRepository.findById(linea.getIdDesayuno())
                    .orElseThrow(() -> new BusinessException(
                            "El desayuno " + linea.getIdDesayuno() + " no existe",
                            HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION));
            if (desayuno.getEstatus() != Estatus.DISPONIBLE) {
                throw new BusinessException(
                        "El desayuno " + linea.getIdDesayuno() + " no está disponible",
                        HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
            }
        }
    }

    private void validarLineasBasico(PedidoRequestDTO dto) {
        for (BasicoPedidoDTO linea : dto.getBasicos()) {
            Basico basico = basicoRepository.findById(linea.getIdBasico())
                    .orElseThrow(() -> new BusinessException(
                            "El básico " + linea.getIdBasico() + " no existe",
                            HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION));
            if (basico.getEstatus() != Estatus.DISPONIBLE) {
                throw new BusinessException(
                        "El básico " + linea.getIdBasico() + " no está disponible",
                        HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
            }
        }
    }

    private void validarLineasProductoCocina(PedidoRequestDTO dto) {
        for (ProductoCocinaPedidoDTO linea : dto.getProductosCocina()) {
            ProductoCocina producto = productoCocinaRepository.findById(linea.getIdProductoCocina())
                    .orElseThrow(() -> new BusinessException(
                            "El producto de cocina " + linea.getIdProductoCocina() + " no existe",
                            HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION));
            if (producto.getEstatus() != Estatus.DISPONIBLE) {
                throw new BusinessException(
                        "El producto de cocina " + linea.getIdProductoCocina() + " no está disponible",
                        HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
            }
        }
    }

    private void validarDomicilioWeb(PedidoRequestDTO dto) {
        if (dto.getDomicilio() == null) return;
        if (!rutaRepository.existsById(dto.getDomicilio().getIdRuta())) {
            throw new BusinessException(
                    "La ruta " + dto.getDomicilio().getIdRuta() + " no existe",
                    HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
        }
    }

    private void validarRutaDomicilioCocina(PedidoRequestDTO dto) {
        if (dto.getPedidoDomicilioCocina() == null) return;
        if (!rutaRepository.existsById(dto.getPedidoDomicilioCocina().getIdRuta())) {
            throw new BusinessException(
                    "La ruta " + dto.getPedidoDomicilioCocina().getIdRuta() + " no existe",
                    HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
        }
    }

    private void validarRegistroCliente(PedidoRequestDTO dto) {
        if (dto.getPedidoDomicilioCocina() == null) return;
        Integer idRegistroCliente = dto.getPedidoDomicilioCocina().getIdRegistroCliente();
        registroClienteRepository
                .findById(idRegistroCliente)
                .orElseThrow(() -> new BusinessException(
                        "El cliente especificado no existe, por favor verificar que sea un cliente dentro del sistema",
                        HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION));
    }

    private void validarMetodosPagoNoDuplicados(PedidoRequestDTO dto) {
        if (dto.getMetodoPagoSecundario() == null) return;
        if (dto.getMetodoPagoSecundario() == dto.getMetodoPagoPrincipal()) {
            throw new BusinessException(
                    "El método de pago secundario no puede ser igual al método de pago principal",
                    HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
        }
    }

    private void validarPaquetesDisponibles(PedidoRequestDTO dto) {
        for (PaquetePedidoDTO linea : dto.getPaquetes()) {
            Paquete paquete = paqueteRepository.findById(linea.getIdPaquete())
                    .orElseThrow(() -> new BusinessException(
                            "El paquete " + linea.getIdPaquete() + " no existe",
                            HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION));
            if (paquete.getEstatus() != Estatus.DISPONIBLE) {
                throw new BusinessException(
                        "El paquete " + linea.getIdPaquete() + " no está disponible",
                        HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
            }
        }
    }

    /**
     * Verifica que el pago del cliente no supere el precio final de la orden
     * (suma de líneas de comida + complementos, desayunos, básicos, productos de cocina
     * y la tarifa de domicilio cocina si aplica).
     */
    private void validarPagoClienteNoExcedaTotal(PedidoRequestDTO dto) {
        if (dto.getPagoCliente() == null) return;

        BigDecimal precioFinalOrden = calcularPrecioFinalOrden(dto);

        if (dto.getPagoCliente().compareTo(precioFinalOrden) > 0) {
            throw new BusinessException(
                    "El pago del cliente (" + dto.getPagoCliente()
                            + ") no puede ser mayor al precio final de la orden ("
                            + precioFinalOrden + ")",
                    HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
        }
    }

    private BigDecimal calcularPrecioFinalOrden(PedidoRequestDTO dto) {
        BigDecimal total = BigDecimal.ZERO;

        for (ComidaPedidoDTO comida : dto.getComidas()) {
            if (comida.getPrecioUnitario() != null) {
                total = total.add(comida.getPrecioUnitario());
            }
            for (ComplementoPedidoDTO comp : comida.getComplementos()) {
                if (comp.getPrecioUnitario() != null) {
                    total = total.add(comp.getPrecioUnitario());
                }
            }
        }
        for (DesayunoPedidoDTO desayuno : dto.getDesayunos()) {
            if (desayuno.getPrecio() != null) {
                total = total.add(desayuno.getPrecio());
            }
        }
        for (BasicoPedidoDTO basico : dto.getBasicos()) {
            if (basico.getPrecioUnitario() != null) {
                total = total.add(basico.getPrecioUnitario());
            }
        }
        for (ProductoCocinaPedidoDTO producto : dto.getProductosCocina()) {
            if (producto.getPrecioUnitario() != null && producto.getCantidad() != null) {
                total = total.add(producto.getPrecioUnitario()
                        .multiply(BigDecimal.valueOf(producto.getCantidad())));
            }
        }
        if (dto.getPedidoDomicilioCocina() != null
                && dto.getPedidoDomicilioCocina().getTarifa() != null) {
            total = total.add(dto.getPedidoDomicilioCocina().getTarifa());
        }
        return total;
    }

    /** Solo WEB: el subtotal de productos debe ser mayor a $80.00. */
    private void validarSubtotalMinimoWeb(PedidoRequestDTO dto) {
        if (dto.getPedidoCreadoDesde() == PedidoCreadoDesde.COCINA) return;

        BigDecimal subtotal = BigDecimal.ZERO;

        for (ComidaPedidoDTO comida : dto.getComidas()) {
            if (comida.getPrecioUnitario() != null)
                subtotal = subtotal.add(comida.getPrecioUnitario());
            for (ComplementoPedidoDTO comp : comida.getComplementos()) {
                if (comp.getPrecioUnitario() != null)
                    subtotal = subtotal.add(comp.getPrecioUnitario());
            }
        }
        for (DesayunoPedidoDTO desayuno : dto.getDesayunos()) {
            if (desayuno.getPrecio() != null)
                subtotal = subtotal.add(desayuno.getPrecio());
        }
        for (BasicoPedidoDTO basico : dto.getBasicos()) {
            if (basico.getPrecioUnitario() != null)
                subtotal = subtotal.add(basico.getPrecioUnitario());
        }
        for (ProductoCocinaPedidoDTO producto : dto.getProductosCocina()) {
            if (producto.getPrecioUnitario() != null && producto.getCantidad() != null)
                subtotal = subtotal.add(producto.getPrecioUnitario()
                        .multiply(BigDecimal.valueOf(producto.getCantidad())));
        }
        for (PaquetePedidoDTO paquete : dto.getPaquetes()) {
            if (paquete.getPrecioUnitario() != null && paquete.getCantidad() != null)
                subtotal = subtotal.add(paquete.getPrecioUnitario()
                        .multiply(BigDecimal.valueOf(paquete.getCantidad())));
        }

        if (subtotal.compareTo(new BigDecimal("80")) <= 0) {
            throw new BusinessException(
                    "El subtotal de los productos ($" + subtotal + ") debe ser mayor a $80.00 para realizar un pedido web",
                    HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
        }
    }
}
