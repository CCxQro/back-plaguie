package itesm.mx.application.usecase.reporte;

import itesm.mx.domain.models.reporte.HistoricoVigilanciaSummary;
import itesm.mx.domain.models.reporte.PrediccionPlaga;
import itesm.mx.domain.models.reporte.ReportePredictivoPlagas;
import itesm.mx.domain.models.reporte.Temporada;
import itesm.mx.domain.repository.reporte.HistoricoVigilanciaRepository;
import itesm.mx.domain.repository.reporte.PrediccionPlagaProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerarReportePredictivoPlagasUseCaseTest {

    @Mock
    HistoricoVigilanciaRepository historicoVigilanciaRepository;

    @Mock
    PrediccionPlagaProvider prediccionPlagaProvider;

    GenerarReportePredictivoPlagasUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GenerarReportePredictivoPlagasUseCase();
        useCase.historicoVigilanciaRepository = historicoVigilanciaRepository;
        useCase.prediccionPlagaProvider = prediccionPlagaProvider;
    }

    @Test
    void execute_WhenRegionIsBlank_ThrowsIllegalArgument() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> useCase.execute("  ", "verano"));
        assertTrue(ex.getMessage().toLowerCase().contains("region"));
    }

    @Test
    void execute_WhenTemporadaIsInvalid_ThrowsIllegalArgument() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> useCase.execute("Jalisco", "no-existe"));
        assertTrue(ex.getMessage().contains("Temporada invalida"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_WhenValid_AggregatesObservacionesAndDelegatesToProvider() {
        List<HistoricoVigilanciaSummary> historico = List.of(
                new HistoricoVigilanciaSummary("Pulgon", "Maiz", "Zea mays", "Jalisco", "Zapopan", 12L, new BigDecimal("5.50")),
                new HistoricoVigilanciaSummary("Trips", "Chile", "Capsicum", "Jalisco", "Tlaquepaque", 8L, new BigDecimal("3.00"))
        );
        when(historicoVigilanciaRepository.findResumenPorRegionYTemporada(eq("Jalisco"), eq(Temporada.VERANO)))
                .thenReturn(historico);

        PrediccionPlaga pred = new PrediccionPlaga("Pulgon", 75, "Verano", "Alto", "Maiz", "alta presion", "Neonic A");
        when(prediccionPlagaProvider.predict(anyString(), any(), any()))
                .thenReturn(new PrediccionPlagaProvider.PrediccionResult("resumen", List.of(pred)));

        ReportePredictivoPlagas result = useCase.execute("Jalisco", "verano");

        ArgumentCaptor<List<HistoricoVigilanciaSummary>> historicoCaptor = ArgumentCaptor.forClass(List.class);
        verify(prediccionPlagaProvider).predict(eq("Jalisco"), eq(Temporada.VERANO), historicoCaptor.capture());
        assertEquals(2, historicoCaptor.getValue().size());

        assertEquals("Jalisco", result.getRegion());
        assertEquals(Temporada.VERANO, result.getTemporada());
        assertEquals(20L, result.getObservacionesAnalizadas());
        assertEquals("resumen", result.getResumenEjecutivo());
        assertEquals(1, result.getPredicciones().size());
        assertEquals("Pulgon", result.getPredicciones().get(0).getPlagaNombre());
        assertNotNull(result.getGeneradoEn());
    }

    @Test
    void execute_AcceptsTemporadaWithAccents() {
        when(historicoVigilanciaRepository.findResumenPorRegionYTemporada(eq("Sonora"), eq(Temporada.OTONO)))
                .thenReturn(List.of());
        when(prediccionPlagaProvider.predict(anyString(), any(), any()))
                .thenReturn(new PrediccionPlagaProvider.PrediccionResult("sin datos", List.of()));

        ReportePredictivoPlagas result = useCase.execute("Sonora", "Otoño");

        assertEquals(Temporada.OTONO, result.getTemporada());
        assertEquals(0L, result.getObservacionesAnalizadas());
    }
}
