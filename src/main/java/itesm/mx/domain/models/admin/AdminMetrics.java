package itesm.mx.domain.models.admin;

public class AdminMetrics {
    private long totalUsers;
    private long totalProducts;
    private long totalSurveillanceRecords;
    private long recentSurveillanceRecords;
    private long totalOrders;
    private long recentOrders;

    public AdminMetrics() {}

    public AdminMetrics(long totalUsers, long totalProducts, long totalSurveillanceRecords, long recentSurveillanceRecords, long totalOrders, long recentOrders) {
        this.totalUsers = totalUsers;
        this.totalProducts = totalProducts;
        this.totalSurveillanceRecords = totalSurveillanceRecords;
        this.recentSurveillanceRecords = recentSurveillanceRecords;
        this.totalOrders = totalOrders;
        this.recentOrders = recentOrders;
    }

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getTotalProducts() { return totalProducts; }
    public void setTotalProducts(long totalProducts) { this.totalProducts = totalProducts; }

    public long getTotalSurveillanceRecords() { return totalSurveillanceRecords; }
    public void setTotalSurveillanceRecords(long totalSurveillanceRecords) { this.totalSurveillanceRecords = totalSurveillanceRecords; }

    public long getRecentSurveillanceRecords() { return recentSurveillanceRecords; }
    public void setRecentSurveillanceRecords(long recentSurveillanceRecords) { this.recentSurveillanceRecords = recentSurveillanceRecords; }

    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }

    public long getRecentOrders() { return recentOrders; }
    public void setRecentOrders(long recentOrders) { this.recentOrders = recentOrders; }
}
