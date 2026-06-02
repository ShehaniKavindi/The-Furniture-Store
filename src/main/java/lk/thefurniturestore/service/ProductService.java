package lk.thefurniturestore.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lk.thefurniturestore.dto.ProductDTO;
import lk.thefurniturestore.entity.Category;
import lk.thefurniturestore.entity.Product;
import lk.thefurniturestore.entity.ProductImages;
import lk.thefurniturestore.util.AppUtil;
import lk.thefurniturestore.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

public class ProductService {

    public String addProduct(ProductDTO productDTO, List<String> savedImagePaths) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message;

        // Validation
        if (productDTO.getTitle() == null || productDTO.getTitle().isBlank()) {
            message = "Title is required!";
        } else if (productDTO.getDescription() == null || productDTO.getDescription().isBlank()) {
            message = "Description is required!";
        } else if (productDTO.getCategoryId() == 0) {
            message = "Category is required!";
        } else if (productDTO.getPrice() == null || productDTO.getPrice().isBlank()) {
            message = "Price is required!";
        } else {
            double parsedPrice;
            try {
                parsedPrice = Double.parseDouble(productDTO.getPrice());
                if (parsedPrice <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Please enter a valid price!");
                return AppUtil.GSON.toJson(responseObject);
            }

            if (productDTO.getQuantity() < 0) {
                message = "Quantity cannot be negative!";
            } else {
                Session session = HibernateUtil.getSessionFactory().openSession();
                Transaction transaction = null;
                try {
                    transaction = session.beginTransaction();

                    Category category = session.get(Category.class, productDTO.getCategoryId());
                    if (category == null) {
                        message = "Category not found!";
                    } else {
                        // Save product
                        Product product = new Product();
                        product.setTitle(productDTO.getTitle());
                        product.setDescription(productDTO.getDescription());
                        product.setCategory(category);
                        product.setPrice(parsedPrice);
                        product.setQuantity(productDTO.getQuantity());
                        session.persist(product);

                        // Save images
                        if (savedImagePaths != null) {
                            for (String imgPath : savedImagePaths) {
                                ProductImages productImage = new ProductImages();
                                productImage.setImgPath(imgPath);
                                productImage.setProduct(product);
                                session.persist(productImage);
                            }
                        }

                        transaction.commit();
                        status = true;
                        message = "Product added successfully!";
                    }
                } catch (Exception e) {
                    if (transaction != null) transaction.rollback();
                    if (isIncorrectStringValue(e)) {
                        message = "Please remove unsupported characters or emojis from the product details.";
                    } else {
                        message = "Something went wrong. Please try again.";
                    }
                    e.printStackTrace();
                } finally {
                    session.close();
                }
            }
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    private boolean isIncorrectStringValue(Exception e) {
        Throwable throwable = e;
        while (throwable != null) {
            if (throwable.getMessage() != null && throwable.getMessage().contains("Incorrect string value")) {
                return true;
            }
            throwable = throwable.getCause();
        }
        return false;
    }

    public String getAllProducts() {
        JsonObject responseObject = new JsonObject();

        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            List<Product> products = session.createQuery(
                    "FROM Product p LEFT JOIN FETCH p.category", Product.class
            ).list();
            List<ProductDTO> productDTOList = new ArrayList<>();
            for (Product product : products) {
                productDTOList.add(toProductDTO(session, product));
            }
            responseObject.addProperty("status", true);
            responseObject.addProperty("message", "Products fetched successfully!");
            responseObject.add("data", AppUtil.GSON.toJsonTree(productDTOList));
        } catch (Exception e) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Failed to fetch products.");
            e.printStackTrace();
        } finally {
            session.close();
        }

        return AppUtil.GSON.toJson(responseObject);
    }

    public String getProductsByCategory(int categoryId, int limit) {
        JsonObject responseObject = new JsonObject();

        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            Category category = session.get(Category.class, categoryId);
            if (category == null) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Category not found!");
            } else {
                org.hibernate.query.Query<Product> query = session.createQuery(
                        "FROM Product p LEFT JOIN FETCH p.category WHERE p.category.id = :categoryId ORDER BY p.id DESC",
                        Product.class
                );
                query.setParameter("categoryId", categoryId);
                if (limit > 0) {
                    query.setMaxResults(limit);
                }

                List<ProductDTO> productDTOList = new ArrayList<>();
                for (Product product : query.list()) {
                    productDTOList.add(toProductDTO(session, product));
                }

                responseObject.addProperty("status", true);
                responseObject.addProperty("message", "Products fetched successfully!");
                responseObject.addProperty("categoryName", category.getName());
                responseObject.add("data", AppUtil.GSON.toJsonTree(productDTOList));
            }
        } catch (Exception e) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Failed to fetch products.");
            e.printStackTrace();
        } finally {
            session.close();
        }

        return AppUtil.GSON.toJson(responseObject);
    }

    public String getProduct(int id) {
        JsonObject responseObject = new JsonObject();

        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            Product product = session.createQuery(
                            "FROM Product p LEFT JOIN FETCH p.category WHERE p.id = :id", Product.class)
                    .setParameter("id", id)
                    .getSingleResultOrNull();

            if (product == null) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Product not found!");
            } else {
                responseObject.addProperty("status", true);
                responseObject.addProperty("message", "Product fetched successfully!");
                responseObject.add("data", AppUtil.GSON.toJsonTree(toProductDTO(session, product)));
            }
        } catch (Exception e) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Failed to fetch product.");
            e.printStackTrace();
        } finally {
            session.close();
        }

        return AppUtil.GSON.toJson(responseObject);
    }

    private ProductDTO toProductDTO(Session session, Product product) {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(product.getId());
        productDTO.setTitle(product.getTitle());
        productDTO.setDescription(product.getDescription());
        productDTO.setPrice(String.valueOf(product.getPrice()));
        productDTO.setQuantity(product.getQuantity());
        if (product.getCategory() != null) {
            productDTO.setCategoryId(product.getCategory().getId());
            productDTO.setCategoryName(product.getCategory().getName());
        }

        List<String> imagePaths = session.createQuery(
                        "SELECT pi.imgPath FROM ProductImages pi WHERE pi.product.id = :productId", String.class)
                .setParameter("productId", product.getId())
                .list();
        productDTO.setImagePaths(imagePaths);
        return productDTO;
    }

    public String deleteProduct(int id) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message;

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            Product product = session.get(Product.class, id);
            if (product == null) {
                message = "Product not found!";
            } else {
                session.remove(product);
                transaction.commit();
                status = true;
                message = "Product deleted successfully!";
            }
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            message = "Something went wrong. Please try again.";
            e.printStackTrace();
        } finally {
            session.close();
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    public String updateProduct(int id, String jsonData) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message;

        JsonObject requestObject = AppUtil.GSON.fromJson(jsonData, JsonObject.class);
        String title = getString(requestObject, "title");
        String description = getString(requestObject, "description");
        String price = getString(requestObject, "price");

        if (title.isBlank()) {
            message = "Title is required!";
        } else if (description.isBlank()) {
            message = "Description is required!";
        } else if (price.isBlank()) {
            message = "Price is required!";
        } else {
            double parsedPrice;
            try {
                parsedPrice = Double.parseDouble(price);
                if (parsedPrice <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Please enter a valid price!");
                return AppUtil.GSON.toJson(responseObject);
            }

            Session session = HibernateUtil.getSessionFactory().openSession();
            Transaction transaction = null;
            try {
                Product product = session.get(Product.class, id);
                if (product == null) {
                    message = "Product not found!";
                } else {
                    transaction = session.beginTransaction();
                    product.setTitle(title);
                    product.setDescription(description);
                    product.setPrice(parsedPrice);
                    session.merge(product);
                    transaction.commit();
                    status = true;
                    message = "Product details updated successfully!";
                }
            } catch (Exception e) {
                if (transaction != null) transaction.rollback();
                message = "Failed to update product details.";
                e.printStackTrace();
            } finally {
                session.close();
            }
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    public String addProductStock(int id, String jsonData) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message;

        JsonObject requestObject = AppUtil.GSON.fromJson(jsonData, JsonObject.class);
        int quantity = getInt(requestObject, "quantity");

        if (quantity <= 0) {
            message = "Please enter a valid quantity!";
        } else {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Transaction transaction = null;
            try {
                Product product = session.get(Product.class, id);
                if (product == null) {
                    message = "Product not found!";
                } else {
                    transaction = session.beginTransaction();
                    product.setQuantity(product.getQuantity() + quantity);
                    session.merge(product);
                    transaction.commit();
                    status = true;
                    message = "Stock updated successfully!";
                }
            } catch (Exception e) {
                if (transaction != null) transaction.rollback();
                message = "Failed to update stock.";
                e.printStackTrace();
            } finally {
                session.close();
            }
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    public String getAllCategories() {
        JsonObject responseObject = new JsonObject();
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            List<Category> categories = session.createQuery("FROM Category", Category.class).list();
            JsonArray categoryArray = new JsonArray();
            for (Category category : categories) {
                Long productCount = session.createQuery(
                                "SELECT COUNT(p.id) FROM Product p WHERE p.category.id = :categoryId", Long.class)
                        .setParameter("categoryId", category.getId())
                        .getSingleResult();

                JsonObject categoryObject = new JsonObject();
                categoryObject.addProperty("id", category.getId());
                categoryObject.addProperty("name", category.getName());
                categoryObject.addProperty("productCount", productCount);
                categoryArray.add(categoryObject);
            }
            responseObject.addProperty("status", true);
            responseObject.add("data", categoryArray);
        } catch (Exception e) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Failed to fetch categories.");
            e.printStackTrace();
        } finally {
            session.close();
        }
        return AppUtil.GSON.toJson(responseObject);
    }

    public String addCategory(String jsonData) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message;

        JsonObject requestObject = AppUtil.GSON.fromJson(jsonData, JsonObject.class);
        String categoryName = requestObject != null && requestObject.has("name")
                ? requestObject.get("name").getAsString().trim()
                : "";

        if (categoryName.isBlank()) {
            message = "Category name is required!";
        } else if (categoryName.length() > 45) {
            message = "Category name is too long!";
        } else {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Transaction transaction = null;
            try {
                Category existingCategory = session.createQuery(
                                "FROM Category c WHERE lower(c.name) = :name", Category.class)
                        .setParameter("name", categoryName.toLowerCase())
                        .getSingleResultOrNull();

                if (existingCategory != null) {
                    message = "This category already exists!";
                } else {
                    transaction = session.beginTransaction();
                    Category category = new Category();
                    category.setName(categoryName);
                    session.persist(category);
                    transaction.commit();
                    status = true;
                    message = "Category added successfully!";
                }
            } catch (Exception e) {
                if (transaction != null) transaction.rollback();
                message = "Something went wrong. Please try again.";
                e.printStackTrace();
            } finally {
                session.close();
            }
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    private String getString(JsonObject requestObject, String key) {
        if (requestObject == null || !requestObject.has(key) || requestObject.get(key).isJsonNull()) {
            return "";
        }
        return requestObject.get(key).getAsString().trim();
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
