package io.bosh.client.v2.releases;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import io.bosh.client.domain.Release;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;

/**
 * @author David Ehringer
 */
public class ReleasesTest extends AbstractDirectorTest{

    private Releases releases;

    @Before
    public void setup(){
        releases = client.releases();
    }

    @Test
    public void releases() {
        // Given
        mockServer.expect(requestTo(url("/releases")))//
                .andRespond(withSuccess(payload("releases/releases.json"), MediaType.TEXT_HTML));
        // When
        releases.list().subscribe(response -> {
            List<Release> releases = response.getReleases();

            // Then
            assertThat(releases.size(), is(2));
            assertThat(releases.get(0).getName(), is("cf-redis"));
            assertThat(releases.get(1).getName(), is("platform-backup"));

            assertThat(releases.get(1).getVersions().size(), is(5));
            assertThat(releases.get(1).getVersions().get(1).getVersion(), is("4+dev.2"));
            assertThat(releases.get(1).getVersions().get(1).getJobNames().size(), is(1));
        });
    }

    @Test
    public void release() {
        // Given
        mockServer.expect(requestTo(url("/releases/cf-redis")))//
                .andRespond(withSuccess(payload("releases/release.json"), MediaType.TEXT_HTML));
        // When
        GetReleaseRequest request = new GetReleaseRequest().withName("cf-redis");
        releases.get(request).subscribe(release -> {

            // Then
            assertThat(release.getName(), is("cf-redis"));
            assertThat(release.getJobs().size(), is(6));
            assertThat(release.getJobs().get(2).getName(), is("cf-redis-broker"));
            assertThat(release.getJobs().get(2).getSha1(),
                    is("be6af0dd9dbe4d6aa80832cd6864229e77b8e90f"));
            assertThat(release.getJobs().get(2).getVersion(),
                    is("13522f98ef5d246f35157e824877d66da1f1a9e5"));
            assertThat(release.getJobs().get(2).getPackages().size(), is(7));
            assertThat(release.getJobs().get(2).getPackages().get(2), is("nginx"));

            assertThat(release.getVersions().size(), is(1));
            assertThat(release.getVersions().get(0), is("345"));

            assertThat(release.getPackages().size(), is(11));
            assertThat(release.getPackages().get(0).getName(), is("cf-redis-broker"));
            assertThat(release.getPackages().get(0).getSha1(),
                    is("50f9dcd949cd8342551eb1a073a2fa0f00018ba7"));
            assertThat(release.getPackages().get(0).getVersion(),
                    is("a3d53f9e1c548a537a67cb5524059f84927560d0"));
            assertThat(release.getPackages().get(0).getDependencies().size(), is(1));
            assertThat(release.getPackages().get(0).getDependencies().get(0), is("go"));
        });
    }

}
