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
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.IndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@ApplicationScoped
public class ReportePredictivoExporterImpl implements ReportePredictivoExporter {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy, HH:mm", new Locale("es", "MX"));

    // Brand palette (from front-plaguie/src/tokens/colors.ts)
    private static final Color BRAND_GREEN = new Color(0x75, 0xC7, 0x9E);
    private static final Color BRAND_GREEN_DARK = new Color(0x4F, 0x8F, 0x73);
    private static final Color TEXT_PRIMARY = new Color(0x0F, 0x17, 0x2B);
    private static final Color TEXT_SECONDARY = new Color(0x45, 0x55, 0x6C);
    private static final Color TEXT_MUTED = new Color(0x62, 0x74, 0x8E);
    private static final Color BORDER_LIGHT = new Color(0xE2, 0xE8, 0xF0);
    private static final Color SURFACE_LIGHT = new Color(0xF8, 0xFA, 0xFC);

    private static final Color RISK_CRITICO_BG = new Color(0xFE, 0xF2, 0xF2);
    private static final Color RISK_CRITICO_BORDER = new Color(0xFB, 0x2C, 0x36);
    private static final Color RISK_CRITICO_TEXT = new Color(0xC1, 0x00, 0x07);

    private static final Color RISK_ALTO_BG = new Color(0xFF, 0xF7, 0xED);
    private static final Color RISK_ALTO_BORDER = new Color(0xFF, 0x69, 0x00);
    private static final Color RISK_ALTO_TEXT = new Color(0xCA, 0x35, 0x00);

    private static final Color RISK_MEDIO_BG = new Color(0xEF, 0xF6, 0xFF);
    private static final Color RISK_MEDIO_BORDER = new Color(0x2B, 0x7F, 0xFF);
    private static final Color RISK_MEDIO_TEXT = new Color(0x14, 0x47, 0xE6);

    private static final Color RISK_BAJO_BG = new Color(0xF0, 0xFD, 0xF4);
    private static final Color RISK_BAJO_BORDER = new Color(0x75, 0xC7, 0x9E);
    private static final Color RISK_BAJO_TEXT = new Color(0x16, 0x65, 0x34);

    @Override
    public byte[] toPdf(ReportePredictivoPlagas reporte) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PdfContext ctx = new PdfContext(document);
            ctx.newPage();
            renderHeader(ctx, reporte);
            renderKpiStrip(ctx, reporte);
            renderExecutiveSummary(ctx, reporte);
            renderPredictionsTitle(ctx);

            List<PrediccionPlaga> predicciones = reporte.getPredicciones() == null
                    ? List.of() : reporte.getPredicciones();
            if (predicciones.isEmpty()) {
                renderEmptyState(ctx);
            } else {
                int idx = 1;
                for (PrediccionPlaga p : predicciones) {
                    renderPredictionCard(ctx, p, idx);
                    idx++;
                }
            }

            ctx.finalizePages();
            renderFooters(document);

            document.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generando PDF de reporte predictivo", e);
        }
    }

    // ============================================================
    // PDF RENDERING
    // ============================================================

    private static final class PdfContext {
        final PDDocument document;
        PDPage page;
        PDPageContentStream stream;
        float y;
        final float marginX = 50f;
        final float marginTop = 50f;
        final float marginBottom = 60f;

        final PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        final PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        final PDType1Font fontOblique = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

        PdfContext(PDDocument document) {
            this.document = document;
        }

        void newPage() throws IOException {
            if (stream != null) {
                stream.close();
            }
            page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);
            stream = new PDPageContentStream(document, page);
            y = page.getMediaBox().getHeight() - marginTop;
        }

        float pageWidth() {
            return page.getMediaBox().getWidth();
        }

        float contentWidth() {
            return pageWidth() - 2 * marginX;
        }

        void ensureSpace(float needed) throws IOException {
            if (y - needed < marginBottom) {
                newPage();
            }
        }

        void finalizePages() throws IOException {
            if (stream != null) {
                stream.close();
                stream = null;
            }
        }
    }

    private void renderHeader(PdfContext ctx, ReportePredictivoPlagas reporte) throws IOException {
        float bandHeight = 80f;
        // Green band at top
        ctx.stream.setNonStrokingColor(toPdColor(BRAND_GREEN));
        ctx.stream.addRect(0, ctx.pageWidth() == 0 ? 0 : ctx.page.getMediaBox().getHeight() - bandHeight,
                ctx.pageWidth(), bandHeight);
        ctx.stream.fill();

        // Brand label (top-left, white)
        drawText(ctx, "PLAGUIE", ctx.marginX, ctx.page.getMediaBox().getHeight() - 30,
                ctx.fontBold, 11, Color.WHITE);
        drawText(ctx, "REPORTE PREDICTIVO DE PLAGAS",
                ctx.marginX, ctx.page.getMediaBox().getHeight() - 50,
                ctx.fontBold, 18, Color.WHITE);
        drawText(ctx, "Inteligencia comercial para ejecutivos de ventas",
                ctx.marginX, ctx.page.getMediaBox().getHeight() - 68,
                ctx.fontRegular, 10, new Color(255, 255, 255, 220));

        ctx.y = ctx.page.getMediaBox().getHeight() - bandHeight - 20f;

        // Region + temporada line (big)
        String regionLabel = (reporte.getRegion() == null ? "-" : reporte.getRegion());
        String seasonLabel = reporte.getTemporada() == null ? "-" : reporte.getTemporada().getDisplayName();
        drawText(ctx, regionLabel + "  ·  " + seasonLabel,
                ctx.marginX, ctx.y, ctx.fontBold, 22, TEXT_PRIMARY);
        ctx.y -= 22f;

        String fecha = reporte.getGeneradoEn() == null ? "-" : reporte.getGeneradoEn().format(DATE_FORMATTER);
        drawText(ctx, "Generado el " + fecha, ctx.marginX, ctx.y,
                ctx.fontRegular, 10, TEXT_MUTED);
        ctx.y -= 22f;
    }

    private void renderKpiStrip(PdfContext ctx, ReportePredictivoPlagas reporte) throws IOException {
        ctx.ensureSpace(80f);

        List<PrediccionPlaga> predicciones = reporte.getPredicciones() == null
                ? List.of() : reporte.getPredicciones();
        long altoRiesgo = predicciones.stream()
                .filter(p -> {
                    String r = p.getNivelRiesgo();
                    return r != null && (r.equalsIgnoreCase("Critico") || r.equalsIgnoreCase("Crítico")
                            || r.equalsIgnoreCase("Alto"));
                })
                .count();
        int probMax = predicciones.stream()
                .map(PrediccionPlaga::getProbabilidad)
                .filter(p -> p != null)
                .max(Integer::compareTo)
                .orElse(0);

        float kpiHeight = 70f;
        float gap = 12f;
        float kpiWidth = (ctx.contentWidth() - gap * 3) / 4f;
        float top = ctx.y;

        drawKpiCard(ctx, ctx.marginX, top, kpiWidth, kpiHeight,
                "Observaciones", String.valueOf(reporte.getObservacionesAnalizadas()),
                "registros analizados", BRAND_GREEN);

        drawKpiCard(ctx, ctx.marginX + (kpiWidth + gap), top, kpiWidth, kpiHeight,
                "Plagas previstas", String.valueOf(predicciones.size()),
                "en este escenario", new Color(0x2B, 0x7F, 0xFF));

        drawKpiCard(ctx, ctx.marginX + 2 * (kpiWidth + gap), top, kpiWidth, kpiHeight,
                "Alto riesgo", String.valueOf(altoRiesgo),
                "plagas criticas/altas", new Color(0xFB, 0x2C, 0x36));

        drawKpiCard(ctx, ctx.marginX + 3 * (kpiWidth + gap), top, kpiWidth, kpiHeight,
                "Prob. maxima", probMax + "%",
                "plaga lider", new Color(0xFF, 0x69, 0x00));

        ctx.y -= kpiHeight + 24f;
    }

    private void drawKpiCard(PdfContext ctx, float x, float y, float w, float h,
                             String label, String value, String hint, Color accent) throws IOException {
        // Background
        ctx.stream.setNonStrokingColor(toPdColor(Color.WHITE));
        ctx.stream.addRect(x, y - h, w, h);
        ctx.stream.fill();
        // Border
        ctx.stream.setStrokingColor(toPdColor(BORDER_LIGHT));
        ctx.stream.setLineWidth(0.6f);
        ctx.stream.addRect(x, y - h, w, h);
        ctx.stream.stroke();
        // Accent left bar
        ctx.stream.setNonStrokingColor(toPdColor(accent));
        ctx.stream.addRect(x, y - h, 3, h);
        ctx.stream.fill();

        drawText(ctx, label.toUpperCase(Locale.ROOT), x + 12, y - 16,
                ctx.fontBold, 8, TEXT_MUTED);
        drawText(ctx, value, x + 12, y - 38, ctx.fontBold, 22, TEXT_PRIMARY);
        drawText(ctx, hint, x + 12, y - 56, ctx.fontRegular, 8, TEXT_SECONDARY);
    }

    private void renderExecutiveSummary(PdfContext ctx, ReportePredictivoPlagas reporte) throws IOException {
        String resumen = reporte.getResumenEjecutivo();
        if (resumen == null || resumen.isBlank()) {
            return;
        }
        List<String> lines = wrapText(resumen, ctx.fontRegular, 11, ctx.contentWidth() - 28);
        float boxHeight = 30f + lines.size() * 15f;

        ctx.ensureSpace(boxHeight + 20f);

        // Container box
        ctx.stream.setNonStrokingColor(toPdColor(SURFACE_LIGHT));
        ctx.stream.addRect(ctx.marginX, ctx.y - boxHeight, ctx.contentWidth(), boxHeight);
        ctx.stream.fill();
        ctx.stream.setStrokingColor(toPdColor(BORDER_LIGHT));
        ctx.stream.setLineWidth(0.6f);
        ctx.stream.addRect(ctx.marginX, ctx.y - boxHeight, ctx.contentWidth(), boxHeight);
        ctx.stream.stroke();
        // Accent left bar (brand green)
        ctx.stream.setNonStrokingColor(toPdColor(BRAND_GREEN));
        ctx.stream.addRect(ctx.marginX, ctx.y - boxHeight, 3, boxHeight);
        ctx.stream.fill();

        float cursorY = ctx.y - 18f;
        drawText(ctx, "RESUMEN EJECUTIVO", ctx.marginX + 14, cursorY,
                ctx.fontBold, 9, BRAND_GREEN_DARK);
        cursorY -= 14f;
        for (String line : lines) {
            drawText(ctx, line, ctx.marginX + 14, cursorY, ctx.fontRegular, 11, TEXT_PRIMARY);
            cursorY -= 15f;
        }

        ctx.y -= boxHeight + 20f;
    }

    private void renderPredictionsTitle(PdfContext ctx) throws IOException {
        ctx.ensureSpace(28f);
        drawText(ctx, "Plagas previstas", ctx.marginX, ctx.y,
                ctx.fontBold, 14, TEXT_PRIMARY);
        ctx.y -= 8f;
        // separator
        ctx.stream.setStrokingColor(toPdColor(BORDER_LIGHT));
        ctx.stream.setLineWidth(0.6f);
        ctx.stream.moveTo(ctx.marginX, ctx.y);
        ctx.stream.lineTo(ctx.marginX + ctx.contentWidth(), ctx.y);
        ctx.stream.stroke();
        ctx.y -= 14f;
    }

    private void renderEmptyState(PdfContext ctx) throws IOException {
        ctx.ensureSpace(80f);
        drawText(ctx, "No se generaron predicciones para este escenario.",
                ctx.marginX, ctx.y, ctx.fontOblique, 11, TEXT_MUTED);
        ctx.y -= 18f;
        drawText(ctx, "Considere ampliar la region o consultar otra temporada.",
                ctx.marginX, ctx.y, ctx.fontRegular, 10, TEXT_MUTED);
        ctx.y -= 18f;
    }

    private void renderPredictionCard(PdfContext ctx, PrediccionPlaga p, int idx) throws IOException {
        Color border = riskColor(p.getNivelRiesgo());
        Color bg = riskBg(p.getNivelRiesgo());
        Color textColor = riskText(p.getNivelRiesgo());

        String plaga = nullSafe(p.getPlagaNombre());
        String host = nullSafe(p.getHospedanteAfectado());
        String periodo = nullSafe(p.getPeriodoEstimado());
        String nivel = nullSafe(p.getNivelRiesgo());
        String justificacion = nullSafe(p.getJustificacion());
        String producto = p.getProductoSugerido();

        List<String> justLines = wrapText(justificacion, ctx.fontRegular, 9.5f, ctx.contentWidth() - 28);
        boolean hasProducto = producto != null && !producto.isBlank();
        List<String> prodLines = hasProducto
                ? wrapText("Producto sugerido: " + producto, ctx.fontBold, 9.5f, ctx.contentWidth() - 28)
                : List.of();

        // Compute card height
        float cardHeight = 22f /* header */
                + 16f /* meta line */
                + 14f /* progress bar + spacing */
                + justLines.size() * 12f
                + (hasProducto ? (8f + prodLines.size() * 12f + 4f) : 0f)
                + 14f /* padding bottom */;

        ctx.ensureSpace(cardHeight + 10f);

        float topY = ctx.y;
        // Card background
        ctx.stream.setNonStrokingColor(toPdColor(Color.WHITE));
        ctx.stream.addRect(ctx.marginX, topY - cardHeight, ctx.contentWidth(), cardHeight);
        ctx.stream.fill();
        // Border
        ctx.stream.setStrokingColor(toPdColor(BORDER_LIGHT));
        ctx.stream.setLineWidth(0.6f);
        ctx.stream.addRect(ctx.marginX, topY - cardHeight, ctx.contentWidth(), cardHeight);
        ctx.stream.stroke();
        // Risk accent bar (left)
        ctx.stream.setNonStrokingColor(toPdColor(border));
        ctx.stream.addRect(ctx.marginX, topY - cardHeight, 4, cardHeight);
        ctx.stream.fill();

        // Header: numbered plague title
        float cursorY = topY - 16f;
        String title = idx + ". " + plaga;
        drawText(ctx, title, ctx.marginX + 14, cursorY, ctx.fontBold, 12, TEXT_PRIMARY);

        // Probability badge (right-aligned)
        Integer prob = p.getProbabilidad();
        if (prob != null) {
            String probText = prob + "%";
            float probWidth = textWidth(ctx.fontBold, 12, probText);
            float badgeX = ctx.marginX + ctx.contentWidth() - probWidth - 110;
            // Risk badge box
            drawRiskBadge(ctx, badgeX + probWidth + 8, cursorY + 4, nivel, bg, textColor, border);
            // Probability text
            drawText(ctx, probText, badgeX, cursorY, ctx.fontBold, 12, TEXT_PRIMARY);
        } else {
            drawRiskBadge(ctx, ctx.marginX + ctx.contentWidth() - 80, cursorY + 4, nivel, bg, textColor, border);
        }

        cursorY -= 16f;
        // Meta line: host + period
        String meta = "Hospedante: " + host + "    ·    Periodo: " + periodo;
        drawText(ctx, meta, ctx.marginX + 14, cursorY, ctx.fontRegular, 9.5f, TEXT_SECONDARY);

        // Probability bar (under meta, with safe spacing)
        if (prob != null) {
            cursorY -= 8f;
            drawProgressBar(ctx, ctx.marginX + 14, cursorY,
                    ctx.contentWidth() - 28, 5, prob, border);
            cursorY -= 12f;
        } else {
            cursorY -= 14f;
        }

        // Justification
        for (String line : justLines) {
            drawText(ctx, line, ctx.marginX + 14, cursorY, ctx.fontRegular, 9.5f, TEXT_PRIMARY);
            cursorY -= 12f;
        }

        if (hasProducto) {
            cursorY -= 6f;
            // Product highlight box
            float prodBoxH = prodLines.size() * 12f + 8f;
            ctx.stream.setNonStrokingColor(toPdColor(new Color(0xEC, 0xFD, 0xF5)));
            ctx.stream.addRect(ctx.marginX + 14, cursorY - prodBoxH + 10, ctx.contentWidth() - 28, prodBoxH);
            ctx.stream.fill();
            float pcy = cursorY + 2;
            for (String line : prodLines) {
                drawText(ctx, line, ctx.marginX + 20, pcy, ctx.fontBold, 9.5f, BRAND_GREEN_DARK);
                pcy -= 12f;
            }
        }

        ctx.y = topY - cardHeight - 10f;
    }

    private void drawRiskBadge(PdfContext ctx, float x, float y, String level,
                               Color bg, Color text, Color border) throws IOException {
        if (level == null || level.isBlank() || "-".equals(level)) {
            return;
        }
        float pad = 6f;
        float w = textWidth(ctx.fontBold, 8.5f, level.toUpperCase(Locale.ROOT)) + pad * 2;
        float h = 14f;
        float bx = x;
        float by = y - h;

        ctx.stream.setNonStrokingColor(toPdColor(bg));
        ctx.stream.addRect(bx, by, w, h);
        ctx.stream.fill();
        ctx.stream.setStrokingColor(toPdColor(border));
        ctx.stream.setLineWidth(0.5f);
        ctx.stream.addRect(bx, by, w, h);
        ctx.stream.stroke();

        drawText(ctx, level.toUpperCase(Locale.ROOT), bx + pad, by + 3.5f,
                ctx.fontBold, 8.5f, text);
    }

    private void drawProgressBar(PdfContext ctx, float x, float y, float width, float height,
                                 int probability, Color fill) throws IOException {
        int p = Math.max(0, Math.min(100, probability));
        // Track
        ctx.stream.setNonStrokingColor(toPdColor(BORDER_LIGHT));
        ctx.stream.addRect(x, y, width, height);
        ctx.stream.fill();
        // Fill
        float fillW = width * (p / 100f);
        ctx.stream.setNonStrokingColor(toPdColor(fill));
        ctx.stream.addRect(x, y, fillW, height);
        ctx.stream.fill();
    }

    private void renderFooters(PDDocument document) throws IOException {
        int total = document.getNumberOfPages();
        PDType1Font regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        for (int i = 0; i < total; i++) {
            PDPage page = document.getPage(i);
            try (PDPageContentStream cs = new PDPageContentStream(document, page,
                    PDPageContentStream.AppendMode.APPEND, true, true)) {
                String label = "Plaguie · Reporte predictivo de plagas";
                String pg = "Pagina " + (i + 1) + " de " + total;
                cs.setStrokingColor(toPdColor(BORDER_LIGHT));
                cs.setLineWidth(0.4f);
                cs.moveTo(40, 40);
                cs.lineTo(page.getMediaBox().getWidth() - 40, 40);
                cs.stroke();

                cs.beginText();
                cs.setFont(regular, 8);
                cs.setNonStrokingColor(toPdColor(TEXT_MUTED));
                cs.newLineAtOffset(40, 28);
                cs.showText(sanitize(label));
                cs.endText();

                float pgWidth = regular.getStringWidth(pg) / 1000f * 8;
                cs.beginText();
                cs.setFont(regular, 8);
                cs.setNonStrokingColor(toPdColor(TEXT_MUTED));
                cs.newLineAtOffset(page.getMediaBox().getWidth() - 40 - pgWidth, 28);
                cs.showText(pg);
                cs.endText();
            }
        }
    }

    private void drawText(PdfContext ctx, String text, float x, float y,
                          PDType1Font font, float size, Color color) throws IOException {
        ctx.stream.beginText();
        ctx.stream.setFont(font, size);
        ctx.stream.setNonStrokingColor(toPdColor(color));
        ctx.stream.newLineAtOffset(x, y);
        ctx.stream.showText(sanitize(text));
        ctx.stream.endText();
    }

    private float textWidth(PDType1Font font, float size, String text) throws IOException {
        return font.getStringWidth(sanitize(text)) / 1000f * size;
    }

    private List<String> wrapText(String text, PDType1Font font, float size, float maxWidth) throws IOException {
        if (text == null || text.isEmpty()) {
            return List.of("");
        }
        String sanitized = sanitize(text);
        String[] words = sanitized.split(" ");
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            String candidate = current.length() == 0 ? word : current + " " + word;
            float w = font.getStringWidth(candidate) / 1000f * size;
            if (w > maxWidth && current.length() > 0) {
                lines.add(current.toString());
                current = new StringBuilder(word);
            } else {
                current = new StringBuilder(candidate);
            }
        }
        if (current.length() > 0) {
            lines.add(current.toString());
        }
        return lines;
    }

    private static PDColor toPdColor(Color c) {
        return new PDColor(new float[]{c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f},
                PDDeviceRGB.INSTANCE);
    }

    private Color riskColor(String level) {
        if (level == null) return RISK_MEDIO_BORDER;
        String l = level.toLowerCase(Locale.ROOT);
        if (l.contains("crit")) return RISK_CRITICO_BORDER;
        if (l.contains("alto")) return RISK_ALTO_BORDER;
        if (l.contains("medio")) return RISK_MEDIO_BORDER;
        if (l.contains("bajo")) return RISK_BAJO_BORDER;
        return RISK_MEDIO_BORDER;
    }

    private Color riskBg(String level) {
        if (level == null) return RISK_MEDIO_BG;
        String l = level.toLowerCase(Locale.ROOT);
        if (l.contains("crit")) return RISK_CRITICO_BG;
        if (l.contains("alto")) return RISK_ALTO_BG;
        if (l.contains("medio")) return RISK_MEDIO_BG;
        if (l.contains("bajo")) return RISK_BAJO_BG;
        return RISK_MEDIO_BG;
    }

    private Color riskText(String level) {
        if (level == null) return RISK_MEDIO_TEXT;
        String l = level.toLowerCase(Locale.ROOT);
        if (l.contains("crit")) return RISK_CRITICO_TEXT;
        if (l.contains("alto")) return RISK_ALTO_TEXT;
        if (l.contains("medio")) return RISK_MEDIO_TEXT;
        if (l.contains("bajo")) return RISK_BAJO_TEXT;
        return RISK_MEDIO_TEXT;
    }

    private String nullSafe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String sanitize(String value) {
        if (value == null) {
            return "";
        }
        // Helvetica WinAnsi: replace unsupported typographic chars
        return value
                .replace("–", "-")
                .replace("—", "-")
                .replace("‘", "'")
                .replace("’", "'")
                .replace("“", "\"")
                .replace("”", "\"")
                .replace("…", "...")
                .replace("•", "*")
                .replace(" ", " ")
                .replaceAll("[\\r\\n\\t]", " ");
    }

    // ============================================================
    // EXCEL RENDERING
    // ============================================================

    @Override
    public byte[] toExcel(ReportePredictivoPlagas reporte) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            ExcelStyles styles = new ExcelStyles(workbook);

            buildResumenSheet(workbook, styles, reporte);
            buildPrediccionesSheet(workbook, styles, reporte);

            workbook.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generando Excel de reporte predictivo", e);
        }
    }

    private void buildResumenSheet(XSSFWorkbook wb, ExcelStyles styles, ReportePredictivoPlagas reporte) {
        XSSFSheet sheet = wb.createSheet("Resumen");
        sheet.setDisplayGridlines(false);

        // Column widths
        sheet.setColumnWidth(0, 4 * 256);   // left margin
        sheet.setColumnWidth(1, 28 * 256);
        sheet.setColumnWidth(2, 28 * 256);
        sheet.setColumnWidth(3, 28 * 256);
        sheet.setColumnWidth(4, 28 * 256);
        sheet.setColumnWidth(5, 4 * 256);

        int r = 1;

        // Title block (rows 1-3)
        sheet.addMergedRegion(new CellRangeAddress(r, r + 2, 1, 4));
        Row titleRow = sheet.createRow(r);
        titleRow.setHeightInPoints(28f);
        sheet.createRow(r + 1).setHeightInPoints(28f);
        sheet.createRow(r + 2).setHeightInPoints(28f);
        XSSFCell titleCell = (XSSFCell) titleRow.createCell(1);
        titleCell.setCellValue("PLAGUIE\nReporte predictivo de plagas");
        titleCell.setCellStyle(styles.titleBanner);
        r += 4;

        // Region / Temporada / Generado (rows of label+value pairs)
        addMetaRow(sheet, styles, r++, "Region",
                reporte.getRegion() == null ? "-" : reporte.getRegion());
        addMetaRow(sheet, styles, r++, "Temporada",
                reporte.getTemporada() == null ? "-" : reporte.getTemporada().getDisplayName());
        addMetaRow(sheet, styles, r++, "Generado",
                reporte.getGeneradoEn() == null ? "-" : reporte.getGeneradoEn().format(DATE_FORMATTER));
        r += 1;

        // KPI cards (one row of 4)
        Row kpiRow = sheet.createRow(r);
        kpiRow.setHeightInPoints(60f);

        List<PrediccionPlaga> predicciones = reporte.getPredicciones() == null
                ? List.of() : reporte.getPredicciones();
        long altoRiesgo = predicciones.stream()
                .filter(p -> {
                    String rl = p.getNivelRiesgo();
                    return rl != null && (rl.toLowerCase(Locale.ROOT).contains("crit")
                            || rl.toLowerCase(Locale.ROOT).contains("alto"));
                }).count();
        int probMax = predicciones.stream()
                .map(PrediccionPlaga::getProbabilidad)
                .filter(p -> p != null)
                .max(Integer::compareTo)
                .orElse(0);

        writeKpiCell(kpiRow, 1, "OBSERVACIONES", String.valueOf(reporte.getObservacionesAnalizadas()),
                "registros analizados", styles.kpiLabel, styles.kpiValueGreen, styles.kpiHint);
        writeKpiCell(kpiRow, 2, "PLAGAS PREVISTAS", String.valueOf(predicciones.size()),
                "en este escenario", styles.kpiLabel, styles.kpiValueBlue, styles.kpiHint);
        writeKpiCell(kpiRow, 3, "ALTO RIESGO", String.valueOf(altoRiesgo),
                "criticas o altas", styles.kpiLabel, styles.kpiValueRed, styles.kpiHint);
        writeKpiCell(kpiRow, 4, "PROB. MAXIMA", probMax + "%",
                "plaga lider", styles.kpiLabel, styles.kpiValueOrange, styles.kpiHint);
        r += 2;

        // Executive summary (label + merged cell)
        Row summaryHeader = sheet.createRow(r);
        XSSFCell summaryHeaderCell = (XSSFCell) summaryHeader.createCell(1);
        summaryHeaderCell.setCellValue("RESUMEN EJECUTIVO");
        summaryHeaderCell.setCellStyle(styles.sectionHeader);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 1, 4));
        r++;

        Row summaryRow = sheet.createRow(r);
        summaryRow.setHeightInPoints(90f);
        XSSFCell summaryCell = (XSSFCell) summaryRow.createCell(1);
        summaryCell.setCellValue(reporte.getResumenEjecutivo() == null
                ? "Sin resumen disponible."
                : reporte.getResumenEjecutivo());
        summaryCell.setCellStyle(styles.summaryBox);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 1, 4));
    }

    private void addMetaRow(XSSFSheet sheet, ExcelStyles styles, int rowIdx, String label, String value) {
        Row row = sheet.createRow(rowIdx);
        row.setHeightInPoints(22f);
        XSSFCell labelCell = (XSSFCell) row.createCell(1);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(styles.metaLabel);
        XSSFCell valueCell = (XSSFCell) row.createCell(2);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(styles.metaValue);
        sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 2, 4));
    }

    private void writeKpiCell(Row row, int col, String label, String value, String hint,
                              CellStyle labelStyle, CellStyle valueStyle, CellStyle hintStyle) {
        XSSFCell cell = (XSSFCell) row.createCell(col);
        cell.setCellValue(label + "\n" + value + "\n" + hint);
        cell.setCellStyle(valueStyle);
    }

    private void buildPrediccionesSheet(XSSFWorkbook wb, ExcelStyles styles, ReportePredictivoPlagas reporte) {
        XSSFSheet sheet = wb.createSheet("Predicciones");
        sheet.setDisplayGridlines(false);

        String[] headers = {"#", "Plaga", "Probabilidad", "Periodo estimado", "Nivel de riesgo",
                "Hospedante afectado", "Justificacion", "Producto sugerido"};
        int[] widths = {4, 28, 14, 18, 14, 26, 50, 28};
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }

        Row header = sheet.createRow(0);
        header.setHeightInPoints(28f);
        for (int i = 0; i < headers.length; i++) {
            XSSFCell c = (XSSFCell) header.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(styles.tableHeader);
        }
        sheet.createFreezePane(0, 1);

        List<PrediccionPlaga> predicciones = reporte.getPredicciones() == null
                ? List.of() : reporte.getPredicciones();
        int r = 1;
        for (PrediccionPlaga p : predicciones) {
            Row row = sheet.createRow(r);
            row.setHeightInPoints(42f);
            boolean alt = (r % 2 == 0);
            CellStyle base = alt ? styles.tableCellAlt : styles.tableCell;
            CellStyle baseCenter = alt ? styles.tableCellCenterAlt : styles.tableCellCenter;
            CellStyle baseBold = alt ? styles.tableCellBoldAlt : styles.tableCellBold;

            createStyledCell(row, 0, String.valueOf(r), baseCenter);
            createStyledCell(row, 1, nullSafe(p.getPlagaNombre()), baseBold);

            XSSFCell probCell = (XSSFCell) row.createCell(2);
            if (p.getProbabilidad() != null) {
                probCell.setCellValue(p.getProbabilidad() + "%");
            } else {
                probCell.setCellValue("-");
            }
            probCell.setCellStyle(baseCenter);

            createStyledCell(row, 3, nullSafe(p.getPeriodoEstimado()), baseCenter);

            XSSFCell riskCell = (XSSFCell) row.createCell(4);
            riskCell.setCellValue(nullSafe(p.getNivelRiesgo()).toUpperCase(Locale.ROOT));
            riskCell.setCellStyle(styles.riskStyle(p.getNivelRiesgo()));

            createStyledCell(row, 5, nullSafe(p.getHospedanteAfectado()), base);
            createStyledCell(row, 6, nullSafe(p.getJustificacion()), base);
            createStyledCell(row, 7, nullSafe(p.getProductoSugerido()), baseBold);
            r++;
        }

        if (predicciones.isEmpty()) {
            Row row = sheet.createRow(1);
            row.setHeightInPoints(30f);
            XSSFCell c = (XSSFCell) row.createCell(1);
            c.setCellValue("Sin predicciones disponibles para este escenario.");
            c.setCellStyle(styles.tableCell);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 1, 7));
        }
    }

    private void createStyledCell(Row row, int col, String value, CellStyle style) {
        XSSFCell c = (XSSFCell) row.createCell(col);
        c.setCellValue(value);
        c.setCellStyle(style);
    }

    /**
     * Holder for reusable styles. POI requires styles to be created once per workbook.
     */
    private static final class ExcelStyles {
        final XSSFCellStyle titleBanner;
        final XSSFCellStyle metaLabel;
        final XSSFCellStyle metaValue;
        final XSSFCellStyle sectionHeader;
        final XSSFCellStyle summaryBox;
        final XSSFCellStyle kpiLabel;
        final XSSFCellStyle kpiHint;
        final XSSFCellStyle kpiValueGreen;
        final XSSFCellStyle kpiValueBlue;
        final XSSFCellStyle kpiValueRed;
        final XSSFCellStyle kpiValueOrange;
        final XSSFCellStyle tableHeader;
        final XSSFCellStyle tableCell;
        final XSSFCellStyle tableCellAlt;
        final XSSFCellStyle tableCellCenter;
        final XSSFCellStyle tableCellCenterAlt;
        final XSSFCellStyle tableCellBold;
        final XSSFCellStyle tableCellBoldAlt;
        final XSSFCellStyle riskCritico;
        final XSSFCellStyle riskAlto;
        final XSSFCellStyle riskMedio;
        final XSSFCellStyle riskBajo;
        final XSSFCellStyle riskNeutral;

        ExcelStyles(XSSFWorkbook wb) {
            IndexedColorMap cm = wb.getStylesSource().getIndexedColors();

            XSSFFont titleFont = wb.createFont();
            titleFont.setFontName("Calibri");
            titleFont.setBold(true);
            titleFont.setColor(new XSSFColor(Color.WHITE, cm));
            titleFont.setFontHeightInPoints((short) 18);

            titleBanner = wb.createCellStyle();
            titleBanner.setFillForegroundColor(new XSSFColor(BRAND_GREEN, cm));
            titleBanner.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            titleBanner.setAlignment(HorizontalAlignment.LEFT);
            titleBanner.setVerticalAlignment(VerticalAlignment.CENTER);
            titleBanner.setWrapText(true);
            titleBanner.setFont(titleFont);

            XSSFFont labelFont = wb.createFont();
            labelFont.setFontName("Calibri");
            labelFont.setBold(true);
            labelFont.setColor(new XSSFColor(TEXT_MUTED, cm));
            labelFont.setFontHeightInPoints((short) 9);

            metaLabel = wb.createCellStyle();
            metaLabel.setFillForegroundColor(new XSSFColor(SURFACE_LIGHT, cm));
            metaLabel.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            metaLabel.setAlignment(HorizontalAlignment.LEFT);
            metaLabel.setVerticalAlignment(VerticalAlignment.CENTER);
            metaLabel.setFont(labelFont);
            applyBorder(metaLabel, BORDER_LIGHT, cm);

            XSSFFont valueFont = wb.createFont();
            valueFont.setFontName("Calibri");
            valueFont.setColor(new XSSFColor(TEXT_PRIMARY, cm));
            valueFont.setFontHeightInPoints((short) 11);

            metaValue = wb.createCellStyle();
            metaValue.setAlignment(HorizontalAlignment.LEFT);
            metaValue.setVerticalAlignment(VerticalAlignment.CENTER);
            metaValue.setFont(valueFont);
            applyBorder(metaValue, BORDER_LIGHT, cm);

            XSSFFont sectionFont = wb.createFont();
            sectionFont.setFontName("Calibri");
            sectionFont.setBold(true);
            sectionFont.setColor(new XSSFColor(BRAND_GREEN_DARK, cm));
            sectionFont.setFontHeightInPoints((short) 10);

            sectionHeader = wb.createCellStyle();
            sectionHeader.setAlignment(HorizontalAlignment.LEFT);
            sectionHeader.setVerticalAlignment(VerticalAlignment.CENTER);
            sectionHeader.setFont(sectionFont);

            XSSFFont summaryFont = wb.createFont();
            summaryFont.setFontName("Calibri");
            summaryFont.setColor(new XSSFColor(TEXT_PRIMARY, cm));
            summaryFont.setFontHeightInPoints((short) 11);

            summaryBox = wb.createCellStyle();
            summaryBox.setFillForegroundColor(new XSSFColor(SURFACE_LIGHT, cm));
            summaryBox.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            summaryBox.setAlignment(HorizontalAlignment.LEFT);
            summaryBox.setVerticalAlignment(VerticalAlignment.TOP);
            summaryBox.setWrapText(true);
            summaryBox.setFont(summaryFont);
            applyBorder(summaryBox, BORDER_LIGHT, cm);

            kpiLabel = wb.createCellStyle();
            kpiHint = wb.createCellStyle();

            kpiValueGreen = buildKpiStyle(wb, cm, BRAND_GREEN);
            kpiValueBlue = buildKpiStyle(wb, cm, RISK_MEDIO_BORDER);
            kpiValueRed = buildKpiStyle(wb, cm, RISK_CRITICO_BORDER);
            kpiValueOrange = buildKpiStyle(wb, cm, RISK_ALTO_BORDER);

            // Table styles
            XSSFFont thFont = wb.createFont();
            thFont.setFontName("Calibri");
            thFont.setBold(true);
            thFont.setColor(new XSSFColor(Color.WHITE, cm));
            thFont.setFontHeightInPoints((short) 11);

            tableHeader = wb.createCellStyle();
            tableHeader.setFillForegroundColor(new XSSFColor(BRAND_GREEN_DARK, cm));
            tableHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            tableHeader.setAlignment(HorizontalAlignment.LEFT);
            tableHeader.setVerticalAlignment(VerticalAlignment.CENTER);
            tableHeader.setFont(thFont);
            tableHeader.setWrapText(true);
            applyBorder(tableHeader, BRAND_GREEN_DARK, cm);

            XSSFFont cellFont = wb.createFont();
            cellFont.setFontName("Calibri");
            cellFont.setColor(new XSSFColor(TEXT_PRIMARY, cm));
            cellFont.setFontHeightInPoints((short) 10);

            XSSFFont cellBoldFont = wb.createFont();
            cellBoldFont.setFontName("Calibri");
            cellBoldFont.setBold(true);
            cellBoldFont.setColor(new XSSFColor(TEXT_PRIMARY, cm));
            cellBoldFont.setFontHeightInPoints((short) 10);

            tableCell = buildTableCell(wb, cm, cellFont, HorizontalAlignment.LEFT, Color.WHITE, true);
            tableCellAlt = buildTableCell(wb, cm, cellFont, HorizontalAlignment.LEFT, SURFACE_LIGHT, true);
            tableCellCenter = buildTableCell(wb, cm, cellFont, HorizontalAlignment.CENTER, Color.WHITE, false);
            tableCellCenterAlt = buildTableCell(wb, cm, cellFont, HorizontalAlignment.CENTER, SURFACE_LIGHT, false);
            tableCellBold = buildTableCell(wb, cm, cellBoldFont, HorizontalAlignment.LEFT, Color.WHITE, true);
            tableCellBoldAlt = buildTableCell(wb, cm, cellBoldFont, HorizontalAlignment.LEFT, SURFACE_LIGHT, true);

            riskCritico = buildRiskStyle(wb, cm, RISK_CRITICO_BG, RISK_CRITICO_TEXT);
            riskAlto = buildRiskStyle(wb, cm, RISK_ALTO_BG, RISK_ALTO_TEXT);
            riskMedio = buildRiskStyle(wb, cm, RISK_MEDIO_BG, RISK_MEDIO_TEXT);
            riskBajo = buildRiskStyle(wb, cm, RISK_BAJO_BG, RISK_BAJO_TEXT);
            riskNeutral = buildRiskStyle(wb, cm, SURFACE_LIGHT, TEXT_SECONDARY);
        }

        private XSSFCellStyle buildKpiStyle(XSSFWorkbook wb, IndexedColorMap cm, Color accent) {
            XSSFFont valueFont = wb.createFont();
            valueFont.setFontName("Calibri");
            valueFont.setBold(true);
            valueFont.setColor(new XSSFColor(accent, cm));
            valueFont.setFontHeightInPoints((short) 16);

            XSSFCellStyle style = wb.createCellStyle();
            style.setFillForegroundColor(new XSSFColor(Color.WHITE, cm));
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setAlignment(HorizontalAlignment.CENTER);
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            style.setWrapText(true);
            style.setFont(valueFont);
            applyBorder(style, accent, cm);
            return style;
        }

        private XSSFCellStyle buildTableCell(XSSFWorkbook wb, IndexedColorMap cm,
                                             XSSFFont font, HorizontalAlignment align,
                                             Color fill, boolean wrap) {
            XSSFCellStyle style = wb.createCellStyle();
            style.setFont(font);
            style.setAlignment(align);
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            style.setWrapText(wrap);
            style.setFillForegroundColor(new XSSFColor(fill, cm));
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            applyBorder(style, BORDER_LIGHT, cm);
            return style;
        }

        private XSSFCellStyle buildRiskStyle(XSSFWorkbook wb, IndexedColorMap cm, Color bg, Color text) {
            XSSFFont font = wb.createFont();
            font.setFontName("Calibri");
            font.setBold(true);
            font.setColor(new XSSFColor(text, cm));
            font.setFontHeightInPoints((short) 10);

            XSSFCellStyle style = wb.createCellStyle();
            style.setFillForegroundColor(new XSSFColor(bg, cm));
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setAlignment(HorizontalAlignment.CENTER);
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            style.setFont(font);
            applyBorder(style, BORDER_LIGHT, cm);
            return style;
        }

        private static void applyBorder(XSSFCellStyle style, Color color, IndexedColorMap cm) {
            style.setBorderTop(BorderStyle.THIN);
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
            XSSFColor xc = new XSSFColor(color, cm);
            style.setTopBorderColor(xc);
            style.setBottomBorderColor(xc);
            style.setLeftBorderColor(xc);
            style.setRightBorderColor(xc);
            // suppress unused warning for IndexedColors
            IndexedColors.AUTOMATIC.getIndex();
        }

        XSSFCellStyle riskStyle(String level) {
            if (level == null) return riskNeutral;
            String l = level.toLowerCase(Locale.ROOT);
            if (l.contains("crit")) return riskCritico;
            if (l.contains("alto")) return riskAlto;
            if (l.contains("medio")) return riskMedio;
            if (l.contains("bajo")) return riskBajo;
            return riskNeutral;
        }
    }
}
