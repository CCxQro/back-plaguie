package itesm.mx.infrastructure.persistence.repository.location;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import itesm.mx.domain.models.location.Locality;
import itesm.mx.domain.repository.location.LocalityRepository;
import itesm.mx.infrastructure.mapper.location.LocalityMapper;
import itesm.mx.infrastructure.persistence.entity.location.LocalityEntity;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class LocalityRepositoryImpl implements PanacheRepositoryBase<LocalityEntity, Long>, LocalityRepository {

    @Override
    public List<Locality> findAllLocalities() {
        return listAll().stream()
                .map(LocalityMapper::toDomain)
                .toList();
    }

    @Override
    public Locality register(Locality locality) {
        LocalityEntity entity = LocalityMapper.toEntity(locality);
        persistAndFlush(entity);
        return LocalityMapper.toDomain(entity);
    }

    @Override
    public Optional<Locality> findByName(String name) {
        return find("name", name).firstResultOptional().map(LocalityMapper::toDomain);
    }
}
