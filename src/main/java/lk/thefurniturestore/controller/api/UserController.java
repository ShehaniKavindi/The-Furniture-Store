package lk.thefurniturestore.controller.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lk.thefurniturestore.annotation.IsUser;
import lk.thefurniturestore.dto.UserDTO;
import lk.thefurniturestore.service.UserService;
import lk.thefurniturestore.util.AppUtil;

@Path("/users")
public class UserController {

    @IsUser
    @Path("/logout")
    @GET
    public Response logout(@Context HttpServletRequest request, @Context jakarta.servlet.http.HttpServletResponse response) {

        HttpSession httpSession = request.getSession(false);
        if (httpSession != null && httpSession.getAttribute("user") != null) {
            httpSession.invalidate();
            lk.thefurniturestore.util.RememberMeUtil.clear(request, response);
            return Response.status(Response.Status.OK).build();
        } else {
            System.out.println("else");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createNewAccount(String jsonData) {
        UserDTO userDTO = AppUtil.GSON.fromJson(jsonData, UserDTO.class);
        String responseJson = new UserService().addNewUser(userDTO);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/login")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response userLogin(String jsonData, @Context HttpServletRequest request, @Context jakarta.servlet.http.HttpServletResponse response) {
        UserDTO userDTO = AppUtil.GSON.fromJson(jsonData, UserDTO.class);
        String responseJson = new UserService().userLogin(userDTO, request, response);
        // manage session cart and db cart
//        new CartService().mergeUserCarts(request);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/forgot-password")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response forgotPassword(String jsonData) {
        UserDTO userDTO = AppUtil.GSON.fromJson(jsonData, UserDTO.class);
        String responseJson = new UserService().forgotPassword(userDTO);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/reset-password")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response resetPassword(String jsonData) {
        UserDTO userDTO = AppUtil.GSON.fromJson(jsonData, UserDTO.class);
        String responseJson = new UserService().resetPassword(userDTO);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/verify")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response verifyUserAccount(String jsonData) {
        UserDTO userDTO = AppUtil.GSON.fromJson(jsonData, UserDTO.class);
        String responseJson = new UserService().verifyUserAccount(userDTO);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/profile")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @IsUser
    public Response getUserProfile(@Context HttpServletRequest request) {
        String responseJson = new UserService().getUserProfile(request);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/profile")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @IsUser
    public Response updateUserProfile(String jsonData, @Context HttpServletRequest request) {
        UserDTO userDTO = AppUtil.GSON.fromJson(jsonData, UserDTO.class);
        String responseJson = new UserService().updateUserProfile(userDTO, request);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/provinces")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProvinces() {
        String responseJson = new UserService().getProvinces();
        return Response.ok().entity(responseJson).build();
    }

    @Path("/provinces/{id}/districts")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDistrictsByProvince(@PathParam("id") int provinceId) {
        String responseJson = new UserService().getDistrictsByProvince(provinceId);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/districts/{id}/cities")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCitiesByDistrict(@PathParam("id") int districtId) {
        String responseJson = new UserService().getCitiesByDistrict(districtId);
        return Response.ok().entity(responseJson).build();
    }
}
