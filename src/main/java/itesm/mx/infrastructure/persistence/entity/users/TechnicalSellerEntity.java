package itesm.mx.infrastructure.persistence.entity.users;

import jakarta.persistence.*;

@Entity
@Table(name = "Tecnico_Vendedor")
public class TechnicalSellerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tecnico_vendedor")
    public Long technicalSellerId;

    @Column(name = "id_usuario", nullable = false, unique = true)
    public Long userId;

    @Column(name = "isActive")
    public Boolean isActive;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario", insertable = false, updatable = false)
    public UserEntity user;
}
