package itesm.mx.infrastructure.persistence.entity.vigilancia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Claves_Identificacion_plaga")
public class ClaveIdentificacionPlagaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cid")
    public Long cidId;

    @Column(name = "nombre", length = 100)
    public String name;
}