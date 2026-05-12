package itesm.mx.domain.models.marketplace;

public class Status {
    private Long statusId;
    private String name;

    public Status() {}

    public Status(Long statusId, String name) {
        this.statusId = statusId;
        this.name = name;
    }

    public Long getStatusId() { return statusId; }
    public void setStatusId(Long statusId) { this.statusId = statusId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}