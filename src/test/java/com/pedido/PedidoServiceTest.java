package com.pedido;

import com.cocinarubi.DBConstants.MetodoPago;
import com.cocinarubi.DBConstants.PedidoCreadoDesde;
import com.cocinarubi.DBConstants.TipoPedido;
import com.cocinarubi.dao.BasicoRepository;
import com.cocinarubi.dao.ComidaRepository;
import com.cocinarubi.dao.ComplementoRepository;
import com.cocinarubi.dao.DesayunoRepository;
import com.cocinarubi.dao.PedidoRepository;
import com.cocinarubi.dao.ProductoCocinaRepository;
import com.cocinarubi.dao.RutaRepository;
import com.cocinarubi.domain.entity.Pedido;
import com.cocinarubi.domain.entity.Ruta;
import com.cocinarubi.domain.service.PedidoService;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.PedidoDomicilioDTO;
import com.cocinarubi.presentation.dto.request.PedidoRequestDTO;
import com.cocinarubi.presentation.dto.response.PedidoResponseDTO;
import com.cocinarubi.presentation.strategy.strategyImplementation.PedidoConfirmationImp;
import com.cocinarubi.presentation.strategy.strategyImplementation.PedidoValidationImp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PedidoServiceTest {

    @Mock private PedidoRepository pedidoRepository;
    @Mock private ComidaRepository comidaRepository;
    @Mock private DesayunoRepository desayunoRepository;
    @Mock private BasicoRepository basicoRepository;
    @Mock private ProductoCocinaRepository productoCocinaRepository;
    @Mock private ComplementoRepository complementoRepository;
    @Mock private RutaRepository rutaRepository;
    @Mock private PedidoValidationImp pedidoValidation;
    @Mock private PedidoConfirmationImp pedidoConfirmation;

    @InjectMocks
    private PedidoService pedidoService;

    public Ruta RUTA_PREPARED = Ruta.builder()
            .idRuta(1)
            .nombre("Zona Centro")
            .tarifaEnvio(BigDecimal.valueOf(30.00))
            .build();

    public Pedido PEDIDO_PREPARED = Pedido.builder()
            .idPedido(10)
            .metodoPago(MetodoPago.EFECTIVO)
            .tipoPedido(TipoPedido.MOSTRADOR)
            .pedidoCreadoDesde(PedidoCreadoDesde.COCINA)
            .precioFinalOrden(BigDecimal.ZERO)
            .impreso(false)
            .build();

    public PedidoRequestDTO crearDtoMostrador() {
        PedidoRequestDTO dto = new PedidoRequestDTO();
        dto.setMetodoPago(MetodoPago.EFECTIVO);
        dto.setTipoPedido(TipoPedido.MOSTRADOR);
        dto.setPedidoCreadoDesde(PedidoCreadoDesde.COCINA);
        dto.setComidas(List.of());
        dto.setDesayunos(List.of());
        dto.setBasicos(List.of());
        dto.setProductosCocina(List.of());
        dto.setSaltarConfirmacion(true);
        return dto;
    }

    public PedidoRequestDTO crearDtoDomicilio(int idRuta) {
        PedidoDomicilioDTO domicilioDto = new PedidoDomicilioDTO();
        domicilioDto.setIdRuta(idRuta);
        domicilioDto.setDireccion("Calle Falsa 123");

        PedidoRequestDTO dto = new PedidoRequestDTO();
        dto.setMetodoPago(MetodoPago.TARJETA);
        dto.setTipoPedido(TipoPedido.DOMICILIO);
        dto.setPedidoCreadoDesde(PedidoCreadoDesde.COCINA);
        dto.setComidas(List.of());
        dto.setDesayunos(List.of());
        dto.setBasicos(List.of());
        dto.setProductosCocina(List.of());
        dto.setDomicilio(domicilioDto);
        dto.setSaltarConfirmacion(true);
        return dto;
    }

    @Test
    @DisplayName("findAll - Debe retornar la lista de pedidos registrados")
    public void findAll() {
        when(pedidoRepository.findAll()).thenReturn(List.of(PEDIDO_PREPARED));

        List<PedidoResponseDTO> result = pedidoService.findAll();

        assertEquals(1, result.size());
        assertEquals(MetodoPago.EFECTIVO, result.get(0).getMetodoPago());
        System.out.println("[OK] findAll retornó " + result.size() + " pedido(s): metodoPago=" + result.get(0).getMetodoPago());
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
    @DisplayName("save - Debe guardar un pedido MOSTRADOR con listas vacías correctamente")
    public void save_mostradorVacio() {
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(PEDIDO_PREPARED);

        PedidoResponseDTO result = pedidoService.save(crearDtoMostrador());

        assertNotNull(result);
        assertEquals(MetodoPago.EFECTIVO, result.getMetodoPago());
        verify(pedidoRepository).save(any(Pedido.class));
        verify(rutaRepository, never()).findById(anyInt());
        System.out.println("[OK] save guardó pedido MOSTRADOR: id=" + result.getIdPedido());
    }

    @Test
    @DisplayName("save - Debe guardar un pedido DOMICILIO y consultar la ruta")
    public void save_domicilioConRuta() {
        when(rutaRepository.findById(1)).thenReturn(Optional.of(RUTA_PREPARED));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(PEDIDO_PREPARED);

        PedidoResponseDTO result = pedidoService.save(crearDtoDomicilio(1));

        assertNotNull(result);
        verify(rutaRepository).findById(1);
        verify(pedidoRepository).save(any(Pedido.class));
        System.out.println("[OK] save guardó pedido DOMICILIO con ruta id=1");
    }

    @Test
    @DisplayName("save - Debe lanzar excepción cuando la ruta del domicilio no existe")
    public void save_domicilioRutaNoEncontrada() {
        when(rutaRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> pedidoService.save(crearDtoDomicilio(99)));
        verify(pedidoRepository, never()).save(any(Pedido.class));
        System.out.println("[OK] save lanzó BusinessException por ruta no encontrada id=99");
    }

    @Test
    @DisplayName("update - Debe actualizar el pedido y retornar el resultado correctamente")
    public void update_exitoso() {
        when(pedidoRepository.findById(10)).thenReturn(Optional.of(PEDIDO_PREPARED));
        Pedido actualizado = Pedido.builder()
                .idPedido(10)
                .metodoPago(MetodoPago.TARJETA)
                .tipoPedido(TipoPedido.MOSTRADOR)
                .pedidoCreadoDesde(PedidoCreadoDesde.COCINA)
                .precioFinalOrden(BigDecimal.ZERO)
                .impreso(false)
                .build();
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(actualizado);

        PedidoRequestDTO dtoUpdate = crearDtoMostrador();
        dtoUpdate.setMetodoPago(MetodoPago.TARJETA);
        PedidoResponseDTO result = pedidoService.update(10, dtoUpdate);

        assertNotNull(result);
        assertEquals(MetodoPago.TARJETA, result.getMetodoPago());
        System.out.println("[OK] update actualizó pedido: metodoPago=" + result.getMetodoPago());
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
    @DisplayName("delete - Debe lanzar excepción cuando el ID no existe")
    public void delete_noEncontrado() {
        when(pedidoRepository.existsById(99)).thenReturn(false);

        assertThrows(BusinessException.class, () -> pedidoService.delete(99));
        verify(pedidoRepository, never()).deleteById(anyInt());
        System.out.println("[OK] delete lanzó BusinessException para id=99");
    }
}
