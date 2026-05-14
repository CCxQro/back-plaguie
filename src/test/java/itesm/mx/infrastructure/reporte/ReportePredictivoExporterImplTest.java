package itesm.mx.infrastructure.reporte;

import itesm.mx.domain.models.reporte.PrediccionPlaga;
import itesm.mx.domain.models.reporte.ReportePredictivoPlagas;
import itesm.mx.domain.models.reporte.Temporada;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportePredictivoExporterImplTest {

    private final ReportePredictivoExporterImpl exporter = new ReportePredictivoExporterImpl();

    private ReportePredictivoPlagas sampleReporte() {
        List<PrediccionPlaga> predicciones = List.of(
                new PrediccionPlaga("Pulgon", 80, "Junio-Julio", "Alto", "Maiz",
                        "Alta densidad observada en historico", "Imidacloprid 70%"),
                new PrediccionPlaga("Mosca blanca", 55, "Verano", "Medio", "Chile",
                        "Tendencia creciente en municipios cercanos", null)
        );
        return new ReportePredictivoPlagas(
                "Jalisco",
                Temporada.VERANO,
                LocalDateTime.of(2026, 6, 15, 10, 30),
                42L,
                "Region con alta presion fitosanitaria para hortalizas en verano.",
                predicciones
        );
    }

    @Test
    void toPdf_GeneratesNonEmptyPdfWithMagicHeader() {
        byte[] pdf = exporter.toPdf(sampleReporte());
        assertNotNull(pdf);
        assertTrue(pdf.length > 100, "PDF debe contener bytes");
        // PDF magic header: %PDF
        assertEquals('%', (char) pdf[0]);
        assertEquals('P', (char) pdf[1]);
        assertEquals('D', (char) pdf[2]);
        assertEquals('F', (char) pdf[3]);
    }

    @Test
    void toExcel_GeneratesWorkbookWithExpectedSheetsAndData() throws Exception {
        byte[] xlsx = exporter.toExcel(sampleReporte());
        assertNotNull(xlsx);
        assertTrue(xlsx.length > 100);

        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(xlsx))) {
            Sheet resumen = wb.getSheet("Resumen");
            assertNotNull(resumen, "Debe existir hoja Resumen");
            assertEquals("Region", resumen.getRow(0).getCell(0).getStringCellValue());
            assertEquals("Jalisco", resumen.getRow(0).getCell(1).getStringCellValue());

            Sheet predicciones = wb.getSheet("Predicciones");
            assertNotNull(predicciones, "Debe existir hoja Predicciones");
            Row header = predicciones.getRow(0);
            assertEquals("Plaga", header.getCell(1).getStringCellValue());
            Row first = predicciones.getRow(1);
            assertEquals("Pulgon", first.getCell(1).getStringCellValue());
            assertEquals(80.0d, first.getCell(2).getNumericCellValue(), 0.001);
        }
    }

    @Test
    void toExcel_WhenNoPredicciones_StillGeneratesValidFile() throws Exception {
        ReportePredictivoPlagas vacio = new ReportePredictivoPlagas(
                "Sonora", Temporada.OTONO, LocalDateTime.now(), 0L, "Sin datos", List.of());
        byte[] xlsx = exporter.toExcel(vacio);
        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(xlsx))) {
            Sheet predicciones = wb.getSheet("Predicciones");
            assertNotNull(predicciones);
            // solo header row
            assertEquals(0, predicciones.getLastRowNum());
        }
    }
}
