package itesm.mx.domain.repository.location;

import itesm.mx.domain.models.location.State;

import java.util.List;
import java.util.Optional;

public interface StateRepository {
    List<State> findAllStates();
    State register(State state);
    Optional<State> findByName(String name);
}
