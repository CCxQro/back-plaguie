package itesm.mx.infrastructure.persistence.repository.sales;

import itesm.mx.domain.models.sales.InventoryAlert;
import itesm.mx.domain.repository.sales.InventoryAlertRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class InventoryAlertRepositoryImpl implements InventoryAlertRepository {

    private final EntityManager em;

    @Inject
    public InventoryAlertRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public List<InventoryAlert> getLowStockAlerts(Long sellerId, int threshold) {
        String query = "SELECT p.skuSellerId, p.name, p.sku, " +
                       "(COALESCE((SELECT SUM(i1.cantidad) FROM InventoryEntity i1 WHERE i1.skuSellerId = p.skuSellerId AND i1.actionId = 1), 0) - " +
                       " COALESCE((SELECT SUM(i2.cantidad) FROM InventoryEntity i2 WHERE i2.skuSellerId = p.skuSellerId AND i2.actionId = 2), 0)) as remainingStock " +
                       "FROM ProductEntity p " +
                       "WHERE p.sellerId = :sellerId AND p.isActive = true " +
                       "HAVING (COALESCE((SELECT SUM(i1.cantidad) FROM InventoryEntity i1 WHERE i1.skuSellerId = p.skuSellerId AND i1.actionId = 1), 0) - " +
                       "        COALESCE((SELECT SUM(i2.cantidad) FROM InventoryEntity i2 WHERE i2.skuSellerId = p.skuSellerId AND i2.actionId = 2), 0)) <= :threshold";

        List<Object[]> results = em.createQuery(query, Object[].class)
                .setParameter("sellerId", sellerId)
                .setParameter("threshold", (long) threshold)
                .getResultList();

        List<InventoryAlert> alerts = new ArrayList<>();
        for (Object[] row : results) {
            Long skuSellerId = (Long) row[0];
            String productName = (String) row[1];
            String sku = (String) row[2];
            Long remainingStock = (Long) row[3];
            alerts.add(new InventoryAlert(skuSellerId, productName, sku, remainingStock != null ? remainingStock.intValue() : 0));
        }

        return alerts;
    }
}
