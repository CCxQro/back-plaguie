package itesm.mx.infrastructure.persistence.repository.marketplace;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import itesm.mx.domain.models.marketplace.Color;
import itesm.mx.domain.repository.marketplace.ColorRepository;
import itesm.mx.infrastructure.mapper.marketplace.ColorMapper;
import itesm.mx.infrastructure.persistence.entity.marketplace.ColorEntity;

import java.util.List;

@ApplicationScoped
public class ColorRepositoryImpl implements PanacheRepositoryBase<ColorEntity, Long>, ColorRepository {

    @Override
    public Color save(Color color) {
        ColorEntity entity = ColorMapper.toEntity(color);
        persistAndFlush(entity);
        return ColorMapper.toDomain(entity);
    }

    @Override
    public List<Color> findAllColors() {
        return listAll().stream()
                .map(ColorMapper::toDomain)
                .toList();
    }

    @Override
    public void delete(Long colorId) {
        ColorEntity entity = findByIdOptional(colorId)
                .orElseThrow(() -> new IllegalArgumentException("Color not found: " + colorId));
        delete(entity);
    }
}