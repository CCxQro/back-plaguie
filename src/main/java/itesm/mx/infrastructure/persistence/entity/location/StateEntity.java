package itesm.mx.infrastructure.persistence.entity.location;

import jakarta.persistence.*;

@Entity
@Table(name = "Estados")
public class StateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado")
    public Long stateId;

    @Column(name = "nombre", length = 100)
    public String name;
}
