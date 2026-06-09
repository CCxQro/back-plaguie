package itesm.mx.application.dto.admin;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Metrics for Admin Dashboard")
public class AdminMetricsDto {

    @Schema(description = "Total registered users")
    public long totalUsers;

    @Schema(description = "Total active products")
    public long totalProducts;

    @Schema(description = "Total surveillance records")
    public long totalSurveillanceRecords;

    @Schema(description = "Surveillance records validated in the last 30 days")
    public long recentSurveillanceRecords;

    @Schema(description = "Total orders placed")
    public long totalOrders;

    @Schema(description = "Orders placed in the last 30 days")
    public long recentOrders;

    public AdminMetricsDto() {}

    public AdminMetricsDto(long totalUsers, long totalProducts, long totalSurveillanceRecords, long recentSurveillanceRecords, long totalOrders, long recentOrders) {
        this.totalUsers = totalUsers;
        this.totalProducts = totalProducts;
        this.totalSurveillanceRecords = totalSurveillanceRecords;
        this.recentSurveillanceRecords = recentSurveillanceRecords;
        this.totalOrders = totalOrders;
        this.recentOrders = recentOrders;
    }
}
