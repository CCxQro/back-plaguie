package itesm.mx.infrastructure.persistence.entity.parcela;

import jakarta.persistence.*;

@Entity
@Table(name = "Estados_Parcelas")
public class EstadoParcelaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_parcela")
    public Long estadoParcelaId;

    @Column(name = "nombre", length = 100)
    public String nombre;
}
