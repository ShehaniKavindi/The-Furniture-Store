package lk.thefurniturestore.entity;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "status")
@NamedQuery(name = "Status.findByValue",
        query = "FROM Status s WHERE s.value=:value")
public class Status implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 45,nullable = false, unique = true)
    private String value;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public enum Type{
        ACTIVE,
        PENDING,
        INACTIVE,
        BLOCKED,
        DELIVERED,
        SHIPPED,
        PACKING,
        APPROVED,
        REJECTED,
        CANCELLED,
        VERIFIED,
        RECEIVED,
        COMPLETED,
        RETURNED
    }
}
