package itesm.mx.infrastructure.persistence.entity.marketplace;

import itesm.mx.infrastructure.persistence.entity.users.UserEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "Proveedores")
public class ProviderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proveedor")
    public Long providerId;

    @Column(name = "id_usuario", nullable = false)
    public Long userId;

    @Column(name = "nombre", length = 100, nullable = false, unique = true)
    public String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario", insertable = false, updatable = false)
    public UserEntity user;
}