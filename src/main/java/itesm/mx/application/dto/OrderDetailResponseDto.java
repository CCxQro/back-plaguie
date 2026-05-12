package itesm.mx.application.dto;

public class OrderDetailResponseDto {
    public Long detailId;
    public Long productId;
    public String productName;
    public Integer quantity;
    public Float unitPrice;

    public OrderDetailResponseDto() {}

    public OrderDetailResponseDto(Long detailId, Long productId, String productName,
                                   Integer quantity, Float unitPrice) {
        this.detailId = detailId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
}
