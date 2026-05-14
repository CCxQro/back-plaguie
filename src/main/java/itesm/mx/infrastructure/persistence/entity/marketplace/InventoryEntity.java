package itesm.mx.infrastructure.persistence.entity.marketplace;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "Inventario", indexes = {
        @Index(name = "idx_inventario_sku_id_vendedor", columnList = "sku_id_vendedor"),
        @Index(name = "idx_inventario_id_accion_inventario", columnList = "id_accion_inventario")
})
public class InventoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inventario")
    public Long inventoryId;

    @Column(name = "sku_id_vendedor", nullable = false)
    public Long skuSellerId;

    @Column(name = "cantidad", nullable = false)
    public Integer cantidad;

    @Column(name = "id_accion_inventario", nullable = false)
    public Long actionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id_vendedor", referencedColumnName = "sku_id_vendedor", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_accion_inventario", referencedColumnName = "id_accion_inventario", insertable = false, updatable = false)
    public InventoryActionEntity action;
}
