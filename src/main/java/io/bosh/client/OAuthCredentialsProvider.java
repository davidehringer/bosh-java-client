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

import java.util.Arrays;

/**
 * @author Jannik Heyl.
 */
public class OAuthCredentialsProvider implements Header {

    private OAuth2AccessToken token;

    private AccessTokenProviderChain CHAIN = new AccessTokenProviderChain(
            Arrays.asList(new UnsecureClientCredentialsAccessTokenProvider()));

    private  ClientCredentialsResourceDetails credentials = new ClientCredentialsResourceDetails();


    public OAuthCredentialsProvider(String host, String username, String password){
        host="https://" + host + ":8443/oauth/token";
        getCredentials(host, username, password);
        requestToken();
    }

    private void requestToken(){

        if (token == null) {
            token = CHAIN.obtainAccessToken(credentials, new DefaultAccessTokenRequest());
        }
        else if (token.isExpired()) {
            refreshAccessToken();
        }

    }

    private void refreshAccessToken() {
        Assert.notNull(token);

        token = CHAIN.refreshAccessToken(credentials, token.getRefreshToken(), new DefaultAccessTokenRequest());
    }

    private ClientCredentialsResourceDetails getCredentials(String host, String username,
                                                            String password){
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
        return token.getTokenType()+ " "+ token.getValue();
    }

    @Override
    public HeaderElement[] getElements() throws ParseException {
        return new HeaderElement[0];
    }
}