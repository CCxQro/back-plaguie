package itesm.mx.application.usecase.marketplace.price;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.domain.models.marketplace.Price;
import itesm.mx.domain.repository.marketplace.PriceRepository;

import java.util.List;

@ApplicationScoped
public class GetAllPricesUseCase {

    @Inject PriceRepository priceRepository;

    public List<Price> execute(Long skuSellerId) {
        if (skuSellerId == null) {
            throw new IllegalArgumentException("skuSellerId is required");
        }
        return priceRepository.findAllBySkuSellerId(skuSellerId);
    }
}
