package com.ProjectGraduation.order.entity;

import java.util.List;

public class OrderRequest {
    private List<OrderDetails> products;
    private double price;

    public List<OrderDetails> getProducts() {
        return products;
    }

    public void setProducts(List<OrderDetails> products) {
        this.products = products;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
