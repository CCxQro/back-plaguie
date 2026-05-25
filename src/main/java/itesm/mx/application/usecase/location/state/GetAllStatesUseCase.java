package itesm.mx.application.usecase.location.state;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.domain.models.location.State;
import itesm.mx.domain.repository.location.StateRepository;

import java.util.List;

@ApplicationScoped
public class GetAllStatesUseCase {

    @Inject
    StateRepository stateRepository;

    public List<State> execute() {
        return stateRepository.findAllStates();
    }
}
