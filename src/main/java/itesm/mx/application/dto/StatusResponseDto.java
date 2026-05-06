package itesm.mx.application.dto;

public class StatusResponseDto {
    public String status;
    public String database;
    public String service;
    public String timestamp;

    public StatusResponseDto() {
    }

    public StatusResponseDto(String status, String database, String service, String timestamp) {
        this.status = status;
        this.database = database;
        this.service = service;
        this.timestamp = timestamp;
    }
}