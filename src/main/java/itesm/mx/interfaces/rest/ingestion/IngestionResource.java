package itesm.mx.interfaces.rest.ingestion;

import io.smallrye.mutiny.Multi;
import itesm.mx.application.dto.ingestion.IngestionFileResponseDto;
import itesm.mx.application.dto.ingestion.IngestionProgressEvent;
import itesm.mx.application.dto.ingestion.IngestionRunResponseDto;
import itesm.mx.application.security.AuthenticatedUserContext;
import itesm.mx.application.usecase.ingestion.CheckIngestionUseCase;
import itesm.mx.application.usecase.ingestion.GetIngestionRunsUseCase;
import itesm.mx.domain.models.ingestion.IngestionFile;
import itesm.mx.domain.models.ingestion.IngestionRun;
import itesm.mx.domain.models.user.RoleConstants;
import jakarta.inject.Inject;
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
import org.jboss.resteasy.reactive.RestStreamElementType;

import java.util.List;

import static itesm.mx.interfaces.rest.utils.ErrorResponseUtils.errorResponse;

/**
 * SCRUM-315, SCRUM-317: Admin-only ingestion pipeline endpoints.
 *
 * POST /api/ingestion/check           — trigger discovery + produce Kafka messages
 * GET  /api/ingestion/runs            — history of ingestion runs
 * GET  /api/ingestion/progress/stream — SSE stream of live progress events
 */
@Path("/api/ingestion")
@Tag(name = "Ingestion", description = "SENASICA phytosanitary data ingestion pipeline (Admin only)")
public class IngestionResource {

    @Inject
    AuthenticatedUserContext authenticatedUserContext;

    @Inject
    CheckIngestionUseCase checkIngestionUseCase;

    @Inject
    GetIngestionRunsUseCase getIngestionRunsUseCase;

    @Inject
    @Channel("ingestion-progress-in")
    Multi<IngestionProgressEvent> progressStream;

    // ---- POST /api/ingestion/check ----

    @POST
    @Path("/check")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Trigger SENASICA ingestion check",
               description = "Admin only. Discovers new/changed CSV files and queues them for processing.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Check initiated, returns run summary"),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "403", description = "Admin role required"),
            @APIResponse(responseCode = "500", description = "Discovery or Kafka error")
    })
    public Response triggerCheck() {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }
        if (!RoleConstants.ADMIN.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo administradores pueden disparar la ingesta");
        }
        try {
            IngestionRun run = checkIngestionUseCase.execute();
            return Response.ok(toRunDto(run)).build();
        } catch (SecurityException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
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
               description = "Admin only. Returns a Server-Sent Events stream of progress updates from the Kafka topic ingestion.progress.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "SSE stream of IngestionProgressEvent"),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "403", description = "Admin role required")
    })
    public Multi<IngestionProgressEvent> progressStream() {
        // Note: SSE endpoints cannot return JAX-RS Response with auth checks in the normal way.
        // Auth is enforced by FirebaseAuthFilter upstream (all protected paths require Bearer token).
        // Admin role check handled here — if user not admin we return empty Multi
        // (the filter already blocks unauthenticated requests before reaching here).
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
