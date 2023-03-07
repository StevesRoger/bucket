package com.wingmoney.web.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wingmoney.core.concurrent.InheritableContextHolder;
import com.wingmoney.core.listener.ApplicationContextRefreshedListener;
import com.wingmoney.core.log.LoggerJ;
import com.wingmoney.core.model.AuthenticationHeader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import java.util.Objects;

import static com.wingmoney.web.context.request.RequestHeaderContext.*;

@Component
public class AuthenticationHeaderFilter extends OncePerRequestFilter implements Ordered, ApplicationContextRefreshedListener {

    private static final LoggerJ LOG = LoggerJ.of(LoggerFactory.getLogger(AuthenticationHeaderFilter.class));

    private ObjectMapper mapper;
    private int order = Ordered.HIGHEST_PRECEDENCE + 2;

    public void setOrder(int order) {
        this.order = order;
    }

    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String authData = request.getHeader(X_AUTH_DATA);
            if (StringUtils.isNotEmpty(authData)) {
                authData = new String(Base64.getDecoder().decode(authData));
                LOG.debug("authentication data:{}", authData);
                AuthenticationHeader authHeader = mapper.readValue(authData, AuthenticationHeader.class);
                authHeader.setAuthenticated(true);
                InheritableContextHolder.setObject(X_AUTH_DATA, authHeader);
                LOG.debug("successful extract authentication data from request header:{}", X_AUTH_DATA);
            }
        } catch (Exception e) {
            LOG.warn("exception occurred while extract {} {}", X_AUTH_DATA, e.getMessage());
        }
        extractAuthenticationHeader(request);
        filterChain.doFilter(request, response);
    }

    protected void extractAuthenticationHeader(HttpServletRequest request) {
        InheritableContextHolder.setString(HEADER_USER_ID_KEY, request.getHeader(HEADER_USER_ID_KEY));
        InheritableContextHolder.setString(HEADER_WING_ACCOUNT_KEY, request.getHeader(HEADER_WING_ACCOUNT_KEY));
        InheritableContextHolder.setString(HEADER_PHONE_NUMBER_KEY, request.getHeader(HEADER_PHONE_NUMBER_KEY));
        InheritableContextHolder.setString(HEADER_CLIENT_ID_KEY, request.getHeader(HEADER_CLIENT_ID_KEY));
        InheritableContextHolder.setString(WING_PLATFORM, request.getHeader(WING_PLATFORM));
        InheritableContextHolder.setString(OS_VERSION, request.getHeader(OS_VERSION));
        InheritableContextHolder.setString(DEVICE_MODEL, request.getHeader(DEVICE_MODEL));
        InheritableContextHolder.setString(DEVICE_ID, request.getHeader(DEVICE_ID));
        InheritableContextHolder.setString(APP_VERSION, request.getHeader(APP_VERSION));
        InheritableContextHolder.setString(API_VERSION, request.getHeader(API_VERSION));
        InheritableContextHolder.setString(REQUEST_ID, request.getHeader(REQUEST_ID));
        InheritableContextHolder.setString(GATEWAY_REQUEST_ID, request.getHeader(GATEWAY_REQUEST_ID));
        InheritableContextHolder.setString(DOWNSTREAM_REDIRECT, request.getHeader(DOWNSTREAM_REDIRECT));
        InheritableContextHolder.setString(SETTING, request.getHeader(SETTING));
        InheritableContextHolder.setString(HEADER_CLIENT_SECRET_KEY, request.getHeader(HEADER_CLIENT_SECRET_KEY));
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (Objects.isNull(mapper)) {
            try {
                ObjectMapper bean = event.getApplicationContext().getBean(ObjectMapper.class);
                this.mapper = bean;
            } catch (BeansException ex) {
                LOG.warn("exception occurred while register bean object mapper to authentication header filter", ex);
            }
        }
    }
}
