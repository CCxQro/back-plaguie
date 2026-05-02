package itesm.mx.application.dto;

public class GetStatusResponseDto {
    public Long statusId;
    public String name;

    public GetStatusResponseDto() {}

    public GetStatusResponseDto(Long statusId, String name) {
        this.statusId = statusId;
        this.name = name;
    }
}