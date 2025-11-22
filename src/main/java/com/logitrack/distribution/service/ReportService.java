package com.logitrack.distribution.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.List;


public class ReportService {

    private final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("LogiTrackPU");

    private EntityManager getEm() {
        return emf.createEntityManager();
    }

    public List<Object[]> findTopSellingProducts(int limit) {
        EntityManager em = getEm();

        List<Object[]> result = em.createQuery(
                        "SELECT i.product, SUM(i.quantity) AS totalQty " +
                                "FROM OrderItem i GROUP BY i.product ORDER BY totalQty DESC",
                        Object[].class
                )
                .setMaxResults(limit)
                .getResultList();

        em.close();
        return result;
    }
}
