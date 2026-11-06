package com.combo;

import com.cocinarubi.DBConstants.Estatus;
import com.cocinarubi.DBConstants.TipoLineaCombo;
import com.cocinarubi.dao.ComidaRepository;
import com.cocinarubi.dao.ComplementoRepository;
import com.cocinarubi.dao.ComboPedidoRepository;
import com.cocinarubi.dao.ComboRepository;
import com.cocinarubi.dao.DesayunoRepository;
import com.cocinarubi.dao.ProductoCocinaRepository;
import com.cocinarubi.domain.entity.Combo;
import com.cocinarubi.domain.entity.ComboProducto;
import com.cocinarubi.domain.entity.Comida;
import com.cocinarubi.domain.entity.Complemento;
import com.cocinarubi.domain.entity.Desayuno;
import com.cocinarubi.domain.mapper.ComboMapper;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.ComboLineaRequestDTO;
import com.cocinarubi.presentation.dto.request.ComboRequestDTO;
import com.cocinarubi.presentation.dto.response.ComboResponseDTO;
import com.cocinarubi.domain.service.ComboService;
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
public class ComboServiceTest {

    @Mock private ComboRepository comboRepository;
    @Mock private ComboPedidoRepository comboPedidoRepository;
    @Mock private ComidaRepository comidaRepository;
    @Mock private DesayunoRepository desayunoRepository;
    @Mock private ComplementoRepository complementoRepository;
    @Mock private ProductoCocinaRepository productoCocinaRepository;

    // Mapper real: sin lógica de negocio, evita mockear cada método
    private final ComboMapper comboMapper = new ComboMapper();

    @InjectMocks private ComboService comboService;

    private ComboRequestDTO buildRequest(List<ComboLineaRequestDTO> lineas) {
        ComboRequestDTO dto = new ComboRequestDTO();
        dto.setPrecio(new BigDecimal("120.00"));
        dto.setDescripcion("Promo mixta");
        dto.setDestacado(false);
        dto.setEstatus(Estatus.DISPONIBLE);
        dto.setProductos(lineas);
        return dto;
    }

    private ComboLineaRequestDTO linea(TipoLineaCombo tipo, int id, int cant) {
        return new ComboLineaRequestDTO(tipo, id, cant);
    }

    private ComboService rebuildService() {
        return new ComboService(comboRepository, comboPedidoRepository,
                comidaRepository, desayunoRepository, complementoRepository,
                productoCocinaRepository, comboMapper);
    }

    @Test
    @DisplayName("save - persiste combo con líneas de comida + desayuno + complemento")
    public void save_persisteConProductosValidosDeVariosTipos_yDevuelveResponse() {
        ComboService service = rebuildService();
        ComboRequestDTO dto = buildRequest(new ArrayList<>(List.of(
                linea(TipoLineaCombo.COMIDA, 1, 1),
                linea(TipoLineaCombo.DESAYUNO, 2, 2),
                linea(TipoLineaCombo.COMPLEMENTO, 3, 1)
        )));

        when(comidaRepository.findAllById(any())).thenReturn(List.of(
                Comida.builder().idComida(1).nombreComida("Bistec").build()));
        when(desayunoRepository.findAllById(any())).thenReturn(List.of(
                Desayuno.builder().idDesayuno(2).nombreDesayuno("Chilaquiles").build()));
        when(complementoRepository.findAllById(any())).thenReturn(List.of(
                Complemento.builder().idComplemento(3).nombreComplemento("Arroz").build()));

        Combo persistido = Combo.builder()
                .idCombo(10).precio(dto.getPrecio()).descripcion(dto.getDescripcion())
                .destacado(dto.getDestacado()).estatus(dto.getEstatus()).productos(new ArrayList<>()).build();
        persistido.addProducto(ComboProducto.builder()
                .idComboProducto(100).tipoProducto(TipoLineaCombo.COMIDA).idProducto(1).cantidad(1).build());
        persistido.addProducto(ComboProducto.builder()
                .idComboProducto(101).tipoProducto(TipoLineaCombo.DESAYUNO).idProducto(2).cantidad(2).build());
        persistido.addProducto(ComboProducto.builder()
                .idComboProducto(102).tipoProducto(TipoLineaCombo.COMPLEMENTO).idProducto(3).cantidad(1).build());
        when(comboRepository.save(any(Combo.class))).thenReturn(persistido);

        ComboResponseDTO res = service.save(dto);

        assertEquals(10, res.getIdCombo());
        assertFalse(res.getDestacado());
        assertEquals(3, res.getProductos().size());
        verify(comboRepository).save(any(Combo.class));
        System.out.println("[OK] save persistió combo con 3 líneas mixtas");
    }

    @Test
    @DisplayName("save - lanza NOT_FOUND cuando una Comida referenciada no existe")
    public void save_lanza404CuandoAlgunaComidaNoExiste() {
        ComboService service = rebuildService();
        ComboRequestDTO dto = buildRequest(new ArrayList<>(List.of(
                linea(TipoLineaCombo.COMIDA, 999, 1)
        )));

        when(comidaRepository.findAllById(any())).thenReturn(List.of());

        BusinessException ex = assertThrows(BusinessException.class, () -> service.save(dto));
        assertEquals(404, ex.getHttpStatus().value());
        verify(comboRepository, never()).save(any(Combo.class));
        System.out.println("[OK] save lanzó 404 por comida inexistente");
    }

    @Test
    @DisplayName("update - sincroniza líneas sin recrear la colección (respeta orphanRemoval)")
    public void update_sincronizaLineas_sinRecrearColeccion() {
        ComboService service = rebuildService();
        Combo existente = Combo.builder()
                .idCombo(5).precio(new BigDecimal("50.00")).descripcion("viejo")
                .destacado(true).estatus(Estatus.DISPONIBLE).productos(new ArrayList<>()).build();
        existente.addProducto(ComboProducto.builder()
                .idComboProducto(50).tipoProducto(TipoLineaCombo.COMIDA).idProducto(1).cantidad(1).build());
        List<ComboProducto> refOriginal = existente.getProductos();

        when(comboRepository.findByIdWithProductos(5)).thenReturn(Optional.of(existente));
        when(comidaRepository.findAllById(any())).thenReturn(List.of(
                Comida.builder().idComida(1).nombreComida("Bistec").build()));
        when(comboRepository.save(any(Combo.class))).thenAnswer(inv -> {
            Combo c = inv.getArgument(0);
            int[] counter = {200};
            c.getProductos().stream()
                .filter(cp -> cp.getIdComboProducto() == null)
                .forEach(cp -> cp.setIdComboProducto(counter[0]++));
            return c;
        });

        ComboRequestDTO dto = buildRequest(new ArrayList<>(List.of(
                linea(TipoLineaCombo.COMIDA, 1, 3)
        )));
        service.update(5, dto);

        assertSame(refOriginal, existente.getProductos(),
                "La colección debe ser la misma instancia (orphanRemoval)");
        assertFalse(existente.isDestacado(), "destacado debe actualizarse al valor del request");
        assertEquals(1, existente.getProductos().size());
        assertEquals(3, existente.getProductos().get(0).getCantidad());
        System.out.println("[OK] update sincronizó líneas sobre la misma colección");
    }

    @Test
    @DisplayName("delete - lanza CONFLICT cuando el combo forma parte de algún pedido")
    public void delete_lanza409CuandoElComboEstaEnAlgunPedido() {
        ComboService service = rebuildService();
        when(comboRepository.existsById(5)).thenReturn(true);
        when(comboPedidoRepository.existsByCombo_IdCombo(5)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.delete(5, false));
        assertEquals(409, ex.getHttpStatus().value());
        verify(comboRepository, never()).deleteById(anyInt());
        System.out.println("[OK] delete lanzó 409 por combo referenciado en pedido");
    }

    @Test
    @DisplayName("delete - lanza NOT_FOUND cuando el id no existe")
    public void delete_lanza404CuandoNoExiste() {
        ComboService service = rebuildService();
        when(comboRepository.existsById(99)).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.delete(99, false));
        assertEquals(404, ex.getHttpStatus().value());
        System.out.println("[OK] delete lanzó 404 para id=99");
    }

    @Test
    @DisplayName("toggleDestacado - invierte el campo destacado y persiste")
    public void toggleDestacado_inverteCampoYPersiste() {
        ComboService service = rebuildService();
        Combo combo = Combo.builder()
                .idCombo(1).precio(new BigDecimal("100.00")).descripcion("Promo")
                .destacado(false).estatus(Estatus.DISPONIBLE).productos(new ArrayList<>()).build();

        when(comboRepository.findByIdWithProductos(1)).thenReturn(Optional.of(combo));
        when(comboRepository.save(any(Combo.class))).thenAnswer(inv -> inv.getArgument(0));

        ComboResponseDTO res = service.toggleDestacado(1);

        assertTrue(res.getDestacado());
        verify(comboRepository).save(any(Combo.class));
        System.out.println("[OK] toggleDestacado invirtió destacado a true");
    }

    @Test
    @DisplayName("updateEstatus - cambia el estatus del combo y persiste")
    public void updateEstatus_cambiaEstatusYPersiste() {
        ComboService service = rebuildService();
        Combo combo = Combo.builder()
                .idCombo(2).precio(new BigDecimal("80.00")).descripcion("Promo B")
                .destacado(false).estatus(Estatus.DISPONIBLE).productos(new ArrayList<>()).build();

        when(comboRepository.findByIdWithProductos(2)).thenReturn(Optional.of(combo));
        when(comboRepository.save(any(Combo.class))).thenAnswer(inv -> inv.getArgument(0));

        ComboResponseDTO res = service.updateEstatus(2, Estatus.NO_DISPONIBLE);

        assertEquals(Estatus.NO_DISPONIBLE, res.getEstatus());
        verify(comboRepository).save(any(Combo.class));
        System.out.println("[OK] updateEstatus cambió estatus a NO_DISPONIBLE");
    }
}
