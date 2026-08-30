package com.menuweb;

import com.cocinarubi.DBConstants.Estatus;
import com.cocinarubi.dao.BasicoRepository;
import com.cocinarubi.dao.ComidaRepository;
import com.cocinarubi.dao.DesayunoRepository;
import com.cocinarubi.dao.ProductoCocinaRepository;
import com.cocinarubi.domain.entity.ProductoCocina;
import com.cocinarubi.domain.service.PaqueteService;
import com.cocinarubi.domain.service.web.MenuWebService;
import com.cocinarubi.presentation.dto.response.MenuWebResponseDTO;
import com.testutil.PedidoMocks;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para {@link MenuWebService}.
 * Verifica que el método getMenu() agrega correctamente las 5 secciones del menú web
 * y que el agrupamiento de productos por categoría funciona sin N+1.
 * Capa: Service — test con Mockito, sin Spring context ni base de datos.
 */
@ExtendWith(MockitoExtension.class)
public class MenuWebServiceTest {

    @Mock private ComidaRepository comidaRepository;
    @Mock private BasicoRepository basicoRepository;
    @Mock private DesayunoRepository desayunoRepository;
    @Mock private PaqueteService paqueteService;
    @Mock private ProductoCocinaRepository productoCocinaRepository;

    @InjectMocks
    private MenuWebService menuWebService;

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getMenu - retorna las 5 secciones con el ítem correcto en cada una")
    public void getMenu_retornaTodosLosGrupos() {
        when(comidaRepository.findDisponiblesOrdenados(Estatus.DISPONIBLE))
                .thenReturn(List.of(PedidoMocks.comida()));
        when(basicoRepository.findDisponiblesOrdenados(Estatus.DISPONIBLE))
                .thenReturn(List.of(PedidoMocks.basicoConComida()));
        when(desayunoRepository.findDisponiblesOrdenados(Estatus.DISPONIBLE))
                .thenReturn(List.of(PedidoMocks.desayuno()));
        when(paqueteService.findDisponibles())
                .thenReturn(List.of(PedidoMocks.paqueteResponseDTO()));
        when(productoCocinaRepository.findDisponiblesOrdenadosConCategoria(Estatus.DISPONIBLE))
                .thenReturn(List.of(PedidoMocks.snack()));

        MenuWebResponseDTO result = menuWebService.getMenu();

        assertEquals(1, result.getComidas().size());
        assertEquals("Pollo en salsa roja", result.getComidas().get(0).getNombreComida());

        assertEquals(1, result.getBasicos().size());
        assertEquals("Pollo en salsa roja", result.getBasicos().get(0).getNombreComida());

        assertEquals(1, result.getDesayunos().size());
        assertEquals("Huevos con jamón", result.getDesayunos().get(0).getNombreDesayuno());

        assertEquals(1, result.getPaquetes().size());
        assertEquals(10, result.getPaquetes().get(0).getIdPaquete());

        assertEquals(1, result.getCategorias().size());
        assertEquals("SNACK", result.getCategorias().get(0).getNombre());

        System.out.println("[OK] getMenu retornó las 5 secciones con 1 ítem cada una");
    }

    @Test
    @DisplayName("getMenu - productos de 2 categorías distintas se agrupan en 2 CategoriaMenuDTO")
    public void getMenu_categorias_agrupaProductosDeDosCategoriasDistintas() {
        stubSinProductosCocina();
        // BEBIDA < SNACK alfabéticamente; el mock respeta ese orden como lo haría el repo
        when(productoCocinaRepository.findDisponiblesOrdenadosConCategoria(Estatus.DISPONIBLE))
                .thenReturn(List.of(PedidoMocks.bebida(), PedidoMocks.snack()));

        MenuWebResponseDTO result = menuWebService.getMenu();

        assertEquals(2, result.getCategorias().size());
        assertEquals("BEBIDA", result.getCategorias().get(0).getNombre());
        assertEquals(1, result.getCategorias().get(0).getProductos().size());
        assertEquals("SNACK", result.getCategorias().get(1).getNombre());
        assertEquals(1, result.getCategorias().get(1).getProductos().size());

        System.out.println("[OK] 2 categorías distintas → 2 CategoriaMenuDTO");
    }

    @Test
    @DisplayName("getMenu - 2 productos en la misma categoría se agrupan en 1 CategoriaMenuDTO")
    public void getMenu_categorias_dosProductosEnMismaCategoria_unaSolaCategoriaEnRespuesta() {
        stubSinProductosCocina();
        ProductoCocina palomitas = ProductoCocina.builder()
                .idProductoCocina(20)
                .uuidProductoCocina("uuid-snack-20")
                .nombreProducto("Palomitas")
                .precioDomicilio(BigDecimal.valueOf(25))
                .precioNormal(BigDecimal.valueOf(20))
                .estatus(Estatus.DISPONIBLE)
                .destacado(false)
                .categoria(PedidoMocks.CATEGORIA_SNACK)
                .build();
        when(productoCocinaRepository.findDisponiblesOrdenadosConCategoria(Estatus.DISPONIBLE))
                .thenReturn(List.of(PedidoMocks.snack(), palomitas));

        MenuWebResponseDTO result = menuWebService.getMenu();

        assertEquals(1, result.getCategorias().size());
        assertEquals("SNACK", result.getCategorias().get(0).getNombre());
        assertEquals(2, result.getCategorias().get(0).getProductos().size());

        System.out.println("[OK] 2 productos de SNACK → 1 CategoriaMenuDTO con 2 productos");
    }

    @Test
    @DisplayName("getMenu - básico con complemento se mapea correctamente en la respuesta")
    public void getMenu_basicoConComplemento_mapeoCorrectoEnRespuesta() {
        when(comidaRepository.findDisponiblesOrdenados(Estatus.DISPONIBLE)).thenReturn(List.of());
        when(basicoRepository.findDisponiblesOrdenados(Estatus.DISPONIBLE))
                .thenReturn(List.of(PedidoMocks.basicoConComplemento()));
        when(desayunoRepository.findDisponiblesOrdenados(Estatus.DISPONIBLE)).thenReturn(List.of());
        when(paqueteService.findDisponibles()).thenReturn(List.of());
        when(productoCocinaRepository.findDisponiblesOrdenadosConCategoria(Estatus.DISPONIBLE))
                .thenReturn(List.of());

        MenuWebResponseDTO result = menuWebService.getMenu();

        assertEquals(1, result.getBasicos().size());
        assertEquals(1, result.getBasicos().get(0).getComplementos().size());
        assertEquals("Arroz", result.getBasicos().get(0).getComplementos().get(0).getNombreComplemento());

        System.out.println("[OK] básico con complemento 'Arroz' mapeado correctamente");
    }

    @Test
    @DisplayName("getMenu - todas las secciones vacías retornan listas vacías, no null")
    public void getMenu_conTodasLasSecciones_sinNullsEnListas() {
        when(comidaRepository.findDisponiblesOrdenados(Estatus.DISPONIBLE)).thenReturn(List.of());
        when(basicoRepository.findDisponiblesOrdenados(Estatus.DISPONIBLE)).thenReturn(List.of());
        when(desayunoRepository.findDisponiblesOrdenados(Estatus.DISPONIBLE)).thenReturn(List.of());
        when(paqueteService.findDisponibles()).thenReturn(List.of());
        when(productoCocinaRepository.findDisponiblesOrdenadosConCategoria(Estatus.DISPONIBLE))
                .thenReturn(List.of());

        MenuWebResponseDTO result = menuWebService.getMenu();

        assertNotNull(result.getComidas(),   "comidas no debe ser null");
        assertNotNull(result.getBasicos(),   "basicos no debe ser null");
        assertNotNull(result.getDesayunos(), "desayunos no debe ser null");
        assertNotNull(result.getPaquetes(),  "paquetes no debe ser null");
        assertNotNull(result.getCategorias(),"categorias no debe ser null");
        assertTrue(result.getComidas().isEmpty());
        assertTrue(result.getCategorias().isEmpty());

        System.out.println("[OK] menú vacío retorna listas vacías en las 5 secciones, sin nulls");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Stub común para tests que no evalúan comidas, básicos, desayunos ni paquetes.
     * Evita "unnecessary stubbing" de Mockito configurando solo los repos irrelevantes.
     */
    private void stubSinProductosCocina() {
        when(comidaRepository.findDisponiblesOrdenados(Estatus.DISPONIBLE)).thenReturn(List.of());
        when(basicoRepository.findDisponiblesOrdenados(Estatus.DISPONIBLE)).thenReturn(List.of());
        when(desayunoRepository.findDisponiblesOrdenados(Estatus.DISPONIBLE)).thenReturn(List.of());
        when(paqueteService.findDisponibles()).thenReturn(List.of());
    }
}
