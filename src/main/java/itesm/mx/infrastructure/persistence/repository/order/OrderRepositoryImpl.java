package itesm.mx.infrastructure.persistence.repository.order;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.TypedQuery;
import itesm.mx.domain.models.order.Order;
import itesm.mx.domain.repository.order.OrderRepository;
import itesm.mx.infrastructure.mapper.order.OrderMapper;
import itesm.mx.infrastructure.persistence.entity.order.OrderEntity;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class OrderRepositoryImpl
        implements PanacheRepositoryBase<OrderEntity, Long>, OrderRepository {

    @Override
    public Order save(Order order) {
        OrderEntity entity = OrderMapper.toEntity(order);
        persistAndFlush(entity);
        return OrderMapper.toDomain(entity);
    }

    @Override
    public Optional<Order> findOrderById(Long orderId) {
        return findByIdOptional(orderId).map(OrderMapper::toDomain);
    }

    @Override
    public Optional<Order> findByIdWithDetails(Long orderId) {
        EntityGraph<?> graph = getEntityManager().getEntityGraph("Pedido.withDetails");
        TypedQuery<OrderEntity> query = getEntityManager()
                .createQuery("select o from OrderEntity o where o.orderId = :id", OrderEntity.class)
                .setParameter("id", orderId)
                .setHint("jakarta.persistence.loadgraph", graph);
        List<OrderEntity> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(OrderMapper.toDomain(results.get(0)));
    }

    @Override
    public List<Order> findAllBySellerId(Long sellerId) {
        return find("sellerId", sellerId).stream().map(OrderMapper::toDomain).toList();
    }

    @Override
    public List<Order> findAllBySellerIdWithFarmerLocation(Long sellerId) {
        EntityGraph<?> graph = getEntityManager().getEntityGraph("Pedido.withFarmerLocation");
        return getEntityManager()
                .createQuery("select o from OrderEntity o where o.sellerId = :sid", OrderEntity.class)
                .setParameter("sid", sellerId)
                .setHint("jakarta.persistence.loadgraph", graph)
                .getResultList()
                .stream()
                .map(OrderMapper::toDomain)
                .toList();
    }

    @Override
    public List<Order> findAllBySellerIdAndFarmerId(Long sellerId, Long farmerId) {
        return find("sellerId = ?1 and farmerId = ?2 order by orderDate desc", sellerId, farmerId)
                .stream()
                .map(OrderMapper::toDomain)
                .toList();
    }

    @Override
    public Order updateStatus(Long orderId, Long orderStatusId) {
        OrderEntity entity = findByIdOptional(orderId)
                .orElseThrow(() -> new IllegalStateException("Pedido no encontrado con id: " + orderId));
        entity.orderStatusId = orderStatusId;
        flush();
        return OrderMapper.toDomain(entity);
    }
}
