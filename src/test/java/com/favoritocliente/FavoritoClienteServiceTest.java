package com.favoritocliente;

import com.cocinarubi.DBConstants.TipoCatalogoProducto;
import com.cocinarubi.dao.BasicoRepository;
import com.cocinarubi.dao.ClienteRepository;
import com.cocinarubi.dao.ComidaRepository;
import com.cocinarubi.dao.DesayunoRepository;
import com.cocinarubi.dao.FavoritoClienteRepository;
import com.cocinarubi.dao.ProductoCocinaRepository;
import com.cocinarubi.domain.entity.Cliente;
import com.cocinarubi.domain.entity.Comida;
import com.cocinarubi.domain.entity.FavoritoCliente;
import com.cocinarubi.domain.service.FavoritoClienteService;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.FavoritoClienteRequestDTO;
import com.cocinarubi.presentation.dto.response.FavoritoClienteResponseDTO;
import com.cocinarubi.presentation.strategy.strategyImplementation.FavoritoClienteConfirmationImp;
import com.cocinarubi.presentation.strategy.strategyImplementation.FavoritoClienteValidationImp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FavoritoClienteServiceTest {

    @Mock
    private FavoritoClienteRepository favoritoClienteRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ComidaRepository comidaRepository;

    @Mock
    private DesayunoRepository desayunoRepository;

    @Mock
    private BasicoRepository basicoRepository;

    @Mock
    private ProductoCocinaRepository productoCocinaRepository;

    @Mock
    private FavoritoClienteValidationImp favoritoClienteValidation;

    @Mock
    private FavoritoClienteConfirmationImp favoritoClienteConfirmation;

    @InjectMocks
    private FavoritoClienteService favoritoClienteService;

    public Comida COMIDA_PREPARED = Comida.builder()
            .idComida(5)
            .nombreComida("Pollo en salsa")
            .build();

    public Cliente CLIENTE_PREPARED = Cliente.builder()
            .idCliente(1)
            .sessionToken("token-favorito-abc123")
            .build();

    public FavoritoCliente FAVORITO_PREPARED = FavoritoCliente.builder()
            .idFavoritoCliente(10)
            .cliente(CLIENTE_PREPARED)
            .idProducto(5)
            .tipoCatalogoProducto(TipoCatalogoProducto.COMIDA)
            .build();

    public FavoritoClienteRequestDTO DTO = crearDto("token-favorito-abc123", 5, TipoCatalogoProducto.COMIDA);

    private FavoritoClienteRequestDTO crearDto(String token, int idProducto, TipoCatalogoProducto tipo) {
        FavoritoClienteRequestDTO dto = new FavoritoClienteRequestDTO();
        dto.setSessionToken(token);
        dto.setIdProducto(idProducto);
        dto.setTipoCatalogoProducto(tipo);
        dto.setSaltarConfirmacion(true);
        return dto;
    }

    @Test
    @DisplayName("findAll - Debe retornar la lista de favoritos registrados")
    public void findAll() {
        when(favoritoClienteRepository.findAll()).thenReturn(List.of(FAVORITO_PREPARED));
        when(comidaRepository.findById(5)).thenReturn(Optional.of(COMIDA_PREPARED));

        List<FavoritoClienteResponseDTO> result = favoritoClienteService.findAll();

        assertEquals(1, result.size());
        assertEquals("Pollo en salsa", result.get(0).getNombreProducto());
        System.out.println("[OK] findAll retornó " + result.size() + " favorito(s): " + result.get(0).getNombreProducto());
    }

    @Test
    @DisplayName("findById - Debe retornar el favorito cuando el ID existe")
    public void findById_encontrado() {
        when(favoritoClienteRepository.findById(10)).thenReturn(Optional.of(FAVORITO_PREPARED));
        when(comidaRepository.findById(5)).thenReturn(Optional.of(COMIDA_PREPARED));

        FavoritoClienteResponseDTO result = favoritoClienteService.findById(10);

        assertNotNull(result);
        assertEquals(10, result.getIdFavoritoCliente());
        assertEquals(TipoCatalogoProducto.COMIDA, result.getTipoCatalogoProducto());
        System.out.println("[OK] findById retornó favorito id=" + result.getIdFavoritoCliente());
    }

    @Test
    @DisplayName("findById - Debe lanzar excepción cuando el ID no existe")
    public void findById_noEncontrado() {
        when(favoritoClienteRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> favoritoClienteService.findById(99));
        System.out.println("[OK] findById lanzó BusinessException para id=99");
    }

    @Test
    @DisplayName("findBySessionToken - Debe retornar los favoritos del cliente por su sessionToken")
    public void findBySessionToken() {
        when(favoritoClienteRepository.findByCliente_SessionToken("token-favorito-abc123"))
                .thenReturn(List.of(FAVORITO_PREPARED));
        when(comidaRepository.findById(5)).thenReturn(Optional.of(COMIDA_PREPARED));

        List<FavoritoClienteResponseDTO> result = favoritoClienteService.findBySessionToken("token-favorito-abc123");

        assertEquals(1, result.size());
        assertEquals("token-favorito-abc123", result.get(0).getSessionToken());
        System.out.println("[OK] findBySessionToken retornó " + result.size() + " favorito(s) para el token");
    }

    @Test
    @DisplayName("save - Debe guardar y retornar el favorito cuando el cliente existe")
    public void save_exitoso() {
        when(clienteRepository.findBySessionToken("token-favorito-abc123")).thenReturn(Optional.of(CLIENTE_PREPARED));
        when(favoritoClienteRepository.save(any(FavoritoCliente.class))).thenReturn(FAVORITO_PREPARED);
        when(comidaRepository.findById(5)).thenReturn(Optional.of(COMIDA_PREPARED));

        FavoritoClienteResponseDTO result = favoritoClienteService.save(DTO);

        assertNotNull(result);
        assertEquals(TipoCatalogoProducto.COMIDA, result.getTipoCatalogoProducto());
        verify(favoritoClienteRepository).save(any(FavoritoCliente.class));
        System.out.println("[OK] save guardó favorito: tipo=" + result.getTipoCatalogoProducto());
    }

    @Test
    @DisplayName("save - Debe lanzar excepción cuando el cliente no existe")
    public void save_clienteNoEncontrado() {
        when(clienteRepository.findBySessionToken("token-favorito-abc123")).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> favoritoClienteService.save(DTO));
        verify(favoritoClienteRepository, never()).save(any(FavoritoCliente.class));
        System.out.println("[OK] save lanzó BusinessException por cliente no encontrado");
    }

    @Test
    @DisplayName("update - Debe actualizar y retornar el favorito correctamente")
    public void update_exitoso() {
        when(favoritoClienteRepository.findById(10)).thenReturn(Optional.of(FAVORITO_PREPARED));
        FavoritoCliente actualizado = FavoritoCliente.builder()
                .idFavoritoCliente(10)
                .cliente(CLIENTE_PREPARED)
                .idProducto(7)
                .tipoCatalogoProducto(TipoCatalogoProducto.COMIDA)
                .build();
        when(favoritoClienteRepository.save(any(FavoritoCliente.class))).thenReturn(actualizado);
        when(comidaRepository.findById(7)).thenReturn(Optional.of(COMIDA_PREPARED));

        FavoritoClienteRequestDTO dtoUpdate = crearDto("token-favorito-abc123", 7, TipoCatalogoProducto.COMIDA);
        FavoritoClienteResponseDTO result = favoritoClienteService.update(10, dtoUpdate);

        assertNotNull(result);
        assertEquals(7, result.getIdProducto());
        System.out.println("[OK] update actualizó favorito: idProducto=" + result.getIdProducto());
    }

    @Test
    @DisplayName("delete - Debe eliminar el favorito cuando el ID existe")
    public void delete_exitoso() {
        when(favoritoClienteRepository.existsById(10)).thenReturn(true);

        assertDoesNotThrow(() -> favoritoClienteService.delete(10));
        verify(favoritoClienteRepository).deleteById(10);
        System.out.println("[OK] delete eliminó favorito id=10");
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción cuando el ID no existe")
    public void delete_noEncontrado() {
        when(favoritoClienteRepository.existsById(99)).thenReturn(false);

        assertThrows(BusinessException.class, () -> favoritoClienteService.delete(99));
        verify(favoritoClienteRepository, never()).deleteById(anyInt());
        System.out.println("[OK] delete lanzó BusinessException para id=99");
    }
}
