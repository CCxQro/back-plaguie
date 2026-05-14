package itesm.mx.application.mapper.marketplace;

import itesm.mx.application.dto.PriceResponseDto;
import itesm.mx.domain.models.marketplace.Price;

public class PriceDtoMapper {

    public static PriceResponseDto toResponseDto(Price price) {
        PriceResponseDto dto = new PriceResponseDto();
        dto.priceId = price.getPriceId();
        dto.skuSellerId = price.getProduct() != null ? price.getProduct().getSkuSellerId() : null;
        dto.price = price.getPrice();
        dto.priceDate = price.getPriceDate();
        return dto;
    }
}