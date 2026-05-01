package itesm.mx.application.usecase.location.state;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import itesm.mx.domain.models.location.State;
import itesm.mx.domain.repository.location.StateRepository;
import itesm.mx.domain.util.LocationNormalizer;

@ApplicationScoped
public class RegisterStateUseCase {

    @Inject
    StateRepository stateRepository;

    @Transactional
    public State execute(State state) {
        if (state == null) {
            throw new IllegalArgumentException("El estado es requerido");
        }

        String normalizedName = LocationNormalizer.normalize(state.getName());

        return stateRepository.findByName(normalizedName)
                .orElseGet(() -> stateRepository.register(
                        new State(state.getStateId(), normalizedName)
                ));
    }
}
