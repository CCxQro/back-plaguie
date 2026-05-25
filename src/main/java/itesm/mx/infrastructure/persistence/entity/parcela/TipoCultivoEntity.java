package itesm.mx.infrastructure.persistence.entity.parcela;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "Tipos_Cultivos")
public class TipoCultivoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_cultivo")
    public Long tipoCultivoId;

    @Column(name = "nombre", length = 100)
    public String nombre;

    @Column(name = "fecha_siembra")
    public LocalDate fechaSiembra;

    @Column(name = "fecha_cosecha")
    public LocalDate fechaCosecha;
}
