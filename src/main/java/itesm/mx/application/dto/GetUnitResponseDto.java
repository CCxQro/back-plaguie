package itesm.mx.application.dto;

public class GetUnitResponseDto {
    public Long unitId;
    public String name;
    public Long userId;
    public Long statusId;
    public String statusName;

    public GetUnitResponseDto() {}

    public GetUnitResponseDto(Long unitId, String name, Long userId, Long statusId, String statusName) {
        this.unitId = unitId;
        this.name = name;
        this.userId = userId;
        this.statusId = statusId;
        this.statusName = statusName;
    }
}