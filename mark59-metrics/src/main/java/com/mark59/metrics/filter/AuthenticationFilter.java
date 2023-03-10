package com.mark59.metrics.filter;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
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

@Component
public class AuthenticationFilter implements Filter {

    public void init(FilterConfig fConfig) throws ServletException {
    	// System.out.println("AuthenticationFilter init");
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        // System.out.println("AuthenticationFilter doFilter at  " + httpServletRequest.getRequestURI());

        HttpSession session = httpServletRequest.getSession(false);
        String authState = "";
        if (session != null) {
        	authState = (String)session.getAttribute("authState");
        }

    	if (StringUtils.equals(httpServletRequest.getRequestURI(), "/mark59-metrics")){
    		chain.doFilter(request, response);
    	} else if (StringUtils.equals(httpServletRequest.getRequestURI(),  "/mark59-metrics/login")){
    		chain.doFilter(request, response);
    	} else if (StringUtils.equals(httpServletRequest.getRequestURI(),  "/mark59-metrics/loginAction")){
    		chain.doFilter(request, response); 
    	} else if (StringUtils.equals(httpServletRequest.getRequestURI(),  "/mark59-metrics/api/metric")){
    		chain.doFilter(request, response); 
    	} else if (!"authOK".equals(authState)){   
    		httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/login" );
    	} else {
    		// System.out.println("validated session exists, continue on");
    		chain.doFilter(request, response);
    	}
    }

    
	public void destroy() {
        //close any resources here
    }
}
