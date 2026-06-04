package itesm.mx.application.mapper.reporte;

import itesm.mx.application.dto.GetPestMapPointDto;
import itesm.mx.domain.models.reporte.PestMapPoint;

import java.time.LocalDateTime;

public final class PestMapDtoMapper {

    private PestMapDtoMapper() {
    }

    public static GetPestMapPointDto toResponseDto(PestMapPoint point) {
        LocalDateTime validatedAt = point.getValidatedAt();
        return new GetPestMapPointDto(
                point.getVigilanciaId(),
                point.getLatitude() != null ? point.getLatitude().doubleValue() : null,
                point.getLongitude() != null ? point.getLongitude().doubleValue() : null,
                point.getPlagaNombre(),
                point.getHospedanteNombre(),
                point.getEspecieNombre(),
                point.getEstadoNombre(),
                point.getMunicipioNombre(),
                point.getAhosp(),
                validatedAt != null ? validatedAt.toString() : null
        );
    }
}
