package itesm.mx.interfaces.rest.sales;

import itesm.mx.application.dto.sales.InventoryAlertDto;
import itesm.mx.application.dto.sales.SalesSummaryDto;
import itesm.mx.application.security.AuthenticatedUserContext;
import itesm.mx.application.usecase.sales.GetInventoryAlertsUseCase;
import itesm.mx.application.usecase.sales.GetSalesSummaryUseCase;
import itesm.mx.domain.models.user.RoleConstants;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.List;

import static itesm.mx.interfaces.rest.utils.ErrorResponseUtils.errorResponse;

@Path("/api/sales")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Sales Dashboard", description = "Endpoints for sales summary and inventory alerts")
public class SalesDashboardResource {

    @Inject
    GetSalesSummaryUseCase getSalesSummaryUseCase;

    @Inject
    GetInventoryAlertsUseCase getInventoryAlertsUseCase;

    @Inject
    AuthenticatedUserContext authenticatedUserContext;

    @GET
    @Path("/summary")
    @Operation(summary = "Get sales summary", description = "Returns total earnings, orders, and products sold within a date range.")
    @APIResponse(responseCode = "200", description = "Summary retrieved successfully", content = @Content(schema = @Schema(implementation = SalesSummaryDto.class)))
    @APIResponse(responseCode = "401", description = "Authentication required")
    @APIResponse(responseCode = "403", description = "Seller role required")
    @SecurityRequirement(name = "jwt")
    public Response getSalesSummary(
            @QueryParam("startDate") String startDateStr,
            @QueryParam("endDate") String endDateStr) {
        
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }
        if (!RoleConstants.SELLER.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo vendedores pueden ver el dashboard de ventas");
        }

        LocalDateTime startDate = startDateStr != null ? LocalDateTime.parse(startDateStr) : LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = endDateStr != null ? LocalDateTime.parse(endDateStr) : LocalDateTime.now();
        Long sellerId = authenticatedUserContext.getCurrentUser().getUserId();

        SalesSummaryDto summary = getSalesSummaryUseCase.execute(sellerId, startDate, endDate);
        return Response.ok(summary).build();
    }

    @GET
    @Path("/inventory-alerts")
    @Operation(summary = "Get inventory alerts", description = "Returns active products with low remaining stock.")
    @APIResponse(responseCode = "200", description = "Alerts retrieved successfully", content = @Content(schema = @Schema(implementation = InventoryAlertDto.class, type = org.eclipse.microprofile.openapi.annotations.enums.SchemaType.ARRAY)))
    @APIResponse(responseCode = "401", description = "Authentication required")
    @APIResponse(responseCode = "403", description = "Seller role required")
    @SecurityRequirement(name = "jwt")
    public Response getInventoryAlerts(@QueryParam("threshold") Integer thresholdParam) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }
        if (!RoleConstants.SELLER.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo vendedores pueden ver alertas de inventario");
        }

        int threshold = thresholdParam != null ? thresholdParam : 5;
        Long sellerId = authenticatedUserContext.getCurrentUser().getUserId();

        List<InventoryAlertDto> alerts = getInventoryAlertsUseCase.execute(sellerId, threshold);
        return Response.ok(alerts).build();
    }
}
