package itesm.mx.infrastructure.gemini;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import itesm.mx.application.dto.ParcelaResponseDto;
import itesm.mx.domain.models.marketplace.Product;
import itesm.mx.domain.models.recomendacion.RecomendacionProducto;
import itesm.mx.domain.models.reporte.HistoricoVigilanciaSummary;
import itesm.mx.domain.models.reporte.Temporada;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Uses Gemini to generate personalized product recommendations for a farmer
 * based on their parcelas (crop types) and regional pest history. (HU-24 CA-02)
 * Falls back to heuristic recommendations when the API key is absent or the
 * call fails, so the feature degrades gracefully (CA-03).
 */
@ApplicationScoped
public class GeminiProductRecommendationProvider {

    private static final Logger LOG = Logger.getLogger(GeminiProductRecommendationProvider.class);

    @Inject
    GeminiHttpClient geminiHttpClient;

    @Inject
    ObjectMapper objectMapper;

    @ConfigProperty(name = "gemini.api.base-url")
    String baseUrl;

    @ConfigProperty(name = "gemini.api.key", defaultValue = "")
    String apiKey;

    @ConfigProperty(name = "gemini.api.model")
    String model;

    @ConfigProperty(name = "gemini.api.timeout-seconds", defaultValue = "30")
    int timeoutSeconds;

    // ─── Result type ─────────────────────────────────────────────────────────

    public static class RecomendacionResult {
        private final String resumenSituacion;
        private final List<RecomendacionProducto> recomendaciones;

        public RecomendacionResult(String resumenSituacion, List<RecomendacionProducto> recomendaciones) {
            this.resumenSituacion = resumenSituacion;
            this.recomendaciones = recomendaciones;
        }

        public String getResumenSituacion() { return resumenSituacion; }
        public List<RecomendacionProducto> getRecomendaciones() { return recomendaciones; }
    }

    // ─── Public API ──────────────────────────────────────────────────────────

    public RecomendacionResult recommend(String region, Temporada temporada,
                                          List<ParcelaResponseDto> parcelas,
                                          List<HistoricoVigilanciaSummary> historico,
                                          List<Product> availableProducts) {
        List<Product> catalog = availableProducts != null ? availableProducts : List.of();

        if (apiKey == null || apiKey.isBlank()) {
            LOG.warn("GEMINI_API_KEY no configurada: generando recomendaciones heurísticas");
            return fallback(region, temporada, parcelas, historico, catalog);
        }

        try {
            String prompt = buildPrompt(region, temporada, parcelas, historico, catalog);
            String body = buildRequestBody(prompt);
            String endpoint = baseUrl + "/v1beta/models/"
                    + URLEncoder.encode(model, StandardCharsets.UTF_8)
                    + ":generateContent?key="
                    + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);

            String response = geminiHttpClient.generateContent(endpoint, body, timeoutSeconds);
            return parseResponse(response, region, temporada, parcelas, historico, catalog);
        } catch (Exception e) {
            // Expected, handled condition (quota/connectivity): log a single WARN line,
            // not a full ERROR stack trace, since we degrade gracefully (HU-24 CA-03).
            LOG.warnf("Gemini no disponible para recomendaciones (region=%s); usando fallback heurístico. Causa: %s",
                    region, e.getMessage());
            return fallback(region, temporada, parcelas, historico, catalog);
        }
    }

    // ─── Prompt ──────────────────────────────────────────────────────────────

    private String buildPrompt(String region, Temporada temporada,
                                List<ParcelaResponseDto> parcelas,
                                List<HistoricoVigilanciaSummary> historico,
                                List<Product> catalog) {
        StringBuilder sb = new StringBuilder();
        sb.append("Eres un agrónomo experto en sanidad vegetal en México. ")
          .append("Tu tarea es generar recomendaciones de productos fitosanitarios personalizadas ")
          .append("para un agricultor, basándote en sus cultivos actuales y en el historial de ")
          .append("plagas registrado en su región.\n\n");

        sb.append("REGIÓN DEL AGRICULTOR: ").append(region).append("\n");
        sb.append("TEMPORADA ACTUAL: ").append(temporada.getDisplayName()).append(" (meses ")
          .append(temporada.getMeses()).append(")\n\n");

        sb.append("PARCELAS DEL AGRICULTOR (nombre | tipoCultivo | estadoParcela | hectáreas):\n");
        if (parcelas.isEmpty()) {
            sb.append("(sin parcelas registradas)\n");
        } else {
            for (ParcelaResponseDto p : parcelas) {
                sb.append("- ").append(nullSafe(p.nombre))
                  .append(" | ").append(nullSafe(p.tipoCultivo))
                  .append(" | ").append(nullSafe(p.estadoParcela))
                  .append(" | ").append(p.tamanoHectareas != null ? p.tamanoHectareas + " ha" : "-")
                  .append('\n');
            }
        }

        sb.append("\nHISTÓRICO REGIONAL DE PLAGAS (plaga | hospedante | especie | observaciones | ahosp_promedio):\n");
        if (historico.isEmpty()) {
            sb.append("(sin registros previos para esta región en esta temporada)\n");
        } else {
            for (HistoricoVigilanciaSummary h : historico) {
                sb.append("- ").append(nullSafe(h.getPlagaNombre()))
                  .append(" | ").append(nullSafe(h.getHospedanteNombre()))
                  .append(" | ").append(nullSafe(h.getEspecieNombre()))
                  .append(" | ").append(h.getObservaciones())
                  .append(" obs | ahosp=").append(h.getAhospPromedio() != null ? h.getAhospPromedio().toPlainString() : "0")
                  .append('\n');
            }
        }

        sb.append("\nPRODUCTOS DISPONIBLES EN EL MERCADO (nombre | categoría | precio MXN):\n");
        if (catalog.isEmpty()) {
            sb.append("(sin productos en el catálogo)\n");
        } else {
            for (Product p : catalog) {
                sb.append("- ").append(nullSafe(p.getName()))
                  .append(" | ").append(p.getCategory() != null ? nullSafe(p.getCategory().getName()) : "-")
                  .append(" | ").append(p.getLatestPrice() != null ? p.getLatestPrice().toPlainString() : "-")
                  .append('\n');
            }
        }

        sb.append("\nResponde EXCLUSIVAMENTE con un JSON válido (sin texto adicional ni bloques de código) ")
          .append("con la siguiente estructura:\n")
          .append("{\n")
          .append("  \"resumenSituacion\": string,\n")
          .append("  \"recomendaciones\": [\n")
          .append("    {\n")
          .append("      \"productoSugerido\": string,\n")
          .append("      \"plagaRelacionada\": string,\n")
          .append("      \"cultivoAfectado\": string,\n")
          .append("      \"nivelUrgencia\": \"Bajo\" | \"Medio\" | \"Alto\" | \"Critico\",\n")
          .append("      \"razonamiento\": string,\n")
          .append("      \"dosisEstimada\": string\n")
          .append("    }\n")
          .append("  ]\n")
          .append("}\n\n")
          .append("Reglas:\n")
          .append("- 'resumenSituacion': párrafo breve (2-3 oraciones) describiendo la situación fitosanitaria ")
          .append("actual del agricultor basada en sus cultivos y la región.\n")
          .append("- 'recomendaciones': 3 a 6 productos fitosanitarios concretos, ordenados por nivelUrgencia descendente.\n")
          .append("- PRIORIZA recomendar productos de la lista 'PRODUCTOS DISPONIBLES EN EL MERCADO' cuando sean ")
          .append("adecuados para las plagas/cultivos del agricultor. Cuando recomiendes uno del catálogo, copia ")
          .append("EXACTAMENTE su nombre tal cual aparece en la lista en el campo 'productoSugerido' (así el agricultor ")
          .append("podrá comprarlo directamente). Solo si ningún producto del catálogo aplica, sugiere un producto ")
          .append("comercial genérico.\n")
          .append("- 'dosisEstimada': incluye unidad (ej. '2 ml/L', '1 kg/ha', '500 g/100L').\n")
          .append("- Si el agricultor no tiene parcelas, basa las recomendaciones en la región y temporada.");

        return sb.toString();
    }

    private String buildRequestBody(String prompt) throws Exception {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode contents = root.putArray("contents");
        ObjectNode content = contents.addObject();
        ArrayNode parts = content.putArray("parts");
        parts.addObject().put("text", prompt);

        ObjectNode config = root.putObject("generationConfig");
        config.put("temperature", 0.35);
        config.put("responseMimeType", "application/json");

        return objectMapper.writeValueAsString(root);
    }

    // ─── Response parsing ─────────────────────────────────────────────────────

    private RecomendacionResult parseResponse(String response, String region, Temporada temporada,
                                               List<ParcelaResponseDto> parcelas,
                                               List<HistoricoVigilanciaSummary> historico,
                                               List<Product> catalog) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode candidates = root.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                return fallback(region, temporada, parcelas, historico, catalog);
            }
            JsonNode parts = candidates.get(0).path("content").path("parts");
            if (!parts.isArray() || parts.isEmpty()) {
                return fallback(region, temporada, parcelas, historico, catalog);
            }
            String text = stripCodeFences(parts.get(0).path("text").asText(""));
            JsonNode payload = objectMapper.readTree(text);

            String resumen = payload.path("resumenSituacion").asText(null);
            List<RecomendacionProducto> recs = new ArrayList<>();
            JsonNode arr = payload.path("recomendaciones");
            if (arr.isArray()) {
                for (JsonNode node : arr) {
                    recs.add(new RecomendacionProducto(
                            node.path("productoSugerido").asText(null),
                            node.path("plagaRelacionada").asText(null),
                            node.path("cultivoAfectado").asText(null),
                            node.path("nivelUrgencia").asText(null),
                            node.path("razonamiento").asText(null),
                            node.path("dosisEstimada").asText(null)
                    ));
                }
            }
            if (recs.isEmpty()) {
                return fallback(region, temporada, parcelas, historico, catalog);
            }
            return new RecomendacionResult(resumen, recs);
        } catch (Exception e) {
            LOG.errorf(e, "No se pudo parsear respuesta de Gemini (recomendaciones): %s", response);
            return fallback(region, temporada, parcelas, historico, catalog);
        }
    }

    // ─── Fallback heurístico ─────────────────────────────────────────────────

    private RecomendacionResult fallback(String region, Temporada temporada,
                                          List<ParcelaResponseDto> parcelas,
                                          List<HistoricoVigilanciaSummary> historico,
                                          List<Product> catalog) {
        List<RecomendacionProducto> recs = new ArrayList<>();
        int catalogSize = catalog.size();

        // Top plagas del historico → emparejar con un producto real del catálogo cuando exista
        int limite = Math.min(historico.size(), 4);
        for (int i = 0; i < limite; i++) {
            HistoricoVigilanciaSummary h = historico.get(i);
            String cultivo = parcelas.isEmpty() ? h.getHospedanteNombre()
                    : parcelas.get(0).tipoCultivo;
            String nivel = h.getObservaciones() >= 10 ? "Alto"
                    : h.getObservaciones() >= 5 ? "Medio" : "Bajo";

            Product prod = catalogSize > 0 ? catalog.get(i % catalogSize) : null;
            String producto = prod != null
                    ? prod.getName()
                    : "Consultar con distribuidor para " + nullSafe(h.getPlagaNombre());

            recs.add(new RecomendacionProducto(
                    producto,
                    h.getPlagaNombre(),
                    cultivo,
                    nivel,
                    "Se registraron " + h.getObservaciones()
                            + " observaciones de " + nullSafe(h.getPlagaNombre())
                            + " en " + region + " durante " + temporada.getDisplayName() + "."
                            + (prod != null
                                ? " Producto disponible en el mercado."
                                : " Consulta a tu agrónomo o distribuidor local para el producto adecuado."),
                    "Según etiqueta del producto"
            ));
        }

        // Sin historico pero con catálogo → recomendar productos disponibles como preventivo
        if (recs.isEmpty() && catalogSize > 0) {
            String cultivo = parcelas.isEmpty()
                    ? "tus cultivos"
                    : Optional.ofNullable(parcelas.get(0).tipoCultivo).orElse("tus cultivos");
            int n = Math.min(catalogSize, 3);
            for (int i = 0; i < n; i++) {
                recs.add(new RecomendacionProducto(
                        catalog.get(i).getName(),
                        "Prevención general",
                        cultivo,
                        "Bajo",
                        "Producto disponible en el mercado para manejo preventivo en " + cultivo + ".",
                        "Según etiqueta del producto"
                ));
            }
        }

        // Sin historico y sin catálogo, pero con parcelas → preventivo genérico
        if (recs.isEmpty() && !parcelas.isEmpty()) {
            String cultivo = Optional.ofNullable(parcelas.get(0).tipoCultivo).orElse("cultivo");
            recs.add(new RecomendacionProducto(
                    "Fungicida preventivo (consultar con distribuidor)",
                    "Prevención general",
                    cultivo,
                    "Bajo",
                    "Sin datos de plagas previas en la región para esta temporada. "
                            + "Se recomienda aplicar tratamiento preventivo según el cultivo: " + cultivo + ".",
                    "Según etiqueta del producto"
            ));
        }

        String resumen = recs.isEmpty()
                ? "No se encontraron datos suficientes para generar recomendaciones personalizadas. "
                + "Registra tus parcelas y reportes de vigilancia para mejorar las sugerencias."
                : "Recomendaciones básicas generadas localmente (servicio de IA no disponible). "
                + "Basadas en " + historico.size() + " observaciones regionales para "
                + temporada.getDisplayName() + " en " + region + ".";

        return new RecomendacionResult(resumen, recs);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private String stripCodeFences(String text) {
        if (text == null) return "";
        String t = text.trim();
        if (t.startsWith("```")) {
            int nl = t.indexOf('\n');
            if (nl >= 0) t = t.substring(nl + 1);
            if (t.endsWith("```")) t = t.substring(0, t.length() - 3);
        }
        return t.trim();
    }

    private String nullSafe(String v) {
        return v == null ? "-" : v;
    }
}
