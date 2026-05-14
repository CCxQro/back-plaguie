package itesm.mx.application.mapper.alerta;

import itesm.mx.application.dto.CreateAlertaDto;
import itesm.mx.application.dto.GetAlertaResponseDto;
import itesm.mx.domain.models.alerta.Alerta;

import java.time.LocalDateTime;

public final class AlertaDtoMapper {

    private AlertaDtoMapper() {
    }

    public static Alerta toDomain(CreateAlertaDto dto, Long reportedByUserId) {
        Alerta alerta = new Alerta();
        alerta.setTitulo(dto.titulo);
        alerta.setDescripcion(dto.descripcion);
        alerta.setUbicacionId(dto.ubicacionId);
        alerta.setTipoPlaga(dto.tipoPlaga);
        alerta.setHectareas(dto.hectareas);
        alerta.setSeveridad(dto.severidad);
        alerta.setReportedByUserId(reportedByUserId);
        alerta.setCreatedAt(LocalDateTime.now());
        alerta.setStatusId(2L); // Default: Revision (pending validation)
        return alerta;
    }

    public static GetAlertaResponseDto toResponseDto(Alerta alerta) {
        LocalDateTime createdAt = alerta.getCreatedAt();
        LocalDateTime validatedAt = alerta.getValidatedAt();
        return new GetAlertaResponseDto(
                alerta.getAlertaId(),
                alerta.getTitulo(),
                alerta.getDescripcion(),
                alerta.getUbicacionId(),
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
