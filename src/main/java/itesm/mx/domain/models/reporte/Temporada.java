package itesm.mx.domain.models.reporte;

import java.util.List;
import java.util.Locale;

public enum Temporada {
    PRIMAVERA(List.of(3, 4, 5), "Primavera"),
    VERANO(List.of(6, 7, 8), "Verano"),
    OTONO(List.of(9, 10, 11), "Otono"),
    INVIERNO(List.of(12, 1, 2), "Invierno");

    private final List<Integer> meses;
    private final String displayName;

    Temporada(List<Integer> meses, String displayName) {
        this.meses = meses;
        this.displayName = displayName;
    }

    public List<Integer> getMeses() {
        return meses;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Temporada fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("La temporada es requerida");
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT)
                .replace("Ñ", "N")
                .replace("Á", "A")
                .replace("É", "E")
                .replace("Í", "I")
                .replace("Ó", "O")
                .replace("Ú", "U");
        for (Temporada temporada : values()) {
            if (temporada.name().equals(normalized)) {
                return temporada;
            }
        }
        throw new IllegalArgumentException(
                "Temporada invalida: " + value + ". Valores aceptados: primavera, verano, otono, invierno"
        );
    }
}
