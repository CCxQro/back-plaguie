package itesm.mx.infrastructure.ingestion;

import java.net.URI;
import java.util.Set;

/**
 * SSRF protection: only allow fetches to the known SENASICA data portals.
 * Never fetches arbitrary user-supplied URLs (SCRUM-318 / SCRUM-292).
 */
public final class SsrfGuard {

    private static final Set<String> ALLOWED_HOSTS = Set.of(
            "datos.gob.mx",
            "www.datos.gob.mx",
            "repodatos.atdt.gob.mx"
    );

    private SsrfGuard() {
    }

    /**
     * Throws {@link SecurityException} if the URL's host is not on the allow-list.
     */
    public static void check(String url) {
        if (url == null || url.isBlank()) {
            throw new SecurityException("URL must not be blank");
        }
        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException e) {
            throw new SecurityException("Malformed URL: " + url);
        }
        String scheme = uri.getScheme();
        if (!"https".equalsIgnoreCase(scheme) && !"http".equalsIgnoreCase(scheme)) {
            throw new SecurityException("Only http/https schemes allowed, got: " + scheme);
        }
        String host = uri.getHost();
        if (host == null || !ALLOWED_HOSTS.contains(host.toLowerCase())) {
            throw new SecurityException("Host not on SSRF allow-list: " + host);
        }
    }

    public static boolean isAllowed(String url) {
        try {
            check(url);
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }
}
