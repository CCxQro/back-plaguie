package itesm.mx.application.usecase.marketplace.category;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import itesm.mx.domain.models.marketplace.Category;
import itesm.mx.domain.repository.marketplace.CategoryRepository;

@ApplicationScoped
public class UpdateCategoryUseCase {

    @Inject
    CategoryRepository categoryRepository;

    @Transactional
    public Category execute(Long categoryId, Category category) {
        if (categoryId == null) {
            throw new IllegalArgumentException("Category id is required");
        }
        if (category == null) {
            throw new IllegalArgumentException("Category is required");
        }
        if (category.getName() == null || category.getName().isBlank()) {
            throw new IllegalArgumentException("Category name is required");
        }
        if (category.getColor() == null || category.getColor().getColorId() == null) {
            throw new IllegalArgumentException("Color is required");
        }
        if (category.getStatus() == null || category.getStatus().getStatusId() == null) {
            throw new IllegalArgumentException("Status is required");
        }
        return categoryRepository.update(categoryId, category);
    }
}
