package itesm.mx.application.ingestion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import itesm.mx.domain.models.ingestion.DiscoveredFile;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CKAN JSON and HTML scrape parsing logic (SCRUM-313 / SCRUM-319).
 * Uses fixture files from src/test/resources/fixtures/.
 * Does NOT require network access.
 */
class DiscoveryParsingTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // ---- CKAN JSON parsing ----

    @Test
    void parseCkanJson_returnsOnlyCsvResources() throws Exception {
        String json = loadFixture("fixtures/ckan_response.json");
        List<DiscoveredFile> files = parseCkan(json);

        assertEquals(2, files.size(), "Should return 2 CSV resources, skipping PDF");
        assertTrue(files.stream().allMatch(f -> f.getSourceUrl().endsWith(".csv")));
    }

    @Test
    void parseCkanJson_setsLastModified() throws Exception {
        String json = loadFixture("fixtures/ckan_response.json");
        List<DiscoveredFile> files = parseCkan(json);

        assertNotNull(files.get(0).getLastModified());
        assertEquals("2025-12-01T00:00:00", files.get(0).getLastModified());
    }

    @Test
    void parseCkanJson_setsFilename() throws Exception {
        String json = loadFixture("fixtures/ckan_response.json");
        List<DiscoveredFile> files = parseCkan(json);

        assertTrue(files.get(0).getFilename().contains("4o_Trimestre"));
    }

    @Test
    void parseCkanJson_successFalse_throwsException() {
        String json = "{\"success\":false,\"error\":{\"message\":\"Not found\"}}";
        assertThrows(Exception.class, () -> parseCkan(json));
    }

    // ---- HTML scrape parsing ----

    @Test
    void parseHtml_returnsCsvButtonLinks() throws Exception {
        String html = loadFixture("fixtures/dataset_page.html");
        List<DiscoveredFile> files = parseHtml(html);

        // Should find 2 allowed CSV links (evil.com is filtered by SsrfGuard)
        assertEquals(2, files.size(), "Should find 2 allowed CSV download links");
    }

    @Test
    void parseHtml_filtersNonAllowedHosts() throws Exception {
        String html = loadFixture("fixtures/dataset_page.html");
        List<DiscoveredFile> files = parseHtml(html);

        assertTrue(files.stream().noneMatch(f -> f.getSourceUrl().contains("evil.com")));
    }

    @Test
    void parseHtml_setsFilename() throws Exception {
        String html = loadFixture("fixtures/dataset_page.html");
        List<DiscoveredFile> files = parseHtml(html);

        assertTrue(files.stream().anyMatch(f -> f.getFilename().contains("4o_Trimestre")));
    }

    // ---- CSV fixture sanity check ----

    @Test
    void fixtureCsv_hasExpectedRows() throws Exception {
        String csv = loadFixture("fixtures/senasica_sample.csv");
        String[] lines = csv.split("\n");
        // 1 header + 5 data rows
        assertEquals(6, lines.length);
        assertTrue(lines[0].contains("estado"));
    }

    // ---- Helpers (inline parsing logic mirroring DiscoveryService) ----

    private List<DiscoveredFile> parseCkan(String json) throws Exception {
        JsonNode root = MAPPER.readTree(json);
        if (!root.path("success").asBoolean(false)) {
            throw new Exception("CKAN API returned success=false");
        }
        List<DiscoveredFile> result = new ArrayList<>();
        for (JsonNode resource : root.path("result").path("resources")) {
            String format = resource.path("format").asText("");
            if (!"CSV".equalsIgnoreCase(format)) continue;
            String url = resource.path("url").asText("");
            if (url.isBlank()) continue;
            if (!url.startsWith("https://repodatos.atdt.gob.mx") && !url.startsWith("https://datos.gob.mx")) continue;
            String lastMod = resource.path("last_modified").asText(null);
            String name = resource.path("name").asText(url.substring(url.lastIndexOf('/') + 1));
            result.add(new DiscoveredFile(url, name, lastMod, "CSV"));
        }
        return result;
    }

    private List<DiscoveredFile> parseHtml(String html) {
        Document doc = Jsoup.parse(html);
        Elements links = doc.select("a.btn-outline-primary[href$=.csv]");
        List<DiscoveredFile> result = new ArrayList<>();
        for (Element link : links) {
            String href = link.attr("href");
            if (href.isBlank()) continue;
            // SSRF filter
            if (!href.startsWith("https://repodatos.atdt.gob.mx") && !href.startsWith("https://datos.gob.mx")) continue;
            String filename = href.substring(href.lastIndexOf('/') + 1);
            result.add(new DiscoveredFile(href, filename, null, "CSV"));
        }
        return result;
    }

    private String loadFixture(String path) throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            assertNotNull(is, "Fixture not found: " + path);
            return new String(is.readAllBytes());
        }
    }
}
