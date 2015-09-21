package io.bosh.client.v2.jobs;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import io.bosh.client.AbstractDirectorTest;

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
        subscriber.assertCompleted();
        subscriber.assertNoErrors();
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

}
