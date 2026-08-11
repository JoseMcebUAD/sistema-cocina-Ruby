package com.archivomodulo;

import com.cloudinary.Api;
import com.cloudinary.Cloudinary;
import com.cocinarubi.dao.ArchivoModuloRepository;
import com.cocinarubi.domain.entity.ArchivoModulo;
import com.cocinarubi.domain.entity.Categoria;
import com.cocinarubi.domain.service.files.ArchivoModuloCache;
import com.cocinarubi.domain.service.files.ArchivoModuloService;
import com.cocinarubi.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ArchivoModuloServiceTest {

    @Mock
    private ArchivoModuloRepository archivoModuloRepository;

    @Mock
    private ArchivoModuloCache archivoModuloCache;

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Api cloudinaryApi;

    @InjectMocks
    private ArchivoModuloService archivoModuloService;

    private final Categoria HELADO = Categoria.builder().idCategoria(5).nombre("HELADO").build();

    @BeforeEach
    void stubCloudinary() {
        // Solo stub cuando se use realmente (evita "unnecessary stubbings" en lenient=false)
        lenient().when(cloudinary.api()).thenReturn(cloudinaryApi);
    }

    @Test
    @DisplayName("crearParaCategoria - persiste el módulo con ruta lowercase y refresca el cache")
    public void crearParaCategoria_ok() throws Exception {
        ArgumentCaptor<ArchivoModulo> captor = ArgumentCaptor.forClass(ArchivoModulo.class);
        when(archivoModuloRepository.save(any(ArchivoModulo.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        archivoModuloService.crearParaCategoria(HELADO);

        verify(archivoModuloRepository).save(captor.capture());
        ArchivoModulo persistido = captor.getValue();
        assertEquals("HELADO", persistido.getNombreModulo());
        assertEquals("cocina_rubi/helado", persistido.getRuta());
        assertNull(persistido.getTipoCatalogoProducto());
        assertEquals(HELADO, persistido.getCategoria());

        verify(archivoModuloCache).refresh();
        verify(cloudinaryApi).createFolder(eq("cocina_rubi/helado"), any());
        System.out.println("[OK] crearParaCategoria persistió y refrescó cache");
    }

    @Test
    @DisplayName("crearParaCategoria - si Cloudinary falla, la operación sigue best-effort (no aborta)")
    public void crearParaCategoria_cloudinaryFalla_noAborta() throws Exception {
        when(archivoModuloRepository.save(any(ArchivoModulo.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(cloudinaryApi.createFolder(anyString(), any()))
                .thenThrow(new RuntimeException("cloudinary caído"));

        assertDoesNotThrow(() -> archivoModuloService.crearParaCategoria(HELADO));
        verify(archivoModuloCache).refresh();
        System.out.println("[OK] fallo de Cloudinary no aborta la creación");
    }

    @Test
    @DisplayName("actualizarRutaParaCategoria - cambia ruta y nombre si difiere")
    public void actualizarRuta_cambiaSiDifiere() throws Exception {
        ArchivoModulo existente = ArchivoModulo.builder()
                .idArchivoModulo(20)
                .nombreModulo("HELADO")
                .categoria(HELADO)
                .ruta("cocina_rubi/helado")
                .archivosAceptados("[]")
                .build();

        Categoria renombrada = Categoria.builder().idCategoria(5).nombre("HELADOS").build();
        when(archivoModuloRepository.findByCategoria_IdCategoria(5)).thenReturn(Optional.of(existente));
        when(archivoModuloRepository.save(any(ArchivoModulo.class))).thenAnswer(inv -> inv.getArgument(0));

        archivoModuloService.actualizarRutaParaCategoria(renombrada);

        assertEquals("cocina_rubi/helados", existente.getRuta());
        assertEquals("HELADOS", existente.getNombreModulo());
        verify(archivoModuloRepository).save(existente);
        verify(archivoModuloCache).refresh();
        System.out.println("[OK] actualizarRuta persistió el nuevo path");
    }

    @Test
    @DisplayName("actualizarRutaParaCategoria - no persiste ni refresca si la ruta no cambió")
    public void actualizarRuta_sinCambio_noHaceNada() {
        ArchivoModulo existente = ArchivoModulo.builder()
                .idArchivoModulo(20)
                .nombreModulo("HELADO")
                .categoria(HELADO)
                .ruta("cocina_rubi/helado")
                .archivosAceptados("[]")
                .build();
        when(archivoModuloRepository.findByCategoria_IdCategoria(5)).thenReturn(Optional.of(existente));

        archivoModuloService.actualizarRutaParaCategoria(HELADO);

        verify(archivoModuloRepository, never()).save(any());
        verify(archivoModuloCache, never()).refresh();
        System.out.println("[OK] actualizarRuta sin cambio es no-op");
    }

    @Test
    @DisplayName("actualizarRutaParaCategoria - lanza 404 si no existe el módulo")
    public void actualizarRuta_noEncontrado() {
        when(archivoModuloRepository.findByCategoria_IdCategoria(99)).thenReturn(Optional.empty());

        Categoria inexistente = Categoria.builder().idCategoria(99).nombre("X").build();
        BusinessException ex = assertThrows(BusinessException.class,
                () -> archivoModuloService.actualizarRutaParaCategoria(inexistente));
        assertEquals(404, ex.getHttpStatus().value());
        System.out.println("[OK] actualizarRuta lanzó 404 sin módulo");
    }

    @Test
    @DisplayName("eliminarParaCategoria - borra el módulo y refresca cache")
    public void eliminarParaCategoria_ok() {
        ArchivoModulo existente = ArchivoModulo.builder()
                .idArchivoModulo(20)
                .categoria(HELADO)
                .ruta("cocina_rubi/helado")
                .build();
        when(archivoModuloRepository.findByCategoria_IdCategoria(5)).thenReturn(Optional.of(existente));

        archivoModuloService.eliminarParaCategoria(5);

        verify(archivoModuloRepository).delete(existente);
        verify(archivoModuloCache).refresh();
        System.out.println("[OK] eliminarParaCategoria borró y refrescó cache");
    }

    @Test
    @DisplayName("eliminarParaCategoria - idempotente si el módulo no existe")
    public void eliminarParaCategoria_idempotente() {
        when(archivoModuloRepository.findByCategoria_IdCategoria(99)).thenReturn(Optional.empty());
        assertDoesNotThrow(() -> archivoModuloService.eliminarParaCategoria(99));
        verify(archivoModuloRepository, never()).delete(any(ArchivoModulo.class));
        verify(archivoModuloCache, never()).refresh();
        System.out.println("[OK] eliminarParaCategoria sin módulo no falla");
    }
}
