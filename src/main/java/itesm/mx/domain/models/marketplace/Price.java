package itesm.mx.domain.models.marketplace;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Price {
    private Long priceId;
    private Product product;
    private BigDecimal price;
    private LocalDateTime priceDate;

    public Price() {}

    public Price(Long priceId, Product product, BigDecimal price, LocalDateTime priceDate) {
        this.priceId = priceId;
        this.product = product;
        this.price = price;
        this.priceDate = priceDate;
    }

    public Long getPriceId() { return priceId; }
    public void setPriceId(Long priceId) { this.priceId = priceId; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public LocalDateTime getPriceDate() { return priceDate; }
    public void setPriceDate(LocalDateTime priceDate) { this.priceDate = priceDate; }
}