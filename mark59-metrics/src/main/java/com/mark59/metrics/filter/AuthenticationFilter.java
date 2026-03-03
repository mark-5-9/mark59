package com.mark59.metrics.filter;

import java.io.IOException;
import java.util.Set;

import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Authentication filter that validates user session state for all requests
 * except whitelisted public endpoints (login, API endpoints).
 */
@Component
public class AuthenticationFilter implements Filter {

	private static final String CONTEXT_PATH = "/mark59-metrics";
	private static final String AUTH_STATE_ATTRIBUTE = "authState";
	private static final String AUTH_STATE_OK = "authOK";

	private static final Set<String> PUBLIC_ENDPOINTS = Set.of(
		CONTEXT_PATH,
		CONTEXT_PATH + "/login",
		CONTEXT_PATH + "/loginAction",
		CONTEXT_PATH + "/api/metric"
	);

	@Override
    public void init(FilterConfig fConfig) throws ServletException {
		// Filter initialization
    }

	@Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        String requestURI = httpServletRequest.getRequestURI();

        // Allow public endpoints without authentication
        if (PUBLIC_ENDPOINTS.contains(requestURI)) {
        	chain.doFilter(request, response);
        	return;
        }

        // Check authentication state for protected endpoints
        HttpSession session = httpServletRequest.getSession(false);
        String authState = null;
        if (session != null) {
        	authState = (String) session.getAttribute(AUTH_STATE_ATTRIBUTE);
        }

    	if (!AUTH_STATE_OK.equals(authState)) {
    		httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/login");
    	} else {
    		chain.doFilter(request, response);
    	}
    }


	@Override
	public void destroy() {
		// Resource cleanup if needed
    }
}
