package itesm.mx.infrastructure.mapper.location;

import itesm.mx.domain.models.location.State;
import itesm.mx.infrastructure.persistence.entity.location.StateEntity;

public class StateMapper {
    public static StateEntity toEntity(State state) {
        StateEntity entity = new StateEntity();
        entity.stateId = state.getStateId();
        entity.name = state.getName();
        return entity;
    }

    public static State toDomain(StateEntity entity) {
        State state = new State();
        state.setStateId(entity.stateId);
        state.setName(entity.name);
        return state;
    }
}
