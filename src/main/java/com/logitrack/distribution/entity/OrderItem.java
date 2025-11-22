package com.logitrack.distribution.entity;

import jakarta.persistence.*;
import jakarta.json.bind.annotation.JsonbTransient;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@IdClass(OrderItemId.class)
public class OrderItem implements Serializable {

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id")
    @JsonbTransient   // 👈 necesario para evitar recursión
    private Order order;

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotal;

    @PrePersist
    @PreUpdate
    public void prePersist() {
        if (unitPrice == null && product != null) {
            unitPrice = product.getPrice();
        }
        if (quantity != null && unitPrice != null) {
            subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    // Getters & setters
    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}
