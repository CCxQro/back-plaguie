package itesm.mx.infrastructure.ingestion;

import itesm.mx.domain.repository.ingestion.IngestionFileRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * Checksum-based dedup for the upload pipeline (EP-05 refactor).
 *
 * Previous version compared ETag/Last-Modified from remote HEAD requests;
 * now we simply hash the uploaded bytes and check against known checksums.
 */
@ApplicationScoped
public class FileDetectionService {

    @Inject
    IngestionFileRepository ingestionFileRepository;

    /**
     * Returns true if an IngestionFile with this checksum already exists
     * and its status is not FAILED (i.e. it was successfully processed).
     */
    public boolean isAlreadyIngested(String checksum) {
        if (checksum == null) return false;
        return ingestionFileRepository.findByChecksum(checksum)
                .map(f -> !"FAILED".equals(f.getStatus()))
                .orElse(false);
    }

    /**
     * Computes SHA-256 of the given input stream.
     * Caller is responsible for closing the stream; this method reads it fully.
     * Size cap: 50 MB (upload endpoint enforces this before calling here).
     */
    public static String sha256(InputStream is) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buf = new byte[65536];
        int read;
        while ((read = is.read(buf)) != -1) {
            digest.update(buf, 0, read);
        }
        return HexFormat.of().formatHex(digest.digest());
    }
}
