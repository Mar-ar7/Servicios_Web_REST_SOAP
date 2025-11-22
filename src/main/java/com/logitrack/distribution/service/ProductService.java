package com.logitrack.distribution.service;

import com.logitrack.distribution.entity.Product;
import com.logitrack.distribution.exception.BusinessException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;

import java.util.List;


public class ProductService {

    private final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("LogiTrackPU");

    private EntityManager getEm() {
        return emf.createEntityManager();
    }

    public Product create(Product product) {
        if (product.getPrice() == null || product.getPrice().signum() < 0) {
            throw new BusinessException("Product price must be positive.");
        }

        EntityManager em = getEm();
        em.getTransaction().begin();

        em.persist(product);

        em.getTransaction().commit();
        em.close();

        return product;
    }

    public Product update(Long id, Product updated) {
        EntityManager em = getEm();
        em.getTransaction().begin();

        Product existing = em.find(Product.class, id);
        if (existing == null) {
            em.getTransaction().rollback();
            em.close();
            throw new BusinessException("Product not found with id " + id);
        }

        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setPrice(updated.getPrice());
        existing.setCategory(updated.getCategory());
        existing.setActive(updated.isActive());

        em.merge(existing);

        em.getTransaction().commit();
        em.close();

        return existing;
    }

    public Product findById(Long id) {
        EntityManager em = getEm();
        Product p = em.find(Product.class, id);
        em.close();

        if (p == null) {
            throw new BusinessException("Product not found with id " + id);
        }

        return p;
    }

    public List<Product> findAll() {
        EntityManager em = getEm();

        List<Product> list = em.createQuery(
                "SELECT p FROM Product p", Product.class
        ).getResultList();

        em.close();
        return list;
    }

    public List<Product> findByCategory(String category) {
        EntityManager em = getEm();

        TypedQuery<Product> q = em.createQuery(
                "SELECT p FROM Product p WHERE LOWER(p.category) = LOWER(:cat)",
                Product.class
        );

        q.setParameter("cat", category);

        List<Product> result = q.getResultList();
        em.close();

        return result;
    }

    public void changeStatus(Long id, boolean active) {
        EntityManager em = getEm();
        em.getTransaction().begin();

        Product p = em.find(Product.class, id);
        if (p == null) {
            em.getTransaction().rollback();
            em.close();
            throw new BusinessException("Product not found with id " + id);
        }

        p.setActive(active);
        em.merge(p);

        em.getTransaction().commit();
        em.close();
    }
}
