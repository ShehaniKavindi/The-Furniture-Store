package lk.thefurniturestore.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import lk.thefurniturestore.entity.Category;
import lk.thefurniturestore.entity.OrderdItems;
import lk.thefurniturestore.entity.Product;
import lk.thefurniturestore.util.AppUtil;
import lk.thefurniturestore.util.HibernateUtil;
import org.hibernate.Session;

import java.util.List;

/** Query examples used by the reporting dashboard and as evidence of Hibernate techniques. */
public class ProductInsightsService {
    public String getLowStockProjection(int threshold) {
        JsonObject response = new JsonObject();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Tuple> criteria = builder.createTupleQuery();
            Root<Product> product = criteria.from(Product.class);
            Join<Product, Category> category = product.join("category", jakarta.persistence.criteria.JoinType.LEFT);
            criteria.multiselect(
                    product.get("id").alias("productId"),
                    product.get("title").alias("title"),
                    product.get("quantity").alias("quantity"),
                    product.get("price").alias("price"),
                    category.get("name").alias("categoryName"))
                    .where(builder.lessThanOrEqualTo(product.get("quantity"), Math.max(0, threshold)))
                    .orderBy(builder.asc(product.get("quantity")), builder.asc(product.get("title")));

            JsonArray data = new JsonArray();
            for (Tuple row : session.createQuery(criteria).getResultList()) {
                JsonObject item = new JsonObject();
                item.addProperty("productId", row.get("productId", Integer.class));
                item.addProperty("title", row.get("title", String.class));
                item.addProperty("quantity", row.get("quantity", Integer.class));
                item.addProperty("price", row.get("price", Double.class));
                item.addProperty("categoryName", row.get("categoryName", String.class));
                data.add(item);
            }
            response.addProperty("status", true);
            response.add("data", data);
        } catch (Exception e) {
            response.addProperty("status", false);
            response.addProperty("message", "Unable to retrieve low-stock data.");
        }
        return AppUtil.GSON.toJson(response);
    }

    public String getCategorySalesSummary() {
        JsonObject response = new JsonObject();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // HQL aggregate and join query.
            List<Object[]> rows = session.createQuery(
                    "SELECT c.name, COUNT(oi.id), COALESCE(SUM(p.price), 0) "
                            + "FROM OrderdItems oi JOIN oi.product p JOIN p.category c "
                            + "GROUP BY c.id, c.name ORDER BY SUM(p.price) DESC", Object[].class).list();
            JsonArray data = new JsonArray();
            for (Object[] row : rows) {
                JsonObject item = new JsonObject();
                item.addProperty("categoryName", (String) row[0]);
                item.addProperty("orderedItemCount", ((Number) row[1]).longValue());
                item.addProperty("salesValue", ((Number) row[2]).doubleValue());
                data.add(item);
            }
            response.addProperty("status", true);
            response.add("data", data);
        } catch (Exception e) {
            response.addProperty("status", false);
            response.addProperty("message", "Unable to retrieve category sales.");
        }
        return AppUtil.GSON.toJson(response);
    }

    public String getAboveCategoryAverageProducts() {
        JsonObject response = new JsonObject();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // HQL subquery: compares each product to the average price in its category.
            List<Product> products = session.createQuery(
                    "FROM Product p JOIN FETCH p.category c WHERE p.price > "
                            + "(SELECT AVG(p2.price) FROM Product p2 WHERE p2.category.id = c.id) "
                            + "ORDER BY c.name, p.price DESC", Product.class).list();
            JsonArray data = new JsonArray();
            for (Product product : products) {
                JsonObject item = new JsonObject();
                item.addProperty("productId", product.getId());
                item.addProperty("title", product.getTitle());
                item.addProperty("price", product.getPrice());
                item.addProperty("categoryName", product.getCategory().getName());
                data.add(item);
            }
            response.addProperty("status", true);
            response.add("data", data);
        } catch (Exception e) {
            response.addProperty("status", false);
            response.addProperty("message", "Unable to retrieve product pricing insights.");
        }
        return AppUtil.GSON.toJson(response);
    }
}
