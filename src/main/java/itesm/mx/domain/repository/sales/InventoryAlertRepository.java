package itesm.mx.domain.repository.sales;

import itesm.mx.domain.models.sales.InventoryAlert;
import java.util.List;

public interface InventoryAlertRepository {
    List<InventoryAlert> getLowStockAlerts(Long sellerId, int threshold);
}
