package itesm.mx.infrastructure.persistence.repository.marketplace;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import itesm.mx.domain.models.marketplace.Price;
import itesm.mx.domain.repository.marketplace.PriceRepository;
import itesm.mx.infrastructure.mapper.marketplace.PriceMapper;
import itesm.mx.infrastructure.persistence.entity.marketplace.PriceEntity;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PriceRepositoryImpl implements PanacheRepositoryBase<PriceEntity, Long>, PriceRepository {

    @Override
    public Price save(Price price) {
        PriceEntity entity = PriceMapper.toEntity(price);
        persistAndFlush(entity);
        return PriceMapper.toDomain(entity);
    }

    @Override
    public Optional<Price> findLatestBySkuSellerId(Long skuSellerId) {
        return find("skuSellerId = ?1", Sort.by("priceDate").descending(), skuSellerId)
                .firstResultOptional()
                .map(PriceMapper::toDomain);
    }

    @Override
    public List<Price> findAllBySkuSellerId(Long skuSellerId) {
        return find("skuSellerId = ?1", Sort.by("priceDate").descending(), skuSellerId)
                .stream()
                .map(PriceMapper::toDomain)
                .toList();
    }
}