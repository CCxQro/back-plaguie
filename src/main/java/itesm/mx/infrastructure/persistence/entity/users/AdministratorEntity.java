package itesm.mx.infrastructure.persistence.entity.users;

import jakarta.persistence.*;

@Entity
@Table(name = "Administrador")
public class AdministratorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_administrador")
    public Long administratorId;

    @Column(name = "id_usuario", nullable = false, unique = true)
    public Long userId;

    @Column(name = "isActive")
    public Boolean isActive;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario", insertable = false, updatable = false)
    public UserEntity user;
}
