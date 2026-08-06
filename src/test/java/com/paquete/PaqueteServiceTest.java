package com.paquete;

import com.cocinarubi.DBConstants.Estatus;
import com.cocinarubi.DBConstants.TipoLineaPaquete;
import com.cocinarubi.dao.ComidaRepository;
import com.cocinarubi.dao.ComplementoRepository;
import com.cocinarubi.dao.DesayunoRepository;
import com.cocinarubi.dao.PaquetePedidoRepository;
import com.cocinarubi.dao.PaqueteRepository;
import com.cocinarubi.dao.ProductoCocinaRepository;
import com.cocinarubi.domain.entity.Comida;
import com.cocinarubi.domain.entity.Complemento;
import com.cocinarubi.domain.entity.Desayuno;
import com.cocinarubi.domain.entity.Paquete;
import com.cocinarubi.domain.entity.PaqueteProducto;
import com.cocinarubi.domain.mapper.PaqueteMapper;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.PaqueteLineaRequestDTO;
import com.cocinarubi.presentation.dto.request.PaqueteRequestDTO;
import com.cocinarubi.presentation.dto.response.PaqueteResponseDTO;
import com.cocinarubi.domain.service.PaqueteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaqueteServiceTest {

    @Mock private PaqueteRepository paqueteRepository;
    @Mock private PaquetePedidoRepository paquetePedidoRepository;
    @Mock private ComidaRepository comidaRepository;
    @Mock private DesayunoRepository desayunoRepository;
    @Mock private ComplementoRepository complementoRepository;
    @Mock private ProductoCocinaRepository productoCocinaRepository;

    // Mapper real: sin lógica de negocio, evita mockear cada método
    private final PaqueteMapper paqueteMapper = new PaqueteMapper();

    @InjectMocks private PaqueteService paqueteService;

    private PaqueteRequestDTO buildRequest(List<PaqueteLineaRequestDTO> lineas) {
        PaqueteRequestDTO dto = new PaqueteRequestDTO();
        dto.setPrecio(new BigDecimal("120.00"));
        dto.setDescripcion("Promo mixta");
        dto.setEstatus(Estatus.DISPONIBLE);
        dto.setProductos(lineas);
        return dto;
    }

    private PaqueteLineaRequestDTO linea(TipoLineaPaquete tipo, int id, int cant) {
        return new PaqueteLineaRequestDTO(tipo, id, cant);
    }

    private PaqueteService rebuildService() {
        // Se reinyecta usando el mapper real (@InjectMocks no puede mezclar mocks + reales)
        return new PaqueteService(paqueteRepository, paquetePedidoRepository,
                comidaRepository, desayunoRepository, complementoRepository,
                productoCocinaRepository, paqueteMapper);
    }

    @Test
    @DisplayName("save - persiste paquete con líneas de comida + desayuno + complemento")
    public void save_persisteConProductosValidosDeVariosTipos_yDevuelveResponse() {
        PaqueteService service = rebuildService();
        PaqueteRequestDTO dto = buildRequest(new ArrayList<>(List.of(
                linea(TipoLineaPaquete.COMIDA, 1, 1),
                linea(TipoLineaPaquete.DESAYUNO, 2, 2),
                linea(TipoLineaPaquete.COMPLEMENTO, 3, 1)
        )));

        when(comidaRepository.findAllById(any())).thenReturn(List.of(
                Comida.builder().idComida(1).nombreComida("Bistec").build()));
        when(desayunoRepository.findAllById(any())).thenReturn(List.of(
                Desayuno.builder().idDesayuno(2).nombreDesayuno("Chilaquiles").build()));
        when(complementoRepository.findAllById(any())).thenReturn(List.of(
                Complemento.builder().idComplemento(3).nombreComplemento("Arroz").build()));

        Paquete persistido = Paquete.builder()
                .idPaquete(10).precio(dto.getPrecio()).descripcion(dto.getDescripcion())
                .estatus(dto.getEstatus()).productos(new ArrayList<>()).build();
        persistido.addProducto(PaqueteProducto.builder()
                .idPaqueteProducto(100).tipoProducto(TipoLineaPaquete.COMIDA).idProducto(1).cantidad(1).build());
        persistido.addProducto(PaqueteProducto.builder()
                .idPaqueteProducto(101).tipoProducto(TipoLineaPaquete.DESAYUNO).idProducto(2).cantidad(2).build());
        persistido.addProducto(PaqueteProducto.builder()
                .idPaqueteProducto(102).tipoProducto(TipoLineaPaquete.COMPLEMENTO).idProducto(3).cantidad(1).build());
        when(paqueteRepository.save(any(Paquete.class))).thenReturn(persistido);

        PaqueteResponseDTO res = service.save(dto);

        assertEquals(10, res.getIdPaquete());
        assertEquals(3, res.getProductos().size());
        verify(paqueteRepository).save(any(Paquete.class));
        System.out.println("[OK] save persistió paquete con 3 líneas mixtas");
    }

    @Test
    @DisplayName("save - lanza NOT_FOUND cuando una Comida referenciada no existe")
    public void save_lanza404CuandoAlgunaComidaNoExiste() {
        PaqueteService service = rebuildService();
        PaqueteRequestDTO dto = buildRequest(new ArrayList<>(List.of(
                linea(TipoLineaPaquete.COMIDA, 999, 1)
        )));

        when(comidaRepository.findAllById(any())).thenReturn(List.of());

        BusinessException ex = assertThrows(BusinessException.class, () -> service.save(dto));
        assertEquals(404, ex.getHttpStatus().value());
        verify(paqueteRepository, never()).save(any(Paquete.class));
        System.out.println("[OK] save lanzó 404 por comida inexistente");
    }

    @Test
    @DisplayName("update - sincroniza líneas sin recrear la colección (respeta orphanRemoval)")
    public void update_sincronizaLineas_sinRecrearColeccion() {
        PaqueteService service = rebuildService();
        Paquete existente = Paquete.builder()
                .idPaquete(5).precio(new BigDecimal("50.00")).descripcion("viejo")
                .estatus(Estatus.DISPONIBLE).productos(new ArrayList<>()).build();
        existente.addProducto(PaqueteProducto.builder()
                .idPaqueteProducto(50).tipoProducto(TipoLineaPaquete.COMIDA).idProducto(1).cantidad(1).build());
        List<PaqueteProducto> refOriginal = existente.getProductos();

        when(paqueteRepository.findByIdWithProductos(5)).thenReturn(Optional.of(existente));
        when(comidaRepository.findAllById(any())).thenReturn(List.of(
                Comida.builder().idComida(1).nombreComida("Bistec").build()));
        when(paqueteRepository.save(any(Paquete.class))).thenAnswer(inv -> inv.getArgument(0));

        PaqueteRequestDTO dto = buildRequest(new ArrayList<>(List.of(
                linea(TipoLineaPaquete.COMIDA, 1, 3)
        )));
        service.update(5, dto);

        assertSame(refOriginal, existente.getProductos(),
                "La colección debe ser la misma instancia (orphanRemoval)");
        assertEquals(1, existente.getProductos().size());
        assertEquals(3, existente.getProductos().get(0).getCantidad());
        System.out.println("[OK] update sincronizó líneas sobre la misma colección");
    }

    @Test
    @DisplayName("delete - lanza CONFLICT cuando el paquete forma parte de algún pedido")
    public void delete_lanza409CuandoElPaqueteEstaEnAlgunPedido() {
        PaqueteService service = rebuildService();
        when(paqueteRepository.existsById(5)).thenReturn(true);
        when(paquetePedidoRepository.existsByPaquete_IdPaquete(5)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.delete(5));
        assertEquals(409, ex.getHttpStatus().value());
        verify(paqueteRepository, never()).deleteById(anyInt());
        System.out.println("[OK] delete lanzó 409 por paquete referenciado en pedido");
    }

    @Test
    @DisplayName("delete - lanza NOT_FOUND cuando el id no existe")
    public void delete_lanza404CuandoNoExiste() {
        PaqueteService service = rebuildService();
        when(paqueteRepository.existsById(99)).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.delete(99));
        assertEquals(404, ex.getHttpStatus().value());
        System.out.println("[OK] delete lanzó 404 para id=99");
    }
}
