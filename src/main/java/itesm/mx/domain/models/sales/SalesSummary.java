package itesm.mx.domain.models.sales;

import java.math.BigDecimal;

public class SalesSummary {
    private BigDecimal totalEarnings;
    private long totalOrders;
    private long totalProductsSold;
    private long remainingInventoryTotal;

    public SalesSummary(BigDecimal totalEarnings, long totalOrders, long totalProductsSold, long remainingInventoryTotal) {
        this.totalEarnings = totalEarnings != null ? totalEarnings : BigDecimal.ZERO;
        this.totalOrders = totalOrders;
        this.totalProductsSold = totalProductsSold;
        this.remainingInventoryTotal = remainingInventoryTotal;
    }

    public BigDecimal getTotalEarnings() { return totalEarnings; }
    public long getTotalOrders() { return totalOrders; }
    public long getTotalProductsSold() { return totalProductsSold; }
    public long getRemainingInventoryTotal() { return remainingInventoryTotal; }
}
