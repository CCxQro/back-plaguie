package itesm.mx.infrastructure.persistence.repository.order;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import itesm.mx.domain.models.order.OrderStatus;
import itesm.mx.domain.repository.order.OrderStatusRepository;
import itesm.mx.infrastructure.mapper.order.OrderStatusMapper;
import itesm.mx.infrastructure.persistence.entity.order.OrderStatusEntity;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class OrderStatusRepositoryImpl
        implements PanacheRepositoryBase<OrderStatusEntity, Long>, OrderStatusRepository {

    @Override
    public List<OrderStatus> findAllStatuses() {
        return listAll().stream().map(OrderStatusMapper::toDomain).toList();
    }

    @Override
    public Optional<OrderStatus> findStatusById(Long id) {
        return findByIdOptional(id).map(OrderStatusMapper::toDomain);
    }

    @Override
    public OrderStatus save(OrderStatus status) {
        OrderStatusEntity entity = OrderStatusMapper.toEntity(status);
        persistAndFlush(entity);
        return OrderStatusMapper.toDomain(entity);
    }
}
