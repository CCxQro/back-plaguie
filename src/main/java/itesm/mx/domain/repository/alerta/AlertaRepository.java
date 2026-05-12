package itesm.mx.domain.repository.alerta;

import itesm.mx.domain.models.alerta.Alerta;

import java.util.List;
import java.util.Optional;

public interface AlertaRepository {
    List<Alerta> findAllAlertas();

    Optional<Alerta> findAlertaById(Long alertaId);

    Alerta save(Alerta alerta);

    Alerta update(Alerta alerta);

    void delete(Long alertaId);
}
