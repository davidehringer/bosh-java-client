/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.bosh.client.v2;

import static org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter.DEFAULT_CHARSET;

import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLContext;

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
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author David Ehringer
 */
public class SpringDirectorClientBuilder {

    private String host;
    private String username;
    private String password;
    
    public SpringDirectorClientBuilder withCredentials(String username, String password){
        this.username = username;
        this.password = password;
        return this;
    }
    
    public SpringDirectorClientBuilder withHost(String host){
        this.host = host;
        return this;
    }
    
    public SpringDirectorClient build(){
        // TODO validate
        URI root = UriComponentsBuilder.newInstance().scheme("https").host(host).port(25555)
                .build().toUri();
        RestTemplate restTemplate = new RestTemplate(createRequestFactory(host, username, password));
        restTemplate.getInterceptors().add(new ContentTypeClientHttpRequestInterceptor());
        handleTextHtmlResponses(restTemplate);
        return new SpringDirectorClient(root, restTemplate);
    }
    
    private ClientHttpRequestFactory createRequestFactory(String host, String username,
            String password) {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(new AuthScope(host, 25555),
                new UsernamePasswordCredentials(username, password));

        SSLContext sslContext = null;
        try {
            sslContext = SSLContexts.custom()
                    .loadTrustMaterial(null, new TrustSelfSignedStrategy()).useTLS().build();
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new DirectorException("Unable to configure ClientHttpRequestFactory", e);
        }

        SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext,
                new AllowAllHostnameVerifier());

        // disabling redirect handling is critical for the way BOSH uses 302's
        HttpClient httpClient = HttpClientBuilder.create().disableRedirectHandling()
                .setDefaultCredentialsProvider(credentialsProvider)
                .setSSLSocketFactory(connectionFactory).build();

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
