package com.cliente;

import com.cocinarubi.dao.ClienteRepository;
import com.cocinarubi.dao.RutaRepository;
import com.cocinarubi.domain.entity.Cliente;
import com.cocinarubi.domain.service.ClienteService;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.ClienteRequestDTO;
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
public class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private RutaRepository rutaRepository;

    @InjectMocks
    private ClienteService clienteService;

    public Cliente CLIENTE_PREPARED = Cliente.builder()
            .idCliente(10)
            .uuidCliente("uuid-cli-10")
            .sessionToken("token-unico-abc123")
            .codigoCliente("CLI-001")
            .nombre("Juan Pérez")
            .direccionCliente("Calle 52 #216")
            .telefono("9991234567")
            .build();

    public ClienteRequestDTO CLIENTE_DTO = crearDto("uuid-cli-10", "token-unico-abc123");

    public ClienteRequestDTO CLIENTE_DTO_MODIFIED = crearDto("uuid-cli-10", "token-modificado-xyz999");

    private ClienteRequestDTO crearDto(String uuid, String token) {
        ClienteRequestDTO dto = new ClienteRequestDTO();
        dto.setUuidCliente(uuid);
        dto.setSessionToken(token);
        dto.setCodigoCliente("CLI-001");
        dto.setNombre("Juan Pérez");
        dto.setDireccionCliente("Calle 52 #216");
        dto.setTelefono("9991234567");
        return dto;
    }

    @Test
    @DisplayName("findAll - Debe retornar la lista de clientes registrados")
    public void findAll() {
        when(clienteRepository.findAll()).thenReturn(List.of(CLIENTE_PREPARED));

        List<Cliente> result = clienteService.findAll();

        assertEquals(1, result.size());
        assertEquals("Juan Pérez", result.get(0).getNombre());
        System.out.println("[OK] findAll retornó " + result.size() + " cliente(s): " + result.get(0).getNombre());
    }

    @Test
    @DisplayName("findById - Debe retornar el cliente cuando el ID existe")
    public void findById_encontrado() {
        when(clienteRepository.findById(10)).thenReturn(Optional.of(CLIENTE_PREPARED));

        Cliente result = clienteService.findById(10);

        assertNotNull(result);
        assertEquals(10, result.getIdCliente());
        assertEquals("Juan Pérez", result.getNombre());
        System.out.println("[OK] findById retornó cliente id=" + result.getIdCliente());
    }

    @Test
    @DisplayName("findById - Debe lanzar excepción cuando el ID no existe")
    public void findById_noEncontrado() {
        when(clienteRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> clienteService.findById(99));
        System.out.println("[OK] findById lanzó BusinessException para id=99");
    }

    @Test
    @DisplayName("save - Debe guardar y retornar el cliente correctamente")
    public void saveCliente() {
        when(clienteRepository.existsBySessionToken("token-unico-abc123")).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(CLIENTE_PREPARED);

        Cliente result = clienteService.save(CLIENTE_DTO);

        assertNotNull(result);
        assertEquals("Juan Pérez", result.getNombre());
        verify(clienteRepository).save(any(Cliente.class));
        System.out.println("[OK] save guardó cliente: " + result.getNombre());
    }

    @Test
    @DisplayName("save - Debe lanzar excepción cuando el sessionToken ya existe")
    public void saveCliente_tokenDuplicado() {
        when(clienteRepository.existsBySessionToken("token-unico-abc123")).thenReturn(true);

        assertThrows(BusinessException.class, () -> clienteService.save(CLIENTE_DTO));
        verify(clienteRepository, never()).save(any(Cliente.class));
        System.out.println("[OK] save lanzó CONFLICT por sessionToken duplicado");
    }

    @Test
    @DisplayName("update - Debe actualizar y retornar el cliente correctamente")
    public void updateCliente() {
        when(clienteRepository.findById(10)).thenReturn(Optional.of(CLIENTE_PREPARED));
        when(clienteRepository.existsBySessionToken("token-modificado-xyz999")).thenReturn(false);
        Cliente actualizado = Cliente.builder().idCliente(10).uuidCliente("uuid-cli-10")
                .sessionToken("token-modificado-xyz999").nombre("Juan Pérez").build();
        when(clienteRepository.save(any(Cliente.class))).thenReturn(actualizado);

        Cliente result = clienteService.update(10, CLIENTE_DTO_MODIFIED);

        assertNotNull(result);
        assertEquals("token-modificado-xyz999", result.getSessionToken());
        System.out.println("[OK] update actualizó cliente: sessionToken=" + result.getSessionToken());
    }

    @Test
    @DisplayName("delete - Debe eliminar el cliente cuando el ID existe")
    public void deleteCliente() {
        when(clienteRepository.existsById(10)).thenReturn(true);

        assertDoesNotThrow(() -> clienteService.delete(10));
        verify(clienteRepository).deleteById(10);
        System.out.println("[OK] delete eliminó cliente id=10");
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción cuando el ID no existe")
    public void deleteCliente_noEncontrado() {
        when(clienteRepository.existsById(99)).thenReturn(false);

        assertThrows(BusinessException.class, () -> clienteService.delete(99));
        verify(clienteRepository, never()).deleteById(anyInt());
        System.out.println("[OK] delete lanzó BusinessException para id=99");
    }
}
