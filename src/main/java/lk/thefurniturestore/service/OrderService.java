package lk.thefurniturestore.service;

import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lk.thefurniturestore.dto.CheckoutRequestDTO;
import lk.thefurniturestore.dto.DeliveryTypeDTO;
import lk.thefurniturestore.dto.InvoiceDTO;
import lk.thefurniturestore.dto.InvoiceItemDTO;
import lk.thefurniturestore.dto.OrderDTO;
import lk.thefurniturestore.dto.PayHereDTO;
import lk.thefurniturestore.entity.Address;
import lk.thefurniturestore.entity.Admin;
import lk.thefurniturestore.entity.Cart;
import lk.thefurniturestore.entity.Delivery;
import lk.thefurniturestore.entity.DeliveryType;
import lk.thefurniturestore.entity.Order;
import lk.thefurniturestore.entity.OrderdItems;
import lk.thefurniturestore.entity.Product;
import lk.thefurniturestore.entity.Status;
import lk.thefurniturestore.entity.User;
import lk.thefurniturestore.util.AppUtil;
import lk.thefurniturestore.util.Env;
import lk.thefurniturestore.util.HibernateUtil;
import lk.thefurniturestore.util.PayHereUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class OrderService {
    private static final DateTimeFormatter ORDER_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
    private static final String STATUS_CANCELLED = Status.Type.CANCELLED.name();
    private static final String STATUS_DELIVERED = Status.Type.DELIVERED.name();
    private static final String STATUS_RECEIVED = Status.Type.RECEIVED.name();
    private static final String STATUS_RETURNED = Status.Type.RETURNED.name();
    private static final String STATUS_SHIPPED = Status.Type.SHIPPED.name();

    public String getDeliveryTypes() {
        JsonObject responseObject = new JsonObject();
        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            List<DeliveryType> deliveryTypes = session.createQuery(
                    "FROM DeliveryType dt ORDER BY dt.id", DeliveryType.class).list();

            List<DeliveryTypeDTO> deliveryTypeDTOList = deliveryTypes.stream()
                    .map(type -> new DeliveryTypeDTO(type.getId(), type.getName(), type.getPrice()))
                    .collect(Collectors.toList());

            responseObject.addProperty("status", true);
            responseObject.addProperty("message", "Delivery types loaded successfully.");
            responseObject.add("data", AppUtil.GSON.toJsonTree(deliveryTypeDTOList));
        } catch (Exception e) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Failed to load delivery types.");
            e.printStackTrace();
        } finally {
            session.close();
        }

        return AppUtil.GSON.toJson(responseObject);
    }

    public String placeOrder(CheckoutRequestDTO checkoutRequestDTO, HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        User sessionUser = getSessionUser(request);

        if (sessionUser == null) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Please login before checkout.");
            return AppUtil.GSON.toJson(responseObject);
        }

        if (checkoutRequestDTO == null || checkoutRequestDTO.getDeliveryTypeId() <= 0) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Please select a delivery type.");
            return AppUtil.GSON.toJson(responseObject);
        }

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            List<Cart> cartItems = session.createQuery(
                            "FROM Cart c JOIN FETCH c.product WHERE c.user.id = :userId ORDER BY c.id",
                            Cart.class)
                    .setParameter("userId", sessionUser.getId())
                    .list();

            if (cartItems.isEmpty()) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Your cart is empty.");
                return AppUtil.GSON.toJson(responseObject);
            }

            for (Cart cart : cartItems) {
                Product product = cart.getProduct();
                if (cart.getQuantity() > product.getQuantity()) {
                    responseObject.addProperty("status", false);
                    responseObject.addProperty("message", "Only " + product.getQuantity()
                            + " item(s) available for " + product.getTitle() + ".");
                    return AppUtil.GSON.toJson(responseObject);
                }
            }

            User user = session.get(User.class, sessionUser.getId());
            DeliveryType deliveryType = session.get(DeliveryType.class, checkoutRequestDTO.getDeliveryTypeId());
            Status packingStatus = session.createNamedQuery("Status.findByValue", Status.class)
                    .setParameter("value", String.valueOf(Status.Type.PACKING))
                    .getSingleResultOrNull();

            if (user == null) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Please login again.");
                return AppUtil.GSON.toJson(responseObject);
            } else if (deliveryType == null) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Delivery type not found.");
                return AppUtil.GSON.toJson(responseObject);
            } else if (packingStatus == null) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Delivery status not found.");
                return AppUtil.GSON.toJson(responseObject);
            }

            transaction = session.beginTransaction();

            Order order = new Order();
            order.setUser(user);
            session.persist(order);

            Delivery delivery = new Delivery();
            delivery.setOrder(order);
            delivery.setDeliveryType(deliveryType);
            delivery.setStatus(packingStatus);
            session.persist(delivery);

            InvoiceDTO invoiceDTO = new InvoiceDTO();
            invoiceDTO.setOrderId(order.getId());
            invoiceDTO.setCustomerName((user.getFname() + " " + user.getLname()).trim());
            invoiceDTO.setCustomerEmail(user.getEmail());
            invoiceDTO.setDeliveryType(deliveryType.getName());
            invoiceDTO.setDeliveryFee(deliveryType.getPrice());

            double subtotal = 0;
            int itemCount = 0;

            for (Cart cart : cartItems) {
                Product product = cart.getProduct();
                int quantity = cart.getQuantity();
                double lineTotal = product.getPrice() * quantity;

                OrderdItems orderdItems = new OrderdItems();
                orderdItems.setOrder(order);
                orderdItems.setProduct(product);
                orderdItems.setQty(String.valueOf(quantity));
                session.persist(orderdItems);

                product.setQuantity(product.getQuantity() - quantity);
                session.merge(product);
                session.remove(cart);

                invoiceDTO.getItems().add(new InvoiceItemDTO(
                        product.getId(),
                        product.getTitle(),
                        product.getPrice(),
                        quantity,
                        lineTotal
                ));
                subtotal += lineTotal;
                itemCount += quantity;
            }

            invoiceDTO.setSubtotal(subtotal);
            invoiceDTO.setTotal(subtotal + deliveryType.getPrice());
            invoiceDTO.setItemCount(itemCount);

            transaction.commit();

            responseObject.addProperty("status", true);
            responseObject.addProperty("message", "Order placed successfully.");
            responseObject.add("data", AppUtil.GSON.toJsonTree(invoiceDTO));
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Failed to place order.");
            e.printStackTrace();
        } finally {
            session.close();
        }

        return AppUtil.GSON.toJson(responseObject);
    }

    public String preparePayHerePayment(CheckoutRequestDTO checkoutRequestDTO, HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        User sessionUser = getSessionUser(request);

        if (sessionUser == null) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Please login before checkout.");
            return AppUtil.GSON.toJson(responseObject);
        }

        if (checkoutRequestDTO == null || checkoutRequestDTO.getDeliveryTypeId() <= 0) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Please select a delivery type.");
            return AppUtil.GSON.toJson(responseObject);
        }

        if (PayHereUtil.getMerchantId() == null || PayHereUtil.getMerchantId().isBlank()
                || PayHereUtil.getMerchantSecret() == null || PayHereUtil.getMerchantSecret().isBlank()) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "PayHere merchant details are not configured.");
            return AppUtil.GSON.toJson(responseObject);
        }

        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            User user = session.get(User.class, sessionUser.getId());
            Address address = getUserAddress(session, sessionUser.getId());
            DeliveryType deliveryType = session.get(DeliveryType.class, checkoutRequestDTO.getDeliveryTypeId());
            List<Cart> cartItems = session.createQuery(
                            "FROM Cart c JOIN FETCH c.product WHERE c.user.id = :userId ORDER BY c.id",
                            Cart.class)
                    .setParameter("userId", sessionUser.getId())
                    .list();

            if (user == null) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Please login again.");
            } else if (address == null || address.getCity() == null) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Please complete your shipping details before checkout.");
            } else if (deliveryType == null) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Delivery type not found.");
            } else if (cartItems.isEmpty()) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Your cart is empty.");
            } else {
                double subtotal = 0;
                StringBuilder itemNames = new StringBuilder();
                for (Cart cart : cartItems) {
                    Product product = cart.getProduct();
                    if (cart.getQuantity() > product.getQuantity()) {
                        responseObject.addProperty("status", false);
                        responseObject.addProperty("message", "Only " + product.getQuantity()
                                + " item(s) available for " + product.getTitle() + ".");
                        return AppUtil.GSON.toJson(responseObject);
                    }

                    subtotal += product.getPrice() * cart.getQuantity();
                    if (itemNames.length() > 0) itemNames.append(", ");
                    itemNames.append(product.getTitle());
                }

                double total = subtotal + deliveryType.getPrice();
                String paymentOrderId = "TFS-" + System.currentTimeMillis();
                String currency = AppUtil.MAIN_APP_CURRENCY;
                String amount = String.format(Locale.US, "%.2f", total);

                PayHereDTO payHereDTO = new PayHereDTO();
                payHereDTO.setMerchantId(PayHereUtil.getMerchantId());
                payHereDTO.setReturnUrl(getAppUrl() + "/cart.html");
                payHereDTO.setCancelUrl(getAppUrl() + "/cart.html");
                payHereDTO.setNotifyUrl(getPublicUrl() + "/api/orders/payhere/notify");
                payHereDTO.setOrderId(paymentOrderId);
                payHereDTO.setItems(itemNames.toString());
                payHereDTO.setCurrency(currency);
                payHereDTO.setAmount(amount);
                payHereDTO.setFirstName(user.getFname());
                payHereDTO.setLastName(user.getLname());
                payHereDTO.setEmail(user.getEmail());
                payHereDTO.setPhone(address.getMobile());
                payHereDTO.setAddress(buildAddress(address));
                payHereDTO.setCity(address.getCity().getName());
                payHereDTO.setCountry(AppUtil.APP_COUNTRY);
                payHereDTO.setHash(PayHereUtil.generateHash(paymentOrderId, total, currency));
                payHereDTO.setSandbox(PayHereUtil.isSandbox());

                JsonObject shippingObject = new JsonObject();
                shippingObject.addProperty("name", (user.getFname() + " " + user.getLname()).trim());
                shippingObject.addProperty("email", user.getEmail());
                shippingObject.addProperty("mobile", address.getMobile());
                shippingObject.addProperty("address", buildAddress(address));
                shippingObject.addProperty("city", address.getCity().getName());
                shippingObject.addProperty("deliveryType", deliveryType.getName());
                shippingObject.addProperty("deliveryFee", deliveryType.getPrice());
                shippingObject.addProperty("subtotal", subtotal);
                shippingObject.addProperty("total", total);

                responseObject.addProperty("status", true);
                responseObject.addProperty("message", "PayHere payment prepared.");
                responseObject.add("payment", AppUtil.GSON.toJsonTree(payHereDTO));
                responseObject.add("shipping", shippingObject);
            }
        } catch (Exception e) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Failed to prepare payment.");
            e.printStackTrace();
        } finally {
            session.close();
        }

        return AppUtil.GSON.toJson(responseObject);
    }

    public String getMyOrders(HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        User sessionUser = getSessionUser(request);

        if (sessionUser == null) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Please login to view your orders.");
            return AppUtil.GSON.toJson(responseObject);
        }

        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            List<Order> orders = session.createQuery(
                            "FROM Order o JOIN FETCH o.user WHERE o.user.id = :userId ORDER BY o.id DESC",
                            Order.class)
                    .setParameter("userId", sessionUser.getId())
                    .list();

            List<OrderDTO> orderDTOList = new ArrayList<>();
            for (Order order : orders) {
                orderDTOList.add(toOrderDTO(session, order));
            }

            responseObject.addProperty("status", true);
            responseObject.addProperty("message", "Orders loaded successfully.");
            responseObject.add("data", AppUtil.GSON.toJsonTree(orderDTOList));
        } catch (Exception e) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Failed to load orders.");
            e.printStackTrace();
        } finally {
            session.close();
        }

        return AppUtil.GSON.toJson(responseObject);
    }

    public String getAllOrders(HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        if (getSessionAdmin(request) == null) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Please login as admin first.");
            return AppUtil.GSON.toJson(responseObject);
        }

        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            List<Order> orders = session.createQuery(
                    "FROM Order o JOIN FETCH o.user ORDER BY o.id DESC", Order.class).list();

            List<OrderDTO> orderDTOList = new ArrayList<>();
            for (Order order : orders) {
                orderDTOList.add(toOrderDTO(session, order));
            }

            responseObject.addProperty("status", true);
            responseObject.addProperty("message", "Orders loaded successfully.");
            responseObject.add("data", AppUtil.GSON.toJsonTree(orderDTOList));
        } catch (Exception e) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Failed to load orders.");
            e.printStackTrace();
        } finally {
            session.close();
        }

        return AppUtil.GSON.toJson(responseObject);
    }

    public String updateMyOrderStatus(int orderId, String jsonData, HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        User sessionUser = getSessionUser(request);

        if (sessionUser == null) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Please login to update your order.");
            return AppUtil.GSON.toJson(responseObject);
        }

        String targetStatusValue = getRequestedStatus(jsonData);
        if (targetStatusValue == null) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Order status is required.");
            return AppUtil.GSON.toJson(responseObject);
        }

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            Order order = session.createQuery(
                            "FROM Order o JOIN FETCH o.user WHERE o.id = :orderId AND o.user.id = :userId",
                            Order.class)
                    .setParameter("orderId", orderId)
                    .setParameter("userId", sessionUser.getId())
                    .getSingleResultOrNull();

            if (order == null) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Order not found.");
                return AppUtil.GSON.toJson(responseObject);
            }

            Delivery delivery = getOrderDelivery(session, orderId);
            if (delivery == null) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Order delivery details not found.");
                return AppUtil.GSON.toJson(responseObject);
            }

            String currentStatusValue = delivery.getStatus().getValue();
            if (!isAllowedUserStatusChange(currentStatusValue, targetStatusValue)) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "This order status cannot be changed by the customer.");
                return AppUtil.GSON.toJson(responseObject);
            }

            transaction = session.beginTransaction();
            Status targetStatus = getOrCreateStatus(session, targetStatusValue);
            applyStockAdjustment(session, orderId, currentStatusValue, targetStatusValue);
            delivery.setStatus(targetStatus);
            session.merge(delivery);
            transaction.commit();

            responseObject.addProperty("status", true);
            responseObject.addProperty("message", "Order status updated successfully.");
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Failed to update order status.");
            e.printStackTrace();
        } finally {
            session.close();
        }

        return AppUtil.GSON.toJson(responseObject);
    }

    public String updateOrderStatus(int orderId, String jsonData, HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        if (getSessionAdmin(request) == null) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Please login as admin first.");
            return AppUtil.GSON.toJson(responseObject);
        }

        String targetStatusValue = getRequestedStatus(jsonData);
        if (targetStatusValue == null) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Order status is required.");
            return AppUtil.GSON.toJson(responseObject);
        }

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            Delivery delivery = getOrderDelivery(session, orderId);
            if (delivery == null) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Order not found.");
                return AppUtil.GSON.toJson(responseObject);
            }

            transaction = session.beginTransaction();
            String currentStatusValue = delivery.getStatus().getValue();
            Status targetStatus = getOrCreateStatus(session, targetStatusValue);
            applyStockAdjustment(session, orderId, currentStatusValue, targetStatusValue);
            delivery.setStatus(targetStatus);
            session.merge(delivery);
            transaction.commit();

            responseObject.addProperty("status", true);
            responseObject.addProperty("message", "Order status updated successfully.");
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", e.getMessage() != null ? e.getMessage() : "Failed to update order status.");
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

    private Address getUserAddress(Session session, int userId) {
        return session.createQuery(
                        "FROM Address a JOIN FETCH a.city WHERE a.user.id = :userId",
                        Address.class)
                .setParameter("userId", userId)
                .setMaxResults(1)
                .getSingleResultOrNull();
    }

    private String buildAddress(Address address) {
        String line2 = address.getLine2() == null || address.getLine2().isBlank()
                ? ""
                : ", " + address.getLine2();
        String postalCode = address.getPostalCode() == null || address.getPostalCode().isBlank()
                ? ""
                : ", " + address.getPostalCode();
        return address.getLine1() + line2 + postalCode;
    }

    private String getAppUrl() {
        String appUrl = Env.get("app.url");
        return appUrl != null && !appUrl.isBlank() ? appUrl : "http://localhost:8080/thefurniturestore";
    }

    private String getPublicUrl() {
        String publicUrl = Env.get("app.public.url");
        return publicUrl != null && !publicUrl.isBlank() ? publicUrl : getAppUrl();
    }

    private Admin getSessionAdmin(HttpServletRequest request) {
        HttpSession httpSession = request.getSession(false);
        if (httpSession == null || httpSession.getAttribute("admin") == null) {
            return null;
        }
        return (Admin) httpSession.getAttribute("admin");
    }

    private String getRequestedStatus(String jsonData) {
        try {
            JsonObject requestObject = AppUtil.GSON.fromJson(jsonData, JsonObject.class);
            if (requestObject == null || !requestObject.has("status")) {
                return null;
            }
            String statusValue = requestObject.get("status").getAsString();
            return normalizeOrderStatus(statusValue);
        } catch (Exception e) {
            return null;
        }
    }

    private String normalizeOrderStatus(String statusValue) {
        if (statusValue == null || statusValue.isBlank()) {
            return null;
        }

        String normalizedValue = statusValue.trim().toUpperCase().replace(' ', '_');
        if (Status.Type.PENDING.name().equals(normalizedValue)
                || Status.Type.PACKING.name().equals(normalizedValue)
                || STATUS_SHIPPED.equals(normalizedValue)
                || STATUS_DELIVERED.equals(normalizedValue)
                || STATUS_CANCELLED.equals(normalizedValue)
                || STATUS_RECEIVED.equals(normalizedValue)
                || STATUS_RETURNED.equals(normalizedValue)) {
            return normalizedValue;
        }
        return null;
    }

    private boolean isAllowedUserStatusChange(String currentStatusValue, String targetStatusValue) {
        if (targetStatusValue == null || currentStatusValue == null) {
            return false;
        }

        if (STATUS_DELIVERED.equals(currentStatusValue)) {
            return STATUS_RECEIVED.equals(targetStatusValue) || STATUS_RETURNED.equals(targetStatusValue);
        }

        return !STATUS_RECEIVED.equals(currentStatusValue)
                && !STATUS_RETURNED.equals(currentStatusValue)
                && !STATUS_CANCELLED.equals(currentStatusValue)
                && !STATUS_DELIVERED.equals(currentStatusValue)
                && STATUS_CANCELLED.equals(targetStatusValue);
    }

    private Delivery getOrderDelivery(Session session, int orderId) {
        return session.createQuery(
                        "FROM Delivery d JOIN FETCH d.deliveryType JOIN FETCH d.status WHERE d.order.id = :orderId",
                        Delivery.class)
                .setParameter("orderId", orderId)
                .setMaxResults(1)
                .getSingleResultOrNull();
    }

    private Status getOrCreateStatus(Session session, String statusValue) {
        Status status = session.createNamedQuery("Status.findByValue", Status.class)
                .setParameter("value", statusValue)
                .getSingleResultOrNull();

        if (status == null) {
            status = new Status();
            status.setValue(statusValue);
            session.persist(status);
        }

        return status;
    }

    private void applyStockAdjustment(Session session, int orderId, String currentStatusValue, String targetStatusValue) {
        boolean currentRestoresStock = isStockRestoringStatus(currentStatusValue);
        boolean targetRestoresStock = isStockRestoringStatus(targetStatusValue);

        if (currentRestoresStock == targetRestoresStock) {
            return;
        }

        List<OrderdItems> orderItems = session.createQuery(
                        "FROM OrderdItems oi JOIN FETCH oi.product WHERE oi.order.id = :orderId",
                        OrderdItems.class)
                .setParameter("orderId", orderId)
                .list();

        for (OrderdItems orderItem : orderItems) {
            Product product = orderItem.getProduct();
            int quantity = parseQuantity(orderItem.getQty());

            if (targetRestoresStock) {
                product.setQuantity(product.getQuantity() + quantity);
            } else {
                if (product.getQuantity() < quantity) {
                    throw new RuntimeException("Not enough stock to move this order back to an active status.");
                }
                product.setQuantity(product.getQuantity() - quantity);
            }

            session.merge(product);
        }
    }

    private boolean isStockRestoringStatus(String statusValue) {
        return STATUS_CANCELLED.equals(statusValue) || STATUS_RETURNED.equals(statusValue);
    }

    private OrderDTO toOrderDTO(Session session, Order order) {
        List<OrderdItems> orderItems = session.createQuery(
                        "FROM OrderdItems oi JOIN FETCH oi.product WHERE oi.order.id = :orderId",
                        OrderdItems.class)
                .setParameter("orderId", order.getId())
                .list();

        Delivery delivery = session.createQuery(
                        "FROM Delivery d JOIN FETCH d.deliveryType JOIN FETCH d.status WHERE d.order.id = :orderId",
                        Delivery.class)
                .setParameter("orderId", order.getId())
                .setMaxResults(1)
                .getSingleResultOrNull();

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderId(order.getId());
        orderDTO.setCustomerName((order.getUser().getFname() + " " + order.getUser().getLname()).trim());
        orderDTO.setCustomerEmail(order.getUser().getEmail());
        orderDTO.setOrderDate(order.getCreatedAt() != null ? order.getCreatedAt().format(ORDER_DATE_FORMATTER) : "");

        if (delivery != null) {
            orderDTO.setDeliveryType(delivery.getDeliveryType().getName());
            orderDTO.setDeliveryFee(delivery.getDeliveryType().getPrice());
            orderDTO.setStatus(delivery.getStatus().getValue());
        } else {
            orderDTO.setDeliveryType("-");
            orderDTO.setStatus("-");
        }

        double subtotal = 0;
        int itemCount = 0;

        for (OrderdItems orderItem : orderItems) {
            int quantity = parseQuantity(orderItem.getQty());
            double price = orderItem.getProduct().getPrice();
            double lineTotal = price * quantity;

            orderDTO.getItems().add(new InvoiceItemDTO(
                    orderItem.getProduct().getId(),
                    orderItem.getProduct().getTitle(),
                    price,
                    quantity,
                    lineTotal
            ));

            subtotal += lineTotal;
            itemCount += quantity;
        }

        orderDTO.setSubtotal(subtotal);
        orderDTO.setItemCount(itemCount);
        orderDTO.setTotal(subtotal + orderDTO.getDeliveryFee());
        return orderDTO;
    }

    private int parseQuantity(String quantity) {
        try {
            return Integer.parseInt(quantity);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
