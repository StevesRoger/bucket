package com.wingmoney.web.client;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ByteArrayInputStreamClientHttpResponse implements ClientHttpResponse {

    private ResponseErrorHandler responseErrorHandler = new DefaultResponseErrorHandler();

    private ClientHttpResponse clientHttpResponse;

    private InputStream inputStream;

    private static final Logger LOGGER = LoggerFactory.getLogger(ByteArrayInputStreamClientHttpResponse.class);

    public ByteArrayInputStreamClientHttpResponse(ClientHttpResponse clientHttpResponse) throws IOException {
        try (InputStream body = clientHttpResponse.getBody()) {
            this.clientHttpResponse = clientHttpResponse;
            this.inputStream = new ByteArrayInputStream(IOUtils.toByteArray(body));
        } catch (IOException e) {
            e.printStackTrace();
            responseErrorHandler.handleError(clientHttpResponse);
        }
    }

    @Override
    public InputStream getBody() throws IOException {
        return inputStream;
    }

    @Override
    public HttpHeaders getHeaders() {
        return clientHttpResponse.getHeaders();
    }

    @Override
    public HttpStatus getStatusCode() throws IOException {
        return clientHttpResponse.getStatusCode();
    }

    @Override
    public int getRawStatusCode() throws IOException {
        return clientHttpResponse.getRawStatusCode();
    }

    @Override
    public String getStatusText() throws IOException {
        return clientHttpResponse.getStatusText();
    }

    @Override
    public void close() {
        try {
            IOUtils.close(inputStream);
        } catch (IOException e) {
            LOGGER.error("Cannot close stream", e);
            throw new RuntimeException(e);
        }
    }
}
