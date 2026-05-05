package itesm.mx.infrastructure.persistence.repository.location;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import itesm.mx.domain.models.location.Property;
import itesm.mx.domain.repository.location.PropertyRepository;
import itesm.mx.infrastructure.mapper.location.PropertyMapper;
import itesm.mx.infrastructure.persistence.entity.location.PropertyEntity;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PropertyRepositoryImpl implements PanacheRepositoryBase<PropertyEntity, Long>, PropertyRepository {

    @Override
    public List<Property> findAllProperties() {
        return listAll().stream()
                .map(PropertyMapper::toDomain)
                .toList();
    }

    @Override
    public Property register(Property property) {
        PropertyEntity entity = PropertyMapper.toEntity(property);
        persistAndFlush(entity);
        return PropertyMapper.toDomain(entity);
    }

    @Override
    public Optional<Property> findByName(String name) {
        return find("name", name).firstResultOptional().map(PropertyMapper::toDomain);
    }
}
