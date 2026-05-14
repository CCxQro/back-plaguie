package itesm.mx.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PriceResponseDto {
    public Long priceId;
    public Long skuSellerId;
    public BigDecimal price;
    public LocalDateTime priceDate;
}