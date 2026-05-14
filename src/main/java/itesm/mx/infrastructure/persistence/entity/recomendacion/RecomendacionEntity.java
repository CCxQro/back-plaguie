package itesm.mx.infrastructure.persistence.entity.recomendacion;

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

import java.time.LocalDateTime;

@Entity
@Table(name = "recomendaciones")
public class RecomendacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recomendacion")
    public Long recomendacionId;

    @Column(name = "titulo", length = 255, nullable = false)
    public String titulo;

    @Column(name = "descripcion", length = 1000)
    public String descripcion;

    @Column(name = "tipo_plaga", length = 100, nullable = false)
    public String tipoPlaga;

    @Column(name = "productos_recomendados", length = 500, nullable = false)
    public String productosRecomendados;

    @Column(name = "id_reported_by")
    public Long reportedByUserId;

    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @Column(name = "id_status")
    public Long statusId;

    @Column(name = "id_validated_by")
    public Long validatedByUserId;

    @Column(name = "validated_at")
    public LocalDateTime validatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "id_reported_by",
            referencedColumnName = "id_usuario",
            insertable = false,
            updatable = false,
            foreignKey = @jakarta.persistence.ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    public UserEntity reportedBy;

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
