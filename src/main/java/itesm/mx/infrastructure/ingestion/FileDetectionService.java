package itesm.mx.infrastructure.ingestion;

import itesm.mx.domain.models.ingestion.DiscoveredFile;
import itesm.mx.domain.models.ingestion.IngestionFile;
import itesm.mx.domain.repository.ingestion.IngestionFileRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Determines whether a discovered file is new, changed, or unchanged
 * relative to what was last ingested (SCRUM-314).
 *
 * Detection logic:
 * 1. Compare ETag + Last-Modified + Content-Length from a HEAD request.
 * 2. If those are inconclusive (absent or unchanged), download and compare SHA-256.
 * 3. New URL = NEW. Same URL + different hash = CHANGED. Same hash = SKIP.
 */
@ApplicationScoped
public class FileDetectionService {

    private static final Logger LOG = Logger.getLogger(FileDetectionService.class);

    /** Max bytes to download during hash-based detection to avoid OOM (200 MB). */
    private static final long MAX_DETECT_BYTES = 200L * 1024 * 1024;

    @Inject
    IngestionFileRepository ingestionFileRepository;

    @Inject
    DiscoveryService discoveryService;

    public enum DetectionResult { NEW, CHANGED, UNCHANGED }

    /**
     * Returns whether this discovered file should be ingested.
     * If NEW or CHANGED, also populates the current ETag/checksum in {@code knownFile} output param.
     */
    public DetectionResult detect(DiscoveredFile discovered, IngestionFileHolder holder) {
        Optional<IngestionFile> previous = ingestionFileRepository.findBySourceUrl(discovered.getSourceUrl());

        DiscoveryService.HeadResult head = discoveryService.head(discovered.getSourceUrl());

        if (previous.isEmpty()) {
            // Brand-new URL — always ingest
            LOG.infof("New file detected: %s", discovered.getSourceUrl());
            holder.etag = head.etag();
            holder.lastModified = head.lastModified();
            holder.contentLength = head.contentLength() > 0 ? head.contentLength() : null;
            return DetectionResult.NEW;
        }

        IngestionFile prev = previous.get();

        // Fast path: ETag or Last-Modified changed
        if (head.etag() != null && prev.getEtag() != null
                && !head.etag().equals(prev.getEtag())) {
            LOG.infof("ETag changed for %s: %s -> %s", discovered.getSourceUrl(), prev.getEtag(), head.etag());
            holder.etag = head.etag();
            holder.lastModified = head.lastModified();
            holder.contentLength = head.contentLength() > 0 ? head.contentLength() : null;
            return DetectionResult.CHANGED;
        }

        if (head.lastModified() != null && prev.getLastModified() != null
                && !head.lastModified().equals(prev.getLastModified())) {
            LOG.infof("Last-Modified changed for %s", discovered.getSourceUrl());
            holder.etag = head.etag();
            holder.lastModified = head.lastModified();
            holder.contentLength = head.contentLength() > 0 ? head.contentLength() : null;
            return DetectionResult.CHANGED;
        }

        // Content-Length changed
        if (head.contentLength() > 0 && prev.getContentLength() != null
                && head.contentLength() != prev.getContentLength()) {
            LOG.infof("Content-Length changed for %s", discovered.getSourceUrl());
            holder.etag = head.etag();
            holder.lastModified = head.lastModified();
            holder.contentLength = head.contentLength();
            return DetectionResult.CHANGED;
        }

        // Inconclusive headers — download and hash
        if (prev.getChecksum() != null) {
            try {
                String currentHash = downloadAndHash(discovered.getSourceUrl());
                holder.etag = head.etag();
                holder.lastModified = head.lastModified();
                holder.contentLength = head.contentLength() > 0 ? head.contentLength() : null;
                holder.checksum = currentHash;

                if (currentHash.equals(prev.getChecksum())) {
                    LOG.debugf("File unchanged (same SHA-256): %s", discovered.getSourceUrl());
                    return DetectionResult.UNCHANGED;
                } else {
                    LOG.infof("SHA-256 changed for %s", discovered.getSourceUrl());
                    return DetectionResult.CHANGED;
                }
            } catch (Exception e) {
                LOG.warnf("Could not hash %s: %s — treating as CHANGED to be safe", discovered.getSourceUrl(), e.getMessage());
                holder.etag = head.etag();
                return DetectionResult.CHANGED;
            }
        }

        // No previous checksum, no header delta — treat as unchanged to avoid re-ingesting
        LOG.debugf("No detectable change for %s, skipping", discovered.getSourceUrl());
        return DetectionResult.UNCHANGED;
    }

    private String downloadAndHash(String url) throws IOException, NoSuchAlgorithmException {
        SsrfGuard.check(url);
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(60))
                .GET()
                .header("User-Agent", "plaguie-ingestion/1.0")
                .build();

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try {
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream is = response.body()) {
                byte[] buf = new byte[65536];
                long total = 0L;
                int read;
                while ((read = is.read(buf)) != -1) {
                    total += read;
                    if (total > MAX_DETECT_BYTES) {
                        throw new IOException("File exceeds max detection size of " + MAX_DETECT_BYTES + " bytes");
                    }
                    digest.update(buf, 0, read);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted during hash download", e);
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    /** Mutable holder passed by caller to receive detected metadata. */
    public static class IngestionFileHolder {
        public String etag;
        public String lastModified;
        public Long contentLength;
        public String checksum;
    }
}
