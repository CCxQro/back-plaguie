package itesm.mx.application.usecase.vigilancia;

import itesm.mx.domain.repository.vigilancia.VigilanciaFitosanitariaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class DeleteVigilanciaFitosanitariaUseCase {

    @Inject
    VigilanciaFitosanitariaRepository vigilanciaFitosanitariaRepository;

    @Transactional
    public void execute(Long vigilanciaFitosanitariaId) {
        if (vigilanciaFitosanitariaId == null || vigilanciaFitosanitariaId <= 0) {
            throw new IllegalArgumentException("El ID de la vigilancia fitosanitaria no es válido");
        }

        vigilanciaFitosanitariaRepository.findVigilanciaFitosanitariaById(vigilanciaFitosanitariaId)
                .orElseThrow(() -> new IllegalStateException("Vigilancia fitosanitaria no encontrada con id: " + vigilanciaFitosanitariaId));

        vigilanciaFitosanitariaRepository.delete(vigilanciaFitosanitariaId);
    }
}