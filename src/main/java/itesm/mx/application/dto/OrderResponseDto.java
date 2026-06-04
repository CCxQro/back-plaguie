package itesm.mx.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponseDto {
    public Long orderId;
    public Long farmerId;
    public String farmerName;
    public Long sellerId;
    public String sellerName;
    public LocalDateTime orderDate;
    public Long orderStatusId;
    public String orderStatusName;
    public BigDecimal totalAmount;
    public Boolean providerShared;
    public List<OrderDetailResponseDto> details;

    public OrderResponseDto() {}

    public OrderResponseDto(Long orderId, Long farmerId, String farmerName, Long sellerId,
                             String sellerName, LocalDateTime orderDate, Long orderStatusId,
                             String orderStatusName, BigDecimal totalAmount, Boolean providerShared,
                             List<OrderDetailResponseDto> details) {
        this.orderId = orderId;
        this.farmerId = farmerId;
        this.farmerName = farmerName;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.orderDate = orderDate;
        this.orderStatusId = orderStatusId;
        this.orderStatusName = orderStatusName;
        this.totalAmount = totalAmount;
        this.providerShared = providerShared;
        this.details = details;
    }
}
