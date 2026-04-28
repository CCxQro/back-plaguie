package itesm.mx.infrastructure.persistence.entity.location;

import jakarta.persistence.*;

@Entity
@Table(name = "Municipios")
public class MunicipalityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_municipio")
    public Long municipalityId;

    @Column(name = "nombre", length = 100)
    public String name;
}
