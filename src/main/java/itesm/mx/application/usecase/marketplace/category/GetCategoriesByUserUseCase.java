package itesm.mx.application.usecase.marketplace.category;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.domain.models.marketplace.Category;
import itesm.mx.domain.repository.marketplace.CategoryRepository;

import java.util.List;

@ApplicationScoped
public class GetCategoriesByUserUseCase {

    @Inject
    CategoryRepository categoryRepository;

    public List<Category> execute(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User id is required");
        }
        return categoryRepository.findAllByUserId(userId);
    }
}