package itesm.mx.application.dto;

public class ParcelaResponseDto {

    public Long parcelaId;
    public String nombreParcela;
    public Double tamanoHectareas;
    public String tipoCultivo;
    public String estadoParcela;

    public ParcelaResponseDto() {
    }

    public ParcelaResponseDto(Long parcelaId, String nombreParcela, Double tamanoHectareas,
                               String tipoCultivo, String estadoParcela) {
        this.parcelaId = parcelaId;
        this.nombreParcela = nombreParcela;
        this.tamanoHectareas = tamanoHectareas;
        this.tipoCultivo = tipoCultivo;
        this.estadoParcela = estadoParcela;
    }
}
