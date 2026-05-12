package itesm.mx.application.dto;

import java.math.BigDecimal;

public class GetAlertaResponseDto {
    public Long alertaId;
    public String titulo;
    public String descripcion;
    public Long ubicacionId;
    public String tipoPlaga;
    public BigDecimal hectareas;
    public String severidad;
    public Long reportedByUserId;
    public String createdAt;
    public Long statusId;
    public String statusName;
    public Long validatedByUserId;
    public String validatedAt;

    public GetAlertaResponseDto() {
    }

    public GetAlertaResponseDto(Long alertaId, String titulo, String descripcion,
                                Long ubicacionId, String tipoPlaga, BigDecimal hectareas,
                                String severidad, Long reportedByUserId, String createdAt,
                                Long statusId, String statusName,
                                Long validatedByUserId, String validatedAt) {
        this.alertaId = alertaId;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.ubicacionId = ubicacionId;
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
