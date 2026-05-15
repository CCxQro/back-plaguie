package itesm.mx.application.mapper.reporte;

import itesm.mx.application.dto.GetReportePredictivoPlagasResponseDto;
import itesm.mx.application.dto.HotspotItemDto;
import itesm.mx.application.dto.PrediccionPlagaItemDto;
import itesm.mx.domain.models.reporte.Hotspot;
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

        List<HotspotItemDto> hotspotItems = reporte.getHotspots() == null
                ? List.of()
                : reporte.getHotspots().stream().map(ReportePredictivoDtoMapper::toHotspotDto).toList();

        List<String> recommendations = reporte.getRecomendaciones() == null
                ? List.of()
                : reporte.getRecomendaciones();

        return new GetReportePredictivoPlagasResponseDto(
                reporte.getRegion(),
                reporte.getTemporada() != null ? reporte.getTemporada().getDisplayName() : null,
                reporte.getGeneradoEn() != null ? reporte.getGeneradoEn().format(ISO_FORMATTER) : null,
                reporte.getObservacionesAnalizadas(),
                reporte.getResumenEjecutivo(),
                items,
                hotspotItems,
                recommendations
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

    private static HotspotItemDto toHotspotDto(Hotspot hotspot) {
        return new HotspotItemDto(
                hotspot.getMunicipio(),
                hotspot.getEstado(),
                hotspot.getObservaciones(),
                hotspot.getPlagasDistintas(),
                hotspot.getNivelRiesgo()
        );
    }
}
