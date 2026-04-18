package itesm.mx.infrastructure.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users") 
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    public Long userId;

    @Column(name = "uuid_firebase", length = 100)
    public String firebaseUuid;

    @Column(length = 100)
    public String name;

    @Column(name = "correo_electronico", length = 50)
    public String email;

    @Column(name = "id_rol")
    public Integer roleId;
}