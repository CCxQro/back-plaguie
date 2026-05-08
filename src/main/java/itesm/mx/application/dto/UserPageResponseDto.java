package itesm.mx.application.dto;

import java.util.List;

public class UserPageResponseDto {
    public List<GetUserResponseDto> content;
    public long totalElements;
    public int totalPages;
    public int page;
    public int size;

    public UserPageResponseDto(List<GetUserResponseDto> content, long totalElements, int page, int size) {
        this.content = content;
        this.totalElements = totalElements;
        this.totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        this.page = page;
        this.size = size;
    }
}
