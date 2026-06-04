package itesm.mx.application.dto.sales;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Sales summary for a seller")
public class SalesSummaryDto {

    @Schema(description = "Total earnings in the specified period")
    public BigDecimal totalEarnings;

    @Schema(description = "Total number of orders in the specified period")
    public long totalOrders;

    @Schema(description = "Total number of products sold in the specified period")
    public long totalProductsSold;

    @Schema(description = "Total remaining inventory across all active products")
    public long remainingInventoryTotal;

    public SalesSummaryDto() {}

    public SalesSummaryDto(BigDecimal totalEarnings, long totalOrders, long totalProductsSold, long remainingInventoryTotal) {
        this.totalEarnings = totalEarnings;
        this.totalOrders = totalOrders;
        this.totalProductsSold = totalProductsSold;
        this.remainingInventoryTotal = remainingInventoryTotal;
    }
}
