package lk.thefurniturestore.controller.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lk.thefurniturestore.dto.AdminDTO;
import lk.thefurniturestore.annotation.IsAdmin;
import lk.thefurniturestore.service.AdminService;
import lk.thefurniturestore.util.AppUtil;

@Path("/admins")
public class AdminController {

    @Path("/login")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response adminLogin(String jsonData, @Context HttpServletRequest request) {
        AdminDTO adminDTO = AppUtil.GSON.fromJson(jsonData, AdminDTO.class);
        String responseJson = new AdminService().adminLogin(adminDTO, request);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/profile")
    @GET
    @IsAdmin
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAdminProfile(@Context HttpServletRequest request) {
        String responseJson = new AdminService().getAdminProfile(request);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/dashboard")
    @GET
    @IsAdmin
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDashboard(@Context HttpServletRequest request) {
        String responseJson = new AdminService().getDashboard(request);
        return Response.ok().entity(responseJson).build();
    }

    @GET
    @IsAdmin
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllAdmins(@Context HttpServletRequest request) {
        String responseJson = new AdminService().getAllAdmins(request);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/customers")
    @GET
    @IsAdmin
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllCustomers(@Context HttpServletRequest request) {
        String responseJson = new AdminService().getAllCustomers(request);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/customers/{id}/status")
    @PUT
    @IsAdmin
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCustomerStatus(@PathParam("id") int customerId, String jsonData, @Context HttpServletRequest request) {
        String responseJson = new AdminService().updateCustomerStatus(customerId, jsonData, request);
        return Response.ok().entity(responseJson).build();
    }

    @POST
    @IsAdmin
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addAdmin(String jsonData, @Context HttpServletRequest request) {
        AdminDTO adminDTO = AppUtil.GSON.fromJson(jsonData, AdminDTO.class);
        String responseJson = new AdminService().addAdmin(adminDTO, request);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/{id}/block")
    @PUT
    @IsAdmin
    @Produces(MediaType.APPLICATION_JSON)
    public Response blockAdmin(@PathParam("id") int adminId, @Context HttpServletRequest request) {
        String responseJson = new AdminService().blockAdmin(adminId, request);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/logout")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response adminLogout(@Context HttpServletRequest request) {
        String responseJson = new AdminService().adminLogout(request);
        return Response.ok().entity(responseJson).build();
    }
}
