package itesm.mx.infrastructure.persistence.entity.parcela;

import itesm.mx.infrastructure.persistence.entity.alerta.AlertaEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "parcela_suggestions")
public class ParcelaSuggestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_suggestion")
    public Long suggestionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_parcela", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    public ParcelaEntity parcela;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_alerta", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    public AlertaEntity alerta;

    @Column(name = "message", length = 1000)
    public String message;

    @Column(name = "created_at")
    public LocalDateTime createdAt;
}
