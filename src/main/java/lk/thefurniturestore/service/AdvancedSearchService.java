package lk.thefurniturestore.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import lk.thefurniturestore.dto.SearchResponseDTO;
import lk.thefurniturestore.entity.Product;
import lk.thefurniturestore.util.AppUtil;
import lk.thefurniturestore.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;

public class AdvancedSearchService {

    public String getAllProductData() {
        JsonObject requestObject = new JsonObject();
        requestObject.addProperty("sort", "recent");
        return getAdvancedSearchData(requestObject);
    }

    public String getAdvancedSearchData(JsonObject requestObject) {
        JsonObject responseObject = new JsonObject();

        String keyword = getString(requestObject, "keyword").trim().toLowerCase();
        int categoryId = getInt(requestObject, "categoryId");
        List<Integer> categoryIds = getIntList(requestObject, "categoryIds");
        if (categoryIds.isEmpty() && categoryId > 0) categoryIds.add(categoryId);
        String sort = getString(requestObject, "sort");
        double minPrice = getDouble(requestObject, "minPrice");
        double maxPrice = getDouble(requestObject, "maxPrice");
        boolean inStockOnly = getBoolean(requestObject, "inStockOnly");
        int page = Math.max(1, getInt(requestObject, "page"));
        int pageSize = getInt(requestObject, "pageSize");
        pageSize = pageSize <= 0 ? 12 : Math.min(pageSize, 48);

        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            StringBuilder conditions = new StringBuilder(" WHERE 1 = 1");

            if (!keyword.isBlank()) {
                conditions.append(" AND (LOWER(p.title) LIKE :keyword OR LOWER(p.description) LIKE :keyword)");
            }

            if (!categoryIds.isEmpty()) {
                conditions.append(" AND p.category.id IN :categoryIds");
            }
            if (minPrice > 0) conditions.append(" AND p.price >= :minPrice");
            if (maxPrice > 0) conditions.append(" AND p.price <= :maxPrice");
            if (inStockOnly) conditions.append(" AND p.quantity > 0");

            long totalItems = session.createQuery("SELECT COUNT(p.id) FROM Product p" + conditions, Long.class)
                    .setProperties(parameters(keyword, categoryIds, minPrice, maxPrice)).getSingleResult();

            StringBuilder hql = new StringBuilder("FROM Product p LEFT JOIN FETCH p.category").append(conditions);

            if ("price-asc".equals(sort)) {
                hql.append(" ORDER BY p.price ASC");
            } else if ("price-desc".equals(sort)) {
                hql.append(" ORDER BY p.price DESC");
            } else {
                hql.append(" ORDER BY p.createdAt DESC, p.id DESC");
            }

            Query<Product> query = session.createQuery(hql.toString(), Product.class);
            query.setProperties(parameters(keyword, categoryIds, minPrice, maxPrice));
            query.setFirstResult((page - 1) * pageSize);
            query.setMaxResults(pageSize);

            List<SearchResponseDTO> products = new ArrayList<>();
            for (Product product : query.list()) {
                products.add(toSearchResponseDTO(session, product));
            }

            responseObject.addProperty("status", true);
            responseObject.addProperty("message", "Products fetched successfully!");
            responseObject.addProperty("count", totalItems);
            responseObject.addProperty("currentPage", page);
            responseObject.addProperty("pageSize", pageSize);
            responseObject.addProperty("totalPages", (int) Math.ceil((double) totalItems / pageSize));
            responseObject.add("data", AppUtil.GSON.toJsonTree(products));
        } catch (Exception e) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Failed to search products.");
            e.printStackTrace();
        } finally {
            session.close();
        }

        return AppUtil.GSON.toJson(responseObject);
    }

    private SearchResponseDTO toSearchResponseDTO(Session session, Product product) {
        SearchResponseDTO searchResponseDTO = new SearchResponseDTO();
        searchResponseDTO.setProductId(product.getId());
        searchResponseDTO.setTitle(product.getTitle());
        searchResponseDTO.setPrice(product.getPrice());

        if (product.getCategory() != null) {
            searchResponseDTO.setCategoryName(product.getCategory().getName());
        }

        String image = session.createQuery(
                        "SELECT pi.imgPath FROM ProductImages pi WHERE pi.product.id = :productId ORDER BY pi.id ASC",
                        String.class)
                .setParameter("productId", product.getId())
                .setMaxResults(1)
                .getSingleResultOrNull();

        searchResponseDTO.setImage(image != null ? image : "assets/images/product-01.jpg");
        return searchResponseDTO;
    }

    private String getString(JsonObject requestObject, String key) {
        if (requestObject == null || !requestObject.has(key) || requestObject.get(key).isJsonNull()) {
            return "";
        }
        return requestObject.get(key).getAsString();
    }

    private int getInt(JsonObject requestObject, String key) {
        if (requestObject == null || !requestObject.has(key) || requestObject.get(key).isJsonNull()) {
            return 0;
        }
        try {
            return requestObject.get(key).getAsInt();
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double getDouble(JsonObject requestObject, String key) {
        try { return requestObject != null && requestObject.has(key) ? Math.max(0, requestObject.get(key).getAsDouble()) : 0; }
        catch (Exception ignored) { return 0; }
    }

    private boolean getBoolean(JsonObject requestObject, String key) {
        return requestObject != null && requestObject.has(key) && requestObject.get(key).getAsBoolean();
    }

    private List<Integer> getIntList(JsonObject requestObject, String key) {
        List<Integer> values = new ArrayList<>();
        if (requestObject == null || !requestObject.has(key) || !requestObject.get(key).isJsonArray()) return values;
        JsonArray array = requestObject.getAsJsonArray(key);
        for (int i = 0; i < array.size(); i++) {
            try { int value = array.get(i).getAsInt(); if (value > 0) values.add(value); }
            catch (Exception ignored) { }
        }
        return values;
    }

    private java.util.Map<String, Object> parameters(String keyword, List<Integer> categoryIds, double minPrice, double maxPrice) {
        java.util.Map<String, Object> values = new java.util.HashMap<>();
        if (!keyword.isBlank()) values.put("keyword", "%" + keyword + "%");
        if (!categoryIds.isEmpty()) values.put("categoryIds", categoryIds);
        if (minPrice > 0) values.put("minPrice", minPrice);
        if (maxPrice > 0) values.put("maxPrice", maxPrice);
        return values;
    }
}
