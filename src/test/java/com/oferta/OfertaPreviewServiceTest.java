package com.oferta;

import com.cocinarubi.DBConstants.ModoMatchOferta;
import com.cocinarubi.DBConstants.TipoComida;
import com.cocinarubi.dao.BasicoRepository;
import com.cocinarubi.dao.ComboRepository;
import com.cocinarubi.dao.ComidaRepository;
import com.cocinarubi.dao.ProductoCocinaRepository;
import com.cocinarubi.domain.entity.Oferta;
import com.cocinarubi.domain.entity.OfertaLinea;
import com.cocinarubi.domain.service.OfertaMatcherService;
import com.cocinarubi.domain.service.OfertaPreviewService;
import com.cocinarubi.domain.service.OfertaService;
import com.cocinarubi.presentation.dto.request.OfertaPreviewRequestDTO;
import com.cocinarubi.presentation.dto.response.OfertaPreviewResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cocinarubi.Constants;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios de {@link OfertaPreviewService}.
 * Sin Spring context — verifica que el preview respete vigencia de fechas y zona horaria.
 */
@ExtendWith(MockitoExtension.class)
public class OfertaPreviewServiceTest {

    @Mock private OfertaService ofertaService;
    @Mock private OfertaMatcherService ofertaMatcher;
    @Mock private ComidaRepository comidaRepository;
    @Mock private ProductoCocinaRepository productoCocinaRepository;
    @Mock private BasicoRepository basicoRepository;
    @Mock private ComboRepository comboRepository;

    private OfertaPreviewService buildService() {
        return new OfertaPreviewService(ofertaService, ofertaMatcher,
                comidaRepository, productoCocinaRepository, basicoRepository, comboRepository);
    }

    private OfertaPreviewRequestDTO emptyDto() {
        OfertaPreviewRequestDTO dto = new OfertaPreviewRequestDTO();
        dto.setComidas(List.of());
        dto.setBasicos(List.of());
        dto.setProductosCocina(List.of());
        dto.setCombos(List.of());
        return dto;
    }

    private Oferta ofertaVigente() {
        LocalDate hoy = LocalDate.now(Constants.ZONA_MERIDA);
        OfertaLinea linea = OfertaLinea.builder()
                .modoMatch(ModoMatchOferta.COMIDA_POR_TIPO)
                .tipoComidaFiltro(TipoComida.FIJA)
                .cantidad(1).build();
        Oferta o = Oferta.builder()
                .idOferta(1).nombre("Oferta Vigente").activo(true)
                .precioOferta(BigDecimal.valueOf(55)).prioridad(5)
                .fechaInicio(hoy.minusDays(1)).fechaFin(hoy.plusDays(30)).build();
        o.addLinea(linea);
        return o;
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("preview - sin ofertas vigentes → matcher no se invoca")
    void preview_sinOfertasVigentes_matcherNoInvocado() {
        when(ofertaService.findActivasEntidades(any(LocalDate.class))).thenReturn(List.of());

        List<OfertaPreviewResponseDTO> result = buildService().preview(emptyDto());

        assertTrue(result.isEmpty());
        verify(ofertaMatcher, never()).detectar(any(), any(), any(), any(), any());
        System.out.println("[OK] preview sin ofertas vigentes → matcher no invocado");
    }

    @Test
    @DisplayName("preview - con oferta vigente → matcher se invoca con esa oferta")
    void preview_conOfertaVigente_matcherInvocado() {
        when(ofertaService.findActivasEntidades(any(LocalDate.class)))
                .thenReturn(List.of(ofertaVigente()));
        when(ofertaMatcher.detectar(any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        buildService().preview(emptyDto());

        verify(ofertaMatcher).detectar(any(), any(), any(), any(), eq(List.of(ofertaVigente())));
        System.out.println("[OK] preview con oferta vigente → matcher invocado");
    }

    @Test
    @DisplayName("preview - fecha pasada a findActivasEntidades usa zona America/Merida")
    void preview_fechaUsaZonaMerida() {
        ArgumentCaptor<LocalDate> captor = ArgumentCaptor.forClass(LocalDate.class);
        when(ofertaService.findActivasEntidades(captor.capture())).thenReturn(List.of());

        buildService().preview(emptyDto());

        LocalDate esperado = LocalDate.now(Constants.ZONA_MERIDA);
        assertEquals(esperado, captor.getValue(),
                "La fecha debe calcularse con zona America/Merida, no UTC ni LocalDate.now()");
        System.out.println("[OK] preview usa zona Merida → fecha=" + captor.getValue());
    }
}
