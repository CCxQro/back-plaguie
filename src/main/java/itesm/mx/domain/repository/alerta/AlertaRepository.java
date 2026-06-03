package itesm.mx.domain.repository.alerta;

import itesm.mx.domain.models.alerta.Alerta;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AlertaRepository {
    List<Alerta> findAllAlertas();

    Optional<Alerta> findAlertaById(Long alertaId);

    List<Alerta> findByReportedUserId(Long userId);

    /**
     * All validated (status = Accepted) alerts created on or after {@code since},
     * across every state, each enriched with its state id/name. Region and other
     * filtering is applied client-side (HU-26 CA-02).
     */
    List<Alerta> findValidatedSince(LocalDateTime since);

    Alerta save(Alerta alerta);

    Alerta update(Alerta alerta);

    void delete(Long alertaId);
}
