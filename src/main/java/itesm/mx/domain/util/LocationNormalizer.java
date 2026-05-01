package itesm.mx.domain.util;

import java.text.Normalizer;
import java.util.Locale;

public final class LocationNormalizer {

    private LocationNormalizer() {
    }

    public static String normalize(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre es requerido");
        }

        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);

        if (normalized.isBlank()) {
            throw new IllegalArgumentException("El nombre es requerido");
        }

        return normalized;
    }
}
