package lk.thefurniturestore.service;

import com.google.gson.JsonObject;
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
        String sort = getString(requestObject, "sort");

        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            StringBuilder hql = new StringBuilder("FROM Product p LEFT JOIN FETCH p.category WHERE 1 = 1");

            if (!keyword.isBlank()) {
                hql.append(" AND (LOWER(p.title) LIKE :keyword OR LOWER(p.description) LIKE :keyword)");
            }

            if (categoryId > 0) {
                hql.append(" AND p.category.id = :categoryId");
            }

            if ("price-asc".equals(sort)) {
                hql.append(" ORDER BY p.price ASC");
            } else if ("price-desc".equals(sort)) {
                hql.append(" ORDER BY p.price DESC");
            } else {
                hql.append(" ORDER BY p.createdAt DESC, p.id DESC");
            }

            Query<Product> query = session.createQuery(hql.toString(), Product.class);

            if (!keyword.isBlank()) {
                query.setParameter("keyword", "%" + keyword + "%");
            }

            if (categoryId > 0) {
                query.setParameter("categoryId", categoryId);
            }

            List<SearchResponseDTO> products = new ArrayList<>();
            for (Product product : query.list()) {
                products.add(toSearchResponseDTO(session, product));
            }

            responseObject.addProperty("status", true);
            responseObject.addProperty("message", "Products fetched successfully!");
            responseObject.addProperty("count", products.size());
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
}
