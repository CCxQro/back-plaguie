package itesm.mx.infrastructure.persistence.entity.vigilancia;

import itesm.mx.infrastructure.persistence.entity.location.LocationEntity;
import itesm.mx.infrastructure.persistence.entity.marketplace.StatusEntity;
import itesm.mx.infrastructure.persistence.entity.users.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vigilancia_fitosanitaria")
public class VigilanciaFitosanitariaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vigilancia_fitosanitaria")
    public Long vigilanciaFitosanitariaId;

    @Column(name = "id_sistema_monitoreo")
    public Long sistemaMonitoreoId;

    @Column(name = "id_cid")
    public Long cidId;

    @Column(name = "lat", precision = 10, scale = 8)
    public BigDecimal latitude;

    @Column(name = "`long`", precision = 11, scale = 8)
    public BigDecimal longitude;

    @Column(name = "id_ubicacion")
    public Long ubicacionId;

    @Column(name = "id_plaga")
    public Long plagaId;

    @Column(name = "id_hospedante")
    public Long hospedanteId;

    @Column(name = "id_variedad")
    public Long variedadId;

    @Column(name = "id_especie")
    public Long especieId;

    @Column(name = "ahosp", precision = 5, scale = 2)
    public BigDecimal ahosp;

    @Column(name = "id_status")
    public Long statusId;

    @Column(name = "id_validated_by")
    public Long validatedByUserId;

    @Column(name = "validated_at")
    public LocalDateTime validatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(
            name = "id_sistema_monitoreo",
            referencedColumnName = "id_sistema_monitoreo",
            insertable = false,
            updatable = false,
            foreignKey = @jakarta.persistence.ForeignKey(ConstraintMode.NO_CONSTRAINT)
        )
    public SistemaMonitoreoEntity sistemaMonitoreo;

    @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(
            name = "id_cid",
            referencedColumnName = "id_cid",
            insertable = false,
            updatable = false,
            foreignKey = @jakarta.persistence.ForeignKey(ConstraintMode.NO_CONSTRAINT)
        )
    public ClaveIdentificacionPlagaEntity claveIdentificacionPlaga;

    @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(
            name = "id_ubicacion",
            referencedColumnName = "id_ubicacion",
            insertable = false,
            updatable = false,
            foreignKey = @jakarta.persistence.ForeignKey(ConstraintMode.NO_CONSTRAINT)
        )
    public LocationEntity ubicacion;

    @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(
            name = "id_plaga",
            referencedColumnName = "id_plaga",
            insertable = false,
            updatable = false,
            foreignKey = @jakarta.persistence.ForeignKey(ConstraintMode.NO_CONSTRAINT)
        )
    public PlagaEntity plaga;

    @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(
            name = "id_hospedante",
            referencedColumnName = "id_hospedante",
            insertable = false,
            updatable = false,
            foreignKey = @jakarta.persistence.ForeignKey(ConstraintMode.NO_CONSTRAINT)
        )
    public HospedanteEntity hospedante;

    @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(
            name = "id_variedad",
            referencedColumnName = "id_variedad",
            insertable = false,
            updatable = false,
            foreignKey = @jakarta.persistence.ForeignKey(ConstraintMode.NO_CONSTRAINT)
        )
    public VariedadEntity variedad;

    @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(
            name = "id_especie",
            referencedColumnName = "id_especie",
            insertable = false,
            updatable = false,
            foreignKey = @jakarta.persistence.ForeignKey(ConstraintMode.NO_CONSTRAINT)
        )
    public EspecieEntity especie;

    @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(
            name = "id_status",
            referencedColumnName = "id_status",
            insertable = false,
            updatable = false,
            foreignKey = @jakarta.persistence.ForeignKey(ConstraintMode.NO_CONSTRAINT)
        )
    public StatusEntity status;

    @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(
            name = "id_validated_by",
            referencedColumnName = "id_usuario",
            insertable = false,
            updatable = false,
            foreignKey = @jakarta.persistence.ForeignKey(ConstraintMode.NO_CONSTRAINT)
        )
    public UserEntity validatedBy;
}