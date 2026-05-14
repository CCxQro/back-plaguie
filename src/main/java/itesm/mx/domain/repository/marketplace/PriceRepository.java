package itesm.mx.domain.repository.marketplace;

import itesm.mx.domain.models.marketplace.Price;

import java.util.List;
import java.util.Optional;

public interface PriceRepository {
    Price save(Price price);
    Optional<Price> findLatestBySkuSellerId(Long skuSellerId);
    List<Price> findAllBySkuSellerId(Long skuSellerId);
}