package itesm.mx.application.dto;

public class OrderStatusResponseDto {
    public Long orderStatusId;
    public String estado;

    public OrderStatusResponseDto() {}

    public OrderStatusResponseDto(Long orderStatusId, String estado) {
        this.orderStatusId = orderStatusId;
        this.estado = estado;
    }
}
