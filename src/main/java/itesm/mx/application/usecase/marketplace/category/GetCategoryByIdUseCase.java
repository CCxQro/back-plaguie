package itesm.mx.application.usecase.marketplace.category;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.domain.models.marketplace.Category;
import itesm.mx.domain.repository.marketplace.CategoryRepository;

import java.util.Optional;

@ApplicationScoped
public class GetCategoryByIdUseCase {

    @Inject
    CategoryRepository categoryRepository;

    public Optional<Category> execute(Long categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("Category id is required");
        }
        return categoryRepository.findByCategoryId(categoryId);
    }
}
