package itesm.mx.domain.models.marketplace;

import itesm.mx.domain.models.user.User;

public class Category {
    private Long categoryId;
    private User user;
    private String name;
    private Color color;
    private Status status;

    public Category() {}

    public Category(Long categoryId, User user, String name, Color color, Status status) {
        this.categoryId = categoryId;
        this.user = user;
        this.name = name;
        this.color = color;
        this.status = status;
    }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
