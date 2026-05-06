package itesm.mx.infrastructure.persistence.entity.marketplace;

import itesm.mx.infrastructure.persistence.entity.users.UserEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "Unidades")
public class UnitEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_unidad")
    public Long unitId;

    @Column(name = "id_usuario", nullable = false)
    public Long userId;

    @Column(name = "nombre", length = 100, nullable = false, unique = true)
    public String name;

    @Column(name = "id_status", nullable = false)
    public Long statusId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario", insertable = false, updatable = false)
    public UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_status", referencedColumnName = "id_status", insertable = false, updatable = false)
    public StatusEntity status;
}