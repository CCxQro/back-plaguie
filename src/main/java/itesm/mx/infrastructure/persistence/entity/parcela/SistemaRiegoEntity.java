package itesm.mx.infrastructure.persistence.entity.parcela;

import jakarta.persistence.*;

@Entity
@Table(name = "Sistemas_Riego")
public class SistemaRiegoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sistema_riego")
    public Long sistemaRiegoId;

    @Column(name = "nombre", length = 100)
    public String nombre;
}
