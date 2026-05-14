package itesm.mx.infrastructure.reporte;

import itesm.mx.domain.models.reporte.PrediccionPlaga;
import itesm.mx.domain.models.reporte.ReportePredictivoPlagas;
import itesm.mx.domain.repository.reporte.ReportePredictivoExporter;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@ApplicationScoped
public class ReportePredictivoExporterImpl implements ReportePredictivoExporter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public byte[] toPdf(ReportePredictivoPlagas reporte) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);

            PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font normalFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                float margin = 50f;
                float y = page.getMediaBox().getHeight() - margin;

                content.beginText();
                content.setFont(titleFont, 16);
                content.newLineAtOffset(margin, y);
                content.showText("Reporte Predictivo de Plagas");
                content.endText();
                y -= 24f;

                content.beginText();
                content.setFont(normalFont, 11);
                content.newLineAtOffset(margin, y);
                content.showText("Region: " + nullSafe(reporte.getRegion()));
                content.endText();
                y -= 16f;

                content.beginText();
                content.setFont(normalFont, 11);
                content.newLineAtOffset(margin, y);
                content.showText("Temporada: " + (reporte.getTemporada() != null ? reporte.getTemporada().getDisplayName() : "-"));
                content.endText();
                y -= 16f;

                content.beginText();
                content.setFont(normalFont, 11);
                content.newLineAtOffset(margin, y);
                content.showText("Generado: " + (reporte.getGeneradoEn() != null ? reporte.getGeneradoEn().format(DATE_FORMATTER) : "-"));
                content.endText();
                y -= 16f;

                content.beginText();
                content.setFont(normalFont, 11);
                content.newLineAtOffset(margin, y);
                content.showText("Observaciones analizadas: " + reporte.getObservacionesAnalizadas());
                content.endText();
                y -= 24f;

                content.beginText();
                content.setFont(titleFont, 12);
                content.newLineAtOffset(margin, y);
                content.showText("Resumen ejecutivo");
                content.endText();
                y -= 16f;

                String resumen = nullSafe(reporte.getResumenEjecutivo());
                for (String line : wrapText(resumen, 95)) {
                    content.beginText();
                    content.setFont(normalFont, 10);
                    content.newLineAtOffset(margin, y);
                    content.showText(sanitize(line));
                    content.endText();
                    y -= 13f;
                }
                y -= 8f;

                content.beginText();
                content.setFont(titleFont, 12);
                content.newLineAtOffset(margin, y);
                content.showText("Predicciones");
                content.endText();
                y -= 18f;

                List<PrediccionPlaga> predicciones = reporte.getPredicciones() == null ? List.of() : reporte.getPredicciones();
                int idx = 1;
                for (PrediccionPlaga p : predicciones) {
                    if (y < margin + 60f) {
                        content.close();
                        page = new PDPage(PDRectangle.LETTER);
                        document.addPage(page);
                        try (PDPageContentStream newContent = new PDPageContentStream(document, page)) {
                            y = page.getMediaBox().getHeight() - margin;
                            renderPrediccion(newContent, p, idx, titleFont, normalFont, margin, y);
                        }
                        idx++;
                        continue;
                    }
                    y = renderPrediccion(content, p, idx, titleFont, normalFont, margin, y);
                    idx++;
                }
            }

            document.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generando PDF de reporte predictivo", e);
        }
    }

    private float renderPrediccion(PDPageContentStream content, PrediccionPlaga p, int idx,
                                   PDType1Font titleFont, PDType1Font normalFont, float margin, float y) throws IOException {
        content.beginText();
        content.setFont(titleFont, 11);
        content.newLineAtOffset(margin, y);
        content.showText(idx + ". " + sanitize(nullSafe(p.getPlagaNombre()))
                + "  (probabilidad: " + (p.getProbabilidad() == null ? "-" : p.getProbabilidad() + "%") + ")");
        content.endText();
        y -= 14f;

        String linea = "Riesgo: " + nullSafe(p.getNivelRiesgo())
                + "   Periodo: " + nullSafe(p.getPeriodoEstimado())
                + "   Hospedante: " + nullSafe(p.getHospedanteAfectado());
        content.beginText();
        content.setFont(normalFont, 10);
        content.newLineAtOffset(margin + 10, y);
        content.showText(sanitize(linea));
        content.endText();
        y -= 13f;

        for (String line : wrapText("Justificacion: " + nullSafe(p.getJustificacion()), 90)) {
            content.beginText();
            content.setFont(normalFont, 10);
            content.newLineAtOffset(margin + 10, y);
            content.showText(sanitize(line));
            content.endText();
            y -= 12f;
        }

        if (p.getProductoSugerido() != null && !p.getProductoSugerido().isBlank()) {
            for (String line : wrapText("Producto sugerido: " + p.getProductoSugerido(), 90)) {
                content.beginText();
                content.setFont(normalFont, 10);
                content.newLineAtOffset(margin + 10, y);
                content.showText(sanitize(line));
                content.endText();
                y -= 12f;
            }
        }

        y -= 6f;
        return y;
    }

    @Override
    public byte[] toExcel(ReportePredictivoPlagas reporte) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet metaSheet = workbook.createSheet("Resumen");
            Font bold = workbook.createFont();
            bold.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(bold);

            int row = 0;
            row = writeKeyValue(metaSheet, row, "Region", nullSafe(reporte.getRegion()), headerStyle);
            row = writeKeyValue(metaSheet, row, "Temporada",
                    reporte.getTemporada() != null ? reporte.getTemporada().getDisplayName() : "-", headerStyle);
            row = writeKeyValue(metaSheet, row, "Generado",
                    reporte.getGeneradoEn() != null ? reporte.getGeneradoEn().format(DATE_FORMATTER) : "-", headerStyle);
            row = writeKeyValue(metaSheet, row, "Observaciones analizadas",
                    String.valueOf(reporte.getObservacionesAnalizadas()), headerStyle);
            writeKeyValue(metaSheet, row, "Resumen ejecutivo", nullSafe(reporte.getResumenEjecutivo()), headerStyle);

            metaSheet.setColumnWidth(0, 30 * 256);
            metaSheet.setColumnWidth(1, 80 * 256);

            Sheet sheet = workbook.createSheet("Predicciones");
            String[] headers = {"#", "Plaga", "Probabilidad (%)", "Periodo estimado", "Nivel de riesgo",
                    "Hospedante afectado", "Justificacion", "Producto sugerido"};
            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            List<PrediccionPlaga> predicciones = reporte.getPredicciones() == null ? List.of() : reporte.getPredicciones();
            int r = 1;
            for (PrediccionPlaga p : predicciones) {
                Row dataRow = sheet.createRow(r);
                dataRow.createCell(0).setCellValue(r);
                dataRow.createCell(1).setCellValue(nullSafe(p.getPlagaNombre()));
                if (p.getProbabilidad() != null) {
                    dataRow.createCell(2).setCellValue(p.getProbabilidad());
                } else {
                    dataRow.createCell(2).setCellValue("-");
                }
                dataRow.createCell(3).setCellValue(nullSafe(p.getPeriodoEstimado()));
                dataRow.createCell(4).setCellValue(nullSafe(p.getNivelRiesgo()));
                dataRow.createCell(5).setCellValue(nullSafe(p.getHospedanteAfectado()));
                dataRow.createCell(6).setCellValue(nullSafe(p.getJustificacion()));
                dataRow.createCell(7).setCellValue(nullSafe(p.getProductoSugerido()));
                r++;
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generando Excel de reporte predictivo", e);
        }
    }

    private int writeKeyValue(Sheet sheet, int rowIndex, String key, String value, CellStyle headerStyle) {
        Row row = sheet.createRow(rowIndex);
        Cell keyCell = row.createCell(0);
        keyCell.setCellValue(key);
        keyCell.setCellStyle(headerStyle);
        row.createCell(1).setCellValue(value == null ? "" : value);
        return rowIndex + 1;
    }

    private String nullSafe(String value) {
        return value == null ? "-" : value;
    }

    private String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("[\\r\\n\\t]", " ");
    }

    private List<String> wrapText(String text, int maxChars) {
        if (text == null || text.isEmpty()) {
            return List.of("");
        }
        String sanitized = sanitize(text);
        java.util.List<String> lines = new java.util.ArrayList<>();
        String[] words = sanitized.split(" ");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            if (current.length() + word.length() + 1 > maxChars) {
                lines.add(current.toString());
                current.setLength(0);
            }
            if (current.length() > 0) {
                current.append(' ');
            }
            current.append(word);
        }
        if (current.length() > 0) {
            lines.add(current.toString());
        }
        return lines;
    }
}
