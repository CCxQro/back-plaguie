package itesm.mx.infrastructure.ingestion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import itesm.mx.domain.models.ingestion.DiscoveredFile;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Discovers CSV files from the SENASICA phytosanitary surveillance dataset.
 *
 * Strategy A (preferred): CKAN package_show API.
 * Strategy B (fallback): jsoup HTML scrape of the dataset page.
 *
 * SSRF-safe: all URLs pass through {@link SsrfGuard#check(String)} before fetching.
 * Resilient: retries with exponential backoff (up to 3 attempts) on 5xx/IOExceptions.
 */
@ApplicationScoped
public class DiscoveryService {

    private static final Logger LOG = Logger.getLogger(DiscoveryService.class);

    private static final String CKAN_API_URL =
            "https://datos.gob.mx/api/3/action/package_show?id=programas_vigilancia_epidemiologica_fitosanitaria";

    private static final String DATASET_PAGE_URL =
            "https://www.datos.gob.mx/dataset/programas_vigilancia_epidemiologica_fitosanitaria";

    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_BACKOFF_MS = 1000L;

    @ConfigProperty(name = "ingestion.discovery.connect-timeout-seconds", defaultValue = "15")
    int connectTimeoutSeconds;

    @ConfigProperty(name = "ingestion.discovery.request-timeout-seconds", defaultValue = "30")
    int requestTimeoutSeconds;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Returns list of discovered CSV files.
     * Tries CKAN API first; falls back to HTML scrape if API fails.
     */
    public List<DiscoveredFile> discoverFiles() {
        LOG.info("Starting SENASICA file discovery via CKAN API");
        try {
            List<DiscoveredFile> files = discoverViaCkan();
            if (!files.isEmpty()) {
                LOG.infof("CKAN API returned %d CSV resources", files.size());
                return files;
            }
            LOG.warn("CKAN API returned no CSV resources, falling back to HTML scrape");
        } catch (Exception e) {
            LOG.warnf("CKAN API discovery failed (%s), falling back to HTML scrape", e.getMessage());
        }

        try {
            List<DiscoveredFile> files = discoverViaHtmlScrape();
            LOG.infof("HTML scrape returned %d CSV links", files.size());
            return files;
        } catch (Exception e) {
            LOG.errorf(e, "HTML scrape fallback also failed");
            throw new RuntimeException("SENASICA discovery failed via both CKAN API and HTML scrape: " + e.getMessage(), e);
        }
    }

    // ---- Strategy A: CKAN API ----

    private List<DiscoveredFile> discoverViaCkan() throws Exception {
        SsrfGuard.check(CKAN_API_URL);
        String json = fetchWithRetry(CKAN_API_URL);

        JsonNode root = objectMapper.readTree(json);
        if (!root.path("success").asBoolean(false)) {
            throw new IOException("CKAN API returned success=false");
        }

        List<DiscoveredFile> result = new ArrayList<>();
        JsonNode resources = root.path("result").path("resources");
        for (JsonNode resource : resources) {
            String format = resource.path("format").asText("");
            if (!"CSV".equalsIgnoreCase(format)) continue;

            String url = resource.path("url").asText("");
            if (url.isBlank()) continue;

            // Only accept downloads from our allowed hosts
            if (!SsrfGuard.isAllowed(url)) {
                LOG.debugf("Skipping resource URL not on allow-list: %s", url);
                continue;
            }

            String lastModified = resource.path("last_modified").asText(null);
            String name = resource.path("name").asText(extractFilename(url));

            result.add(new DiscoveredFile(url, name, lastModified, "CSV"));
        }
        return result;
    }

    // ---- Strategy B: HTML scrape ----

    private List<DiscoveredFile> discoverViaHtmlScrape() throws Exception {
        SsrfGuard.check(DATASET_PAGE_URL);
        String html = fetchWithRetry(DATASET_PAGE_URL);

        Document doc = Jsoup.parse(html);
        // Selector: anchor buttons with class btn-outline-primary whose href ends in .csv
        Elements links = doc.select("a.btn-outline-primary[href$=.csv]");

        List<DiscoveredFile> result = new ArrayList<>();
        for (Element link : links) {
            String href = link.absUrl("href");
            if (href.isBlank()) {
                href = link.attr("href");
            }
            if (!SsrfGuard.isAllowed(href)) continue;

            String filename = extractFilename(href);
            result.add(new DiscoveredFile(href, filename, null, "CSV"));
        }
        return result;
    }

    // ---- HTTP fetch with retry/backoff ----

    private String fetchWithRetry(String url) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
                .build();

        Exception lastException = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(requestTimeoutSeconds))
                        .GET()
                        .header("Accept", "application/json, text/html")
                        .header("User-Agent", "plaguie-ingestion/1.0")
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    return response.body();
                }

                if (response.statusCode() >= 500) {
                    lastException = new IOException("Server error " + response.statusCode() + " for " + url);
                    LOG.warnf("Attempt %d/%d: HTTP %d for %s, retrying...", attempt, MAX_RETRIES, response.statusCode(), url);
                } else {
                    throw new IOException("HTTP " + response.statusCode() + " for " + url);
                }
            } catch (IOException | InterruptedException e) {
                lastException = e;
                LOG.warnf("Attempt %d/%d: %s for %s, retrying...", attempt, MAX_RETRIES, e.getMessage(), url);
            }

            if (attempt < MAX_RETRIES) {
                try {
                    Thread.sleep(INITIAL_BACKOFF_MS * (1L << (attempt - 1))); // exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted during retry backoff", ie);
                }
            }
        }
        throw new IOException("Exhausted " + MAX_RETRIES + " retries for " + url, lastException);
    }

    private static String extractFilename(String url) {
        if (url == null) return "unknown.csv";
        int slash = url.lastIndexOf('/');
        return slash >= 0 && slash < url.length() - 1 ? url.substring(slash + 1) : url;
    }

    /**
     * Perform an HTTP HEAD request and return selected response headers.
     * Used by the detection service to compare ETag/Last-Modified without downloading.
     */
    public HeadResult head(String url) {
        SsrfGuard.check(url);
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
                .build();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(requestTimeoutSeconds))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .header("User-Agent", "plaguie-ingestion/1.0")
                    .build();
            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            String etag = response.headers().firstValue("ETag").orElse(null);
            String lastMod = response.headers().firstValue("Last-Modified").orElse(null);
            long contentLen = response.headers().firstValueAsLong("Content-Length").orElse(-1L);
            return new HeadResult(etag, lastMod, contentLen, response.statusCode());
        } catch (Exception e) {
            LOG.warnf("HEAD request failed for %s: %s", url, e.getMessage());
            return new HeadResult(null, null, -1L, -1);
        }
    }

    public record HeadResult(String etag, String lastModified, long contentLength, int statusCode) {}
}
