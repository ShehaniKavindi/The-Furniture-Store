package lk.thefurniturestore.dto;

import java.io.Serializable;

public class CheckoutRequestDTO implements Serializable {

    private int deliveryTypeId;

    public CheckoutRequestDTO() {
    }

    public int getDeliveryTypeId() {
        return deliveryTypeId;
    }

    public void setDeliveryTypeId(int deliveryTypeId) {
        this.deliveryTypeId = deliveryTypeId;
    }
}
