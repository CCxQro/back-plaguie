package itesm.mx.infrastructure.persistence.entity.marketplace;

import jakarta.persistence.*;

@Entity
@Table(name = "Colores")
public class ColorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_color")
    public Long colorId;

    @Column(name = "name", length = 100)
    public String name;

    @Column(name = "hexa", length = 100)
    public String hexa;
}