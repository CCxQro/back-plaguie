package itesm.mx.infrastructure.persistence.entity.location;

import jakarta.persistence.*;

@Entity
@Table(name = "Predios")
public class PropertyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_predio")
    public Long propertyId;

    @Column(name = "nombre", length = 100)
    public String name;
}
