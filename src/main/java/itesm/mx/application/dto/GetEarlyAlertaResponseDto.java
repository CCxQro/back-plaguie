package itesm.mx.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

/**
 * A validated early alert enriched with its state, for the seller's
 * early-alerts feed with client-side filtering (HU-26 CA-02).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetEarlyAlertaResponseDto {

    public Long alertaId;
    public String titulo;
    public String descripcion;
    public Long ubicacionId;
    public Long stateId;
    public String stateName;
    public String tipoPlaga;
    public BigDecimal hectareas;
    public String severidad;
    public Long reportedByUserId;
    public String createdAt;
    public Long statusId;
    public String statusName;
    public Long validatedByUserId;
    public String validatedAt;

    public GetEarlyAlertaResponseDto() {
    }

    public GetEarlyAlertaResponseDto(Long alertaId, String titulo, String descripcion,
                                      Long ubicacionId, Long stateId, String stateName,
                                      String tipoPlaga, BigDecimal hectareas, String severidad,
                                      Long reportedByUserId, String createdAt,
                                      Long statusId, String statusName,
                                      Long validatedByUserId, String validatedAt) {
        this.alertaId = alertaId;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.ubicacionId = ubicacionId;
        this.stateId = stateId;
        this.stateName = stateName;
        this.tipoPlaga = tipoPlaga;
        this.hectareas = hectareas;
        this.severidad = severidad;
        this.reportedByUserId = reportedByUserId;
        this.createdAt = createdAt;
        this.statusId = statusId;
        this.statusName = statusName;
        this.validatedByUserId = validatedByUserId;
        this.validatedAt = validatedAt;
    }
}
