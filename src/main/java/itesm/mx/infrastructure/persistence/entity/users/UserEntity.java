package itesm.mx.infrastructure.persistence.entity.users;

import itesm.mx.infrastructure.persistence.entity.location.LocationEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "Usuario")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    public Long userId;

    @Column(name = "uuid_firebase", length = 100)
    public String firebaseUuid;

    @Column(name = "nombre", length = 100)
    public String name;

    @Column(name = "email", length = 100)
    public String email;

    @Column(name = "id_rol")
    public Integer roleId;

    @Column(name = "isActive")
    public Boolean isActive;

    @Column(name = "id_ubicacion")
    public Long locationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ubicacion", referencedColumnName = "id_ubicacion",
                insertable = false, updatable = false)
    public LocationEntity location;
}
