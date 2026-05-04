package itesm.mx.domain.models.vigilancia;

public class SistemaMonitoreo {
    private Long sistemaMonitoreoId;
    private String name;

    public SistemaMonitoreo() {
    }

    public SistemaMonitoreo(Long sistemaMonitoreoId, String name) {
        this.sistemaMonitoreoId = sistemaMonitoreoId;
        this.name = name;
    }

    public Long getSistemaMonitoreoId() {
        return sistemaMonitoreoId;
    }

    public void setSistemaMonitoreoId(Long sistemaMonitoreoId) {
        this.sistemaMonitoreoId = sistemaMonitoreoId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}