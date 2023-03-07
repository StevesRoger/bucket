package com.wingmoney.web.client.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wingmoney.core.concurrent.InheritableContextHolder;
import com.wingmoney.core.log.LoggerJ;
import com.wingmoney.core.util.ContextUtil;
import com.wingmoney.web.context.request.RequestHeaderContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static com.wingmoney.web.context.request.RequestHeaderContext.*;

public class AuthenticationHeaderRequestInterceptor implements ClientHttpRequestInterceptor {

    private static final LoggerJ LOG = LoggerJ.of(LoggerFactory.getLogger(AuthenticationHeaderRequestInterceptor.class));

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        HttpHeaders headers = request.getHeaders();
        if (!headers.containsKey(HEADER_USER_ID_KEY))
            headers.add(HEADER_USER_ID_KEY, RequestHeaderContext.getUserId());
        if (!headers.containsKey(HEADER_CLIENT_ID_KEY))
            headers.add(HEADER_CLIENT_ID_KEY, RequestHeaderContext.getClientId());
        if (!headers.containsKey(HEADER_PHONE_NUMBER_KEY))
            headers.add(HEADER_PHONE_NUMBER_KEY, RequestHeaderContext.getPhoneNumber());
        if (!headers.containsKey(HEADER_WING_ACCOUNT_KEY))
            headers.addAll(HEADER_WING_ACCOUNT_KEY, RequestHeaderContext.getWingAccounts());
        if (!headers.containsKey(X_AUTH_DATA))
            headers.add(X_AUTH_DATA, RequestHeaderContext.getHeader(X_AUTH_DATA));
        if (!headers.containsKey(WING_PLATFORM))
            headers.add(WING_PLATFORM, RequestHeaderContext.getWingPlatform());
        if (!headers.containsKey(OS_VERSION))
            headers.add(OS_VERSION, RequestHeaderContext.getOsVersion());
        if (!headers.containsKey(DEVICE_MODEL))
            headers.add(DEVICE_MODEL, RequestHeaderContext.getDeviceModel());
        if (!headers.containsKey(DEVICE_ID))
            headers.add(DEVICE_ID, RequestHeaderContext.getDeviceId());
        if (!headers.containsKey(APP_VERSION))
            headers.add(APP_VERSION, RequestHeaderContext.getAppVersion());
        if (!headers.containsKey(API_VERSION))
            headers.add(API_VERSION, RequestHeaderContext.getApiVersion());
        if (!headers.containsKey(REQUEST_ID))
            headers.add(REQUEST_ID, RequestHeaderContext.getRequestId());
        if (!headers.containsKey(GATEWAY_REQUEST_ID))
            headers.add(GATEWAY_REQUEST_ID, RequestHeaderContext.getGatewayRequestId());
        if (!headers.containsKey(DOWNSTREAM_REDIRECT))
            headers.add(DOWNSTREAM_REDIRECT, RequestHeaderContext.getDownstreamRedirect());
        if (!headers.containsKey(HEADER_CLIENT_SECRET_KEY))
            headers.add(HEADER_CLIENT_SECRET_KEY, RequestHeaderContext.getClientSecret());
        String serviceName = System.getenv("SERVICE_NAME");
        if (StringUtils.isEmpty(serviceName))
            serviceName = ContextUtil.getProperty("spring.application.name", "");
        if (!headers.containsKey(SERVICE_ID))
            headers.add(SERVICE_ID, serviceName);
        extractSetting(headers);
        return execution.execute(request, body);
    }

    @SuppressWarnings("unchecked")
    private void extractSetting(HttpHeaders headers) {
        try {
            String setting = Optional.ofNullable(InheritableContextHolder.getString(SETTING)).orElseGet(() -> RequestHeaderContext.getHeader(SETTING));
            if (!headers.containsKey(SETTING) && StringUtils.isNotEmpty(setting)) headers.add(SETTING, setting);
            if (StringUtils.isNotEmpty(setting) && setting.startsWith("{")) {
                Map<String, Object> settingMap = new ObjectMapper().readValue(setting, Map.class);
                for (Map.Entry<String, Object> entry : settingMap.entrySet()) {
                    String key = entry.getKey().replace("_", "-");
                    if (!headers.containsKey(key)) headers.add(key, String.valueOf(entry.getValue()));
                }
            }
        } catch (Exception ex) {
            LOG.warn("exception occurred while extract setting from header {}", ex.getMessage());
        }
    }
}
