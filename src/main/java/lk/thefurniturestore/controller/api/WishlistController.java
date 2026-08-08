package lk.thefurniturestore.controller.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lk.thefurniturestore.annotation.IsUser;
import lk.thefurniturestore.service.WishlistService;

@Path("/wishlist")
@IsUser
@Produces(MediaType.APPLICATION_JSON)
public class WishlistController {
    private final WishlistService wishlistService = new WishlistService();

    @GET public Response get(@Context HttpServletRequest request) { return Response.ok(wishlistService.getWishlist(request)).build(); }
    @POST @Path("/{productId}") public Response add(@PathParam("productId") int productId, @Context HttpServletRequest request) { return Response.ok(wishlistService.add(productId, request)).build(); }
    @DELETE @Path("/{productId}") public Response remove(@PathParam("productId") int productId, @Context HttpServletRequest request) { return Response.ok(wishlistService.remove(productId, request)).build(); }
}
