package itesm.mx.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

/**
 * A validated surveillance observation for the interactive pest map (HU-27).
 * The client aggregates these by zone and applies filters.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetPestMapPointDto {

    public Long vigilanciaId;
    public Double latitude;
    public Double longitude;
    public String plagaNombre;
    public String hospedanteNombre;
    public String especieNombre;
    public String estadoNombre;
    public String municipioNombre;
    public BigDecimal ahosp;
    public String validatedAt;

    public GetPestMapPointDto() {
    }

    public GetPestMapPointDto(Long vigilanciaId, Double latitude, Double longitude,
                              String plagaNombre, String hospedanteNombre, String especieNombre,
                              String estadoNombre, String municipioNombre, BigDecimal ahosp,
                              String validatedAt) {
        this.vigilanciaId = vigilanciaId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.plagaNombre = plagaNombre;
        this.hospedanteNombre = hospedanteNombre;
        this.especieNombre = especieNombre;
        this.estadoNombre = estadoNombre;
        this.municipioNombre = municipioNombre;
        this.ahosp = ahosp;
        this.validatedAt = validatedAt;
    }
}
