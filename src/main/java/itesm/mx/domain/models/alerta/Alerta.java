package itesm.mx.domain.models.alerta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Alerta {
    private Long alertaId;
    private String titulo;
    private String descripcion;
    private Long ubicacionId;
    private String tipoPlaga;
    private BigDecimal hectareas;
    private String severidad;
    private Long reportedByUserId;
    private LocalDateTime createdAt;
    private Long statusId;
    private String statusName;
    private Long validatedByUserId;
    private LocalDateTime validatedAt;

    public Alerta() {
    }

    public Alerta(Long alertaId, String titulo, String descripcion, Long ubicacionId,
                  String tipoPlaga, BigDecimal hectareas, String severidad,
                  Long reportedByUserId, LocalDateTime createdAt,
                  Long statusId, String statusName,
                  Long validatedByUserId, LocalDateTime validatedAt) {
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

    public Long getAlertaId() {
        return alertaId;
    }

    public void setAlertaId(Long alertaId) {
        this.alertaId = alertaId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Long getUbicacionId() {
        return ubicacionId;
    }

    public void setUbicacionId(Long ubicacionId) {
        this.ubicacionId = ubicacionId;
    }

    public String getTipoPlaga() {
        return tipoPlaga;
    }

    public void setTipoPlaga(String tipoPlaga) {
        this.tipoPlaga = tipoPlaga;
    }

    public BigDecimal getHectareas() {
        return hectareas;
    }

    public void setHectareas(BigDecimal hectareas) {
        this.hectareas = hectareas;
    }

    public String getSeveridad() {
        return severidad;
    }

    public void setSeveridad(String severidad) {
        this.severidad = severidad;
    }

    public Long getReportedByUserId() {
        return reportedByUserId;
    }

    public void setReportedByUserId(Long reportedByUserId) {
        this.reportedByUserId = reportedByUserId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getStatusId() {
        return statusId;
    }

    public void setStatusId(Long statusId) {
        this.statusId = statusId;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public Long getValidatedByUserId() {
        return validatedByUserId;
    }

    public void setValidatedByUserId(Long validatedByUserId) {
        this.validatedByUserId = validatedByUserId;
    }

    public LocalDateTime getValidatedAt() {
        return validatedAt;
    }

    public void setValidatedAt(LocalDateTime validatedAt) {
        this.validatedAt = validatedAt;
    }
}
