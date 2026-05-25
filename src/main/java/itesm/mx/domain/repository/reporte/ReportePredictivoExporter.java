package itesm.mx.domain.repository.reporte;

import itesm.mx.domain.models.reporte.ReportePredictivoPlagas;

public interface ReportePredictivoExporter {
    byte[] toPdf(ReportePredictivoPlagas reporte);

    byte[] toExcel(ReportePredictivoPlagas reporte);
}
