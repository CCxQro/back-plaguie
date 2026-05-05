package itesm.mx.infrastructure.persistence.entity.vigilancia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Sistemas_Monitoreos")
public class SistemaMonitoreoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sistema_monitoreo")
    public Long sistemaMonitoreoId;

    @Column(name = "nombre", length = 100)
    public String name;
}