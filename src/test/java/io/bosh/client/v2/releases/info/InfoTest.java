package io.bosh.client.v2.releases.info;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import io.bosh.client.AbstractDirectorTest;
import io.bosh.client.v2.info.Info;
import io.bosh.client.v2.stemcells.StemcellDetails;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;

/**
 * @author David Ehringer
 */
public class InfoTest extends AbstractDirectorTest{

    private Info info;

    @Before
    public void setup(){
        info = client.info();
    }

    @Test
    public void releases() {
        // Given
        mockServer.expect(requestTo(url("/info")))//
                .andRespond(withSuccess(payload("info/info-bosh-lite.json"), MediaType.TEXT_HTML));
        // When
        info.info().subscribe(response -> {

            // Then
            assertThat(response.getCpi(), is("vsphere"));
            assertThat(response.getName(), is("Bosh Lite Director"));
            assertThat(response.getUser(), is("admin"));
            assertThat(response.getUuid(), is("c6f166bd-ddac-4f7d-9c57-d11c6ad5133b"));
            assertThat(response.getVersion(), is("1.2811.0 (00000000)"));
            assertThat(response.getFeatures().size(), is(3));
        });
    }

}
