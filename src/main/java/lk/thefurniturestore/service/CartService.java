package lk.thefurniturestore.service;

import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lk.thefurniturestore.dto.CartDTO;
import lk.thefurniturestore.entity.Cart;
import lk.thefurniturestore.entity.Product;
import lk.thefurniturestore.entity.User;
import lk.thefurniturestore.util.AppUtil;
import lk.thefurniturestore.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

public class CartService {

    public String addToCart(CartDTO cartDTO, HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        User sessionUser = getSessionUser(request);

        if (sessionUser == null) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Please login before adding products to your cart.");
            return AppUtil.GSON.toJson(responseObject);
        }

        if (cartDTO == null || cartDTO.getProductId() <= 0) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Product is required.");
        } else if (cartDTO.getQuantity() <= 0) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Quantity must be at least 1.");
        } else {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Transaction transaction = null;
            try {
                Product product = session.get(Product.class, cartDTO.getProductId());
                User user = session.get(User.class, sessionUser.getId());

                if (product == null) {
                    responseObject.addProperty("status", false);
                    responseObject.addProperty("message", "Product not found.");
                } else if (user == null) {
                    responseObject.addProperty("status", false);
                    responseObject.addProperty("message", "Please login again.");
                } else if (cartDTO.getQuantity() > product.getQuantity()) {
                    responseObject.addProperty("status", false);
                    responseObject.addProperty("message", "Only " + product.getQuantity() + " item(s) available.");
                } else {
                    transaction = session.beginTransaction();
                    Cart cart = session.createQuery(
                                    "FROM Cart c WHERE c.user.id = :userId AND c.product.id = :productId",
                                    Cart.class)
                            .setParameter("userId", user.getId())
                            .setParameter("productId", product.getId())
                            .getSingleResultOrNull();

                    if (cart == null) {
                        cart = new Cart();
                        cart.setUser(user);
                        cart.setProduct(product);
                        cart.setQuantity(cartDTO.getQuantity());
                        session.persist(cart);
                    } else {
                        int newQuantity = cart.getQuantity() + cartDTO.getQuantity();
                        if (newQuantity > product.getQuantity()) {
                            responseObject.addProperty("status", false);
                            responseObject.addProperty("message", "Only " + product.getQuantity() + " item(s) available.");
                            transaction.rollback();
                            return AppUtil.GSON.toJson(responseObject);
                        }
                        cart.setQuantity(newQuantity);
                        session.merge(cart);
                    }

                    transaction.commit();
                    responseObject.addProperty("status", true);
                    responseObject.addProperty("message", "Product added to cart.");
                }
            } catch (Exception e) {
                if (transaction != null) transaction.rollback();
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Failed to add product to cart.");
                e.printStackTrace();
            } finally {
                if (session.isOpen()) session.close();
            }
        }

        return AppUtil.GSON.toJson(responseObject);
    }

    public String getCart(HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        User sessionUser = getSessionUser(request);

        if (sessionUser == null) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Please login to view your cart.");
            return AppUtil.GSON.toJson(responseObject);
        }

        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            List<Cart> cartItems = session.createQuery(
                            "FROM Cart c JOIN FETCH c.product p WHERE c.user.id = :userId ORDER BY c.id DESC",
                            Cart.class)
                    .setParameter("userId", sessionUser.getId())
                    .list();

            List<CartDTO> cartDTOList = new ArrayList<>();
            double subtotal = 0;

            for (Cart cart : cartItems) {
                CartDTO cartDTO = toCartDTO(session, cart);
                double lineTotal = cartDTO.getLineTotal();
                subtotal += lineTotal;
                cartDTOList.add(cartDTO);
            }

            responseObject.addProperty("status", true);
            responseObject.addProperty("message", "Cart loaded successfully.");
            responseObject.addProperty("subtotal", subtotal);
            responseObject.add("data", AppUtil.GSON.toJsonTree(cartDTOList));
        } catch (Exception e) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Failed to load cart.");
            e.printStackTrace();
        } finally {
            session.close();
        }

        return AppUtil.GSON.toJson(responseObject);
    }

    public String updateCartItem(CartDTO cartDTO, HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        User sessionUser = getSessionUser(request);

        if (sessionUser == null) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Please login to update your cart.");
            return AppUtil.GSON.toJson(responseObject);
        }

        if (cartDTO == null || cartDTO.getCartId() <= 0) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Cart item is required.");
        } else if (cartDTO.getQuantity() <= 0) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Quantity must be at least 1.");
        } else {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Transaction transaction = null;
            try {
                Cart cart = session.createQuery(
                        "FROM Cart c JOIN FETCH c.product WHERE c.id = :cartId AND c.user.id = :userId",
                        Cart.class)
                        .setParameter("cartId", cartDTO.getCartId())
                        .setParameter("userId", sessionUser.getId())
                        .getSingleResultOrNull();

                if (cart == null) {
                    responseObject.addProperty("status", false);
                    responseObject.addProperty("message", "Cart item not found.");
                } else if (cartDTO.getQuantity() > cart.getProduct().getQuantity()) {
                    responseObject.addProperty("status", false);
                    responseObject.addProperty("message", "Only " + cart.getProduct().getQuantity() + " item(s) available.");
                } else {
                    transaction = session.beginTransaction();
                    cart.setQuantity(cartDTO.getQuantity());
                    session.merge(cart);
                    transaction.commit();
                    responseObject.addProperty("status", true);
                    responseObject.addProperty("message", "Cart updated.");
                }
            } catch (Exception e) {
                if (transaction != null) transaction.rollback();
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Failed to update cart.");
                e.printStackTrace();
            } finally {
                session.close();
            }
        }

        return AppUtil.GSON.toJson(responseObject);
    }

    public String removeCartItem(int cartId, HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        User sessionUser = getSessionUser(request);

        if (sessionUser == null) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Please login to remove cart items.");
            return AppUtil.GSON.toJson(responseObject);
        }

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            Cart cart = session.createQuery(
                            "FROM Cart c WHERE c.id = :cartId AND c.user.id = :userId",
                            Cart.class)
                    .setParameter("cartId", cartId)
                    .setParameter("userId", sessionUser.getId())
                    .getSingleResultOrNull();

            if (cart == null) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Cart item not found.");
            } else {
                transaction = session.beginTransaction();
                session.remove(cart);
                transaction.commit();
                responseObject.addProperty("status", true);
                responseObject.addProperty("message", "Cart item removed.");
            }
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Failed to remove cart item.");
            e.printStackTrace();
        } finally {
            session.close();
        }

        return AppUtil.GSON.toJson(responseObject);
    }

    private User getSessionUser(HttpServletRequest request) {
        HttpSession httpSession = request.getSession(false);
        if (httpSession == null || httpSession.getAttribute("user") == null) {
            return null;
        }
        return (User) httpSession.getAttribute("user");
    }

    private CartDTO toCartDTO(Session session, Cart cart) {
        Product product = cart.getProduct();
        String imagePath = session.createQuery(
                        "SELECT pi.imgPath FROM ProductImages pi WHERE pi.product.id = :productId",
                        String.class)
                .setParameter("productId", product.getId())
                .setMaxResults(1)
                .getSingleResultOrNull();

        CartDTO cartDTO = new CartDTO();
        cartDTO.setCartId(cart.getId());
        cartDTO.setUserId(cart.getUser().getId());
        cartDTO.setProductId(product.getId());
        cartDTO.setTitle(product.getTitle());
        cartDTO.setDescription(product.getDescription());
        cartDTO.setPrice(product.getPrice());
        cartDTO.setQuantity(cart.getQuantity());
        cartDTO.setAvailableQuantity(product.getQuantity());
        cartDTO.setLineTotal(product.getPrice() * cart.getQuantity());
        cartDTO.setImagePath(imagePath != null ? imagePath : "assets/images/product-01.jpg");
        return cartDTO;
    }
}
