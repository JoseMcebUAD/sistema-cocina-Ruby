package com.cocinarubi.presentation.dto.response;

import com.cocinarubi.DBConstants.MetodoPago;
import com.cocinarubi.DBConstants.PedidoCreadoDesde;
import com.cocinarubi.DBConstants.TipoPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PedidoResponseDTO {

    private int idPedido;
    private MetodoPago metodoPagoPrincipal;
    private MetodoPago metodoPagoSecundario;
    private TipoPedido tipoPedido;
    private LocalDateTime fechaExpedicionPedido;
    private PedidoCreadoDesde pedidoCreadoDesde;
    private BigDecimal precioFinalOrden;
    private BigDecimal pagoCliente;
    private BigDecimal cambio;
    private String uuidCliente;
    /**
     * Nombre del cliente, ya resuelto sea cual sea el origen del pedido.
     *
     * <p>Antes el nombre solo viajaba dentro de {@code pedidoCocina} y {@code domicilioCocina}, que
     * son null en los pedidos WEB: el frontend recibía únicamente {@code uuidCliente} y acababa
     * pintando un placeholder ("Cliente f381215f") aunque la BD tuviera el nombre. La vista SQL
     * {@code vista_resumen_pedido} sí lo resolvía con COALESCE, así que consulta y API no coincidían.
     *
     * <p>Se expone en la raíz —y no dentro de {@code domicilio}— porque el hueco no era exclusivo de
     * los domicilios: WEB + PICK_UP tenía el mismo problema.
     */
    private String nombreCliente;
    private boolean pagado;
    private boolean impreso;
    private String comentario;
    private List<ComidaPedidoResponseDTO> comidas;
    private List<DesayunoPedidoResponseDTO> desayunos;
    private List<BasicoPedidoResponseDTO> basicos;
    private List<ProductoCocinaPedidoResponseDTO> productosCocina;
    private List<PaquetePedidoResponseDTO> paquetes;
    private PedidoDomicilioResponseDTO domicilio;
    private PedidoDomicilioCocinaResponseDTO domicilioCocina;
    private PedidoCocinaResponseDTO pedidoCocina;
    private List<String> tarifasAplicadas;

    public PedidoResponseDTO() {}

    public PedidoResponseDTO(int idPedido, MetodoPago metodoPagoPrincipal, MetodoPago metodoPagoSecundario,
                             TipoPedido tipoPedido,
                             LocalDateTime fechaExpedicionPedido, PedidoCreadoDesde pedidoCreadoDesde,
                             BigDecimal precioFinalOrden, BigDecimal pagoCliente, BigDecimal cambio,
                             String uuidCliente, boolean pagado, boolean impreso, String comentario,
                             List<ComidaPedidoResponseDTO> comidas,
                             List<DesayunoPedidoResponseDTO> desayunos,
                             List<BasicoPedidoResponseDTO> basicos,
                             List<ProductoCocinaPedidoResponseDTO> productosCocina,
                             List<PaquetePedidoResponseDTO> paquetes,
                             PedidoDomicilioResponseDTO domicilio,
                             PedidoDomicilioCocinaResponseDTO domicilioCocina,
                             PedidoCocinaResponseDTO pedidoCocina) {
        this.idPedido = idPedido;
        this.metodoPagoPrincipal = metodoPagoPrincipal;
        this.metodoPagoSecundario = metodoPagoSecundario;
        this.tipoPedido = tipoPedido;
        this.fechaExpedicionPedido = fechaExpedicionPedido;
        this.pedidoCreadoDesde = pedidoCreadoDesde;
        this.precioFinalOrden = precioFinalOrden;
        this.pagoCliente = pagoCliente;
        this.cambio = cambio;
        this.uuidCliente = uuidCliente;
        this.pagado = pagado;
        this.impreso = impreso;
        this.comentario = comentario;
        this.comidas = comidas;
        this.desayunos = desayunos;
        this.basicos = basicos;
        this.productosCocina = productosCocina;
        this.paquetes = paquetes;
        this.domicilio = domicilio;
        this.domicilioCocina = domicilioCocina;
        this.pedidoCocina = pedidoCocina;
    }

    public int getIdPedido() { return idPedido; }
    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }

    public MetodoPago getMetodoPagoPrincipal() { return metodoPagoPrincipal; }
    public void setMetodoPagoPrincipal(MetodoPago metodoPagoPrincipal) { this.metodoPagoPrincipal = metodoPagoPrincipal; }

    public MetodoPago getMetodoPagoSecundario() { return metodoPagoSecundario; }
    public void setMetodoPagoSecundario(MetodoPago metodoPagoSecundario) { this.metodoPagoSecundario = metodoPagoSecundario; }

    public TipoPedido getTipoPedido() { return tipoPedido; }
    public void setTipoPedido(TipoPedido tipoPedido) { this.tipoPedido = tipoPedido; }

    public LocalDateTime getFechaExpedicionPedido() { return fechaExpedicionPedido; }
    public void setFechaExpedicionPedido(LocalDateTime fechaExpedicionPedido) { this.fechaExpedicionPedido = fechaExpedicionPedido; }

    public PedidoCreadoDesde getPedidoCreadoDesde() { return pedidoCreadoDesde; }
    public void setPedidoCreadoDesde(PedidoCreadoDesde pedidoCreadoDesde) { this.pedidoCreadoDesde = pedidoCreadoDesde; }

    public BigDecimal getPrecioFinalOrden() { return precioFinalOrden; }
    public void setPrecioFinalOrden(BigDecimal precioFinalOrden) { this.precioFinalOrden = precioFinalOrden; }

    public BigDecimal getPagoCliente() { return pagoCliente; }
    public void setPagoCliente(BigDecimal pagoCliente) { this.pagoCliente = pagoCliente; }

    public BigDecimal getCambio() { return cambio; }
    public void setCambio(BigDecimal cambio) { this.cambio = cambio; }

    public String getUuidCliente() { return uuidCliente; }
    public void setUuidCliente(String uuidCliente) { this.uuidCliente = uuidCliente; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public boolean isPagado() { return pagado; }
    public void setPagado(boolean pagado) { this.pagado = pagado; }

    public boolean isImpreso() { return impreso; }
    public void setImpreso(boolean impreso) { this.impreso = impreso; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public List<ComidaPedidoResponseDTO> getComidas() { return comidas; }
    public void setComidas(List<ComidaPedidoResponseDTO> comidas) { this.comidas = comidas; }

    public List<DesayunoPedidoResponseDTO> getDesayunos() { return desayunos; }
    public void setDesayunos(List<DesayunoPedidoResponseDTO> desayunos) { this.desayunos = desayunos; }

    public List<BasicoPedidoResponseDTO> getBasicos() { return basicos; }
    public void setBasicos(List<BasicoPedidoResponseDTO> basicos) { this.basicos = basicos; }

    public List<ProductoCocinaPedidoResponseDTO> getProductosCocina() { return productosCocina; }
    public void setProductosCocina(List<ProductoCocinaPedidoResponseDTO> productosCocina) { this.productosCocina = productosCocina; }

    public List<PaquetePedidoResponseDTO> getPaquetes() { return paquetes; }
    public void setPaquetes(List<PaquetePedidoResponseDTO> paquetes) { this.paquetes = paquetes; }

    public PedidoDomicilioResponseDTO getDomicilio() { return domicilio; }
    public void setDomicilio(PedidoDomicilioResponseDTO domicilio) { this.domicilio = domicilio; }

    public PedidoDomicilioCocinaResponseDTO getDomicilioCocina() { return domicilioCocina; }
    public void setDomicilioCocina(PedidoDomicilioCocinaResponseDTO domicilioCocina) { this.domicilioCocina = domicilioCocina; }

    public PedidoCocinaResponseDTO getPedidoCocina() { return pedidoCocina; }
    public void setPedidoCocina(PedidoCocinaResponseDTO pedidoCocina) { this.pedidoCocina = pedidoCocina; }

    public List<String> getTarifasAplicadas() { return tarifasAplicadas; }
    public void setTarifasAplicadas(List<String> tarifasAplicadas) { this.tarifasAplicadas = tarifasAplicadas; }
}
