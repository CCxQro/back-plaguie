package itesm.mx.infrastructure.persistence.entity.users;

import itesm.mx.infrastructure.persistence.entity.location.LocationEntity;
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

    @Column(name = "id_ubicacion", nullable = false)
    public Long locationId;

    @Column(name = "isActive")
    public Boolean isActive;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario", insertable = false, updatable = false)
    public UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ubicacion", referencedColumnName = "id_ubicacion", insertable = false, updatable = false)
    public LocationEntity location;
}
