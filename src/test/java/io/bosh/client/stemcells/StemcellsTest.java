package io.bosh.client.stemcells;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import io.bosh.client.AbstractDirectorTest;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;

/**
 * @author David Ehringer
 */
public class StemcellsTest extends AbstractDirectorTest{

    private Stemcells stemcells;

    @Before
    public void setup(){
        stemcells = client.stemcells();
    }

    @Test
    public void list() {
        // Given
        mockServer.expect(requestTo(url("/stemcells")))//
                .andRespond(withSuccess(payload("stemcells/stemcells.json"), MediaType.TEXT_HTML));
        // When
        stemcells.list().subscribe(details -> {
            // Then
            assertThat(details.size(), is(5));
            
            assertThat(details.get(0).getName(), is("bosh-vsphere-esxi-ubuntu-trusty-go_agent"));
            assertThat(details.get(0).getVersion(), is("2889"));
            assertThat(details.get(0).getCid(), is("sc-77fe9b3d-6396-4f83-ae55-9f9848babd92"));
            assertThat(details.get(0).getDeployments().size(), is(4));
            assertThat(details.get(0).getDeployments().get(0), is("cf-redis-61423dfddec885b6e28d"));
            assertThat(details.get(0).getDeployments().get(1), is("metrics-nozzle-d210a05a419256e66b39"));
            assertThat(details.get(0).getDeployments().get(2), is("p-rabbitmq-371eef62ac5320e87f9d"));
            assertThat(details.get(0).getDeployments().get(3), is("p-rabbitmq-aaab88123797df139b13"));
            
            assertThat(details.get(1).getName(), is("bosh-vsphere-esxi-ubuntu-trusty-go_agent"));
            assertThat(details.get(1).getVersion(), is("2865.1"));
            assertThat(details.get(1).getCid(), is("sc-8d211a04-2689-4dd3-9d8d-edea442f832e"));
            assertThat(details.get(1).getDeployments().size(), is(2));
        });
    }

}
