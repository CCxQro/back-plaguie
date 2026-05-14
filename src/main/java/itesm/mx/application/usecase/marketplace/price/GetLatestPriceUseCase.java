package itesm.mx.application.usecase.marketplace.price;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.domain.models.marketplace.Price;
import itesm.mx.domain.repository.marketplace.PriceRepository;

import java.util.Optional;

@ApplicationScoped
public class GetLatestPriceUseCase {

    @Inject PriceRepository priceRepository;

    public Optional<Price> execute(Long skuSellerId) {
        if (skuSellerId == null) {
            throw new IllegalArgumentException("skuSellerId is required");
        }
        return priceRepository.findLatestBySkuSellerId(skuSellerId);
    }
}