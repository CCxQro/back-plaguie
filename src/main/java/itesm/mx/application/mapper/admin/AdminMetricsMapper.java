package itesm.mx.application.mapper.admin;

import itesm.mx.application.dto.admin.AdminMetricsDto;
import itesm.mx.domain.models.admin.AdminMetrics;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AdminMetricsMapper {
    public AdminMetricsDto toDto(AdminMetrics domain) {
        if (domain == null) return null;
        return new AdminMetricsDto(
            domain.getTotalUsers(),
            domain.getTotalProducts(),
            domain.getTotalSurveillanceRecords(),
            domain.getRecentSurveillanceRecords(),
            domain.getTotalOrders(),
            domain.getRecentOrders()
        );
    }
}
