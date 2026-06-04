package itesm.mx.infrastructure.persistence.repository.sales;

import itesm.mx.domain.models.sales.SalesSummary;
import itesm.mx.domain.repository.sales.SalesDashboardRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@ApplicationScoped
public class SalesDashboardRepositoryImpl implements SalesDashboardRepository {

    private final EntityManager em;

    @Inject
    public SalesDashboardRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public SalesSummary getSalesSummary(Long sellerId, LocalDateTime startDate, LocalDateTime endDate) {
        String earningsQuery = "SELECT SUM(o.totalAmount) FROM OrderEntity o WHERE o.sellerId = :sellerId AND o.orderDate >= :start AND o.orderDate <= :end";
        BigDecimal totalEarnings = em.createQuery(earningsQuery, BigDecimal.class)
                .setParameter("sellerId", sellerId)
                .setParameter("start", startDate)
                .setParameter("end", endDate)
                .getSingleResult();

        String ordersQuery = "SELECT COUNT(o) FROM OrderEntity o WHERE o.sellerId = :sellerId AND o.orderDate >= :start AND o.orderDate <= :end";
        Long totalOrders = em.createQuery(ordersQuery, Long.class)
                .setParameter("sellerId", sellerId)
                .setParameter("start", startDate)
                .setParameter("end", endDate)
                .getSingleResult();

        String productsSoldQuery = "SELECT COALESCE(SUM(d.quantity), 0) FROM OrderDetailEntity d JOIN d.order o WHERE o.sellerId = :sellerId AND o.orderDate >= :start AND o.orderDate <= :end";
        Long totalProductsSold = em.createQuery(productsSoldQuery, Long.class)
                .setParameter("sellerId", sellerId)
                .setParameter("start", startDate)
                .setParameter("end", endDate)
                .getSingleResult();

        // Calculate total remaining inventory. This logic might need native queries or nested queries.
        // For H2 and MySQL, it's easier to do:
        // sum(quantity where action=add) - sum(quantity where action=substract)
        // action=1 (ADD), action=2 (SUBTRACT). Wait, what are the constant values?
        // Let's assume actionId=1 is add, actionId=2 is subtract. I will use the constants from InventoryActionConstants if possible, but they are in Domain.
        // I'll calculate it using native query or JPQL:
        String invQuery = "SELECT COALESCE(SUM(CASE WHEN i.actionId = 1 THEN i.cantidad ELSE 0 END), 0) - " +
                          "COALESCE(SUM(CASE WHEN i.actionId = 2 THEN i.cantidad ELSE 0 END), 0) " +
                          "FROM InventoryEntity i WHERE i.skuSellerId IN (SELECT p.skuSellerId FROM ProductEntity p WHERE p.sellerId = :sellerId AND p.isActive = true)";
        
        Long totalRemainingInventory = em.createQuery(invQuery, Long.class)
                .setParameter("sellerId", sellerId)
                .getSingleResult();

        return new SalesSummary(
                totalEarnings,
                totalOrders != null ? totalOrders : 0L,
                totalProductsSold != null ? totalProductsSold : 0L,
                totalRemainingInventory != null ? totalRemainingInventory : 0L
        );
    }
}
