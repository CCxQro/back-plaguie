package itesm.mx.infrastructure.persistence.entity.order;

import itesm.mx.infrastructure.persistence.entity.users.FarmerEntity;
import itesm.mx.infrastructure.persistence.entity.users.TechnicalSellerEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Pedido")
@NamedEntityGraphs({
    @NamedEntityGraph(
        name = "Pedido.withFarmerLocation",
        attributeNodes = {
            @NamedAttributeNode(value = "farmer", subgraph = "farmer-full"),
            @NamedAttributeNode("seller"),
            @NamedAttributeNode("orderStatus")
        },
        subgraphs = {
            @NamedSubgraph(
                name = "farmer-full",
                attributeNodes = {
                    @NamedAttributeNode(value = "user", subgraph = "user-with-location")
                }
            ),
            @NamedSubgraph(
                name = "user-with-location",
                attributeNodes = {
                    @NamedAttributeNode("location")
                }
            )
        }
    ),
    @NamedEntityGraph(
        name = "Pedido.withDetails",
        attributeNodes = {
            @NamedAttributeNode(value = "details", subgraph = "detail-product"),
            @NamedAttributeNode("farmer"),
            @NamedAttributeNode("seller"),
            @NamedAttributeNode("orderStatus")
        },
        subgraphs = {
            @NamedSubgraph(
                name = "detail-product",
                attributeNodes = { @NamedAttributeNode("product") }
            )
        }
    )
})
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    public Long orderId;

    @Column(name = "id_agricultor", nullable = false)
    public Long farmerId;

    @Column(name = "id_vendedor", nullable = false)
    public Long sellerId;

    @Column(name = "fecha_pedido")
    public LocalDateTime orderDate;

    @Column(name = "id_estado_pedido", nullable = false)
    public Long orderStatusId;

    @Column(name = "monto_total")
    public BigDecimal totalAmount;

    @Column(name = "compartido_proveedor")
    public Boolean providerShared = false;

    @Column(name = "shipping_street", length = 255)
    public String shippingStreet;

    @Column(name = "shipping_city", length = 100)
    public String shippingCity;

    @Column(name = "shipping_state", length = 100)
    public String shippingState;

    @Column(name = "shipping_lat")
    public Double shippingLat;

    @Column(name = "shipping_lon")
    public Double shippingLon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_agricultor", referencedColumnName = "id_agricultor",
                insertable = false, updatable = false)
    public FarmerEntity farmer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vendedor", referencedColumnName = "id_tecnico_vendedor",
                insertable = false, updatable = false)
    public TechnicalSellerEntity seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado_pedido", referencedColumnName = "id_estado_pedido",
                insertable = false, updatable = false)
    public OrderStatusEntity orderStatus;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    public List<OrderDetailEntity> details;
}
