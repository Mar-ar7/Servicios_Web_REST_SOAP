package com.logitrack.distribution.service;

import com.logitrack.distribution.entity.Customer;
import com.logitrack.distribution.exception.BusinessException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;

import java.util.List;


public class CustomerService {

    private final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("LogiTrackPU");

    private EntityManager getEm() {
        return emf.createEntityManager();
    }

    public Customer create(Customer customer) {
        EntityManager em = getEm();
        em.getTransaction().begin();

        em.persist(customer);

        em.getTransaction().commit();
        em.close();
        return customer;
    }

    public Customer update(Long id, Customer updated) {
        EntityManager em = getEm();
        em.getTransaction().begin();

        Customer existing = em.find(Customer.class, id);
        if (existing == null) {
            em.getTransaction().rollback();
            em.close();
            throw new BusinessException("Customer not found with id " + id);
        }

        existing.setFullName(updated.getFullName());
        existing.setTaxId(updated.getTaxId());
        existing.setEmail(updated.getEmail());
        existing.setAddress(updated.getAddress());
        existing.setActive(updated.isActive());

        em.merge(existing);

        em.getTransaction().commit();
        em.close();
        return existing;
    }

    public Customer findById(Long id) {
        EntityManager em = getEm();
        Customer c = em.find(Customer.class, id);
        em.close();

        if (c == null) {
            throw new BusinessException("Customer not found with id " + id);
        }

        return c;
    }

    public List<Customer> findAll() {
        EntityManager em = getEm();

        List<Customer> customers = em.createQuery(
                "SELECT c FROM Customer c", Customer.class
        ).getResultList();

        em.close();
        return customers;
    }

    public Customer findByTaxId(String taxId) {
        EntityManager em = getEm();

        TypedQuery<Customer> q = em.createQuery(
                "SELECT c FROM Customer c WHERE c.taxId = :tax",
                Customer.class
        );
        q.setParameter("tax", taxId);

        List<Customer> result = q.getResultList();
        em.close();

        if (result.isEmpty()) {
            throw new BusinessException("Customer not found with taxId " + taxId);
        }

        return result.get(0);
    }

    public void changeStatus(Long id, boolean active) {
        EntityManager em = getEm();
        em.getTransaction().begin();

        Customer c = em.find(Customer.class, id);
        if (c == null) {
            em.getTransaction().rollback();
            em.close();
            throw new BusinessException("Customer not found with id " + id);
        }

        c.setActive(active);
        em.merge(c);

        em.getTransaction().commit();
        em.close();
    }
}
