package com.cocinarubi.domain.service.impresion.strategy;

import com.cocinarubi.DBConstants.TipoEntidadImpresion;
import com.cocinarubi.DBConstants.TipoPedido;
import com.cocinarubi.dao.PedidoRepository;
import com.cocinarubi.domain.entity.Pedido;
import com.cocinarubi.domain.service.PedidoService;
import com.cocinarubi.domain.service.impresion.ImpresionStrategy;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.response.PedidoResponseDTO;
import com.cocinarubi.util.template.BaseTicketTemplate;
import com.cocinarubi.util.template.data.PedidoTicketData;
import com.cocinarubi.util.template.impl.PedidoTicketTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PedidoImpresionStrategy implements ImpresionStrategy {

    private final PedidoService pedidoService;
    private final PedidoRepository pedidoRepository;

    public PedidoImpresionStrategy(PedidoService pedidoService, PedidoRepository pedidoRepository) {
        this.pedidoService = pedidoService;
        this.pedidoRepository = pedidoRepository;
    }

    @Override
    public TipoEntidadImpresion getTipo() {
        return TipoEntidadImpresion.PEDIDO;
    }

    @Override
    public BaseTicketTemplate<?> buildTemplate(Integer id) {
        PedidoResponseDTO dto = pedidoService.findById(id);
        return new PedidoTicketTemplate(toTicketData(dto));
    }

    @Override
    @Transactional
    public void onPrintSuccess(Integer id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Pedido no encontrado con id: " + id, HttpStatus.NOT_FOUND));
        pedido.setImpreso(true);
        pedidoRepository.save(pedido);
    }

    private PedidoTicketData toTicketData(PedidoResponseDTO dto) {
        PedidoTicketData data = new PedidoTicketData();
        data.setNumeroPedido(dto.getIdPedido());
        data.setMetodoPagoPrincipal(dto.getMetodoPagoPrincipal());
        data.setMetodoPagoSecundario(dto.getMetodoPagoSecundario());
        data.setTipoPedido(dto.getTipoPedido());
        data.setFechaExpedicionPedido(dto.getFechaExpedicionPedido());
        data.setPrecioFinalOrden(dto.getPrecioFinalOrden());
        data.setPagoCliente(dto.getPagoCliente());
        data.setCambio(dto.getCambio());
        data.setComidas(dto.getComidas());
        data.setDesayunos(dto.getDesayunos());
        data.setBasicos(dto.getBasicos());
        data.setProductosCocina(dto.getProductosCocina());
        data.setDomicilio(dto.getTipoPedido() == TipoPedido.DOMICILIO ? dto.getDomicilio() : null);
        return data;
    }
}
