package itesm.mx.application.usecase.sales;

import itesm.mx.application.dto.sales.SalesSummaryDto;
import itesm.mx.domain.models.sales.SalesSummary;
import itesm.mx.domain.repository.sales.SalesDashboardRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;

@ApplicationScoped
public class GetSalesSummaryUseCase {

    private final SalesDashboardRepository repository;

    @Inject
    public GetSalesSummaryUseCase(SalesDashboardRepository repository) {
        this.repository = repository;
    }

    public SalesSummaryDto execute(Long sellerId, LocalDateTime startDate, LocalDateTime endDate) {
        SalesSummary domain = repository.getSalesSummary(sellerId, startDate, endDate);
        return new SalesSummaryDto(
                domain.getTotalEarnings(),
                domain.getTotalOrders(),
                domain.getTotalProductsSold(),
                domain.getRemainingInventoryTotal()
        );
    }
}
