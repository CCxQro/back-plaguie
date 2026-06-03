package itesm.mx.application.usecase;

import itesm.mx.application.dto.GetEarlyAlertaResponseDto;
import itesm.mx.application.usecase.region.GetRecentEarlyAlertsUseCase;
import itesm.mx.domain.models.alerta.Alerta;
import itesm.mx.domain.repository.alerta.AlertaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetRecentEarlyAlertsUseCaseTest {

    @Mock
    private AlertaRepository alertaRepository;

    @InjectMocks
    private GetRecentEarlyAlertsUseCase useCase;

    private Alerta sampleAlerta() {
        Alerta a = new Alerta(1L, "Brote", "desc", 10L, "Pulgón",
                null, "critico", 2L, LocalDateTime.now(), 1L, "Aceptado", 3L, LocalDateTime.now());
        a.setStateId(2L);
        a.setStateName("Michoacán");
        return a;
    }

    @Test
    void execute_ReturnsMappedAlertsEnrichedWithState() {
        when(alertaRepository.findValidatedSince(any(LocalDateTime.class)))
                .thenReturn(List.of(sampleAlerta()));

        List<GetEarlyAlertaResponseDto> result = useCase.execute();

        assertEquals(1, result.size());
        assertEquals("Brote", result.get(0).titulo);
        assertEquals(2L, result.get(0).stateId);
        assertEquals("Michoacán", result.get(0).stateName);
        verify(alertaRepository).findValidatedSince(any(LocalDateTime.class));
    }

    @Test
    void execute_NoAlerts_ReturnsEmpty() {
        when(alertaRepository.findValidatedSince(any(LocalDateTime.class))).thenReturn(List.of());
        assertTrue(useCase.execute().isEmpty());
    }
}
