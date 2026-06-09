package itesm.mx.infrastructure.config;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

/**
 * Agrega cabeceras de seguridad a todas las respuestas HTTP servidas por JAX-RS.
 *
 * Mitiga la vulnerabilidad reportada por ZAP "Content Security Policy (CSP)
 * Header Not Set" (CWE-693). Al ser una API JSON, se aplica una política
 * estricta que no permite cargar ningún recurso ni embebido en frames.
 */
@Provider
public class CspHeaderFilter implements ContainerResponseFilter {

    private static final String CONTENT_SECURITY_POLICY_HEADER = "Content-Security-Policy";
    private static final String CSP_API_POLICY = "default-src 'none'; frame-ancestors 'none'";

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        MultivaluedMap<String, Object> headers = responseContext.getHeaders();
        if (!headers.containsKey(CONTENT_SECURITY_POLICY_HEADER)) {
            headers.add(CONTENT_SECURITY_POLICY_HEADER, CSP_API_POLICY);
        }
    }
}
