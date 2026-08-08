package lk.thefurniturestore.middleware;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import lk.thefurniturestore.util.RememberMeUtil;

public class AccessControlFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        RememberMeUtil.restoreUserSession(request);
        HttpSession httpSession = request.getSession(false);
        String requestUri = request.getRequestURI();

        if (requestUri.endsWith("/admin.html")) {
            if (httpSession != null && httpSession.getAttribute("admin") != null) {
                addNoCacheHeaders(response);
                filterChain.doFilter(servletRequest, servletResponse);
            } else {
                response.sendRedirect(request.getContextPath() + "/adminLogin.html");
            }
            return;
        }

        if (httpSession != null && httpSession.getAttribute("user") != null) {
            addNoCacheHeaders(response);
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            response.sendRedirect(request.getContextPath() + "/login.html");
        }
    }

    private void addNoCacheHeaders(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }
}
