package itesm.mx.application.usecase.reporte;

import itesm.mx.domain.models.reporte.ReportePredictivoPlagas;
import itesm.mx.domain.repository.reporte.ReportePredictivoExporter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ExportarReportePredictivoExcelUseCase {

    @Inject
    GenerarReportePredictivoPlagasUseCase generarReportePredictivoPlagasUseCase;

    @Inject
    ReportePredictivoExporter reportePredictivoExporter;

    public byte[] execute(String region, String temporada) {
        ReportePredictivoPlagas reporte = generarReportePredictivoPlagasUseCase.execute(region, temporada);
        return reportePredictivoExporter.toExcel(reporte);
    }
}
