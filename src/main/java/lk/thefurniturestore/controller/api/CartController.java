package lk.thefurniturestore.controller.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lk.thefurniturestore.annotation.IsUser;
import lk.thefurniturestore.dto.CartDTO;
import lk.thefurniturestore.service.CartService;
import lk.thefurniturestore.util.AppUtil;

@IsUser
@Path("/cart")
public class CartController {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCart(@Context HttpServletRequest request) {
        String responseJson = new CartService().getCart(request);
        return Response.ok().entity(responseJson).build();
    }

    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addToCart(String jsonData, @Context HttpServletRequest request) {
        CartDTO cartDTO = AppUtil.GSON.fromJson(jsonData, CartDTO.class);
        String responseJson = new CartService().addToCart(cartDTO, request);
        return Response.ok().entity(responseJson).build();
    }

    @PUT
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCartItem(String jsonData, @Context HttpServletRequest request) {
        CartDTO cartDTO = AppUtil.GSON.fromJson(jsonData, CartDTO.class);
        String responseJson = new CartService().updateCartItem(cartDTO, request);
        return Response.ok().entity(responseJson).build();
    }

    @DELETE
    @Path("/remove/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeCartItem(@PathParam("id") int cartId, @Context HttpServletRequest request) {
        String responseJson = new CartService().removeCartItem(cartId, request);
        return Response.ok().entity(responseJson).build();
    }
}
