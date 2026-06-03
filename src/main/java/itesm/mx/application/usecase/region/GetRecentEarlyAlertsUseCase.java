package itesm.mx.application.usecase.region;

import itesm.mx.application.dto.GetEarlyAlertaResponseDto;
import itesm.mx.application.mapper.alerta.EarlyAlertaDtoMapper;
import itesm.mx.domain.repository.alerta.AlertaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Returns ALL validated pest alerts created within the last
 * {@link #EARLY_ALERT_WINDOW_MONTHS} months, across every state, each enriched
 * with its state id/name (HU-26 CA-02).
 *
 * The seller gets access to every recent early alert and filters them on the
 * client (by region of interest, severity, pest type, etc.).
 */
@ApplicationScoped
public class GetRecentEarlyAlertsUseCase {

    /** Early alerts only consider validated alerts from the last N months. */
    static final int EARLY_ALERT_WINDOW_MONTHS = 3;

    @Inject
    AlertaRepository alertaRepository;

    public List<GetEarlyAlertaResponseDto> execute() {
        LocalDateTime since = LocalDateTime.now().minusMonths(EARLY_ALERT_WINDOW_MONTHS);
        return alertaRepository.findValidatedSince(since)
                .stream()
                .map(EarlyAlertaDtoMapper::toResponseDto)
                .toList();
    }
}
