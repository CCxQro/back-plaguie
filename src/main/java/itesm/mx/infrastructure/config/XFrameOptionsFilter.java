package itesm.mx.infrastructure.config;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

/**
 * Agrega cabeceras de seguridad a todas las respuestas HTTP.
 *
 * <p>X-Frame-Options: DENY mitiga ataques de clickjacking (CWE-1021) impidiendo
 * que el backend sea embebido en iframes. Aunque navegadores modernos prefieren
 * la directiva CSP frame-ancestors, X-Frame-Options sigue siendo necesario para
 * compatibilidad con navegadores antiguos.</p>
 */
@Provider
public class XFrameOptionsFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        responseContext.getHeaders().putSingle("X-Frame-Options", "DENY");
    }
}
