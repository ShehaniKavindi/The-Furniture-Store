package lk.thefurniturestore.controller.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lk.thefurniturestore.dto.ProductDTO;
import lk.thefurniturestore.annotation.IsAdmin;
import lk.thefurniturestore.service.ProductService;
import org.apache.commons.io.FilenameUtils;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/products")
public class ProductController {

    @POST
    @IsAdmin
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addProduct(
            @FormDataParam("title") String title,
            @FormDataParam("description") String description,
            @FormDataParam("categoryId") int categoryId,
            @FormDataParam("price") String price,
            @FormDataParam("quantity") int quantity,
            FormDataMultiPart multiPart,
            @Context HttpServletRequest request
    ) {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setTitle(title);
        productDTO.setDescription(description);
        productDTO.setCategoryId(categoryId);
        productDTO.setPrice(price);
        productDTO.setQuantity(quantity);

        // Handle image uploads
        List<String> savedImagePaths = new ArrayList<>();
        List<FormDataBodyPart> imageParts = multiPart.getFields("images");

        if (imageParts != null && !imageParts.isEmpty()) {
            // Save to webapp/assets/images/products/
            String uploadDir = request.getServletContext().getRealPath("/assets/images/products");
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            for (FormDataBodyPart part : imageParts) {
                try {
                    InputStream inputStream = part.getValueAs(InputStream.class);
                    String originalName = part.getContentDisposition().getFileName();
                    String ext = FilenameUtils.getExtension(originalName);
                    String fileName = UUID.randomUUID().toString() + "." + ext;
                    java.nio.file.Path dest = Paths.get(uploadDir, fileName);
                    Files.copy(inputStream, dest, StandardCopyOption.REPLACE_EXISTING);
                    savedImagePaths.add("assets/images/products/" + fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        String responseJson = new ProductService().addProduct(productDTO, savedImagePaths);
        return Response.ok().entity(responseJson).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllProducts() {
        String responseJson = new ProductService().getAllProducts();
        return Response.ok().entity(responseJson).build();
    }

    @DELETE
    @IsAdmin
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteProduct(@PathParam("id") int id) {
        String responseJson = new ProductService().deleteProduct(id);
        return Response.ok().entity(responseJson).build();
    }

    @PUT
    @IsAdmin
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateProduct(@PathParam("id") int id, String jsonData) {
        String responseJson = new ProductService().updateProduct(id, jsonData);
        return Response.ok().entity(responseJson).build();
    }

    @PUT
    @IsAdmin
    @Path("/{id}/stock")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addProductStock(@PathParam("id") int id, String jsonData) {
        String responseJson = new ProductService().addProductStock(id, jsonData);
        return Response.ok().entity(responseJson).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProduct(@PathParam("id") int id) {
        String responseJson = new ProductService().getProduct(id);
        return Response.ok().entity(responseJson).build();
    }

    @GET
    @Path("/categories")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllCategories() {
        String responseJson = new ProductService().getAllCategories();
        return Response.ok().entity(responseJson).build();
    }

    @GET
    @Path("/categories/{id}/products")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProductsByCategory(@PathParam("id") int categoryId, @QueryParam("limit") int limit) {
        String responseJson = new ProductService().getProductsByCategory(categoryId, limit);
        return Response.ok().entity(responseJson).build();
    }

    @POST
    @Path("/categories")
    @IsAdmin
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addCategory(String jsonData) {
        String responseJson = new ProductService().addCategory(jsonData);
        return Response.ok().entity(responseJson).build();
    }
}
