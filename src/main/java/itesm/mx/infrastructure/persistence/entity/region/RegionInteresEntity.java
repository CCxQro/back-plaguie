package itesm.mx.infrastructure.persistence.entity.region;

import itesm.mx.infrastructure.persistence.entity.location.StateEntity;
import itesm.mx.infrastructure.persistence.entity.users.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * A seller's region of interest (HU-26). Links a seller (id_usuario) to a state
 * (id_estado). The state name is resolved via a read-only join for display.
 */
@Entity
@Table(name = "Region_Interes_Vendedor")
public class RegionInteresEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_region_interes")
    public Long regionInteresId;

    @Column(name = "id_usuario", nullable = false)
    public Long userId;

    @Column(name = "id_estado", nullable = false)
    public Long stateId;

    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "id_estado",
            referencedColumnName = "id_estado",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    public StateEntity state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "id_usuario",
            referencedColumnName = "id_usuario",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    public UserEntity user;
}
