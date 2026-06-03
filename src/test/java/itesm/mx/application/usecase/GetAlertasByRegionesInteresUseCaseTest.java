package itesm.mx.application.usecase;

import itesm.mx.application.dto.GetAlertaResponseDto;
import itesm.mx.application.usecase.region.GetAlertasByRegionesInteresUseCase;
import itesm.mx.domain.models.alerta.Alerta;
import itesm.mx.domain.models.region.RegionInteres;
import itesm.mx.domain.repository.alerta.AlertaRepository;
import itesm.mx.domain.repository.region.RegionInteresRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAlertasByRegionesInteresUseCaseTest {

    @Mock
    private RegionInteresRepository regionInteresRepository;

    @Mock
    private AlertaRepository alertaRepository;

    @InjectMocks
    private GetAlertasByRegionesInteresUseCase useCase;

    private Alerta sampleAlerta() {
        return new Alerta(1L, "Brote", "desc", 10L, "Pulgón",
                null, "critico", 2L, LocalDateTime.now(), 1L, "Aceptado", 3L, LocalDateTime.now());
    }

    @Test
    void execute_NoRegions_ReturnsEmptyAndSkipsAlertQuery() {
        when(regionInteresRepository.findByUserId(7L)).thenReturn(List.of());

        List<GetAlertaResponseDto> result = useCase.execute(7L);

        assertTrue(result.isEmpty());
        verify(alertaRepository, never()).findValidatedByStateIds(anyList());
    }

    @Test
    void execute_WithRegions_ReturnsMappedAlerts() {
        when(regionInteresRepository.findByUserId(7L)).thenReturn(List.of(
                new RegionInteres(1L, 7L, 5L, "Michoacán", LocalDateTime.now()),
                new RegionInteres(2L, 7L, 8L, "Jalisco", LocalDateTime.now())
        ));
        when(alertaRepository.findValidatedByStateIds(List.of(5L, 8L)))
                .thenReturn(List.of(sampleAlerta()));

        List<GetAlertaResponseDto> result = useCase.execute(7L);

        assertEquals(1, result.size());
        assertEquals("Brote", result.get(0).titulo);
        verify(alertaRepository).findValidatedByStateIds(List.of(5L, 8L));
    }
}
