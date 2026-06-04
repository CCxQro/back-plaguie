package itesm.mx.application.usecase.alerta;

import itesm.mx.application.dto.GetAlertaResponseDto;
import itesm.mx.domain.models.alerta.Alerta;
import itesm.mx.domain.models.location.Location;
import itesm.mx.domain.models.parcela.EstadoParcela;
import itesm.mx.domain.models.parcela.Parcela;
import itesm.mx.domain.models.parcela.ParcelaSuggestion;
import itesm.mx.domain.repository.alerta.AlertaRepository;
import itesm.mx.domain.repository.parcela.ParcelaRepository;
import itesm.mx.domain.repository.parcela.ParcelaSuggestionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test verifying HU-15 / SCRUM-175-177 requirement:
 * When a pest alert is validated and approved (statusId=1),
 * suggestions are generated for all parcelas within the zone.
 */
@ExtendWith(MockitoExtension.class)
class ValidateAlertaSuggestionTest {

    private static final GeometryFactory GF = new GeometryFactory();

    @Mock AlertaRepository alertaRepository;
    @Mock ParcelaRepository parcelaRepository;
    @Mock ParcelaSuggestionRepository parcelaSuggestionRepository;
    @Mock GenerateParcelaSuggestionsUseCase generateParcelaSuggestionsUseCase;

    @InjectMocks ValidateAlertaUseCase validateAlertaUseCase;

    private Alerta pendingAlerta(long id, double lat, double lon) {
        Alerta a = new Alerta();
        a.setAlertaId(id);
        a.setTipoPlaga("Gusano cogollero");
        a.setTitulo("Alerta Test");
        a.setStatusId(2L); // pending
        a.setLatitude(lat);
        a.setLongitude(lon);
        a.setCreatedAt(LocalDateTime.now());
        return a;
    }

    @Test
    void validate_WhenApproved_CallsGenerateSuggestions() {
        Alerta alerta = pendingAlerta(10L, 20.58, -100.38);
        when(alertaRepository.findAlertaById(10L)).thenReturn(Optional.of(alerta));

        Alerta updated = pendingAlerta(10L, 20.58, -100.38);
        updated.setStatusId(1L);
        when(alertaRepository.update(any())).thenReturn(updated);

        GetAlertaResponseDto response = validateAlertaUseCase.execute(10L, 1L, 1L);

        assertEquals(1L, response.statusId);
        verify(generateParcelaSuggestionsUseCase, times(1)).execute(any(Alerta.class));
    }

    @Test
    void validate_WhenRejected_DoesNotGenerateSuggestions() {
        Alerta alerta = pendingAlerta(11L, 20.58, -100.38);
        when(alertaRepository.findAlertaById(11L)).thenReturn(Optional.of(alerta));

        Alerta updated = pendingAlerta(11L, 20.58, -100.38);
        updated.setStatusId(3L); // rejected
        when(alertaRepository.update(any())).thenReturn(updated);

        GetAlertaResponseDto response = validateAlertaUseCase.execute(11L, 3L, 1L);

        assertEquals(3L, response.statusId);
        verify(generateParcelaSuggestionsUseCase, never()).execute(any());
    }

    @Test
    void validate_AlertNotFound_ThrowsIllegalState() {
        when(alertaRepository.findAlertaById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class, () -> validateAlertaUseCase.execute(99L, 1L, 1L));
    }

    @Test
    void validate_InvalidStatus_ThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> validateAlertaUseCase.execute(1L, 2L, 1L));
    }
}
