package itesm.mx.application.dto;

public class GetProviderResponseDto {
    public Long providerId;
    public String name;
    public Long userId;

    public GetProviderResponseDto() {}

    public GetProviderResponseDto(Long providerId, String name, Long userId) {
        this.providerId = providerId;
        this.name = name;
        this.userId = userId;
    }
}