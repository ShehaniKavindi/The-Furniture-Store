package lk.thefurniturestore.controller.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import lk.thefurniturestore.annotation.IsAdmin;
import lk.thefurniturestore.service.AdminExportService;

@Path("/exports")
@IsAdmin
@Produces("text/csv")
public class AdminExportController {
    private final AdminExportService exportService = new AdminExportService();

    @GET @Path("/orders.csv")
    public Response orders() { return download(exportService.ordersCsv(), "orders-report.csv"); }

    @GET @Path("/products.csv")
    public Response products() { return download(exportService.productsCsv(), "products-report.csv"); }

    @GET @Path("/customers.csv")
    public Response customers() { return download(exportService.customersCsv(), "customers-report.csv"); }

    @GET @Path("/categories.csv")
    public Response categories() { return download(exportService.categoriesCsv(), "categories-report.csv"); }

    private Response download(String csv, String fileName) {
        return Response.ok(csv).header("Content-Disposition", "attachment; filename=" + fileName).build();
    }
}
