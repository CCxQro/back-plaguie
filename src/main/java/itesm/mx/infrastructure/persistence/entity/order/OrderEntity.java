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
                    @NamedAttributeNode("user"),
                    @NamedAttributeNode("location")
                }
            )
        }
    ),
    @NamedEntityGraph(
        name = "Pedido.withDetails",
        attributeNodes = {
            @NamedAttributeNode(value = "details", subgraph = "detail-product"),
            @NamedAttributeNode(value = "farmer", subgraph = "farmer-user"),
            @NamedAttributeNode(value = "seller", subgraph = "seller-user"),
            @NamedAttributeNode("orderStatus")
        },
        subgraphs = {
            @NamedSubgraph(
                name = "detail-product",
                attributeNodes = { @NamedAttributeNode("product") }
            ),
            @NamedSubgraph(
                name = "farmer-user",
                attributeNodes = { @NamedAttributeNode("user") }
            ),
            @NamedSubgraph(
                name = "seller-user",
                attributeNodes = { @NamedAttributeNode("user") }
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
