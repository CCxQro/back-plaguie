package itesm.mx.application.dto;

public class GetCategoryResponseDto {
    public Long categoryId;
    public String name;
    public Long userId;
    public Long colorId;
    public String colorName;
    public String colorHexa;
    public Long statusId;
    public String statusName;

    public GetCategoryResponseDto() {}

    public GetCategoryResponseDto(Long categoryId, String name, Long userId,
                                  Long colorId, String colorName, String colorHexa,
                                  Long statusId, String statusName) {
        this.categoryId = categoryId;
        this.name = name;
        this.userId = userId;
        this.colorId = colorId;
        this.colorName = colorName;
        this.colorHexa = colorHexa;
        this.statusId = statusId;
        this.statusName = statusName;
    }
}
