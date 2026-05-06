package itesm.mx.infrastructure.persistence.repository.marketplace;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import itesm.mx.domain.models.marketplace.Category;
import itesm.mx.domain.repository.marketplace.CategoryRepository;
import itesm.mx.infrastructure.mapper.marketplace.CategoryMapper;
import itesm.mx.infrastructure.persistence.entity.marketplace.CategoryEntity;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CategoryRepositoryImpl implements PanacheRepositoryBase<CategoryEntity, Long>, CategoryRepository {

    @Override
    public Category save(Category category) {
        CategoryEntity entity = CategoryMapper.toEntity(category);
        persistAndFlush(entity);
        return CategoryMapper.toDomain(entity);
    }

    @Override
    public Category update(Long categoryId, Category category) {
        CategoryEntity entity = findByIdOptional(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));
        entity.name = category.getName();
        entity.colorId = category.getColor().getColorId();
        entity.statusId = category.getStatus().getStatusId();
        flush();
        return CategoryMapper.toDomain(entity);
    }

    @Override
    public void delete(Long categoryId) {
        CategoryEntity entity = findByIdOptional(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));
        delete(entity);
    }

    @Override
    public Optional<Category> findByCategoryId(Long categoryId) {
        return findByIdOptional(categoryId).map(CategoryMapper::toDomain);
    }

    @Override
    public List<Category> findAllCategories() {
        return listAll().stream()
                .map(CategoryMapper::toDomain)
                .toList();
    }

    @Override
    public List<Category> findAllByStatus(Long statusId) {
        return find("statusId", statusId).stream()
                .map(CategoryMapper::toDomain)
                .toList();
    }

    @Override
    public List<Category> findAllByUserId(Long userId) {
        return find("userId", userId).stream()
                .map(CategoryMapper::toDomain)
                .toList();
    }
}
