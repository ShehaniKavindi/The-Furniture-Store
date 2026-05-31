package lk.thefurniturestore.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lk.thefurniturestore.dto.AdminDTO;
import lk.thefurniturestore.entity.Admin;
import lk.thefurniturestore.entity.Delivery;
import lk.thefurniturestore.entity.Order;
import lk.thefurniturestore.entity.OrderdItems;
import lk.thefurniturestore.entity.Product;
import lk.thefurniturestore.entity.Role;
import lk.thefurniturestore.entity.Status;
import lk.thefurniturestore.entity.User;
import lk.thefurniturestore.util.AppUtil;
import lk.thefurniturestore.util.HibernateUtil;
import lk.thefurniturestore.validation.Validator;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminService {
    private static final DateTimeFormatter CUSTOMER_JOINED_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy");

    public String adminLogin(AdminDTO adminDTO, HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message;

        if (adminDTO.getEmail() == null) {
            message = "Email is required!";
        } else if (adminDTO.getEmail().isBlank()) {
            message = "Email cannot be empty!";
        } else if (!adminDTO.getEmail().matches(Validator.EMAIL_VALIDATION)) {
            message = "Please enter a valid email!";
        } else if (adminDTO.getPassword() == null) {
            message = "Password is required!";
        } else if (adminDTO.getPassword().isBlank()) {
            message = "Password cannot be empty!";
        } else {
            Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
            try {
                Admin admin = hibernateSession.createNamedQuery("Admin.getByEmail", Admin.class)
                        .setParameter("email", adminDTO.getEmail())
                        .getSingleResultOrNull();

                if (admin == null) {
                    message = "Admin account not found!";
                } else if (!admin.getPassword().equals(adminDTO.getPassword())) {
                    message = "Something went wrong. Please check your login credentials!";
                } else if (!canAdminLogin(admin)) {
                    message = "Your admin account is not approved. Please contact the super admin!";
                } else {
                    HttpSession httpSession = request.getSession();
                    httpSession.setAttribute("admin", admin);
                    status = true;
                    message = "Admin login successful!";
                    responseObject.add("data", AppUtil.GSON.toJsonTree(toAdminDTO(admin)));
                }
            } finally {
                hibernateSession.close();
            }
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    public String getAdminProfile(HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        Admin sessionAdmin = getSessionAdmin(request);

        if (sessionAdmin == null) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Please login as admin first.");
            return AppUtil.GSON.toJson(responseObject);
        }

        Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
        try {
            Admin admin = hibernateSession.createQuery(
                            "FROM Admin a JOIN FETCH a.role JOIN FETCH a.status WHERE a.id = :adminId",
                            Admin.class)
                    .setParameter("adminId", sessionAdmin.getId())
                    .getSingleResultOrNull();

            if (admin == null) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Admin account not found. Please login again.");
            } else {
                responseObject.addProperty("status", true);
                responseObject.addProperty("message", "Admin profile loaded successfully.");
                responseObject.add("data", AppUtil.GSON.toJsonTree(toAdminDTO(admin)));
            }
        } finally {
            hibernateSession.close();
        }

        return AppUtil.GSON.toJson(responseObject);
    }

    public String getDashboard(HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        if (getSessionAdmin(request) == null) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Please login as admin first.");
            return AppUtil.GSON.toJson(responseObject);
        }

        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            long orderCount = session.createQuery("SELECT COUNT(o.id) FROM Order o", Long.class).getSingleResult();
            long customerCount = session.createQuery("SELECT COUNT(u.id) FROM User u", Long.class).getSingleResult();
            long pendingCount = session.createQuery(
                            "SELECT COUNT(d.id) FROM Delivery d WHERE d.status.value = :pending OR d.status.value = :packing",
                            Long.class)
                    .setParameter("pending", Status.Type.PENDING.name())
                    .setParameter("packing", Status.Type.PACKING.name())
                    .getSingleResult();

            JsonObject dataObject = new JsonObject();
            dataObject.addProperty("totalRevenue", calculateTotalRevenue(session));
            dataObject.addProperty("orderCount", orderCount);
            dataObject.addProperty("customerCount", customerCount);
            dataObject.addProperty("pendingOrderCount", pendingCount);
            dataObject.add("recentOrders", getRecentOrders(session));
            dataObject.add("activity", getDashboardActivity(session));
            dataObject.add("topProducts", getTopProducts(session));

            responseObject.addProperty("status", true);
            responseObject.addProperty("message", "Dashboard loaded successfully.");
            responseObject.add("data", dataObject);
        } catch (Exception e) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Failed to load dashboard.");
            e.printStackTrace();
        } finally {
            session.close();
        }

        return AppUtil.GSON.toJson(responseObject);
    }

    public String getAllAdmins(HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        Admin sessionAdmin = getSessionAdmin(request);

        if (sessionAdmin == null) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Please login as admin first.");
            return AppUtil.GSON.toJson(responseObject);
        } else if (!isSuperAdmin(sessionAdmin)) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Only super admins can manage admins.");
            return AppUtil.GSON.toJson(responseObject);
        }

        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            List<Admin> admins = session.createQuery(
                    "FROM Admin a JOIN FETCH a.role JOIN FETCH a.status ORDER BY a.id DESC", Admin.class).list();

            JsonArray adminArray = new JsonArray();
            for (Admin admin : admins) {
                adminArray.add(AppUtil.GSON.toJsonTree(toAdminDTO(admin)));
            }

            responseObject.addProperty("status", true);
            responseObject.addProperty("message", "Admins loaded successfully.");
            responseObject.add("data", adminArray);
        } catch (Exception e) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Failed to load admins.");
            e.printStackTrace();
        } finally {
            session.close();
        }

        return AppUtil.GSON.toJson(responseObject);
    }

    public String getAllCustomers(HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();

        if (getSessionAdmin(request) == null) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Please login as admin first.");
            return AppUtil.GSON.toJson(responseObject);
        }

        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            List<User> users = session.createQuery(
                    "FROM User u JOIN FETCH u.status ORDER BY u.id DESC", User.class).list();

            JsonArray userArray = new JsonArray();
            for (User user : users) {
                Long orderCount = session.createQuery(
                                "SELECT COUNT(o.id) FROM Order o WHERE o.user.id = :userId", Long.class)
                        .setParameter("userId", user.getId())
                        .getSingleResult();

                JsonObject userObject = new JsonObject();
                userObject.addProperty("id", user.getId());
                userObject.addProperty("name", (user.getFname() + " " + user.getLname()).trim());
                userObject.addProperty("email", user.getEmail());
                userObject.addProperty("joined", user.getCreatedAt() != null
                        ? user.getCreatedAt().format(CUSTOMER_JOINED_FORMATTER)
                        : "-");
                userObject.addProperty("orderCount", orderCount);
                userObject.addProperty("status", user.getStatus() != null ? user.getStatus().getValue() : "-");
                userArray.add(userObject);
            }

            responseObject.addProperty("status", true);
            responseObject.addProperty("message", "Customers loaded successfully.");
            responseObject.add("data", userArray);
        } catch (Exception e) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Failed to load customers.");
            e.printStackTrace();
        } finally {
            session.close();
        }

        return AppUtil.GSON.toJson(responseObject);
    }

    public String updateCustomerStatus(int customerId, String jsonData, HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message;

        if (getSessionAdmin(request) == null) {
            message = "Please login as admin first.";
        } else {
            String targetStatusValue = getRequestedCustomerStatus(jsonData);
            if (targetStatusValue == null) {
                message = "Please select a valid customer status.";
            } else {
                Session session = HibernateUtil.getSessionFactory().openSession();
                Transaction transaction = null;
                try {
                    User user = session.get(User.class, customerId);
                    if (user == null) {
                        message = "Customer account not found.";
                    } else {
                        transaction = session.beginTransaction();
                        Status targetStatus = getOrCreateStatus(session, targetStatusValue);
                        user.setStatus(targetStatus);
                        if (Status.Type.VERIFIED.name().equals(targetStatusValue)) {
                            user.setVerificationCode("");
                        }
                        session.merge(user);
                        transaction.commit();

                        status = true;
                        message = "Customer status updated successfully.";
                    }
                } catch (Exception e) {
                    if (transaction != null) transaction.rollback();
                    message = "Failed to update customer status.";
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

    public String addAdmin(AdminDTO adminDTO, HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        Admin sessionAdmin = getSessionAdmin(request);
        boolean status = false;
        String message;

        if (sessionAdmin == null) {
            message = "Please login as admin first.";
        } else if (!isSuperAdmin(sessionAdmin)) {
            message = "Only super admins can add admins.";
        } else if (adminDTO.getUsername() == null || adminDTO.getUsername().isBlank()) {
            message = "Username is required!";
        } else if (adminDTO.getEmail() == null || adminDTO.getEmail().isBlank()) {
            message = "Email is required!";
        } else if (!adminDTO.getEmail().matches(Validator.EMAIL_VALIDATION)) {
            message = "Please enter a valid email!";
        } else if (adminDTO.getPassword() == null || adminDTO.getPassword().isBlank()) {
            message = "Password is required!";
        } else if (adminDTO.getPassword().length() > 10) {
            message = "Password cannot be longer than 10 characters!";
        } else if (adminDTO.getRoleId() <= 0) {
            message = "Role is required!";
        } else {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Transaction transaction = null;
            try {
                Admin existingAdmin = session.createNamedQuery("Admin.getByEmail", Admin.class)
                        .setParameter("email", adminDTO.getEmail())
                        .getSingleResultOrNull();

                if (existingAdmin != null) {
                    message = "This admin email already exists!";
                } else {
                    Role role = session.get(Role.class, adminDTO.getRoleId());
                    Status approvedStatus = session.createNamedQuery("Status.findByValue", Status.class)
                            .setParameter("value", Status.Type.APPROVED.name())
                            .getSingleResultOrNull();

                    if (role == null) {
                        message = "Selected role not found!";
                    } else if (approvedStatus == null) {
                        message = "Approved status not found!";
                    } else {
                        Admin admin = new Admin();
                        admin.setUsername(adminDTO.getUsername().trim());
                        admin.setEmail(adminDTO.getEmail().trim());
                        admin.setPassword(adminDTO.getPassword());
                        admin.setRole(role);
                        admin.setStatus(approvedStatus);

                        transaction = session.beginTransaction();
                        session.persist(admin);
                        transaction.commit();

                        status = true;
                        message = "Admin added successfully.";
                    }
                }
            } catch (Exception e) {
                if (transaction != null) transaction.rollback();
                message = "Failed to add admin.";
                e.printStackTrace();
            } finally {
                session.close();
            }
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    public String blockAdmin(int adminId, HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        Admin sessionAdmin = getSessionAdmin(request);
        boolean status = false;
        String message;

        if (sessionAdmin == null) {
            message = "Please login as admin first.";
        } else if (!isSuperAdmin(sessionAdmin)) {
            message = "Only super admins can block admins.";
        } else if (sessionAdmin.getId() == adminId) {
            message = "You cannot block your own admin account.";
        } else {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Transaction transaction = null;
            try {
                Admin admin = session.get(Admin.class, adminId);
                Status blockedStatus = session.createNamedQuery("Status.findByValue", Status.class)
                        .setParameter("value", Status.Type.BLOCKED.name())
                        .getSingleResultOrNull();

                if (admin == null) {
                    message = "Admin account not found.";
                } else if (blockedStatus == null) {
                    message = "Blocked status not found.";
                } else {
                    transaction = session.beginTransaction();
                    admin.setStatus(blockedStatus);
                    session.merge(admin);
                    transaction.commit();

                    status = true;
                    message = "Admin blocked successfully.";
                }
            } catch (Exception e) {
                if (transaction != null) transaction.rollback();
                message = "Failed to block admin.";
                e.printStackTrace();
            } finally {
                session.close();
            }
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    public String adminLogout(HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        HttpSession httpSession = request.getSession(false);

        if (httpSession != null && httpSession.getAttribute("admin") != null) {
            httpSession.invalidate();
            responseObject.addProperty("status", true);
            responseObject.addProperty("message", "Logout successful.");
        } else {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "No admin session found.");
        }

        return AppUtil.GSON.toJson(responseObject);
    }

    private Admin getSessionAdmin(HttpServletRequest request) {
        HttpSession httpSession = request.getSession(false);
        if (httpSession == null || httpSession.getAttribute("admin") == null) {
            return null;
        }
        return (Admin) httpSession.getAttribute("admin");
    }

    private boolean isSuperAdmin(Admin admin) {
        return admin != null
                && admin.getRole() != null
                && "super admin".equalsIgnoreCase(admin.getRole().getName());
    }

    private boolean canAdminLogin(Admin admin) {
        if (admin == null || admin.getStatus() == null) {
            return false;
        }
        String statusValue = admin.getStatus().getValue();
        return Status.Type.APPROVED.name().equals(statusValue)
                || Status.Type.ACTIVE.name().equals(statusValue);
    }

    private String getRequestedCustomerStatus(String jsonData) {
        try {
            JsonObject requestObject = AppUtil.GSON.fromJson(jsonData, JsonObject.class);
            if (requestObject == null || !requestObject.has("status")) {
                return null;
            }

            String statusValue = requestObject.get("status").getAsString();
            if (statusValue == null || statusValue.isBlank()) {
                return null;
            }

            String normalizedValue = statusValue.trim().toUpperCase().replace(' ', '_');
            if (Status.Type.VERIFIED.name().equals(normalizedValue)
                    || Status.Type.BLOCKED.name().equals(normalizedValue)) {
                return normalizedValue;
            }
        } catch (Exception ignored) {
        }
        return null;
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

    private double calculateTotalRevenue(Session session) {
        double total = 0;
        List<OrderdItems> orderItems = session.createQuery(
                "FROM OrderdItems oi JOIN FETCH oi.product", OrderdItems.class).list();

        for (OrderdItems orderItem : orderItems) {
            total += orderItem.getProduct().getPrice() * parseQuantity(orderItem.getQty());
        }

        List<Delivery> deliveries = session.createQuery(
                "FROM Delivery d JOIN FETCH d.deliveryType", Delivery.class).list();
        for (Delivery delivery : deliveries) {
            total += delivery.getDeliveryType().getPrice();
        }

        return total;
    }

    private JsonArray getRecentOrders(Session session) {
        JsonArray orderArray = new JsonArray();
        List<Order> orders = session.createQuery(
                        "FROM Order o JOIN FETCH o.user ORDER BY o.id DESC", Order.class)
                .setMaxResults(5)
                .list();

        for (Order order : orders) {
            Delivery delivery = session.createQuery(
                            "FROM Delivery d JOIN FETCH d.status JOIN FETCH d.deliveryType WHERE d.order.id = :orderId",
                            Delivery.class)
                    .setParameter("orderId", order.getId())
                    .setMaxResults(1)
                    .getSingleResultOrNull();

            JsonObject orderObject = new JsonObject();
            orderObject.addProperty("orderId", order.getId());
            orderObject.addProperty("customerName", (order.getUser().getFname() + " " + order.getUser().getLname()).trim());
            orderObject.addProperty("total", calculateOrderTotal(session, order.getId(), delivery));
            orderObject.addProperty("status", delivery != null ? delivery.getStatus().getValue() : "-");
            orderArray.add(orderObject);
        }

        return orderArray;
    }

    private JsonArray getDashboardActivity(Session session) {
        JsonArray activityArray = new JsonArray();

        List<Order> recentOrders = session.createQuery(
                        "FROM Order o JOIN FETCH o.user ORDER BY o.id DESC", Order.class)
                .setMaxResults(3)
                .list();
        for (Order order : recentOrders) {
            JsonObject activityObject = new JsonObject();
            activityObject.addProperty("type", "order");
            activityObject.addProperty("message", "New order #" + order.getId() + " placed by "
                    + (order.getUser().getFname() + " " + order.getUser().getLname()).trim());
            activityObject.addProperty("time", "Recent");
            activityArray.add(activityObject);
        }

        List<Product> lowStockProducts = session.createQuery(
                        "FROM Product p WHERE p.quantity <= 3 ORDER BY p.quantity ASC", Product.class)
                .setMaxResults(3)
                .list();
        for (Product product : lowStockProducts) {
            JsonObject activityObject = new JsonObject();
            activityObject.addProperty("type", "stock");
            activityObject.addProperty("message", "Low stock - " + product.getTitle() + " (" + product.getQuantity() + " left)");
            activityObject.addProperty("time", "Now");
            activityArray.add(activityObject);
        }

        return activityArray;
    }

    private JsonArray getTopProducts(Session session) {
        JsonArray productArray = new JsonArray();
        List<Product> products = session.createQuery(
                        "FROM Product p LEFT JOIN FETCH p.category ORDER BY p.quantity ASC, p.id DESC",
                        Product.class)
                .setMaxResults(3)
                .list();

        for (Product product : products) {
            JsonObject productObject = new JsonObject();
            productObject.addProperty("id", product.getId());
            productObject.addProperty("title", product.getTitle());
            productObject.addProperty("categoryName", product.getCategory() != null ? product.getCategory().getName() : "-");
            productObject.addProperty("price", product.getPrice());
            productObject.addProperty("quantity", product.getQuantity());
            productArray.add(productObject);
        }

        return productArray;
    }

    private double calculateOrderTotal(Session session, int orderId, Delivery delivery) {
        double total = delivery != null ? delivery.getDeliveryType().getPrice() : 0;
        List<OrderdItems> orderItems = session.createQuery(
                        "FROM OrderdItems oi JOIN FETCH oi.product WHERE oi.order.id = :orderId",
                        OrderdItems.class)
                .setParameter("orderId", orderId)
                .list();

        for (OrderdItems orderItem : orderItems) {
            total += orderItem.getProduct().getPrice() * parseQuantity(orderItem.getQty());
        }

        return total;
    }

    private int parseQuantity(String quantity) {
        try {
            return Integer.parseInt(quantity);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private AdminDTO toAdminDTO(Admin admin) {
        AdminDTO adminDTO = new AdminDTO();
        adminDTO.setId(admin.getId());
        adminDTO.setUsername(admin.getUsername());
        adminDTO.setEmail(admin.getEmail());

        if (admin.getRole() != null) {
            adminDTO.setRoleId(admin.getRole().getId());
            adminDTO.setRoleName(admin.getRole().getName());
        }

        if (admin.getStatus() != null) {
            adminDTO.setStatusId(admin.getStatus().getId());
            adminDTO.setStatusValue(admin.getStatus().getValue());
        }

        return adminDTO;
    }
}
