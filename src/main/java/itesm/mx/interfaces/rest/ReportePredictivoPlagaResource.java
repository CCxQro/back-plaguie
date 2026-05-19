package itesm.mx.interfaces.rest;

import itesm.mx.application.dto.GetReportePredictivoPlagasResponseDto;
import itesm.mx.application.mapper.reporte.ReportePredictivoDtoMapper;
import itesm.mx.application.security.AuthenticatedUserContext;
import itesm.mx.application.usecase.reporte.ExportarReportePredictivoExcelUseCase;
import itesm.mx.application.usecase.reporte.ExportarReportePredictivoPdfUseCase;
import itesm.mx.application.usecase.reporte.GenerarReportePredictivoPlagasUseCase;
import itesm.mx.domain.models.reporte.ReportePredictivoPlagas;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static itesm.mx.interfaces.rest.utils.ErrorResponseUtils.errorResponse;

@Path("/api/reports/plagas/predictivo")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Reporte Predictivo de Plagas", description = "Reporte predictivo de plagas por region y temporada para ejecutivos de ventas")
public class ReportePredictivoPlagaResource {

    private static final Logger LOG = Logger.getLogger(ReportePredictivoPlagaResource.class);

    private static final Set<Integer> ALLOWED_ROLE_IDS = Set.of(1, 3);

    @Inject
    GenerarReportePredictivoPlagasUseCase generarReportePredictivoPlagasUseCase;

    @Inject
    ExportarReportePredictivoPdfUseCase exportarReportePredictivoPdfUseCase;

    @Inject
    ExportarReportePredictivoExcelUseCase exportarReportePredictivoExcelUseCase;

    @Inject
    AuthenticatedUserContext authenticatedUserContext;

    @GET
    @Operation(summary = "Generar reporte predictivo",
            description = "Genera un reporte predictivo de plagas por region y temporada usando datos historicos de vigilancia fitosanitaria.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Reporte generado",
                    content = @Content(schema = @Schema(implementation = GetReportePredictivoPlagasResponseDto.class))),
            @APIResponse(responseCode = "400", description = "Parametros invalidos"),
            @APIResponse(responseCode = "401", description = "Autenticacion requerida"),
            @APIResponse(responseCode = "403", description = "Rol no autorizado"),
            @APIResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public Response getReportePredictivo(
            @Parameter(description = "Nombre del estado o municipio", required = true) @QueryParam("region") String region,
            @Parameter(description = "Temporada: primavera, verano, otono, invierno", required = true) @QueryParam("temporada") String temporada
    ) {
        Response auth = enforceAuth();
        if (auth != null) {
            return auth;
        }

        try {
            ReportePredictivoPlagas reporte = generarReportePredictivoPlagasUseCase.execute(region, temporada);
            return Response.ok(ReportePredictivoDtoMapper.toResponseDto(reporte)).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            LOG.errorf(e, "Error generando reporte predictivo region=%s temporada=%s", region, temporada);
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @GET
    @Path("/export/pdf")
    @Produces("application/pdf")
    @Operation(summary = "Descargar reporte predictivo en PDF",
            description = "Genera el reporte predictivo y lo devuelve como archivo PDF descargable.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "PDF generado"),
            @APIResponse(responseCode = "400", description = "Parametros invalidos"),
            @APIResponse(responseCode = "401", description = "Autenticacion requerida"),
            @APIResponse(responseCode = "403", description = "Rol no autorizado"),
            @APIResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public Response exportarPdf(
            @QueryParam("region") String region,
            @QueryParam("temporada") String temporada
    ) {
        Response auth = enforceAuth();
        if (auth != null) {
            return auth;
        }

        try {
            byte[] pdf = exportarReportePredictivoPdfUseCase.execute(region, temporada);
            String filename = buildFilename(region, temporada, "pdf");
            return Response.ok(pdf)
                    .type("application/pdf")
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            LOG.errorf(e, "Error exportando PDF region=%s temporada=%s", region, temporada);
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @GET
    @Path("/export/excel")
    @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @Operation(summary = "Descargar reporte predictivo en Excel",
            description = "Genera el reporte predictivo y lo devuelve como archivo XLSX descargable.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Excel generado"),
            @APIResponse(responseCode = "400", description = "Parametros invalidos"),
            @APIResponse(responseCode = "401", description = "Autenticacion requerida"),
            @APIResponse(responseCode = "403", description = "Rol no autorizado"),
            @APIResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public Response exportarExcel(
            @QueryParam("region") String region,
            @QueryParam("temporada") String temporada
    ) {
        Response auth = enforceAuth();
        if (auth != null) {
            return auth;
        }

        try {
            byte[] excel = exportarReportePredictivoExcelUseCase.execute(region, temporada);
            String filename = buildFilename(region, temporada, "xlsx");
            return Response.ok(excel)
                    .type("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            LOG.errorf(e, "Error exportando Excel region=%s temporada=%s", region, temporada);
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    private Response enforceAuth() {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }
        Integer roleId = authenticatedUserContext.getCurrentUser().getRoleId();
        if (roleId == null || !ALLOWED_ROLE_IDS.contains(roleId)) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo administradores y ejecutivos de ventas pueden consultar este reporte");
        }
        return null;
    }

    private String buildFilename(String region, String temporada, String extension) {
        String regionPart = region == null ? "region" : region.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-");
        String temporadaPart = temporada == null ? "temporada" : temporada.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-");
        String base = "reporte-predictivo-plagas-" + regionPart + "-" + temporadaPart + "." + extension;
        return URLEncoder.encode(base, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
