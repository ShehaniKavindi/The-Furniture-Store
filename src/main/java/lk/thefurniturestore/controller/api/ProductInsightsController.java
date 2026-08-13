package lk.thefurniturestore.controller.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lk.thefurniturestore.annotation.IsAdmin;
import lk.thefurniturestore.service.ProductInsightsService;

@Path("/insights")
@IsAdmin
@Produces(MediaType.APPLICATION_JSON)
public class ProductInsightsController {
    private final ProductInsightsService insightsService = new ProductInsightsService();

    @GET
    @Path("/low-stock")
    public Response lowStock(@QueryParam("threshold") int threshold) {
        return Response.ok(insightsService.getLowStockProjection(threshold <= 0 ? 3 : threshold)).build();
    }

    @GET
    @Path("/category-sales")
    public Response categorySales() { return Response.ok(insightsService.getCategorySalesSummary()).build(); }

    @GET
    @Path("/above-category-average")
    public Response aboveCategoryAverage() { return Response.ok(insightsService.getAboveCategoryAverageProducts()).build(); }

    @GET
    @Path("/category-sales.csv")
    @Produces("text/csv")
    public Response exportCategorySales() {
        return Response.ok(insightsService.getCategorySalesCsv())
                .header("Content-Disposition", "attachment; filename=category-sales-report.csv").build();
    }

    @GET
    @Path("/low-stock.csv")
    @Produces("text/csv")
    public Response exportLowStock(@QueryParam("threshold") int threshold) {
        return Response.ok(insightsService.getLowStockCsv(threshold <= 0 ? 3 : threshold))
                .header("Content-Disposition", "attachment; filename=low-stock-report.csv").build();
    }

    @GET
    @Path("/above-category-average.csv")
    @Produces("text/csv")
    public Response exportAboveCategoryAverage() {
        return Response.ok(insightsService.getAboveCategoryAverageCsv())
                .header("Content-Disposition", "attachment; filename=pricing-insights-report.csv").build();
    }
}
