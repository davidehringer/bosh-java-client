package io.bosh.client.v2.deployments;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import io.bosh.client.AbstractDirectorTest;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;

/**
 * @author David Ehringer
 */
public class DeploymentsTest extends AbstractDirectorTest{

    private Deployments deployments;

    @Before
    public void setup(){
        deployments = client.deployments();
    }

    @Test
    public void list() {
        // Given
        mockServer.expect(requestTo(url("/deployments")))//
                .andRespond(withSuccess(payload("deployments/deployments.json"), MediaType.TEXT_HTML));
        // When
        deployments.list().subscribe(response -> {
            List<Deployment> deps = response.getDeployments();

            // Then
            assertThat(deps.size(), is(2));
            assertThat(deps.get(0).getName(), is("deployment-1"));
            assertThat(deps.get(0).getReleases().size(), is(1));
            assertThat(deps.get(0).getStemcells().size(), is(1));
            assertThat(deps.get(1).getName(), is("deployment-2"));
        });
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void get() {
        // Given
        mockServer.expect(requestTo(url("/deployments/cf-redis-61423dfddec885b6e28d")))//
                .andRespond(withSuccess(payload("deployments/deployment.json"), MediaType.TEXT_HTML));
        // When
        GetDeploymentRequest request = new GetDeploymentRequest().withName("cf-redis-61423dfddec885b6e28d");
        deployments.get(request).subscribe(response -> {

            // Then
            assertThat(response.getName(), is("cf-redis-61423dfddec885b6e28d"));
            assertThat(response.getRawManifest(), is(notNullValue()));
            assertThat(response.getManifest().get("name"), is("cf-redis-61423dfddec885b6e28d"));
            assertThat(response.getManifest().get("director_uuid"), is("51eba5a1-8a1e-47fe-b54f-3a051edb06fd"));
            assertThat(((List)response.getManifest().get("releases")).size(), is(1));
        });
    }

}
