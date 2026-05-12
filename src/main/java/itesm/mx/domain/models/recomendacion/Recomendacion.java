package itesm.mx.domain.models.recomendacion;

import java.time.LocalDateTime;

public class Recomendacion {
    private Long recomendacionId;
    private String titulo;
    private String descripcion;
    private String tipoPlaga;
    private String productosRecomendados;
    private Long reportedByUserId;
    private LocalDateTime createdAt;
    private Long statusId;
    private String statusName;
    private Long validatedByUserId;
    private LocalDateTime validatedAt;

    public Recomendacion() {
    }

    public Recomendacion(Long recomendacionId, String titulo, String descripcion,
                         String tipoPlaga, String productosRecomendados,
                         Long reportedByUserId, LocalDateTime createdAt,
                         Long statusId, String statusName,
                         Long validatedByUserId, LocalDateTime validatedAt) {
        this.recomendacionId = recomendacionId;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.tipoPlaga = tipoPlaga;
        this.productosRecomendados = productosRecomendados;
        this.reportedByUserId = reportedByUserId;
        this.createdAt = createdAt;
        this.statusId = statusId;
        this.statusName = statusName;
        this.validatedByUserId = validatedByUserId;
        this.validatedAt = validatedAt;
    }

    public Long getRecomendacionId() {
        return recomendacionId;
    }

    public void setRecomendacionId(Long recomendacionId) {
        this.recomendacionId = recomendacionId;
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

    public String getTipoPlaga() {
        return tipoPlaga;
    }

    public void setTipoPlaga(String tipoPlaga) {
        this.tipoPlaga = tipoPlaga;
    }

    public String getProductosRecomendados() {
        return productosRecomendados;
    }

    public void setProductosRecomendados(String productosRecomendados) {
        this.productosRecomendados = productosRecomendados;
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
