package itesm.mx.domain.repository.alerta;

import itesm.mx.domain.models.alerta.Alerta;

import java.util.List;
import java.util.Optional;

public interface AlertaRepository {
    List<Alerta> findAllAlertas();

    Optional<Alerta> findAlertaById(Long alertaId);

    List<Alerta> findByReportedUserId(Long userId);

    /**
     * Validated (status = Accepted) alerts whose location belongs to any of the
     * given states. Used for the seller's early-alerts feed scoped to their
     * regions of interest (HU-26 CA-02). Returns empty when the list is empty.
     */
    List<Alerta> findValidatedByStateIds(List<Long> stateIds);

    Alerta save(Alerta alerta);

    Alerta update(Alerta alerta);

    void delete(Long alertaId);
}
