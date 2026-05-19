package itesm.mx.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ClientAlertaSummaryDto {
    public Long alertaId;
    public String titulo;
    public String tipoPlaga;
    public String severidad;
    public BigDecimal hectareas;
    public LocalDateTime createdAt;
    public Long statusId;
    public String statusName;
    public Boolean isActive;

    public ClientAlertaSummaryDto() {}

    public ClientAlertaSummaryDto(Long alertaId, String titulo, String tipoPlaga, String severidad,
                                   BigDecimal hectareas, LocalDateTime createdAt,
                                   Long statusId, String statusName, Boolean isActive) {
        this.alertaId = alertaId;
        this.titulo = titulo;
        this.tipoPlaga = tipoPlaga;
        this.severidad = severidad;
        this.hectareas = hectareas;
        this.createdAt = createdAt;
        this.statusId = statusId;
        this.statusName = statusName;
        this.isActive = isActive;
    }
}
