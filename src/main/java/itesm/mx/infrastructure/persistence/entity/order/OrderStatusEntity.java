package itesm.mx.infrastructure.persistence.entity.order;

import jakarta.persistence.*;

@Entity
@Table(name = "Estados_Pedido")
public class OrderStatusEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_pedido")
    public Long orderStatusId;

    @Column(name = "estado", length = 100, nullable = false, unique = true)
    public String estado;
}
