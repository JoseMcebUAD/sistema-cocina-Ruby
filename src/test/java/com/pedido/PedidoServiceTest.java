package com.pedido;

import com.cocinarubi.DBConstants.MetodoPago;
import com.cocinarubi.DBConstants.PedidoCreadoDesde;
import com.cocinarubi.DBConstants.TipoPedido;
import com.cocinarubi.dao.PedidoRepository;
import com.cocinarubi.domain.entity.Basico;
import com.cocinarubi.domain.entity.BasicoComplemento;
import com.cocinarubi.domain.entity.BasicoPedido;
import com.cocinarubi.domain.entity.Comida;
import com.cocinarubi.domain.entity.Complemento;
import com.cocinarubi.domain.entity.Pedido;
import com.cocinarubi.domain.mapper.PedidoMapper;
import com.cocinarubi.domain.service.CatalogoPedidoService;
import com.cocinarubi.domain.service.PedidoService;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.PedidoDomicilioDTO;
import com.cocinarubi.presentation.dto.request.PedidoRequestDTO;
import com.cocinarubi.presentation.dto.response.BasicoPedidoResponseDTO;
import com.cocinarubi.presentation.dto.response.PedidoResponseDTO;
import com.cocinarubi.presentation.strategy.strategyImplementation.PedidoConfirmationImp;
import com.cocinarubi.presentation.strategy.strategyImplementation.PedidoValidationImp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PedidoServiceTest {

    @Mock private PedidoRepository pedidoRepository;
    @Mock private PedidoValidationImp pedidoValidation;
    @Mock private PedidoConfirmationImp pedidoConfirmation;
    @Mock private CatalogoPedidoService catalogoPedido;
    @Spy  private PedidoMapper pedidoMapper = new PedidoMapper();

    @InjectMocks
    private PedidoService pedidoService;

    public Pedido PEDIDO_PREPARED = Pedido.builder()
            .idPedido(10)
            .metodoPagoPrincipal(MetodoPago.EFECTIVO)
            .tipoPedido(TipoPedido.MOSTRADOR)
            .pedidoCreadoDesde(PedidoCreadoDesde.COCINA)
            .precioFinalOrden(BigDecimal.ZERO)
            .impreso(false)
            .build();

    public PedidoRequestDTO crearDtoMostrador() {
        PedidoRequestDTO dto = new PedidoRequestDTO();
        dto.setMetodoPagoPrincipal(MetodoPago.EFECTIVO);
        dto.setTipoPedido(TipoPedido.MOSTRADOR);
        dto.setPedidoCreadoDesde(PedidoCreadoDesde.COCINA);
        dto.setNombreCliente("Cliente Mostrador");
        dto.setComidas(List.of());
        dto.setDesayunos(List.of());
        dto.setBasicos(List.of());
        dto.setProductosCocina(List.of());
        dto.setSaltarConfirmacion(true);
        return dto;
    }

    public PedidoRequestDTO crearDtoWebDomicilio() {
        PedidoDomicilioDTO domicilioDto = new PedidoDomicilioDTO();
        domicilioDto.setIdRuta(1);
        domicilioDto.setDireccion("Calle Falsa 123");

        PedidoRequestDTO dto = new PedidoRequestDTO();
        dto.setMetodoPagoPrincipal(MetodoPago.TARJETA);
        dto.setTipoPedido(TipoPedido.DOMICILIO);
        dto.setPedidoCreadoDesde(PedidoCreadoDesde.WEB);
        dto.setComidas(List.of());
        dto.setDesayunos(List.of());
        dto.setBasicos(List.of());
        dto.setProductosCocina(List.of());
        dto.setDomicilio(domicilioDto);
        dto.setSaltarConfirmacion(true);
        return dto;
    }

    public PedidoRequestDTO crearDtoCocinaPickUp(String nombreCliente) {
        PedidoRequestDTO dto = new PedidoRequestDTO();
        dto.setMetodoPagoPrincipal(MetodoPago.EFECTIVO);
        dto.setTipoPedido(TipoPedido.PICK_UP);
        dto.setPedidoCreadoDesde(PedidoCreadoDesde.COCINA);
        dto.setNombreCliente(nombreCliente);
        dto.setComidas(List.of());
        dto.setDesayunos(List.of());
        dto.setBasicos(List.of());
        dto.setProductosCocina(List.of());
        dto.setSaltarConfirmacion(true);
        return dto;
    }

    public PedidoRequestDTO crearDtoCocinaDomicilio(int idRegistroCliente) {
        PedidoRequestDTO dto = new PedidoRequestDTO();
        dto.setMetodoPagoPrincipal(MetodoPago.EFECTIVO);
        dto.setTipoPedido(TipoPedido.DOMICILIO);
        dto.setPedidoCreadoDesde(PedidoCreadoDesde.COCINA);
        dto.setIdRegistroCliente(idRegistroCliente);
        dto.setComidas(List.of());
        dto.setDesayunos(List.of());
        dto.setBasicos(List.of());
        dto.setProductosCocina(List.of());
        dto.setSaltarConfirmacion(true);
        return dto;
    }

    @Test
    @DisplayName("findAll - Debe retornar la lista de pedidos registrados")
    public void findAll() {
        when(pedidoRepository.findAll()).thenReturn(List.of(PEDIDO_PREPARED));

        List<PedidoResponseDTO> result = pedidoService.findAll();

        assertEquals(1, result.size());
        assertEquals(MetodoPago.EFECTIVO, result.get(0).getMetodoPagoPrincipal());
        System.out.println("[OK] findAll retornó " + result.size() + " pedido(s): metodoPago=" + result.get(0).getMetodoPagoPrincipal());
    }

    @Test
    @DisplayName("findById - Debe retornar el pedido cuando el ID existe")
    public void findById_encontrado() {
        when(pedidoRepository.findById(10)).thenReturn(Optional.of(PEDIDO_PREPARED));

        PedidoResponseDTO result = pedidoService.findById(10);

        assertNotNull(result);
        assertEquals(10, result.getIdPedido());
        assertEquals(TipoPedido.MOSTRADOR, result.getTipoPedido());
        System.out.println("[OK] findById retornó pedido id=" + result.getIdPedido());
    }

    @Test
    @DisplayName("findById - Debe lanzar excepción cuando el ID no existe")
    public void findById_noEncontrado() {
        when(pedidoRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> pedidoService.findById(99));
        System.out.println("[OK] findById lanzó BusinessException para id=99");
    }

    @Test
    @DisplayName("save - Debe guardar un pedido MOSTRADOR COCINA y delegar al catálogo")
    public void save_mostradorVacio() {
        when(catalogoPedido.calcularTotal(any(Pedido.class))).thenReturn(BigDecimal.ZERO);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(PEDIDO_PREPARED);

        PedidoResponseDTO result = pedidoService.save(crearDtoMostrador());

        assertNotNull(result);
        assertEquals(MetodoPago.EFECTIVO, result.getMetodoPagoPrincipal());
        verify(catalogoPedido).handleTipoPedido(any(Pedido.class), any(PedidoRequestDTO.class));
        verify(pedidoRepository).save(any(Pedido.class));
        System.out.println("[OK] save guardó pedido MOSTRADOR: id=" + result.getIdPedido());
    }

    @Test
    @DisplayName("save - Pedido WEB DOMICILIO debe invocar catalogoPedido.handleTipoPedido")
    public void save_webDomicilio() {
        when(catalogoPedido.calcularTotal(any(Pedido.class))).thenReturn(BigDecimal.valueOf(30));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(PEDIDO_PREPARED);

        PedidoResponseDTO result = pedidoService.save(crearDtoWebDomicilio());

        assertNotNull(result);
        verify(catalogoPedido).handleTipoPedido(any(Pedido.class), any(PedidoRequestDTO.class));
        verify(pedidoRepository).save(any(Pedido.class));
        System.out.println("[OK] save guardó pedido WEB-DOMICILIO");
    }

    @Test
    @DisplayName("save - Cuando el catálogo lanza excepción el pedido no se guarda")
    public void save_catalogoLanzaExcepcion_noGuardaPedido() {
        doThrow(new BusinessException("Ruta no encontrada", HttpStatus.BAD_REQUEST))
                .when(catalogoPedido).handleTipoPedido(any(Pedido.class), any(PedidoRequestDTO.class));

        assertThrows(BusinessException.class, () -> pedidoService.save(crearDtoWebDomicilio()));
        verify(pedidoRepository, never()).save(any(Pedido.class));
        System.out.println("[OK] save no guardó pedido cuando catálogo lanza excepción");
    }

    @Test
    @DisplayName("save - Pedido COCINA PICK_UP con nombre de cliente debe invocar catalogoPedido")
    public void save_cocinaPickUp() {
        when(catalogoPedido.calcularTotal(any(Pedido.class))).thenReturn(BigDecimal.ZERO);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(PEDIDO_PREPARED);

        PedidoResponseDTO result = pedidoService.save(crearDtoCocinaPickUp("Juan Pérez"));

        assertNotNull(result);
        verify(catalogoPedido).handleTipoPedido(any(Pedido.class), any(PedidoRequestDTO.class));
        verify(pedidoRepository).save(any(Pedido.class));
        System.out.println("[OK] save guardó pedido COCINA-PICK_UP");
    }

    @Test
    @DisplayName("save - Pedido COCINA DOMICILIO con idRegistroCliente debe invocar catalogoPedido")
    public void save_cocinaDomicilio() {
        when(catalogoPedido.calcularTotal(any(Pedido.class))).thenReturn(BigDecimal.valueOf(35));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(PEDIDO_PREPARED);

        PedidoResponseDTO result = pedidoService.save(crearDtoCocinaDomicilio(1));

        assertNotNull(result);
        verify(catalogoPedido).handleTipoPedido(any(Pedido.class), any(PedidoRequestDTO.class));
        verify(pedidoRepository).save(any(Pedido.class));
        System.out.println("[OK] save guardó pedido COCINA-DOMICILIO");
    }

    @Test
    @DisplayName("update - Debe actualizar el pedido y retornar el resultado correctamente")
    public void update_exitoso() {
        when(pedidoRepository.findById(10)).thenReturn(Optional.of(PEDIDO_PREPARED));
        Pedido actualizado = Pedido.builder()
                .idPedido(10)
                .metodoPagoPrincipal(MetodoPago.TARJETA)
                .tipoPedido(TipoPedido.MOSTRADOR)
                .pedidoCreadoDesde(PedidoCreadoDesde.COCINA)
                .precioFinalOrden(BigDecimal.ZERO)
                .impreso(false)
                .build();
        when(catalogoPedido.calcularTotal(any(Pedido.class))).thenReturn(BigDecimal.ZERO);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(actualizado);

        PedidoRequestDTO dtoUpdate = crearDtoMostrador();
        dtoUpdate.setMetodoPagoPrincipal(MetodoPago.TARJETA);
        PedidoResponseDTO result = pedidoService.update(10, dtoUpdate);

        assertNotNull(result);
        assertEquals(MetodoPago.TARJETA, result.getMetodoPagoPrincipal());
        System.out.println("[OK] update actualizó pedido: metodoPago=" + result.getMetodoPagoPrincipal());
    }

    @Test
    @DisplayName("delete - Debe eliminar el pedido cuando el ID existe")
    public void delete_exitoso() {
        when(pedidoRepository.existsById(10)).thenReturn(true);

        assertDoesNotThrow(() -> pedidoService.delete(10));
        verify(pedidoRepository).deleteById(10);
        System.out.println("[OK] delete eliminó pedido id=10");
    }

    @Test
    @DisplayName("findById - Debe retornar basicoPedido con objeto basico anidado y complementos")
    public void findById_conBasicos() {
        Complemento complemento = new Complemento();
        complemento.setIdComplemento(1);
        complemento.setNombreComplemento("Agua");
        complemento.setPrecioExtra(BigDecimal.ZERO);

        BasicoComplemento bc = BasicoComplemento.builder()
                .idBasicoComplemento(1)
                .complemento(complemento)
                .build();

        Comida comida = new Comida();
        comida.setIdComida(5);
        comida.setNombreComida("Pollo Asado");

        Basico basico = Basico.builder()
                .idBasico(3)
                .comida(comida)
                .descripcion("Básico completo")
                .destacado(true)
                .precioBasico(BigDecimal.valueOf(80.00))
                .complementos(List.of(bc))
                .build();

        BasicoPedido bp = BasicoPedido.builder()
                .idBasicoPedido(7)
                .basico(basico)
                .precioUnitario(BigDecimal.valueOf(80.00))
                .build();

        Pedido pedidoConBasicos = Pedido.builder()
                .idPedido(10)
                .metodoPagoPrincipal(MetodoPago.EFECTIVO)
                .tipoPedido(TipoPedido.MOSTRADOR)
                .pedidoCreadoDesde(PedidoCreadoDesde.COCINA)
                .precioFinalOrden(BigDecimal.valueOf(80.00))
                .impreso(false)
                .basicosPedido(List.of(bp))
                .build();

        when(pedidoRepository.findById(10)).thenReturn(Optional.of(pedidoConBasicos));

        PedidoResponseDTO result = pedidoService.findById(10);

        assertNotNull(result);
        assertEquals(1, result.getBasicos().size());
        BasicoPedidoResponseDTO basicoDTO = result.getBasicos().get(0);
        assertEquals(7, basicoDTO.getIdBasicoPedido());
        assertNotNull(basicoDTO.getBasico());
        assertEquals(3, basicoDTO.getBasico().getIdBasico());
        assertEquals("Pollo Asado", basicoDTO.getBasico().getNombreComida());
        assertEquals(1, basicoDTO.getBasico().getComplementos().size());
        assertEquals("Agua", basicoDTO.getBasico().getComplementos().get(0).getNombreComplemento());
        assertEquals(0, BigDecimal.valueOf(80.00).compareTo(basicoDTO.getPrecioUnitario()));
        System.out.println("[OK] findById retornó basicoPedido id=" + basicoDTO.getIdBasicoPedido()
                + " con basico.nombreComida=" + basicoDTO.getBasico().getNombreComida());
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción cuando el ID no existe")
    public void delete_noEncontrado() {
        when(pedidoRepository.existsById(99)).thenReturn(false);

        assertThrows(BusinessException.class, () -> pedidoService.delete(99));
        verify(pedidoRepository, never()).deleteById(anyInt());
        System.out.println("[OK] delete lanzó BusinessException para id=99");
    }
}
