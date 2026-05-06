package itesm.mx.infrastructure.persistence.entity.marketplace;

import jakarta.persistence.*;

@Entity
@Table(name = "Status")
public class StatusEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_status")
    public Long statusId;

    @Column(name = "nombre", length = 100, unique = true, nullable = false)
    public String name;
}