package io.bosh.client;

import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Jannik Heyl.
 */
public class RequestLoggingInterceptor implements ClientHttpRequestInterceptor {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(RequestLoggingInterceptor.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        ClientHttpResponse response = execution.execute(request, body);

        log.debug("request method: {}, request URI: {}, request headers: {}, request body: {}, response status code: {}, response headers: {}, response body: {}",
                request.getMethod(),
                request.getURI(),
                request.getHeaders(),
                new String(body, Charset.forName("UTF-8")),
                response.getStatusCode(),
                response.getHeaders(),
                response.getBody());

        return response;
    }
}