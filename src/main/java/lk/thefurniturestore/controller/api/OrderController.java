package lk.thefurniturestore.controller.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lk.thefurniturestore.annotation.IsUser;
import lk.thefurniturestore.dto.CheckoutRequestDTO;
import lk.thefurniturestore.service.OrderService;
import lk.thefurniturestore.util.AppUtil;

@Path("/orders")
public class OrderController {

    @GET
    @Path("/my-orders")
    @Produces(MediaType.APPLICATION_JSON)
    @IsUser
    public Response getMyOrders(@Context HttpServletRequest request) {
        String responseJson = new OrderService().getMyOrders(request);
        return Response.ok().entity(responseJson).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllOrders(@Context HttpServletRequest request) {
        String responseJson = new OrderService().getAllOrders(request);
        return Response.ok().entity(responseJson).build();
    }

    @PUT
    @Path("/{id}/status")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateOrderStatus(@PathParam("id") int orderId, String jsonData, @Context HttpServletRequest request) {
        String responseJson = new OrderService().updateOrderStatus(orderId, jsonData, request);
        return Response.ok().entity(responseJson).build();
    }

    @PUT
    @Path("/my-orders/{id}/status")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @IsUser
    public Response updateMyOrderStatus(@PathParam("id") int orderId, String jsonData, @Context HttpServletRequest request) {
        String responseJson = new OrderService().updateMyOrderStatus(orderId, jsonData, request);
        return Response.ok().entity(responseJson).build();
    }

    @GET
    @Path("/delivery-types")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDeliveryTypes() {
        String responseJson = new OrderService().getDeliveryTypes();
        return Response.ok().entity(responseJson).build();
    }

    @POST
    @Path("/place")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @IsUser
    public Response placeOrder(String jsonData, @Context HttpServletRequest request) {
        CheckoutRequestDTO checkoutRequestDTO = AppUtil.GSON.fromJson(jsonData, CheckoutRequestDTO.class);
        String responseJson = new OrderService().placeOrder(checkoutRequestDTO, request);
        return Response.ok().entity(responseJson).build();
    }

    @POST
    @Path("/payhere/prepare")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @IsUser
    public Response preparePayHerePayment(String jsonData, @Context HttpServletRequest request) {
        CheckoutRequestDTO checkoutRequestDTO = AppUtil.GSON.fromJson(jsonData, CheckoutRequestDTO.class);
        String responseJson = new OrderService().preparePayHerePayment(checkoutRequestDTO, request);
        return Response.ok().entity(responseJson).build();
    }

    @POST
    @Path("/payhere/notify")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response payHereNotify(
            @FormParam("merchant_id") String merchantId,
            @FormParam("order_id") String orderId,
            @FormParam("payment_id") String paymentId,
            @FormParam("payhere_amount") String amount,
            @FormParam("payhere_currency") String currency,
            @FormParam("status_code") String statusCode
    ) {
        return Response.ok().build();
    }
}
