package itesm.mx.domain.repository.marketplace;

import itesm.mx.domain.models.marketplace.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    Category save(Category category);
    Category update(Long categoryId, Category category);
    void delete(Long categoryId);
    Optional<Category> findByCategoryId(Long categoryId);
    List<Category> findAllCategories();
    List<Category> findAllByStatus(Long statusId);
    List<Category> findAllByUserId(Long userId);
}
