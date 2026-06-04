package itesm.mx.infrastructure.persistence.entity.order;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Farmer_Data_Sharing")
public class FarmerDataSharingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

    @Column(name = "id_pedido", nullable = false)
    public Long orderId;

    @Column(name = "id_agricultor", nullable = false)
    public Long farmerId;

    @Column(name = "id_proveedor", nullable = false)
    public Long providerId;

    @Column(name = "shared_at", nullable = false)
    public LocalDateTime sharedAt;

    @Column(name = "snapshot_json", columnDefinition = "TEXT")
    public String snapshotJson;
}
