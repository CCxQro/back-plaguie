package itesm.mx.application.dto;

public class GetRecomendacionResponseDto {
    public Long recomendacionId;
    public String titulo;
    public String descripcion;
    public String tipoPlaga;
    public String productosRecomendados;
    public Long reportedByUserId;
    public String createdAt;
    public Long statusId;
    public String statusName;
    public Long validatedByUserId;
    public String validatedAt;

    public GetRecomendacionResponseDto() {
    }

    public GetRecomendacionResponseDto(Long recomendacionId, String titulo, String descripcion,
                                       String tipoPlaga, String productosRecomendados,
                                       Long reportedByUserId, String createdAt,
                                       Long statusId, String statusName,
                                       Long validatedByUserId, String validatedAt) {
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
}
