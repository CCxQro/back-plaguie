package itesm.mx.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductResponseDto {
    public Long skuSellerId;
    public Long sellerId;
    public String sellerName;
    public String name;
    public String sku;
    public Long categoryId;
    public String categoryName;
    public Long providerId;
    public String providerName;
    public Double unitValue;
    public Long unitId;
    public String unitName;
    public String description;
    public Long statusId;
    public String statusName;
    public String firebaseImageId;
    public BigDecimal latestPrice;
    public LocalDateTime latestPriceDate;
    public Integer stock;
    public Boolean isActive;
}
