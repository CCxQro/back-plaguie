package itesm.mx.infrastructure.persistence.entity.order;

import itesm.mx.infrastructure.persistence.entity.marketplace.ProductEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "Detalle_Pedido")
public class OrderDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    public Long detailId;

    @Column(name = "id_pedido", nullable = false)
    public Long orderId;

    @Column(name = "id_producto", nullable = false)
    public Long productId;

    @Column(name = "cantidad", nullable = false)
    public Integer quantity;

    @Column(name = "precio_unitario", nullable = false)
    public Float unitPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido", referencedColumnName = "id_pedido",
                insertable = false, updatable = false)
    public OrderEntity order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", referencedColumnName = "sku_id_vendedor",
                insertable = false, updatable = false)
    public ProductEntity product;
}
