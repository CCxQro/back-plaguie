package itesm.mx.application.usecase.marketplace.category;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.domain.models.marketplace.Category;
import itesm.mx.domain.repository.marketplace.CategoryRepository;

import java.util.List;

@ApplicationScoped
public class GetCategoriesByStatusUseCase {

    @Inject
    CategoryRepository categoryRepository;

    public List<Category> execute(Long statusId) {
        if (statusId == null) {
            throw new IllegalArgumentException("Status id is required");
        }
        return categoryRepository.findAllByStatus(statusId);
    }
}
