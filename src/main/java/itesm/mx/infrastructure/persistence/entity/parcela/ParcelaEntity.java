package itesm.mx.infrastructure.persistence.entity.parcela;

import itesm.mx.infrastructure.persistence.entity.location.LocationEntity;
import itesm.mx.infrastructure.persistence.entity.users.FarmerEntity;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "Parcela")
public class ParcelaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_parcela")
    public Long parcelaId;

    @Column(name = "nombre_parcela", length = 100, nullable = false)
    public String nombreParcela;

    @Column(name = "tamano_hectareas")
    public Double tamanoHectareas;

    @Column(name = "fecha_siembra")
    public LocalDate fechaSiembra;

    @Column(name = "fecha_cosecha")
    public LocalDate fechaCosecha;

    @Column(name = "ph_suelo")
    public Double phSuelo;

    @Column(name = "id_agricultor", nullable = false)
    public Long farmerId;

    @Column(name = "id_ubicacion", nullable = false)
    public Long locationId;

    @Column(name = "id_estado_parcela", nullable = false)
    public Long estadoParcelaId;

    @Column(name = "id_tipo_cultivo", nullable = false)
    public Long tipoCultivoId;

    @Column(name = "id_sistema_riego", nullable = false)
    public Long sistemaRiegoId;

    @Column(name = "activo", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    public Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_agricultor", referencedColumnName = "id_agricultor",
            insertable = false, updatable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    public FarmerEntity farmer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ubicacion", referencedColumnName = "id_ubicacion",
            insertable = false, updatable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    public LocationEntity location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado_parcela", referencedColumnName = "id_estado_parcela",
            insertable = false, updatable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    public EstadoParcelaEntity estadoParcela;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_cultivo", referencedColumnName = "id_tipo_cultivo",
            insertable = false, updatable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    public TipoCultivoEntity tipoCultivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sistema_riego", referencedColumnName = "id_sistema_riego",
            insertable = false, updatable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    public SistemaRiegoEntity sistemaRiego;
}
