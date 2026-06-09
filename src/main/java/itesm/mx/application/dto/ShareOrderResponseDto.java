package itesm.mx.application.dto;

public class ShareOrderResponseDto {

    public Long orderId;
    public Boolean shared;
    public String message;

    public ShareOrderResponseDto() {}

    public ShareOrderResponseDto(Long orderId, Boolean shared, String message) {
        this.orderId = orderId;
        this.shared = shared;
        this.message = message;
    }
}
