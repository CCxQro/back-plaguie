package itesm.mx.application.usecase.marketplace.category;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import itesm.mx.domain.repository.marketplace.CategoryRepository;

@ApplicationScoped
public class DeleteCategoryUseCase {

    @Inject
    CategoryRepository categoryRepository;

    @Transactional
    public void execute(Long categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("Category id is required");
        }
        categoryRepository.delete(categoryId);
    }
}
