package itesm.mx.infrastructure.persistence.repository.location;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import itesm.mx.domain.models.location.Municipality;
import itesm.mx.domain.repository.location.MunicipalityRepository;
import itesm.mx.infrastructure.mapper.location.MunicipalityMapper;
import itesm.mx.infrastructure.persistence.entity.location.MunicipalityEntity;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class MunicipalityRepositoryImpl implements PanacheRepositoryBase<MunicipalityEntity, Long>, MunicipalityRepository {

    @Override
    public List<Municipality> findAllMunicipalities() {
        return listAll().stream()
                .map(MunicipalityMapper::toDomain)
                .toList();
    }

    @Override
    public Municipality register(Municipality municipality) {
        MunicipalityEntity entity = MunicipalityMapper.toEntity(municipality);
        persistAndFlush(entity);
        return MunicipalityMapper.toDomain(entity);
    }

    @Override
    public Optional<Municipality> findByName(String name) {
        return find("name", name).firstResultOptional().map(MunicipalityMapper::toDomain);
    }
}
