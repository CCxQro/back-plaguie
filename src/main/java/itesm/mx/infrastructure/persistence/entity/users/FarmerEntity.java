package itesm.mx.infrastructure.persistence.entity.users;

import itesm.mx.infrastructure.persistence.entity.marketplace.StatusEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "Agricultor")
public class FarmerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_agricultor")
    public Long farmerId;

    @Column(name = "id_usuario", nullable = false, unique = true)
    public Long userId;

    @Column(name = "isActive")
    public Boolean isActive;

    /** Account approval status FK (Status catalog): Accepted / Revision / Rejected. */
    @Column(name = "id_status")
    public Long statusId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario", insertable = false, updatable = false)
    public UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_status", referencedColumnName = "id_status", insertable = false, updatable = false)
    public StatusEntity status;
}
