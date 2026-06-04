package itesm.mx.domain.models.ingestion;

/**
 * Represents a CSV resource discovered from the SENASICA data portal.
 * Produced by DiscoveryService (CKAN API or HTML scrape fallback).
 */
public class DiscoveredFile {
    private String sourceUrl;
    private String filename;
    private String lastModified; // from CKAN metadata
    private String format;       // "CSV"

    public DiscoveredFile() {
    }

    public DiscoveredFile(String sourceUrl, String filename, String lastModified, String format) {
        this.sourceUrl = sourceUrl;
        this.filename = filename;
        this.lastModified = lastModified;
        this.format = format;
    }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getLastModified() { return lastModified; }
    public void setLastModified(String lastModified) { this.lastModified = lastModified; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
}
