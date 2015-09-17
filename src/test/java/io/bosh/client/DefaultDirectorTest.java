package io.bosh.client;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import io.bosh.client.domain.Deployment;
import io.bosh.client.domain.DirectorInfo;
import io.bosh.client.domain.LogType;
import io.bosh.client.domain.Task;
import io.bosh.client.domain.Vm;
import io.bosh.client.v2.releases.Release;
import io.bosh.client.v2.releases.ReleaseDetails;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.RestTemplate;

/**
 * @author David Ehringer
 */
public class DefaultDirectorTest {

    private MockRestServiceServer mockServer;
    private RestTemplate restTemplate;
    private DirectorClient director;

    @Before
    public void setup() {
        restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        director = new DirectorClient("192.168.50.4", restTemplate);
    }

    private String url(String url) {
        return director.getRoot().toString() + url;
    }

    private String response(String file) {
        String filename = "responses/" + file;
        ClassPathResource resource = new ClassPathResource(filename);
        try {
            return FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(
                    "TEST SETUP ERROR: unable to find test resources file with name " + filename, e);
        }

    }

    @Test
    public void infoForBoshLite() {
        // Given
        mockServer.expect(requestTo(url("/info")))//
                .andRespond(
                        withSuccess(response("info-bosh-lite.json"), MediaType.APPLICATION_JSON));
        // When
        DirectorInfo info = director.getInfo();

        // Then
        assertThat(info.getCpi(), is("vsphere"));
        assertThat(info.getName(), is("Bosh Lite Director"));
        assertThat(info.getUser(), is("admin"));
        assertThat(info.getUuid(), is("c6f166bd-ddac-4f7d-9c57-d11c6ad5133b"));
        assertThat(info.getVersion(), is("1.2811.0 (00000000)"));
        assertThat(info.getFeatures().size(), is(3));
    }

    @Test
    public void deployments() {
        // Given
        mockServer.expect(requestTo(url("/deployments")))//
                .andRespond(withSuccess(response("deployments.json"), MediaType.TEXT_HTML));
        // When
        List<Deployment> deployments = director.getDeployments();

        // Then
        assertThat(deployments.size(), is(2));
        assertThat(deployments.get(0).getName(), is("deployment-1"));
        assertThat(deployments.get(0).getReleases().size(), is(1));
        assertThat(deployments.get(0).getStemcells().size(), is(1));
        assertThat(deployments.get(1).getName(), is("deployment-2"));
    }

    @Test
    public void releases() {
        // Given
        mockServer.expect(requestTo(url("/releases")))//
                .andRespond(withSuccess(response("releases.json"), MediaType.TEXT_HTML));
        // When
        List<Release> releases = director.getReleases();

        // Then
        assertThat(releases.size(), is(2));
        assertThat(releases.get(0).getName(), is("cf-redis"));
        assertThat(releases.get(1).getName(), is("platform-backup"));

        assertThat(releases.get(1).getVersions().size(), is(5));
        assertThat(releases.get(1).getVersions().get(1).getVersion(), is("4+dev.2"));
        assertThat(releases.get(1).getVersions().get(1).getJobNames().size(), is(1));
    }

    @Test
    public void release() {
        // Given
        mockServer.expect(requestTo(url("/releases/cf-redis")))//
                .andRespond(withSuccess(response("release.json"), MediaType.TEXT_HTML));
        // When
        ReleaseDetails release = director.getRelease("cf-redis");

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
    }

    @Test
    public void recentTasks() {
        // Given
        mockServer.expect(requestTo(url("/tasks?limit=30&verbose=1")))//
                .andRespond(withSuccess(response("tasks.json"), MediaType.TEXT_HTML));
        // When
        List<Task> tasks = director.getRecentTasks();

        // Then
        assertThat(tasks.size(), is(14));
        assertThat(tasks.get(12).getDescription(), is("create stemcell"));
        assertThat(tasks.get(12).getResult(),
                is("/stemcells/bosh-warden-boshlite-ubuntu-trusty-go_agent/389"));
        assertThat(tasks.get(12).getState(), is("done"));
        assertThat(tasks.get(12).getUser(), is("admin"));
        assertThat(tasks.get(12).getId(), is("2"));
    }

    @Test
    public void vms() {
        // Given
        mockServer.expect(requestTo(url("/deployments/example/vms")))//
                .andRespond(withSuccess(response("vms.json"), MediaType.TEXT_HTML));
        // When
        List<Vm> vm = director.getVms("example");

        // Then
        assertThat(vm.size(), is(1));
        assertThat(vm.get(0).getAgentId(), is("30096cdf-d96b-4c73-a9d0-55b50c6ce37d"));
        assertThat(vm.get(0).getCid(), is("5585315f-8280-4de6-6694-9b6ab2f21393"));
        assertThat(vm.get(0).getIndex(), is(0));
        assertThat(vm.get(0).getJob(), is("cfbackup"));
    }

    @Test
    public void fetchLogs() throws IOException {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.set("Location", "https://10.174.52.151/tasks/3307");
        mockServer
                .expect(requestTo(url("/deployments/platform-backup/jobs/cfbackup/0/logs?type=agent&filters=")))//
                .andRespond(withSuccess().headers(headers));

        mockServer.expect(requestTo(url("/tasks/3307")))//
                .andRespond(withSuccess(response("in-progress-task.json"), MediaType.TEXT_HTML));
        mockServer
                .expect(requestTo(url("/tasks/3307")))
                //
                .andRespond(
                        withSuccess(response("fetch-logs-task-complete.json"), MediaType.TEXT_HTML));
        mockServer
                .expect(requestTo(url("/tasks/3307")))
                //
                .andRespond(
                        withSuccess(response("fetch-logs-task-complete.json"), MediaType.TEXT_HTML));
        mockServer.expect(requestTo(url("/resources/d0a1877d-3802-4922-408c-85b553d13aba")))//
                .andRespond(
                        withSuccess(new ClassPathResource("responses/compressed-logs.gz"),
                                MediaType.TEXT_HTML));

        // When
        InputStream inputStream = director.fetchLogs("platform-backup", "cfbackup", 0,
                LogType.AGENT);

        // Then
        File logs = File.createTempFile("fetchLogs-", ".tgz");
        FileUtils.copyInputStreamToFile(inputStream, logs);
        assertTrue(logs.length() > 150000);
    }
}
