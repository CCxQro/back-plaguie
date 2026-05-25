package itesm.mx.infrastructure.persistence.entity.marketplace;

import itesm.mx.infrastructure.persistence.entity.users.TechnicalSellerEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "Productos")
public class ProductEntity {

    @Id
    @Column(name = "sku_id_vendedor")
    public Long skuSellerId;

    @Column(name = "id_vendedor", nullable = false)
    public Long sellerId;

    @Column(name = "nombre", length = 100, nullable = false)
    public String name;

    @Column(name = "sku", length = 50, nullable = false, unique = true)
    public String sku;

    @Column(name = "id_categoria", nullable = false)
    public Long categoryId;

    @Column(name = "id_proveedor", nullable = false)
    public Long providerId;

    @Column(name = "valor_unidad", nullable = false)
    public Double unitValue;

    @Column(name = "id_unidad", nullable = false)
    public Long unitId;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    public String description;

    @Column(name = "id_status", nullable = false)
    public Long statusId;

    @Column(name = "imagen_firebase_id")
    public String firebaseImageId;

    @Column(name = "activo")
    public Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vendedor", referencedColumnName = "id_tecnico_vendedor", insertable = false, updatable = false)
    public TechnicalSellerEntity seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria", referencedColumnName = "id_categoria", insertable = false, updatable = false)
    public CategoryEntity category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor", referencedColumnName = "id_proveedor", insertable = false, updatable = false)
    public ProviderEntity provider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_unidad", referencedColumnName = "id_unidad", insertable = false, updatable = false)
    public UnitEntity unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_status", referencedColumnName = "id_status", insertable = false, updatable = false)
    public StatusEntity status;
}