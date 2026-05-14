package itesm.mx.domain.models.marketplace;

import itesm.mx.domain.models.user.TechnicalSeller;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Product {
    private Long skuSellerId;
    private TechnicalSeller seller;
    private String name;
    private String sku;
    private Category category;
    private Provider provider;
    private Double unitValue;
    private Unit unit;
    private String description;
    private Status status;
    private String firebaseImageId;
    private BigDecimal latestPrice;
    private LocalDateTime latestPriceDate;
    private Integer stock;

    public Product() {}

    public Product(Long skuSellerId, TechnicalSeller seller, String name, String sku,
                   Category category, Provider provider, Double unitValue, Unit unit,
                   String description, Status status, String firebaseImageId) {
        this.skuSellerId = skuSellerId;
        this.seller = seller;
        this.name = name;
        this.sku = sku;
        this.category = category;
        this.provider = provider;
        this.unitValue = unitValue;
        this.unit = unit;
        this.description = description;
        this.status = status;
        this.firebaseImageId = firebaseImageId;
    }

    public Long getSkuSellerId() { return skuSellerId; }
    public void setSkuSellerId(Long skuSellerId) { this.skuSellerId = skuSellerId; }

    public TechnicalSeller getSeller() { return seller; }
    public void setSeller(TechnicalSeller seller) { this.seller = seller; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Provider getProvider() { return provider; }
    public void setProvider(Provider provider) { this.provider = provider; }

    public Double getUnitValue() { return unitValue; }
    public void setUnitValue(Double unitValue) { this.unitValue = unitValue; }

    public Unit getUnit() { return unit; }
    public void setUnit(Unit unit) { this.unit = unit; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getFirebaseImageId() { return firebaseImageId; }
    public void setFirebaseImageId(String firebaseImageId) { this.firebaseImageId = firebaseImageId; }

    public BigDecimal getLatestPrice() { return latestPrice; }
    public void setLatestPrice(BigDecimal latestPrice) { this.latestPrice = latestPrice; }

    public LocalDateTime getLatestPriceDate() { return latestPriceDate; }
    public void setLatestPriceDate(LocalDateTime latestPriceDate) { this.latestPriceDate = latestPriceDate; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
}