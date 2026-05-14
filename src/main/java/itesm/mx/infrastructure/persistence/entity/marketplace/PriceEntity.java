package itesm.mx.infrastructure.persistence.entity.marketplace;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Precios", indexes = {
        @Index(name = "idx_precio_sku_id_vendedor", columnList = "sku_id_vendedor")
})
public class PriceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_precio")
    public Long priceId;

    @Column(name = "sku_id_vendedor", nullable = false)
    public Long skuSellerId;

    @Column(name = "precio", nullable = false, precision = 10, scale = 5)
    public BigDecimal price;

    @Column(name = "fecha_precio", nullable = false)
    public LocalDateTime priceDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id_vendedor", referencedColumnName = "sku_id_vendedor", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public ProductEntity product;
}