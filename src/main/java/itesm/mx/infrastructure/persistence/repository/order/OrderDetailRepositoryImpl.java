package itesm.mx.infrastructure.persistence.repository.order;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import itesm.mx.domain.models.order.OrderDetail;
import itesm.mx.domain.repository.order.OrderDetailRepository;
import itesm.mx.infrastructure.mapper.order.OrderDetailMapper;
import itesm.mx.infrastructure.persistence.entity.order.OrderDetailEntity;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class OrderDetailRepositoryImpl
        implements PanacheRepositoryBase<OrderDetailEntity, Long>, OrderDetailRepository {

    @Override
    @Transactional
    public List<OrderDetail> saveAll(List<OrderDetail> details) {
        return details.stream().map(detail -> {
            OrderDetailEntity entity = OrderDetailMapper.toEntity(detail);
            persistAndFlush(entity);
            return OrderDetailMapper.toDomain(entity);
        }).toList();
    }

    @Override
    public List<OrderDetail> findAllByOrderId(Long orderId) {
        return find("orderId", orderId).stream().map(OrderDetailMapper::toDomain).toList();
    }

    @Override
    public Optional<OrderDetail> findDetailById(Long detailId) {
        return findByIdOptional(detailId).map(OrderDetailMapper::toDomain);
    }
}
