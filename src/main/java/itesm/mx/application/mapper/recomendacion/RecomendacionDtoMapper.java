package itesm.mx.application.mapper.recomendacion;

import itesm.mx.application.dto.CreateRecomendacionDto;
import itesm.mx.application.dto.GetRecomendacionResponseDto;
import itesm.mx.domain.models.recomendacion.Recomendacion;

import java.time.LocalDateTime;

public final class RecomendacionDtoMapper {

    private RecomendacionDtoMapper() {
    }

    public static Recomendacion toDomain(CreateRecomendacionDto dto, Long reportedByUserId) {
        Recomendacion recomendacion = new Recomendacion();
        recomendacion.setTitulo(dto.titulo);
        recomendacion.setDescripcion(dto.descripcion);
        recomendacion.setTipoPlaga(dto.tipoPlaga);
        recomendacion.setProductosRecomendados(dto.productosRecomendados);
        recomendacion.setReportedByUserId(reportedByUserId);
        recomendacion.setCreatedAt(LocalDateTime.now());
        recomendacion.setStatusId(2L); // Default: Revision (pending validation)
        return recomendacion;
    }

    public static GetRecomendacionResponseDto toResponseDto(Recomendacion recomendacion) {
        LocalDateTime createdAt = recomendacion.getCreatedAt();
        LocalDateTime validatedAt = recomendacion.getValidatedAt();
        return new GetRecomendacionResponseDto(
                recomendacion.getRecomendacionId(),
                recomendacion.getTitulo(),
                recomendacion.getDescripcion(),
                recomendacion.getTipoPlaga(),
                recomendacion.getProductosRecomendados(),
                recomendacion.getReportedByUserId(),
                createdAt != null ? createdAt.toString() : null,
                recomendacion.getStatusId(),
                recomendacion.getStatusName(),
                recomendacion.getValidatedByUserId(),
                validatedAt != null ? validatedAt.toString() : null
        );
    }
}
