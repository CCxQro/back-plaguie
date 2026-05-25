package itesm.mx.infrastructure.persistence.entity.location;

import jakarta.persistence.*;

@Entity
@Table(name = "Localidades")
public class LocalityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_localidad")
    public Long localityId;

    @Column(name = "nombre", length = 100)
    public String name;
}
