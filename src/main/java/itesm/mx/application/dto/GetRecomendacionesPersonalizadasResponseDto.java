package itesm.mx.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Response for GET /api/recomendaciones/personalizadas (HU-24 CA-02).
 * Contains an LLM-generated summary and a list of personalized product
 * recommendations for the authenticated farmer.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetRecomendacionesPersonalizadasResponseDto {

    /** LLM-generated natural-language overview of the farmer's pest situation. */
    public String resumenSituacion;

    /** Ordered list of product recommendations (most urgent first). */
    public List<RecomendacionProductoDto> recomendaciones;

    /** Names of the parcelas that were analyzed. */
    public List<String> parcelasAnalizadas;

    /** State / region used to pull historical surveillance data. */
    public String region;

    /** Season used for the historical data query. */
    public String temporada;

    public GetRecomendacionesPersonalizadasResponseDto() {
    }

    public GetRecomendacionesPersonalizadasResponseDto(
            String resumenSituacion,
            List<RecomendacionProductoDto> recomendaciones,
            List<String> parcelasAnalizadas,
            String region,
            String temporada) {
        this.resumenSituacion = resumenSituacion;
        this.recomendaciones = recomendaciones;
        this.parcelasAnalizadas = parcelasAnalizadas;
        this.region = region;
        this.temporada = temporada;
    }

    // ─── Inner DTO ───────────────────────────────────────────────────────────

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RecomendacionProductoDto {
        public String productoSugerido;
        public String plagaRelacionada;
        public String cultivoAfectado;
        /** Bajo | Medio | Alto | Critico */
        public String nivelUrgencia;
        public String razonamiento;
        public String dosisEstimada;

        public RecomendacionProductoDto() {
        }

        public RecomendacionProductoDto(String productoSugerido, String plagaRelacionada,
                                         String cultivoAfectado, String nivelUrgencia,
                                         String razonamiento, String dosisEstimada) {
            this.productoSugerido = productoSugerido;
            this.plagaRelacionada = plagaRelacionada;
            this.cultivoAfectado = cultivoAfectado;
            this.nivelUrgencia = nivelUrgencia;
            this.razonamiento = razonamiento;
            this.dosisEstimada = dosisEstimada;
        }
    }
}
