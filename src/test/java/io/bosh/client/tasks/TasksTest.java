package io.bosh.client.tasks;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import io.bosh.client.AbstractDirectorTest;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;

import rx.observers.TestSubscriber;

/**
 * @author David Ehringer
 */
public class TasksTest extends AbstractDirectorTest{

    private Tasks tasks;

    @Before
    public void setup(){
        tasks = client.tasks();
    }

    @Test
    public void listRunning() {
        // Given
        mockServer.expect(requestTo(url("/tasks?state=processing,cancelling,queued")))//
                .andRespond(withSuccess(payload("tasks/running-tasks.json"), MediaType.TEXT_HTML));
        // When
        tasks.listRunning().subscribe(taskList -> {
            // Then
            assertThat(taskList.size(), is(1));
            assertThat(taskList.get(0).getId(), is("68"));
            assertThat(taskList.get(0).getDescription(), is("fetch logs"));
        });
    }

    @Test
    public void listRecent() {
        // Given
        mockServer.expect(requestTo(url("/tasks?limit=30&verbose=1")))//
                .andRespond(withSuccess(payload("tasks/recent-tasks.json"), MediaType.TEXT_HTML));
        // When
        tasks.listRecent().subscribe(taskList -> {
            // Then
            assertThat(taskList.size(), is(18));
        });
    }

    @Test
    public void listRecentWithCount() {
        // Given
        mockServer.expect(requestTo(url("/tasks?limit=50&verbose=1")))//
                .andRespond(withSuccess(payload("tasks/recent-tasks.json"), MediaType.TEXT_HTML));
        // When
        tasks.listRecent(50).subscribe();
    }

    @Test
    public void get() {
        // Given
        mockServer.expect(requestTo(url("/tasks/68")))//
                .andRespond(withSuccess(payload("tasks/in-progress-task.json"), MediaType.TEXT_HTML));
        // When
        tasks.get("68").subscribe(response -> {
            // Then
            assertThat(response.getId(), is("68"));
            assertThat(response.getState(), is("processing"));
            assertThat(response.getTimestamp(), is("1437532622"));
            assertThat(response.getDescription(), is("fetch logs"));
            assertThat(response.getUser(), is("admin"));
        });
    }
    

    @Test
    public void trackToCompletion() {
        // Given
        mockServer.expect(requestTo(url("/tasks/68")))//
                .andRespond(withSuccess(payload("tasks/in-progress-task.json"), MediaType.TEXT_HTML));
        mockServer.expect(requestTo(url("/tasks/68")))//
                .andRespond(withSuccess(payload("tasks/in-progress-task.json"), MediaType.TEXT_HTML));
        mockServer.expect(requestTo(url("/tasks/68")))//
                .andRespond(withSuccess(payload("tasks/in-progress-task.json"), MediaType.TEXT_HTML));
        mockServer.expect(requestTo(url("/tasks/68")))//
                .andRespond(withSuccess(payload("tasks/in-progress-task.json"), MediaType.TEXT_HTML));
        mockServer.expect(requestTo(url("/tasks/68")))//
                .andRespond(withSuccess(payload("tasks/done-task.json"), MediaType.TEXT_HTML));
        // When
        TestSubscriber<Task> subscriber = new TestSubscriber<Task>();
        tasks.trackToCompletion("68").subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        
        //Then
        subscriber.assertCompleted();
        subscriber.assertNoErrors();
        Task task = subscriber.getOnNextEvents().get(0);
        assertThat(task.getId(), is("68"));
        assertThat(task.getState(), is("done"));
        assertThat(task.getTimestamp(), is("14375326235"));
        assertThat(task.getDescription(), is("fetch logs"));
        assertThat(task.getUser(), is("admin"));
        assertThat(task.getResult(), is("task is done"));     
        
        mockServer.verify(); 
    }

}
