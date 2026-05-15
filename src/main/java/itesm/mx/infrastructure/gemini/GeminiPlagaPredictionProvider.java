package itesm.mx.infrastructure.gemini;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import itesm.mx.domain.models.reporte.HistoricoVigilanciaSummary;
import itesm.mx.domain.models.reporte.PrediccionPlaga;
import itesm.mx.domain.models.reporte.Temporada;
import itesm.mx.domain.repository.reporte.PrediccionPlagaProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class GeminiPlagaPredictionProvider implements PrediccionPlagaProvider {

    private static final Logger LOG = Logger.getLogger(GeminiPlagaPredictionProvider.class);

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

    @Override
    public PrediccionResult predict(String region, Temporada temporada, List<HistoricoVigilanciaSummary> historico) {
        if (apiKey == null || apiKey.isBlank()) {
            LOG.warn("GEMINI_API_KEY no configurada: regresando prediccion heuristica basada en historico");
            return fallbackHeuristic(region, temporada, historico);
        }

        try {
            String prompt = buildPrompt(region, temporada, historico);
            String requestBody = buildRequestBody(prompt);
            String endpoint = baseUrl + "/v1beta/models/" + URLEncoder.encode(model, StandardCharsets.UTF_8)
                    + ":generateContent?key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);

            String response = geminiHttpClient.generateContent(endpoint, requestBody, timeoutSeconds);
            return parseResponse(response, region, temporada, historico);
        } catch (Exception e) {
            LOG.errorf(e, "Error invocando Gemini para region=%s temporada=%s; usando fallback", region, temporada);
            return fallbackHeuristic(region, temporada, historico);
        }
    }

    private String buildPrompt(String region, Temporada temporada, List<HistoricoVigilanciaSummary> historico) {
        StringBuilder sb = new StringBuilder();
        sb.append("Eres un agronomo experto en sanidad vegetal en Mexico. ")
                .append("Con base en el siguiente historico de vigilancia fitosanitaria, predice las plagas ")
                .append("mas probables para la region '")
                .append(region)
                .append("' durante la temporada de ")
                .append(temporada.getDisplayName().toLowerCase())
                .append(" (meses ")
                .append(temporada.getMeses())
                .append(").\n\n");

        sb.append("HISTORICO (plaga | hospedante | especie | estado | municipio | observaciones | ahosp_promedio):\n");
        if (historico.isEmpty()) {
            sb.append("(sin registros previos para esta region en esta temporada)\n");
        } else {
            for (HistoricoVigilanciaSummary h : historico) {
                sb.append("- ")
                        .append(nullSafe(h.getPlagaNombre())).append(" | ")
                        .append(nullSafe(h.getHospedanteNombre())).append(" | ")
                        .append(nullSafe(h.getEspecieNombre())).append(" | ")
                        .append(nullSafe(h.getEstadoNombre())).append(" | ")
                        .append(nullSafe(h.getMunicipioNombre())).append(" | ")
                        .append(h.getObservaciones()).append(" | ")
                        .append(h.getAhospPromedio() != null ? h.getAhospPromedio().toPlainString() : "0")
                        .append('\n');
            }
        }

        sb.append("\nResponde EXCLUSIVAMENTE con un JSON valido (sin texto adicional ni bloques de codigo) ")
                .append("con la siguiente estructura:\n")
                .append("{\n")
                .append("  \"resumenEjecutivo\": string,\n")
                .append("  \"predicciones\": [\n")
                .append("    {\n")
                .append("      \"plagaNombre\": string,\n")
                .append("      \"probabilidad\": number (0-100),\n")
                .append("      \"periodoEstimado\": string,\n")
                .append("      \"nivelRiesgo\": \"Bajo\" | \"Medio\" | \"Alto\" | \"Critico\",\n")
                .append("      \"hospedanteAfectado\": string,\n")
                .append("      \"justificacion\": string,\n")
                .append("      \"productoSugerido\": string\n")
                .append("    }\n")
                .append("  ],\n")
                .append("  \"recomendaciones\": [string]\n")
                .append("}\n")
                .append("- 'predicciones': 3 a 7 plagas, ordenadas por probabilidad descendente.\n")
                .append("- 'recomendaciones': 4 a 6 acciones concretas para el ejecutivo de ventas ")
                .append("(visitas, posicionamiento de producto, manejo de inventario, comunicacion a productores). ")
                .append("Cada accion debe iniciar con un verbo en infinitivo. ")
                .append("Considera la temporada, los hospedantes afectados y los productos sugeridos.");

        return sb.toString();
    }

    private String buildRequestBody(String prompt) throws Exception {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode contents = root.putArray("contents");
        ObjectNode content = contents.addObject();
        ArrayNode parts = content.putArray("parts");
        parts.addObject().put("text", prompt);

        ObjectNode generationConfig = root.putObject("generationConfig");
        generationConfig.put("temperature", 0.4);
        generationConfig.put("responseMimeType", "application/json");

        return objectMapper.writeValueAsString(root);
    }

    private PrediccionResult parseResponse(String response, String region, Temporada temporada, List<HistoricoVigilanciaSummary> historico) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode candidates = root.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                LOG.warnf("Gemini sin candidates; usando fallback. Respuesta=%s", response);
                return fallbackHeuristic(region, temporada, historico);
            }
            JsonNode parts = candidates.get(0).path("content").path("parts");
            if (!parts.isArray() || parts.isEmpty()) {
                return fallbackHeuristic(region, temporada, historico);
            }
            String text = parts.get(0).path("text").asText("");
            String cleaned = stripCodeFences(text);
            JsonNode payload = objectMapper.readTree(cleaned);

            String resumen = payload.path("resumenEjecutivo").asText("");
            List<PrediccionPlaga> predicciones = new ArrayList<>();
            JsonNode arr = payload.path("predicciones");
            if (arr.isArray()) {
                for (JsonNode node : arr) {
                    predicciones.add(new PrediccionPlaga(
                            node.path("plagaNombre").asText(null),
                            node.has("probabilidad") && !node.get("probabilidad").isNull()
                                    ? node.get("probabilidad").asInt()
                                    : null,
                            node.path("periodoEstimado").asText(null),
                            node.path("nivelRiesgo").asText(null),
                            node.path("hospedanteAfectado").asText(null),
                            node.path("justificacion").asText(null),
                            node.path("productoSugerido").asText(null)
                    ));
                }
            }
            if (predicciones.isEmpty()) {
                return fallbackHeuristic(region, temporada, historico);
            }
            List<String> recomendaciones = new ArrayList<>();
            JsonNode recArr = payload.path("recomendaciones");
            if (recArr.isArray()) {
                for (JsonNode rec : recArr) {
                    String item = rec.asText("").trim();
                    if (!item.isEmpty()) {
                        recomendaciones.add(item);
                    }
                }
            }
            if (recomendaciones.isEmpty()) {
                recomendaciones = deriveRecomendaciones(predicciones, region, temporada);
            }
            return new PrediccionResult(resumen, predicciones, recomendaciones);
        } catch (Exception e) {
            LOG.errorf(e, "No se pudo parsear respuesta de Gemini: %s", response);
            return fallbackHeuristic(region, temporada, historico);
        }
    }

    private PrediccionResult fallbackHeuristic(String region, Temporada temporada, List<HistoricoVigilanciaSummary> historico) {
        if (historico.isEmpty()) {
            return new PrediccionResult(
                    "Sin historico disponible para la region '" + region + "' en " + temporada.getDisplayName()
                            + ". Recolectar mas datos de campo para mejorar la prediccion.",
                    List.of()
            );
        }

        long total = historico.stream().mapToLong(HistoricoVigilanciaSummary::getObservaciones).sum();
        List<PrediccionPlaga> predicciones = new ArrayList<>();
        int limite = Math.min(historico.size(), 5);
        for (int i = 0; i < limite; i++) {
            HistoricoVigilanciaSummary h = historico.get(i);
            int probabilidad = total == 0 ? 0 : (int) Math.round((h.getObservaciones() * 100.0) / total);
            String nivel = probabilidad >= 60 ? "Alto" : probabilidad >= 30 ? "Medio" : "Bajo";

            predicciones.add(new PrediccionPlaga(
                    Optional.ofNullable(h.getPlagaNombre()).orElse("Plaga sin nombre"),
                    probabilidad,
                    temporada.getDisplayName(),
                    nivel,
                    h.getHospedanteNombre(),
                    "Estimacion basada en historico local: " + h.getObservaciones() + " observaciones registradas.",
                    null
            ));
        }

        String resumen = "Prediccion heuristica (sin Gemini) basada en " + total + " observaciones historicas para "
                + region + " en " + temporada.getDisplayName() + ".";
        return new PrediccionResult(resumen, predicciones, deriveRecomendaciones(predicciones, region, temporada));
    }

    /**
     * Recomendaciones derivadas localmente cuando Gemini no las provee.
     * Se basan en los hospedantes y productos sugeridos ya presentes en las predicciones.
     */
    private List<String> deriveRecomendaciones(List<PrediccionPlaga> predicciones, String region, Temporada temporada) {
        List<String> recs = new ArrayList<>();
        if (predicciones == null || predicciones.isEmpty()) {
            return recs;
        }
        PrediccionPlaga top = predicciones.get(0);
        String topPlaga = Optional.ofNullable(top.getPlagaNombre()).orElse("la plaga principal");
        String topHosp = Optional.ofNullable(top.getHospedanteAfectado()).orElse("cultivos relevantes");

        recs.add("Intensificar visitas de monitoreo en zonas productoras de " + topHosp + " durante "
                + temporada.getDisplayName().toLowerCase() + ".");
        recs.add("Priorizar campanas comerciales en torno a " + topPlaga + " en " + region + ".");

        String productosTop = predicciones.stream()
                .map(PrediccionPlaga::getProductoSugerido)
                .filter(p -> p != null && !p.isBlank())
                .findFirst()
                .orElse(null);
        if (productosTop != null) {
            recs.add("Asegurar inventario disponible de " + productosTop + " antes del periodo critico.");
        }
        recs.add("Comunicar a productores recurrentes el periodo estimado de mayor presion fitosanitaria.");
        return recs;
    }

    private String stripCodeFences(String text) {
        if (text == null) {
            return "";
        }
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline >= 0) {
                trimmed = trimmed.substring(firstNewline + 1);
            }
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3);
            }
        }
        return trimmed.trim();
    }

    private String nullSafe(String value) {
        return value == null ? "-" : value;
    }
}
