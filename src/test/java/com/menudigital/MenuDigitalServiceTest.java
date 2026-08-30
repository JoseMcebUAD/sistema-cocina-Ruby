package com.menudigital;

import com.cocinarubi.DBConstants.Estatus;
import com.cocinarubi.dao.BasicoRepository;
import com.cocinarubi.dao.CategoriaRepository;
import com.cocinarubi.dao.ComidaRepository;
import com.cocinarubi.dao.ComplementoRepository;
import com.cocinarubi.dao.ProductoCocinaRepository;
import com.cocinarubi.domain.entity.Categoria;
import com.cocinarubi.domain.entity.ProductoCocina;
import com.cocinarubi.domain.service.MenuDigitalService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MenuDigitalServiceTest {

    @Mock private ComidaRepository comidaRepository;
    @Mock private BasicoRepository basicoRepository;
    @Mock private ComplementoRepository complementoRepository;
    @Mock private ProductoCocinaRepository productoCocinaRepository;
    @Mock private CategoriaRepository categoriaRepository;

    @InjectMocks
    private MenuDigitalService menuDigitalService;

    // Seeder V23: BEBIDA=1, CHAROLA=2, SNACK=3, POSTRE=4
    private final Categoria BEBIDA = Categoria.builder().idCategoria(1).nombre("BEBIDA").build();
    private final Categoria SNACK  = Categoria.builder().idCategoria(3).nombre("SNACK").build();
    private final Categoria HELADO = Categoria.builder().idCategoria(5).nombre("HELADO").build();

    private ProductoCocina producto(int id, String nombre, Categoria cat, BigDecimal precio) {
        return ProductoCocina.builder()
                .idProductoCocina(id)
                .uuidProductoCocina("uuid-" + id)
                .nombreProducto(nombre)
                .precioDomicilio(precio)
                .precioNormal(precio)
                .estatus(Estatus.DISPONIBLE)
                .destacado(false)
                .categoria(cat)
                .build();
    }

    @Test
    @DisplayName("generarMenu - las categorías seed conservan sus headers custom")
    public void seed_conservaFormatoCustom() {
        when(comidaRepository.findDisponiblesOrdenados(Estatus.DISPONIBLE)).thenReturn(List.of());
        when(basicoRepository.findDisponiblesOrdenados(Estatus.DISPONIBLE)).thenReturn(List.of());
        when(complementoRepository.findDisponiblesOrdenados(Estatus.DISPONIBLE)).thenReturn(List.of());
        when(productoCocinaRepository.findDisponiblesOrdenadosConSubcategoria(Estatus.DISPONIBLE)).thenReturn(List.of(
                producto(10, "Refresco", BEBIDA, BigDecimal.valueOf(20)),
                producto(11, "Papas", SNACK, BigDecimal.valueOf(25))
        ));
        when(categoriaRepository.findAll(any(Sort.class))).thenReturn(List.of(BEBIDA, SNACK));

        String menu = menuDigitalService.generarMenu();

        // "Bebidas" es el header custom de appendBebidas (no "BEBIDA")
        assertTrue(menu.contains("Bebidas\n"), "Debe conservar header custom 'Bebidas'");
        assertTrue(menu.contains("Refresco"));
        // Snacks agrupa por precio: header "*SNACK $..."
        assertTrue(menu.contains("*SNACK $25*"), "Debe conservar header agrupado por precio de SNACK");
        assertTrue(menu.contains("Papas"));
        System.out.println("[OK] menú seed conserva formato custom (Bebidas + *SNACK $x*)");
    }

    @Test
    @DisplayName("generarMenu - categoría dinámica (HELADO) aparece con plantilla genérica")
    public void dinamica_usaPlantillaGenerica() {
        when(comidaRepository.findDisponiblesOrdenados(Estatus.DISPONIBLE)).thenReturn(List.of());
        when(basicoRepository.findDisponiblesOrdenados(Estatus.DISPONIBLE)).thenReturn(List.of());
        when(complementoRepository.findDisponiblesOrdenados(Estatus.DISPONIBLE)).thenReturn(List.of());
        when(productoCocinaRepository.findDisponiblesOrdenadosConSubcategoria(Estatus.DISPONIBLE)).thenReturn(List.of(
                producto(20, "Cono chocolate", HELADO, BigDecimal.valueOf(30))
        ));
        when(categoriaRepository.findAll(any(Sort.class))).thenReturn(List.of(HELADO));

        String menu = menuDigitalService.generarMenu();

        // Plantilla genérica: "\nHELADO\n▪️Cono chocolate $30\n"
        assertTrue(menu.contains("HELADO"), "Debe contener el nombre de la categoría en mayúsculas");
        assertTrue(menu.contains("▪️Cono chocolate $30"),
                "Debe usar la plantilla genérica ▪️ para categorías nuevas");
        System.out.println("[OK] categoría dinámica HELADO renderizada con plantilla genérica");
    }
}
