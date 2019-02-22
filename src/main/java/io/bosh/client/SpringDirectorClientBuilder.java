package io.bosh.client;

import static org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter.DEFAULT_CHARSET;

import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.SSLContext;

import io.bosh.client.Authentication;
import io.bosh.client.DirectorException;
import io.bosh.client.RequestLoggingInterceptor;
import io.bosh.client.SpringDirectorClient;
import org.apache.http.Header;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.oauth2.client.token.AccessTokenProvider;
import org.springframework.security.oauth2.client.token.AccessTokenProviderChain;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.implicit.ImplicitAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.security.oauth2.common.*;

/**
 * @author David Ehringer, Jannik Heyl.
 */
public class SpringDirectorClientBuilder {

    private Scheme scheme;
    private int port;
    private String host;
    private String username;
    private String password;
    private Authentication auth;

    public SpringDirectorClientBuilder withCredentials(String username, String password, Authentication auth){
        this.username = username;
        this.password = password;
        this.auth = auth;
        return this;
    }

    public SpringDirectorClientBuilder withScheme(Scheme scheme){
        this.scheme = scheme;
        return this;
    }

    public SpringDirectorClientBuilder withPort(int port){
        this.port = port;
        return this;
    }

    public SpringDirectorClientBuilder withHost(String host){
        this.host = host;
        return this;
    }

    public SpringDirectorClient build(){
        // TODO validate
        URI root = UriComponentsBuilder.newInstance().scheme(scheme.name()).host(host).port(port)
                .build().toUri();
        RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(createRequestFactory(host, username, password, auth)));
        restTemplate.getInterceptors().add(new ContentTypeClientHttpRequestInterceptor());
        restTemplate.getInterceptors().add(new RequestLoggingInterceptor());
        handleTextHtmlResponses(restTemplate);
        return new SpringDirectorClient(root, restTemplate);
    }

    private ClientHttpRequestFactory createRequestFactory(String host, String username,
                                                          String password, Authentication auth) {

        SSLContext sslContext = null;
        try {
            sslContext = SSLContexts.custom()
                    .loadTrustMaterial(null, new TrustSelfSignedStrategy()).useTLS().build();
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new DirectorException("Unable to configure ClientHttpRequestFactory", e);
        }

        SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext,
                new AllowAllHostnameVerifier());

        HttpClient httpClient;

        if(auth.equals(Authentication.BASIC)){
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(new AuthScope(host, 25555),
                    new UsernamePasswordCredentials(username, password));

            // disabling redirect handling is critical for the way BOSH uses 302's
            httpClient = HttpClientBuilder.create().disableRedirectHandling()
                    .setDefaultCredentialsProvider(credentialsProvider)
                    .setSSLSocketFactory(connectionFactory).build();
        } else {

            // disabling redirect handling is critical for the way BOSH uses 302's
            httpClient = HttpClientBuilder.create().disableRedirectHandling()
                    .setDefaultHeaders(Arrays.asList(new OAuthCredentialsProvider(host, username, password)))
                    .setSSLSocketFactory(connectionFactory).build();

        }


        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }

    private void handleTextHtmlResponses(RestTemplate restTemplate) {
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
        messageConverters.add(new StringHttpMessageConverter());
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        messageConverter.setSupportedMediaTypes(Arrays.asList(new MediaType("application", "json",
                        DEFAULT_CHARSET), new MediaType("application", "*+json", DEFAULT_CHARSET),
                new MediaType("text", "html", DEFAULT_CHARSET)));
        messageConverters.add(messageConverter);
        restTemplate.setMessageConverters(messageConverters);
    }

    private static class ContentTypeClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                            ClientHttpRequestExecution execution) throws IOException {
            ClientHttpResponse response = execution.execute(request, body);
            // some BOSH resources return text/plain and this modifies this response
            // so we can use Jackson
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return response;
        }

    }
}