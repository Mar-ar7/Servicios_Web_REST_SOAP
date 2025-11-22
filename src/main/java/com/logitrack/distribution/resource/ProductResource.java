package com.logitrack.distribution.resource;

import com.logitrack.distribution.entity.Product;
import com.logitrack.distribution.service.ProductService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {


    private final ProductService service = new ProductService();

    @GET
    public List<Product> list(@QueryParam("category") String category) {
        if (category != null && !category.isBlank()) {
            return service.findByCategory(category);
        }
        return service.findAll();
    }

    @GET
    @Path("/{id}")
    public Product get(@PathParam("id") Long id) {
        return service.findById(id);
    }

    @POST
    public Response create(Product product) {
        Product created = service.create(product);
        return Response.created(URI.create("/api/products/" + created.getProductId()))
                .entity(created)
                .build();
    }

    @PUT
    @Path("/{id}")
    public Product update(@PathParam("id") Long id, Product product) {
        return service.update(id, product);
    }

    @PATCH
    @Path("/{id}/status")
    public Response changeStatus(@PathParam("id") Long id, @QueryParam("active") boolean active) {
        service.changeStatus(id, active);
        return Response.noContent().build();
    }
}
