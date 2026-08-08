package lk.thefurniturestore.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lk.thefurniturestore.entity.Product;
import lk.thefurniturestore.entity.ProductImages;
import lk.thefurniturestore.entity.User;
import lk.thefurniturestore.entity.Wishlist;
import lk.thefurniturestore.util.AppUtil;
import lk.thefurniturestore.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.List;

public class WishlistService {
    public String getWishlist(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        User user = sessionUser(request);
        if (user == null) return failure(response, "Please login to view your wishlist.");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Wishlist> items = session.createQuery(
                            "FROM Wishlist w JOIN FETCH w.product p LEFT JOIN FETCH p.category WHERE w.user.id = :userId ORDER BY w.createdAt DESC",
                            Wishlist.class)
                    .setParameter("userId", user.getId()).list();
            JsonArray data = new JsonArray();
            for (Wishlist item : items) data.add(toJson(session, item));
            response.addProperty("status", true);
            response.add("data", data);
        } catch (Exception e) {
            return failure(response, "Unable to load wishlist.");
        }
        return AppUtil.GSON.toJson(response);
    }

    public String add(int productId, HttpServletRequest request) {
        JsonObject response = new JsonObject();
        User user = sessionUser(request);
        if (user == null) return failure(response, "Please login to save products.");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Product product = session.get(Product.class, productId);
            Wishlist existing = session.createQuery("FROM Wishlist w WHERE w.user.id = :userId AND w.product.id = :productId", Wishlist.class)
                    .setParameter("userId", user.getId()).setParameter("productId", productId).getSingleResultOrNull();
            if (product == null) return failure(response, "Product not found.");
            if (existing != null) return failure(response, "This product is already in your wishlist.");
            Transaction transaction = session.beginTransaction();
            Wishlist item = new Wishlist();
            item.setUser(session.get(User.class, user.getId()));
            item.setProduct(product);
            item.setCreatedAt(LocalDateTime.now());
            session.persist(item);
            transaction.commit();
            response.addProperty("status", true);
            response.addProperty("message", "Product saved to your wishlist.");
        } catch (Exception e) { return failure(response, "Unable to save product."); }
        return AppUtil.GSON.toJson(response);
    }

    public String remove(int productId, HttpServletRequest request) {
        JsonObject response = new JsonObject();
        User user = sessionUser(request);
        if (user == null) return failure(response, "Please login to manage your wishlist.");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Wishlist item = session.createQuery("FROM Wishlist w WHERE w.user.id = :userId AND w.product.id = :productId", Wishlist.class)
                    .setParameter("userId", user.getId()).setParameter("productId", productId).getSingleResultOrNull();
            if (item == null) return failure(response, "Wishlist item not found.");
            Transaction transaction = session.beginTransaction();
            session.remove(item);
            transaction.commit();
            response.addProperty("status", true);
            response.addProperty("message", "Product removed from your wishlist.");
        } catch (Exception e) { return failure(response, "Unable to remove product."); }
        return AppUtil.GSON.toJson(response);
    }

    private JsonObject toJson(Session session, Wishlist item) {
        Product product = item.getProduct();
        JsonObject value = new JsonObject();
        value.addProperty("productId", product.getId());
        value.addProperty("title", product.getTitle());
        value.addProperty("price", product.getPrice());
        value.addProperty("categoryName", product.getCategory() == null ? "Furniture" : product.getCategory().getName());
        String image = session.createQuery("SELECT pi.imgPath FROM ProductImages pi WHERE pi.product.id = :id ORDER BY pi.id", String.class)
                .setParameter("id", product.getId()).setMaxResults(1).getSingleResultOrNull();
        value.addProperty("image", image == null ? "assets/images/product-01.jpg" : image);
        return value;
    }

    private User sessionUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (User) session.getAttribute("user");
    }

    private String failure(JsonObject response, String message) {
        response.addProperty("status", false);
        response.addProperty("message", message);
        return AppUtil.GSON.toJson(response);
    }
}
