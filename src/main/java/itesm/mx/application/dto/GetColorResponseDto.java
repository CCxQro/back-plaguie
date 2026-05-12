package itesm.mx.application.dto;

public class GetColorResponseDto {
    public Long colorId;
    public String name;
    public String hexa;

    public GetColorResponseDto() {}

    public GetColorResponseDto(Long colorId, String name, String hexa) {
        this.colorId = colorId;
        this.name = name;
        this.hexa = hexa;
    }
}