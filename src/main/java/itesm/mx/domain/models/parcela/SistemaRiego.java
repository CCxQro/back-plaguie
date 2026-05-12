package itesm.mx.domain.models.parcela;

public class SistemaRiego {
    private Long sistemaRiegoId;
    private String nombre;

    public SistemaRiego() {
    }

    public SistemaRiego(Long sistemaRiegoId, String nombre) {
        this.sistemaRiegoId = sistemaRiegoId;
        this.nombre = nombre;
    }

    public Long getSistemaRiegoId() {
        return sistemaRiegoId;
    }

    public void setSistemaRiegoId(Long sistemaRiegoId) {
        this.sistemaRiegoId = sistemaRiegoId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
