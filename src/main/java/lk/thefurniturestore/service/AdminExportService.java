package lk.thefurniturestore.service;

import lk.thefurniturestore.entity.Category;
import lk.thefurniturestore.entity.Delivery;
import lk.thefurniturestore.entity.Product;
import lk.thefurniturestore.entity.User;
import lk.thefurniturestore.util.HibernateUtil;
import org.hibernate.Session;

import java.time.format.DateTimeFormatter;
import java.util.List;

/** CSV exports used by the administrator's management pages. */
public class AdminExportService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public String ordersCsv() {
        StringBuilder csv = new StringBuilder("Order ID,Customer,Email,Delivery Type,Status,Order Date\n");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Delivery> deliveries = session.createQuery(
                    "FROM Delivery d JOIN FETCH d.order o JOIN FETCH o.user JOIN FETCH d.deliveryType JOIN FETCH d.status ORDER BY o.id DESC",
                    Delivery.class).list();
            for (Delivery delivery : deliveries) {
                User user = delivery.getOrder().getUser();
                csv.append(delivery.getOrder().getId()).append(',')
                        .append(value(user.getFname() + " " + user.getLname())).append(',')
                        .append(value(user.getEmail())).append(',')
                        .append(value(delivery.getDeliveryType().getName())).append(',')
                        .append(value(delivery.getStatus().getValue())).append(',')
                        .append(value(delivery.getOrder().getCreatedAt() == null ? "" : DATE_FORMAT.format(delivery.getOrder().getCreatedAt())))
                        .append('\n');
            }
        }
        return csv.toString();
    }

    public String productsCsv() {
        StringBuilder csv = new StringBuilder("Product ID,Product,Category,Unit Price,Stock,Inventory Status\n");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Product> products = session.createQuery("FROM Product p LEFT JOIN FETCH p.category ORDER BY p.title", Product.class).list();
            for (Product product : products) {
                String status = product.getQuantity() == 0 ? "Out of Stock" : product.getQuantity() <= 3 ? "Low Stock" : "Active";
                csv.append(product.getId()).append(',').append(value(product.getTitle())).append(',')
                        .append(value(product.getCategory() == null ? "" : product.getCategory().getName())).append(',')
                        .append(product.getPrice()).append(',').append(product.getQuantity()).append(',').append(value(status)).append('\n');
            }
        }
        return csv.toString();
    }

    public String customersCsv() {
        StringBuilder csv = new StringBuilder("Customer ID,First Name,Last Name,Email,Status,Joined\n");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<User> customers = session.createQuery("FROM User u JOIN FETCH u.status ORDER BY u.id DESC", User.class).list();
            for (User customer : customers) {
                csv.append(customer.getId()).append(',').append(value(customer.getFname())).append(',')
                        .append(value(customer.getLname())).append(',').append(value(customer.getEmail())).append(',')
                        .append(value(customer.getStatus().getValue())).append(',')
                        .append(value(customer.getCreatedAt() == null ? "" : DATE_FORMAT.format(customer.getCreatedAt()))).append('\n');
            }
        }
        return csv.toString();
    }

    public String categoriesCsv() {
        StringBuilder csv = new StringBuilder("Category ID,Category,Product Count\n");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Object[]> rows = session.createQuery(
                    "SELECT c.id, c.name, COUNT(p.id) FROM Category c LEFT JOIN Product p ON p.category.id = c.id GROUP BY c.id, c.name ORDER BY c.name",
                    Object[].class).list();
            for (Object[] row : rows) {
                csv.append(row[0]).append(',').append(value((String) row[1])).append(',').append(row[2]).append('\n');
            }
        }
        return csv.toString();
    }

    private String value(String text) {
        return '"' + (text == null ? "" : text.replace("\"", "\"\"")) + '"';
    }
}
