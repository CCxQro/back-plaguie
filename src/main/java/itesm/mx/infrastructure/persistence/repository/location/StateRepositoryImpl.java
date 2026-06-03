package itesm.mx.infrastructure.persistence.repository.location;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import itesm.mx.domain.models.location.State;
import itesm.mx.domain.repository.location.StateRepository;
import itesm.mx.infrastructure.mapper.location.StateMapper;
import itesm.mx.infrastructure.persistence.entity.location.StateEntity;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class StateRepositoryImpl implements PanacheRepositoryBase<StateEntity, Long>, StateRepository {

    @Override
    public List<State> findAllStates() {
        return listAll().stream()
                .map(StateMapper::toDomain)
                .toList();
    }

    @Override
    public State register(State state) {
        StateEntity entity = StateMapper.toEntity(state);
        persistAndFlush(entity);
        return StateMapper.toDomain(entity);
    }

    @Override
    public Optional<State> findByName(String name) {
        return find("name", name).firstResultOptional().map(StateMapper::toDomain);
    }
}
