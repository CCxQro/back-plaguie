package itesm.mx.domain.repository.sales;

import itesm.mx.domain.models.sales.SalesSummary;
import java.time.LocalDateTime;

public interface SalesDashboardRepository {
    SalesSummary getSalesSummary(Long sellerId, LocalDateTime startDate, LocalDateTime endDate);
}
