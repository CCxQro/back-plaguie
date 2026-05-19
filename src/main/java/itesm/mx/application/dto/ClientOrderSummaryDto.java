package itesm.mx.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ClientOrderSummaryDto {
    public Integer totalOrders;
    public BigDecimal totalAmount;
    public LocalDateTime lastOrderDate;
    public String lastOrderStatus;

    public ClientOrderSummaryDto() {}

    public ClientOrderSummaryDto(Integer totalOrders, BigDecimal totalAmount,
                                  LocalDateTime lastOrderDate, String lastOrderStatus) {
        this.totalOrders = totalOrders;
        this.totalAmount = totalAmount;
        this.lastOrderDate = lastOrderDate;
        this.lastOrderStatus = lastOrderStatus;
    }
}
