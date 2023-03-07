package com.wingmoney.web.client.interceptor;

import com.wingmoney.core.ICore;
import com.wingmoney.core.log.LoggerJ;
import com.wingmoney.core.util.StringUtil;
import com.wingmoney.web.client.ByteArrayInputStreamClientHttpResponse;
import com.wingmoney.web.configure.properties.LoggingProperties;
import com.wingmoney.web.log.ServletContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class LoggingClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private static final LoggerJ LOGGER = LoggerJ.of(LoggerFactory.getLogger(LoggingClientHttpRequestInterceptor.class));

    private int maxRequest = 2048;
    private int maxResponse = 2048;
    private int traceIdLength = 10;
    private static final Set<String> DEFAULT_EXCLUDE_HEADER_REQUEST =
            new HashSet<>(Arrays.asList(LoggingProperties.DEFAULT_EXCLUDE_REQUEST_HEADER.split(",")));

    public LoggingClientHttpRequestInterceptor() {
    }

    public LoggingClientHttpRequestInterceptor(int maxRequest, int maxResponse, int traceIdLength) {
        this.maxRequest = maxRequest;
        this.maxResponse = maxResponse;
        this.traceIdLength = traceIdLength;
    }

    public void setMaxRequest(int maxRequest) {
        this.maxRequest = maxRequest;
    }

    public void setMaxResponse(int maxResponse) {
        this.maxResponse = maxResponse;
    }

    public void setTraceIdLength(int traceIdLength) {
        this.traceIdLength = traceIdLength;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String uuid = StringUtil.random(traceIdLength, ICore.NUMBERS_AND_ALPHABET);
        logRequestData(uuid, request, body);
        ClientHttpResponse response = new ByteArrayInputStreamClientHttpResponse(execution.execute(request, body));
        logResponseData(uuid, request.getURI(), response);
        return response;
    }

    private void logRequestData(String uuid, HttpRequest request, byte[] bytes) {
        try {
            MultiValueMap<String, String> requestHeader = new LinkedMultiValueMap<>();
            HttpHeaders headers = request.getHeaders();
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                if (!DEFAULT_EXCLUDE_HEADER_REQUEST.contains(entry.getKey()))
                    requestHeader.put(entry.getKey(), entry.getValue());
            }
            String body = IOUtils.toString(bytes, StandardCharsets.UTF_8.name());
            String header = ServletContext.buildHeader(requestHeader);
            URI uri = request.getURI();
            StringBuilder builder = new StringBuilder();
            builder.append(request.getMethod()).append(" ").append(uuid).append(", ");
            builder.append(uri.getPath()).append(", ");
            builder.append(uri.getScheme()).append("://").append(uri.getHost());
            int port = uri.getPort();
            if (port > 0) builder.append(":").append(port);
            if (StringUtils.isNotEmpty(header)) builder.append(", ").append(header);
            boolean isMaxRequest = ServletContext.truncateBody(body, builder, maxRequest);
            LOGGER.info("http-outgoing request {}", builder.toString());
            if (isMaxRequest)
                LOGGER.info("Truncated request body length longer than " + maxRequest);
        } catch (Exception ex) {
            LOGGER.warn("exception occurred while try to log http outgoing request", ex);
        }
    }

    private void logResponseData(String uuid, URI uri, ClientHttpResponse response) throws IOException {
        InputStream inputStream = response.getBody();
        try {
            MultiValueMap<String, String> responseHeader = new LinkedMultiValueMap<>();
            HttpHeaders headers = response.getHeaders();
            for (Map.Entry<String, List<String>> entry : headers.entrySet())
                responseHeader.put(entry.getKey(), entry.getValue());
            String body = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            String header = ServletContext.buildHeader(responseHeader);
            StringBuilder builder = new StringBuilder();
            builder.append(response.getRawStatusCode()).append(" ").append(uuid).append(", ");
            builder.append(uri.getPath()).append(", ");
            builder.append(uri.getScheme()).append("://").append(uri.getHost());
            int port = uri.getPort();
            if (port > 0) builder.append(":").append(port);
            if (StringUtils.isNotEmpty(header)) builder.append(", ").append(header);
            boolean isMaxResponse = ServletContext.truncateBody(body, builder, maxResponse);
            LOGGER.info("http-outgoing response {}", builder.toString());
            if (isMaxResponse)
                LOGGER.info("Truncated response body length longer than " + maxResponse);
        } catch (Exception ex) {
            LOGGER.warn("exception occurred while try to log http outgoing response", ex);
        } finally {
            if (inputStream.markSupported())
                inputStream.reset();
        }
    }
}
