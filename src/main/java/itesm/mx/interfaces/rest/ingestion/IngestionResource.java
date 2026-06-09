package itesm.mx.interfaces.rest.ingestion;

import io.smallrye.mutiny.Multi;
import itesm.mx.application.dto.ingestion.IngestionFileResponseDto;
import itesm.mx.application.dto.ingestion.IngestionProgressEvent;
import itesm.mx.application.dto.ingestion.IngestionRunResponseDto;
import itesm.mx.application.security.AuthenticatedUserContext;
import itesm.mx.application.usecase.ingestion.GetIngestionRunsUseCase;
import itesm.mx.application.usecase.ingestion.UploadIngestionUseCase;
import itesm.mx.domain.models.ingestion.IngestionFile;
import itesm.mx.domain.models.ingestion.IngestionRun;
import itesm.mx.domain.models.user.RoleConstants;
import itesm.mx.infrastructure.ingestion.FileDetectionService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestStreamElementType;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import static itesm.mx.interfaces.rest.utils.ErrorResponseUtils.errorResponse;

/**
 * EP-05 Admin-only ingestion endpoints (upload variant).
 *
 * POST /api/ingestion/upload           — admin uploads a SENASICA CSV
 * GET  /api/ingestion/runs             — history of ingestion runs
 * GET  /api/ingestion/progress/stream  — SSE stream of live progress events
 */
@Path("/api/ingestion")
@Tag(name = "Ingestion", description = "SENASICA phytosanitary data ingestion pipeline (Admin only)")
public class IngestionResource {

    private static final Logger LOG = Logger.getLogger(IngestionResource.class);

<<<<<<< HEAD
    /** 200 MB upload cap (bytes). */
    private static final long MAX_UPLOAD_BYTES = 200L * 1024 * 1024;
=======
    /** 50 MB upload cap (bytes). */
    private static final long MAX_UPLOAD_BYTES = 50L * 1024 * 1024;
>>>>>>> origin/main

    @Inject
    AuthenticatedUserContext authenticatedUserContext;

    @Inject
    UploadIngestionUseCase uploadIngestionUseCase;

    @Inject
    GetIngestionRunsUseCase getIngestionRunsUseCase;

    @Inject
<<<<<<< HEAD
    @Channel("ingestion-progress")
=======
    @Channel("ingestion-progress-in")
>>>>>>> origin/main
    Multi<IngestionProgressEvent> progressStream;

    // ---- POST /api/ingestion/upload ----

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Upload SENASICA CSV for ingestion",
<<<<<<< HEAD
               description = "Admin only. Accepts a CSV file (≤200 MB), deduplicates by SHA-256 checksum, "
=======
               description = "Admin only. Accepts a CSV file (≤50 MB), deduplicates by SHA-256 checksum, "
>>>>>>> origin/main
                       + "and queues it for processing. Original filename stored in source_url column.")
    @APIResponses({
            @APIResponse(responseCode = "200",  description = "File accepted and queued (or already ingested)"),
            @APIResponse(responseCode = "400",  description = "Invalid file: not CSV, empty, or too large"),
            @APIResponse(responseCode = "401",  description = "Authentication required"),
            @APIResponse(responseCode = "403",  description = "Admin role required"),
            @APIResponse(responseCode = "500",  description = "Internal server error")
    })
    public Response upload(@RestForm("file") FileUpload fileUpload) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }
        if (!RoleConstants.ADMIN.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo administradores pueden cargar archivos de ingesta");
        }

        // Validate: must have a file
        if (fileUpload == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "Se requiere el campo 'file' en el formulario multipart");
        }

        String originalName = fileUpload.fileName();
        if (originalName == null || originalName.isBlank()) {
            originalName = "upload.csv";
        }
        // Normalize to basename only (prevent path traversal in the name itself)
        originalName = Paths.get(originalName).getFileName().toString();

        // Validate CSV extension/content-type
        String contentType = fileUpload.contentType();
        boolean validExt  = originalName.toLowerCase().endsWith(".csv");
        boolean validMime = contentType == null
                || contentType.contains("text/csv")
                || contentType.contains("text/plain")
                || contentType.contains("application/csv")
                || contentType.contains("application/octet-stream");
        if (!validExt) {
            return errorResponse(Response.Status.BAD_REQUEST,
                    "Solo se aceptan archivos CSV (extensión .csv). Tipo recibido: " + contentType);
        }

        // Size check
        long fileSize = fileUpload.size();
        if (fileSize <= 0) {
            return errorResponse(Response.Status.BAD_REQUEST, "El archivo está vacío");
        }
        if (fileSize > MAX_UPLOAD_BYTES) {
            return errorResponse(Response.Status.BAD_REQUEST,
<<<<<<< HEAD
                    "El archivo excede el límite de 200 MB (tamaño recibido: " + fileSize + " bytes)");
=======
                    "El archivo excede el límite de 50 MB (tamaño recibido: " + fileSize + " bytes)");
>>>>>>> origin/main
        }

        // Read bytes, compute SHA-256
        byte[] csvBytes;
        String checksum;
        try {
            csvBytes = Files.readAllBytes(fileUpload.uploadedFile());
            checksum = FileDetectionService.sha256(
                    new java.io.ByteArrayInputStream(csvBytes));
        } catch (Exception e) {
            LOG.errorf(e, "Failed to read/hash uploaded file");
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    "Error al leer el archivo: " + e.getMessage());
        }

        // Write to a path-safe temp file
        String tempFilePath;
        try {
            java.nio.file.Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"), "plaguie-ingestion");
            Files.createDirectories(tmpDir);
            // Safe filename: uuid + sanitized original name (no path separators)
            String safeName = UUID.randomUUID() + "_" + originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
            java.nio.file.Path dest = tmpDir.resolve(safeName).normalize().toAbsolutePath();
            // Ensure dest is inside tmpDir (path traversal guard)
            if (!dest.startsWith(tmpDir.normalize().toAbsolutePath())) {
                return errorResponse(Response.Status.BAD_REQUEST, "Nombre de archivo inválido");
            }
            try (InputStream in = new java.io.ByteArrayInputStream(csvBytes)) {
                Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
            }
            tempFilePath = dest.toString();
        } catch (Exception e) {
            LOG.errorf(e, "Failed to write temp file");
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    "Error al guardar el archivo temporal: " + e.getMessage());
        }

        try {
            IngestionRun run = uploadIngestionUseCase.execute(originalName, csvBytes, checksum, tempFilePath);
            if ("SKIPPED".equals(run.getStatus())) {
                return Response.ok(java.util.Map.of(
                        "status", "skipped/already ingested",
                        "message", "Este archivo ya fue procesado anteriormente (mismo checksum SHA-256)"
                )).build();
            }
            return Response.ok(toRunDto(run)).build();
        } catch (Exception e) {
            LOG.errorf(e, "Upload ingestion failed");
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    // ---- GET /api/ingestion/runs ----

    @GET
    @Path("/runs")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Ingestion run history",
               description = "Admin only. Returns all ingestion runs ordered by most recent first.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "List of ingestion runs"),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "403", description = "Admin role required")
    })
    public Response getRuns() {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }
        if (!RoleConstants.ADMIN.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo administradores pueden ver el historial de ingesta");
        }
        try {
            List<IngestionRunResponseDto> dtos = getIngestionRunsUseCase.execute()
                    .stream()
                    .map(this::toRunDto)
                    .toList();
            return Response.ok(dtos).build();
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    // ---- GET /api/ingestion/progress/stream (SSE) ----

    @GET
    @Path("/progress/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    @Operation(summary = "Live ingestion progress (SSE)",
               description = "Admin only. SSE stream of IngestionProgressEvents from topic ingestion.progress.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "SSE stream of IngestionProgressEvent"),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "403", description = "Admin role required")
    })
    public Multi<IngestionProgressEvent> progressStream() {
        if (authenticatedUserContext.getCurrentUser() == null
                || !RoleConstants.ADMIN.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return Multi.createFrom().empty();
        }
        return progressStream;
    }

    // ---- DTO mapping ----

    private IngestionRunResponseDto toRunDto(IngestionRun run) {
        IngestionRunResponseDto dto = new IngestionRunResponseDto();
        dto.id = run.getId();
        dto.startedAt = run.getStartedAt();
        dto.finishedAt = run.getFinishedAt();
        dto.status = run.getStatus();
        dto.filesFound = run.getFilesFound();
        dto.filesProcessed = run.getFilesProcessed();
        if (run.getFiles() != null) {
            dto.files = run.getFiles().stream().map(this::toFileDto).toList();
        }
        return dto;
    }

    private IngestionFileResponseDto toFileDto(IngestionFile file) {
        IngestionFileResponseDto dto = new IngestionFileResponseDto();
        dto.id = file.getId();
        dto.sourceUrl = file.getSourceUrl();
        dto.filename = file.getFilename();
        dto.status = file.getStatus();
        dto.rowsTotal = file.getRowsTotal();
        dto.rowsInserted = file.getRowsInserted();
        dto.rowsSkipped = file.getRowsSkipped();
        dto.error = file.getError();
        dto.createdAt = file.getCreatedAt();
        return dto;
    }
}
