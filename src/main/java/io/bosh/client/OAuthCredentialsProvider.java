package io.bosh.client;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.ParseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenProvider;
import org.springframework.security.oauth2.client.token.AccessTokenProviderChain;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.client.token.grant.implicit.ImplicitAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.security.oauth2.common.AuthenticationScheme;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.util.Assert;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

/**
 * Created by jannikheyl on 09.02.18.
 */
public class OAuthCredentialsProvider implements Header {
    private OAuth2AccessToken token;
    private AccessTokenProviderChain CHAIN;
    private ClientCredentialsResourceDetails credentials = new ClientCredentialsResourceDetails();


    public OAuthCredentialsProvider(String host, String username,
                                    String password, String scheme, boolean unsecure) throws URISyntaxException {
        if (unsecure) {
            CHAIN = new AccessTokenProviderChain(
                    Arrays.<AccessTokenProvider>asList(new UnsecureClientCredentialsAccessTokenProvider()));
        } else {
            CHAIN = new AccessTokenProviderChain(
                    Arrays.<AccessTokenProvider>asList(new ClientCredentialsAccessTokenProvider()));
        }
        URI uri = new URI(scheme + host + ":8443/oauth/token");

        getCredentials(uri.toString(), username, password);
        requestToken();
    }

    private void requestToken() {

        if (token == null) {
            token = CHAIN.obtainAccessToken(credentials, new DefaultAccessTokenRequest());
        } else if (token.isExpired()) {
            refreshAccessToken();
        }

    }

    private void refreshAccessToken() {
        Assert.notNull(token);

        token = CHAIN.refreshAccessToken(credentials, token.getRefreshToken(), new DefaultAccessTokenRequest());
    }

    private ClientCredentialsResourceDetails getCredentials(String host, String username,
                                                            String password) {
        credentials.setAccessTokenUri(host);
        credentials.setClientAuthenticationScheme(AuthenticationScheme.form);
        credentials.setClientId(username);
        credentials.setClientSecret(password);
        credentials.setGrantType("client_credentials");
        return credentials;
    }

    @Override
    public String getName() {
        return "Authorization";
    }

    @Override
    public String getValue() {
        return token.getTokenType() + " " + token.getValue();
    }

    @Override
    public HeaderElement[] getElements() throws ParseException {
        return new HeaderElement[0];
    }
}
