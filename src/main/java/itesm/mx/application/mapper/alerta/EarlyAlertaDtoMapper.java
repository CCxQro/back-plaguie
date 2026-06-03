package itesm.mx.application.mapper.alerta;

import itesm.mx.application.dto.GetEarlyAlertaResponseDto;
import itesm.mx.domain.models.alerta.Alerta;

import java.time.LocalDateTime;

public final class EarlyAlertaDtoMapper {

    private EarlyAlertaDtoMapper() {
    }

    public static GetEarlyAlertaResponseDto toResponseDto(Alerta alerta) {
        LocalDateTime createdAt = alerta.getCreatedAt();
        LocalDateTime validatedAt = alerta.getValidatedAt();
        return new GetEarlyAlertaResponseDto(
                alerta.getAlertaId(),
                alerta.getTitulo(),
                alerta.getDescripcion(),
                alerta.getUbicacionId(),
                alerta.getStateId(),
                alerta.getStateName(),
                alerta.getLatitude(),
                alerta.getLongitude(),
                alerta.getDistanceKm(),
                alerta.getTipoPlaga(),
                alerta.getHectareas(),
                alerta.getSeveridad(),
                alerta.getReportedByUserId(),
                createdAt != null ? createdAt.toString() : null,
                alerta.getStatusId(),
                alerta.getStatusName(),
                alerta.getValidatedByUserId(),
                validatedAt != null ? validatedAt.toString() : null
        );
    }
}
