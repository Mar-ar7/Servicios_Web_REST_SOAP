package com.logitrack.distribution.resource;

import com.logitrack.distribution.entity.Product;
import com.logitrack.distribution.service.ReportService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/reports")
@Produces(MediaType.APPLICATION_JSON)
public class ReportResource {


    private final ReportService service = new ReportService();

    @GET
    @Path("/top-products")
    public List<Map<String, Object>> topProducts(@QueryParam("limit") @DefaultValue("10") int limit) {

        List<Object[]> raw = service.findTopSellingProducts(limit);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] row : raw) {
            Product p = (Product) row[0];
            Long totalQty = (Long) row[1];

            Map<String, Object> m = new HashMap<>();
            m.put("productId", p.getProductId());
            m.put("name", p.getName());
            m.put("category", p.getCategory());
            m.put("totalQuantity", totalQty);

            result.add(m);
        }

        return result;
    }
}
