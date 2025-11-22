package com.logitrack.distribution.service;

import com.logitrack.distribution.entity.Order;
import com.logitrack.distribution.entity.Payment;
import com.logitrack.distribution.exception.BusinessException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


public class PaymentService {

    private final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("LogiTrackPU");

    private EntityManager getEm() {
        return emf.createEntityManager();
    }

    public Payment registerPayment(Long orderId, BigDecimal amount, String method) {
        EntityManager em = getEm();
        em.getTransaction().begin();

        Order order = em.find(Order.class, orderId);
        if (order == null) {
            em.getTransaction().rollback();
            em.close();
            throw new BusinessException("Order not found with id " + orderId);
        }

        if (amount == null || amount.signum() <= 0) {
            em.getTransaction().rollback();
            em.close();
            throw new BusinessException("Payment amount must be positive.");
        }

        BigDecimal pending = order.getPendingAmount();
        if (amount.compareTo(pending) > 0) {
            em.getTransaction().rollback();
            em.close();
            throw new BusinessException("Payment exceeds pending amount for the order.");
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(amount);
        payment.setMethod(method);
        payment.setPaymentDate(LocalDateTime.now());

        em.persist(payment);
        order.getPayments().add(payment);
        em.merge(order);

        em.getTransaction().commit();
        em.close();
        return payment;
    }

    public List<Payment> findByOrder(Long orderId) {
        EntityManager em = getEm();

        TypedQuery<Payment> q = em.createQuery(
                "SELECT p FROM Payment p WHERE p.order.orderId = :oid",
                Payment.class
        );

        q.setParameter("oid", orderId);

        List<Payment> list = q.getResultList();
        em.close();
        return list;
    }
}
