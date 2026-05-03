package itesm.mx.infrastructure.mapper.marketplace;

import itesm.mx.domain.models.marketplace.Category;
import itesm.mx.domain.models.marketplace.Color;
import itesm.mx.domain.models.marketplace.Status;
import itesm.mx.domain.models.user.User;
import itesm.mx.infrastructure.mapper.user.UserMapper;
import itesm.mx.infrastructure.persistence.entity.marketplace.CategoryEntity;
import itesm.mx.infrastructure.persistence.entity.marketplace.ColorEntity;
import itesm.mx.infrastructure.persistence.entity.marketplace.StatusEntity;
import itesm.mx.infrastructure.persistence.entity.users.UserEntity;

public class CategoryMapper {

    public static CategoryEntity toEntity(Category category) {
        CategoryEntity entity = new CategoryEntity();
        entity.categoryId = category.getCategoryId();
        entity.userId = category.getUser().getUserId();
        entity.name = category.getName();
        entity.colorId = category.getColor().getColorId();
        entity.statusId = category.getStatus().getStatusId();
        return entity;
    }

    public static Category toDomain(CategoryEntity entity) {
        Category category = new Category();
        category.setCategoryId(entity.categoryId);
        category.setName(entity.name);
        category.setUser(mapUser(entity));
        category.setColor(mapColor(entity));
        category.setStatus(mapStatus(entity));
        return category;
    }

    private static User mapUser(CategoryEntity entity) {
        UserEntity userEntity = entity.user;
        if (userEntity != null) {
            return UserMapper.toDomain(userEntity);
        }
        User user = new User();
        user.setUserId(entity.userId);
        return user;
    }

    private static Color mapColor(CategoryEntity entity) {
        ColorEntity colorEntity = entity.color;
        if (colorEntity != null) {
            return ColorMapper.toDomain(colorEntity);
        }
        Color color = new Color();
        color.setColorId(entity.colorId);
        return color;
    }

    private static Status mapStatus(CategoryEntity entity) {
        StatusEntity statusEntity = entity.status;
        if (statusEntity != null) {
            return StatusMapper.toDomain(statusEntity);
        }
        Status status = new Status();
        status.setStatusId(entity.statusId);
        return status;
    }
}
