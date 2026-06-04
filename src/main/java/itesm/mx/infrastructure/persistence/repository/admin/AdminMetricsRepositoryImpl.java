package itesm.mx.infrastructure.persistence.repository.admin;

import itesm.mx.domain.models.admin.AdminMetrics;
import itesm.mx.domain.repository.admin.AdminMetricsRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;

@ApplicationScoped
public class AdminMetricsRepositoryImpl implements AdminMetricsRepository {

    private final EntityManager em;

    @Inject
    public AdminMetricsRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public AdminMetrics getMetrics() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        long totalUsers = em.createQuery("SELECT COUNT(u) FROM UserEntity u", Long.class)
                .getSingleResult();

        long totalProducts = em.createQuery("SELECT COUNT(p) FROM ProductEntity p", Long.class)
                .getSingleResult();

        long totalSurveillance = em.createQuery("SELECT COUNT(v) FROM VigilanciaFitosanitariaEntity v", Long.class)
                .getSingleResult();

        long recentSurveillance = em.createQuery("SELECT COUNT(v) FROM VigilanciaFitosanitariaEntity v WHERE v.validatedAt >= :thirtyDaysAgo", Long.class)
                .setParameter("thirtyDaysAgo", thirtyDaysAgo)
                .getSingleResult();

        long totalOrders = em.createQuery("SELECT COUNT(o) FROM OrderEntity o", Long.class)
                .getSingleResult();

        long recentOrders = em.createQuery("SELECT COUNT(o) FROM OrderEntity o WHERE o.orderDate >= :thirtyDaysAgo", Long.class)
                .setParameter("thirtyDaysAgo", thirtyDaysAgo)
                .getSingleResult();

        return new AdminMetrics(
                totalUsers,
                totalProducts,
                totalSurveillance,
                recentSurveillance,
                totalOrders,
                recentOrders
        );
    }
}
