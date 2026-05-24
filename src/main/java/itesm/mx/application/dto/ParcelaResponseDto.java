package itesm.mx.application.dto;

public class ParcelaResponseDto {

    public Long parcelaId;
    public String nombre;
    public Double tamanoHectareas;
    public String tipoCultivo;
    public String estadoParcela;
    public Boolean isActive;

    public ParcelaResponseDto() {
    }

    public ParcelaResponseDto(Long parcelaId, String nombre, Double tamanoHectareas,
                               String tipoCultivo, String estadoParcela, Boolean isActive) {
        this.parcelaId = parcelaId;
        this.nombre = nombre;
        this.tamanoHectareas = tamanoHectareas;
        this.tipoCultivo = tipoCultivo;
        this.estadoParcela = estadoParcela;
        this.isActive = isActive;
    }
}
