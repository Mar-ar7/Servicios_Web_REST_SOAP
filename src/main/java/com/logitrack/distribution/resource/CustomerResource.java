package com.logitrack.distribution.resource;

import com.logitrack.distribution.entity.Customer;
import com.logitrack.distribution.service.CustomerService;
import com.logitrack.distribution.service.OrderService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerResource {


    private final CustomerService service = new CustomerService();
    private final OrderService orderService = new OrderService();

    @GET
    public List<Customer> list() {
        return service.findAll();
    }

    @GET
    @Path("/{id}")
    public Customer get(@PathParam("id") Long id) {
        return service.findById(id);
    }

    @GET
    @Path("/by-taxid/{taxId}")
    public Customer getByTaxId(@PathParam("taxId") String taxId) {
        return service.findByTaxId(taxId);
    }

    @POST
    public Response create(Customer customer) {
        Customer created = service.create(customer);
        return Response.created(URI.create("/api/customers/" + created.getCustomerId()))
                .entity(created)
                .build();
    }

    @PUT
    @Path("/{id}")
    public Customer update(@PathParam("id") Long id, Customer customer) {
        return service.update(id, customer);
    }

    @PATCH
    @Path("/{id}/status")
    public Response changeStatus(@PathParam("id") Long id, @QueryParam("active") boolean active) {
        service.changeStatus(id, active);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/debt")
    public Map<String, Object> getDebt(@PathParam("id") Long id) {
        var debt = orderService.getTotalDebtByCustomer(id);
        Map<String, Object> result = new HashMap<>();
        result.put("customerId", id);
        result.put("totalDebt", debt);
        return result;
    }
}
