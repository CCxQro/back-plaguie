package itesm.mx.application.mapper.reporte;

import itesm.mx.application.dto.GetReportePredictivoPlagasResponseDto;
import itesm.mx.application.dto.PrediccionPlagaItemDto;
import itesm.mx.domain.models.reporte.PrediccionPlaga;
import itesm.mx.domain.models.reporte.ReportePredictivoPlagas;

import java.time.format.DateTimeFormatter;
import java.util.List;

public final class ReportePredictivoDtoMapper {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private ReportePredictivoDtoMapper() {
    }

    public static GetReportePredictivoPlagasResponseDto toResponseDto(ReportePredictivoPlagas reporte) {
        if (reporte == null) {
            return null;
        }
        List<PrediccionPlagaItemDto> items = reporte.getPredicciones() == null
                ? List.of()
                : reporte.getPredicciones().stream().map(ReportePredictivoDtoMapper::toItemDto).toList();

        return new GetReportePredictivoPlagasResponseDto(
                reporte.getRegion(),
                reporte.getTemporada() != null ? reporte.getTemporada().getDisplayName() : null,
                reporte.getGeneradoEn() != null ? reporte.getGeneradoEn().format(ISO_FORMATTER) : null,
                reporte.getObservacionesAnalizadas(),
                reporte.getResumenEjecutivo(),
                items
        );
    }

    private static PrediccionPlagaItemDto toItemDto(PrediccionPlaga prediccion) {
        return new PrediccionPlagaItemDto(
                prediccion.getPlagaNombre(),
                prediccion.getProbabilidad(),
                prediccion.getPeriodoEstimado(),
                prediccion.getNivelRiesgo(),
                prediccion.getHospedanteAfectado(),
                prediccion.getJustificacion(),
                prediccion.getProductoSugerido()
        );
    }
}
