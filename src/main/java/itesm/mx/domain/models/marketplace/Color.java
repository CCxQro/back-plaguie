package itesm.mx.domain.models.marketplace;

public class Color {
    private Long colorId;
    private String name;
    private String hexa;

    public Color() {}

    public Color(Long colorId, String name, String hexa) {
        this.colorId = colorId;
        this.name = name;
        this.hexa = hexa;
    }

    public Long getColorId() { return colorId; }
    public void setColorId(Long colorId) { this.colorId = colorId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getHexa() { return hexa; }
    public void setHexa(String hexa) { this.hexa = hexa; }
}