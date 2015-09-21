package io.bosh.client.jobs;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import io.bosh.client.AbstractDirectorTest;
import io.bosh.client.jobs.FetchLogsRequest;
import io.bosh.client.jobs.Jobs;
import io.bosh.client.jobs.LogType;
import io.bosh.client.jobs.StopJobRequest;
import io.bosh.client.tasks.Task;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import rx.observers.TestSubscriber;

/**
 * @author David Ehringer
 */
public class JobsTest extends AbstractDirectorTest{

    private Jobs jobs;

    @Before
    public void setup(){
        jobs = client.jobs();
    }

    @Test
    public void fetchLogs() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.set("Location", "https://10.174.52.151/tasks/3307");
        mockServer
                .expect(requestTo(url("/deployments/platform-backup/jobs/cfbackup/0/logs?type=agent&filters=filter1,filter2")))//
                .andRespond(withSuccess().headers(headers));

        mockServer.expect(requestTo(url("/tasks/3307")))//
                .andRespond(withSuccess(payload("tasks/in-progress-task.json"), MediaType.TEXT_HTML));
        mockServer
                .expect(requestTo(url("/tasks/3307")))
                .andRespond(
                        withSuccess(payload("jobs/fetch-logs-task-complete.json"), MediaType.TEXT_HTML));
        mockServer.expect(requestTo(url("/resources/d0a1877d-3802-4922-408c-85b553d13aba")))//
                .andRespond(
                        withSuccess(new ClassPathResource("jobs/compressed-logs.gz"),
                                MediaType.TEXT_HTML));
        
        // When
        FetchLogsRequest request = new FetchLogsRequest();
        request.withDeploymentName("platform-backup");
        request.withJobName("cfbackup");
        request.withJobIndex(0);
        request.withLogType(LogType.AGENT);
        request.withFilters("filter1", "filter2");
        
        TestSubscriber<InputStream> subscriber = new TestSubscriber<InputStream>();
        jobs.fetchLogs(request).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        
        //Then
        subscriber.assertNoErrors();
        subscriber.assertCompleted();
        InputStream response = subscriber.getOnNextEvents().get(0);
        try {
            File logs = File.createTempFile("fetchLogs-", ".tgz");
            FileUtils.copyInputStreamToFile(response, logs);
            assertTrue(logs.length() > 150000);
        } catch (Exception e) {
            fail(e.getMessage());
        } 
        
        mockServer.verify();
    }

    @Test
    public void stopJob() {
        // Given
        mockServer
            .expect(requestTo(url("/deployments/cf-redis-61423dfddec885b6e28d")))//
            .andRespond(withSuccess(payload("deployments/deployment.json"), MediaType.TEXT_HTML));
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Location", "https://10.174.52.151/tasks/11319");
        mockServer
                .expect(requestTo(url("/deployments/cf-redis-61423dfddec885b6e28d/jobs/cf-redis-broker-partition-6e7ff5ae3b5fed5a7435/0?state=detached")))//
                .andRespond(withSuccess().headers(headers));

        mockServer.expect(requestTo(url("/tasks/11319")))//
                .andRespond(withSuccess(payload("jobs/11319-in-progress-task.json"), MediaType.TEXT_HTML));
        mockServer.expect(requestTo(url("/tasks/11319")))//
                .andRespond(withSuccess(payload("jobs/11319-in-progress-task.json"), MediaType.TEXT_HTML));
        mockServer.expect(requestTo(url("/tasks/11319")))//
                .andRespond(withSuccess(payload("jobs/11319-in-progress-task.json"), MediaType.TEXT_HTML));
        
        mockServer
                .expect(requestTo(url("/tasks/11319")))
                .andRespond(
                        withSuccess(payload("jobs/11319-complete.json"), MediaType.TEXT_HTML));
        
        // When
        StopJobRequest request = new StopJobRequest();
        request.withDeploymentName("cf-redis-61423dfddec885b6e28d");
        request.withJobName("cf-redis-broker-partition-6e7ff5ae3b5fed5a7435");
        request.withJobIndex(0);
        request.withPowerOffVm(true);
        
        TestSubscriber<Task> subscriber = new TestSubscriber<Task>();
        jobs.stopJob(request).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        
        //Then
        subscriber.assertNoErrors();
        subscriber.assertCompleted();
        Task response = subscriber.getOnNextEvents().get(0);
        assertThat(response.getState(), is("done"));
        
        mockServer.verify();
    }

}
