package com.logitrack.distribution.resource;

import com.logitrack.distribution.dto.AddItemRequest;
import com.logitrack.distribution.dto.ChangeStatusRequest;
import com.logitrack.distribution.dto.CreateOrderRequest;
import com.logitrack.distribution.entity.Order;
import com.logitrack.distribution.service.OrderService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.util.List;

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {


    private final OrderService service = new OrderService();

    // LISTAR ÓRDENES
    @GET
    public List<Order> list(@QueryParam("customerId") Long customerId,
                            @QueryParam("status") String status,
                            @QueryParam("from") String from,
                            @QueryParam("to") String to) {

        if (customerId != null) {
            return service.findByCustomer(customerId);
        }
        if (status != null && !status.isBlank()) {
            return service.findByStatus(status);
        }
        if (from != null && to != null) {
            LocalDate fromDate = LocalDate.parse(from);
            LocalDate toDate = LocalDate.parse(to);
            return service.findByDateRange(fromDate, toDate);
        }

        return service.findAll();
    }


    @GET
    @Path("/{id}")
    public Order get(@PathParam("id") Long id) {
        return service.findById(id);
    }


    @GET
    @Path("/incomplete")
    public List<Order> incomplete() {
        return service.findIncompleteOrders();
    }


    @POST
    public Response create(CreateOrderRequest request) {
        Order created = service.createOrder(request.getCustomerId());
        return Response.status(Response.Status.CREATED)
                .entity(created)
                .build();
    }


    @POST
    @Path("/{id}/items")
    public Order addItem(@PathParam("id") Long orderId, AddItemRequest request) {
        return service.addItem(orderId, request.getProductId(), request.getQuantity());
    }


    @PATCH
    @Path("/{id}/status")
    public Order changeStatus(@PathParam("id") Long orderId, ChangeStatusRequest request) {
        return service.changeStatus(orderId, request.getStatus());
    }
}
