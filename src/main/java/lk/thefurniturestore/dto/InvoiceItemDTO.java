package lk.thefurniturestore.dto;

import java.io.Serializable;

public class InvoiceItemDTO implements Serializable {

    private int productId;
    private String title;
    private double price;
    private int quantity;
    private double lineTotal;

    public InvoiceItemDTO() {
    }

    public InvoiceItemDTO(int productId, String title, double price, int quantity, double lineTotal) {
        this.productId = productId;
        this.title = title;
        this.price = price;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(double lineTotal) {
        this.lineTotal = lineTotal;
    }
}
