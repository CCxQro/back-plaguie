package itesm.mx.domain.repository.recomendacion;

import itesm.mx.domain.models.recomendacion.Recomendacion;

import java.util.List;
import java.util.Optional;

public interface RecomendacionRepository {
    List<Recomendacion> findAllRecomendaciones();

    Optional<Recomendacion> findRecomendacionById(Long recomendacionId);

    Recomendacion save(Recomendacion recomendacion);

    Recomendacion update(Recomendacion recomendacion);

    void delete(Long recomendacionId);
}
