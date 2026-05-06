package itesm.mx.domain.repository.vigilancia;

import itesm.mx.domain.models.vigilancia.VigilanciaFitosanitaria;

import java.util.List;
import java.util.Optional;

public interface VigilanciaFitosanitariaRepository {
    List<VigilanciaFitosanitaria> findAllVigilanciasFitosanitarias();

    Optional<VigilanciaFitosanitaria> findVigilanciaFitosanitariaById(Long vigilanciaFitosanitariaId);

    VigilanciaFitosanitaria save(VigilanciaFitosanitaria vigilanciaFitosanitaria);

    VigilanciaFitosanitaria update(VigilanciaFitosanitaria vigilanciaFitosanitaria);

    void delete(Long vigilanciaFitosanitariaId);
}