package itesm.mx.infrastructure.persistence.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import itesm.mx.domain.models.user.Administrator;
import itesm.mx.domain.repository.AdministratorRepository;
import itesm.mx.infrastructure.mapper.user.AdministratorMapper;
import itesm.mx.infrastructure.persistence.entity.users.AdministratorEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class AdministratorRepositoryImpl implements PanacheRepositoryBase<AdministratorEntity, Long>, AdministratorRepository {

    @Override
    @Transactional
    public Administrator save(Administrator administrator) {
        AdministratorEntity entity = AdministratorMapper.toEntity(administrator);
        persistAndFlush(entity);
        return AdministratorMapper.toDomain(entity);
    }

    @Override
    @Transactional
    public Administrator update(Administrator administrator) {
        if (administrator.getAdministratorId() == null) {
            throw new IllegalArgumentException("administratorId is required for update");
        }

        AdministratorEntity entity = findByIdOptional(administrator.getAdministratorId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Administrador no encontrado con id: " + administrator.getAdministratorId()));

        if (administrator.getUser() != null && administrator.getUser().getUserId() != null) {
            entity.userId = administrator.getUser().getUserId();
        }
        if (administrator.getActive() != null) {
            entity.isActive = administrator.getActive();
        }

        persistAndFlush(entity);
        return AdministratorMapper.toDomain(entity);
    }

    @Override
    public Optional<Administrator> findByAdministratorId(Long administratorId) {
        return findByIdOptional(administratorId).map(AdministratorMapper::toDomain);
    }

    @Override
    public Optional<Administrator> findByIdUser(Long userId) {
        return find("userId", userId).firstResultOptional().map(AdministratorMapper::toDomain);
    }

    @Override
    public List<Administrator> findAllAdministrators() {
        return listAll().stream()
                .map(AdministratorMapper::toDomain)
                .collect(Collectors.toList());
    }
}

