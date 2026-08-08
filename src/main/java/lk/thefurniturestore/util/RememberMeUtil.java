package lk.thefurniturestore.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.thefurniturestore.entity.Status;
import lk.thefurniturestore.entity.User;
import org.hibernate.Session;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public final class RememberMeUtil {
    private static final String COOKIE_NAME = "TFS_REMEMBER_ME";
    private static final int MAX_AGE_SECONDS = 60 * 60 * 24 * 14;

    private RememberMeUtil() {
    }

    public static void issue(HttpServletRequest request, HttpServletResponse response, User user) {
        long expiresAt = System.currentTimeMillis() + MAX_AGE_SECONDS * 1000L;
        String payload = user.getId() + "." + expiresAt;
        addCookie(request, response, payload + "." + sign(payload), MAX_AGE_SECONDS);
    }

    public static void clear(HttpServletRequest request, HttpServletResponse response) {
        addCookie(request, response, "", 0);
    }

    public static boolean restoreUserSession(HttpServletRequest request) {
        HttpSession existingSession = request.getSession(false);
        if (existingSession != null && existingSession.getAttribute("user") != null) {
            return true;
        }

        String token = cookieValue(request);
        if (token == null) {
            return false;
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return false;
        }

        try {
            int userId = Integer.parseInt(parts[0]);
            long expiresAt = Long.parseLong(parts[1]);
            String payload = parts[0] + "." + parts[1];
            if (expiresAt < System.currentTimeMillis() || !MessageDigest.isEqual(
                    parts[2].getBytes(StandardCharsets.UTF_8), sign(payload).getBytes(StandardCharsets.UTF_8))) {
                return false;
            }

            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                User user = session.get(User.class, userId);
                if (user == null || user.getStatus() == null
                        || !Status.Type.VERIFIED.name().equals(user.getStatus().getValue())) {
                    return false;
                }
                request.getSession(true).setAttribute("user", user);
                return true;
            }
        } catch (Exception ignored) {
            return false;
        }
    }

    private static String cookieValue(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private static void addCookie(HttpServletRequest request, HttpServletResponse response, String value, int maxAge) {
        Cookie cookie = new Cookie(COOKIE_NAME, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setPath(request.getContextPath().isBlank() ? "/" : request.getContextPath());
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    private static String sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(Env.require("app.remember-me.secret").getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to sign remember-me token", e);
        }
    }
}
