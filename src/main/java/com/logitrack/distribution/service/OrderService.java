package com.logitrack.distribution.service;

import com.logitrack.distribution.entity.*;
import com.logitrack.distribution.exception.BusinessException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class OrderService {

    private final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("LogiTrackPU");

    private EntityManager getEm() {
        return emf.createEntityManager();
    }

    // ============================================
    // CREAR ORDEN
    // ============================================
    public Order createOrder(Long customerId) {
        EntityManager em = getEm();
        em.getTransaction().begin();

        Customer customer = em.find(Customer.class, customerId);
        if (customer == null || !customer.isActive()) {
            em.getTransaction().rollback();
            em.close();
            throw new BusinessException("Cannot create order: customer is invalid or inactive.");
        }

        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus("Pending");
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(BigDecimal.ZERO);

        em.persist(order);

        em.getTransaction().commit();
        em.close();

        return order;
    }

    // ============================================
    // AGREGAR ITEM (CORREGIDO CON MANEJO DE DUPLICADOS)
    // ============================================
    public Order addItem(Long orderId, Long productId, int quantity) {

        if (quantity <= 0) {
            throw new BusinessException("Quantity must be greater than zero.");
        }

        EntityManager em = getEm();
        em.getTransaction().begin();

        Order order = em.find(Order.class, orderId);
        if (order == null) {
            em.getTransaction().rollback();
            em.close();
            throw new BusinessException("Order not found with id " + orderId);
        }

        if ("Cancelled".equalsIgnoreCase(order.getStatus())) {
            em.getTransaction().rollback();
            em.close();
            throw new BusinessException("Cannot add items to a cancelled order.");
        }

        Product product = em.find(Product.class, productId);
        if (product == null || !product.isActive()) {
            em.getTransaction().rollback();
            em.close();
            throw new BusinessException("Product is invalid or inactive.");
        }


        TypedQuery<OrderItem> q = em.createQuery(
                "SELECT i FROM OrderItem i WHERE i.order = :order AND i.product = :product",
                OrderItem.class
        );
        q.setParameter("order", order);
        q.setParameter("product", product);

        List<OrderItem> existingList = q.getResultList();
        OrderItem item;

        if (!existingList.isEmpty()) {

            item = existingList.get(0);
            item.setQuantity(item.getQuantity() + quantity);
            item.prePersist();
            em.merge(item);
        } else {

            item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setUnitPrice(product.getPrice());
            item.prePersist();
            em.persist(item);
        }


        order.recalculateTotal();
        em.merge(order);

        em.getTransaction().commit();
        em.close();

        return order;
    }

    // ============================================
    // CAMBIAR ESTADO ORDEN
    // ============================================
    public Order changeStatus(Long orderId, String status) {
        EntityManager em = getEm();
        em.getTransaction().begin();

        Order order = em.find(Order.class, orderId);
        if (order == null) {
            em.getTransaction().rollback();
            em.close();
            throw new BusinessException("Order not found with id " + orderId);
        }

        order.setStatus(status);
        em.merge(order);

        em.getTransaction().commit();
        em.close();
        return order;
    }

    // ============================================
    // BUSCAR POR ID
    // ============================================
    public Order findById(Long id) {
        EntityManager em = getEm();
        Order o = em.find(Order.class, id);
        em.close();

        if (o == null) {
            throw new BusinessException("Order not found with id " + id);
        }

        return o;
    }

    // ============================================
    // LISTADOS
    // ============================================
    public List<Order> findAll() {
        EntityManager em = getEm();

        List<Order> result = em.createQuery(
                "SELECT o FROM Order o", Order.class
        ).getResultList();

        em.close();
        return result;
    }

    public List<Order> findByCustomer(Long customerId) {
        EntityManager em = getEm();

        TypedQuery<Order> q = em.createQuery(
                "SELECT o FROM Order o WHERE o.customer.customerId = :cid",
                Order.class
        );

        q.setParameter("cid", customerId);

        List<Order> result = q.getResultList();
        em.close();
        return result;
    }

    public List<Order> findByStatus(String status) {
        EntityManager em = getEm();

        TypedQuery<Order> q = em.createQuery(
                "SELECT o FROM Order o WHERE LOWER(o.status) = LOWER(:st)",
                Order.class
        );

        q.setParameter("st", status);

        List<Order> result = q.getResultList();
        em.close();
        return result;
    }

    public List<Order> findByDateRange(LocalDate from, LocalDate to) {
        EntityManager em = getEm();

        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);

        TypedQuery<Order> q = em.createQuery(
                "SELECT o FROM Order o WHERE o.orderDate BETWEEN :from AND :to",
                Order.class
        );

        q.setParameter("from", start);
        q.setParameter("to", end);

        List<Order> result = q.getResultList();
        em.close();
        return result;
    }

    public List<Order> findIncompleteOrders() {
        EntityManager em = getEm();

        TypedQuery<Order> q = em.createQuery(
                "SELECT DISTINCT o FROM Order o LEFT JOIN o.items i " +
                        "WHERE i IS NULL OR LOWER(o.status) <> 'completed'",
                Order.class
        );

        List<Order> result = q.getResultList();
        em.close();
        return result;
    }

    // ============================================
    // DEUDAS DEL CLIENTE
    // ============================================
    public BigDecimal getTotalDebtByCustomer(Long customerId) {
        EntityManager em = getEm();

        String jpql =
                "SELECT COALESCE(SUM(o.totalAmount - " +
                        "   (SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.order = o)" +
                        "), 0) " +
                        "FROM Order o WHERE o.customer.customerId = :cid";

        TypedQuery<BigDecimal> q = em.createQuery(jpql, BigDecimal.class);
        q.setParameter("cid", customerId);

        BigDecimal result = q.getSingleResult();
        em.close();
        return result != null ? result : BigDecimal.ZERO;
    }
}
