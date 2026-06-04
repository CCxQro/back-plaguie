package itesm.mx.application.usecase.sales;

import itesm.mx.application.dto.sales.InventoryAlertDto;
import itesm.mx.domain.models.sales.InventoryAlert;
import itesm.mx.domain.repository.sales.InventoryAlertRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class GetInventoryAlertsUseCase {

    private final InventoryAlertRepository repository;

    @Inject
    public GetInventoryAlertsUseCase(InventoryAlertRepository repository) {
        this.repository = repository;
    }

    public List<InventoryAlertDto> execute(Long sellerId, int threshold) {
        List<InventoryAlert> alerts = repository.getLowStockAlerts(sellerId, threshold);
        return alerts.stream()
                .map(a -> new InventoryAlertDto(a.getSkuSellerId(), a.getProductName(), a.getSku(), a.getRemainingStock()))
                .collect(Collectors.toList());
    }
}
