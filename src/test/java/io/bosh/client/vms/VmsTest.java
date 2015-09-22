package io.bosh.client.vms;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import io.bosh.client.AbstractDirectorTest;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import rx.observers.TestSubscriber;

/**
 * @author David Ehringer
 */
public class VmsTest extends AbstractDirectorTest{

    private Vms vms;

    @Before
    public void setup(){
        vms = client.vms();
    }

    @Test
    public void list() {
        // Given
        mockServer.expect(requestTo(url("/deployments/example/vms")))//
                .andRespond(withSuccess(payload("vms/vms.json"), MediaType.TEXT_HTML));
        // When
        vms.list("example").subscribe(vmList -> {
            // Then
            assertThat(vmList.size(), is(1));
            assertThat(vmList.get(0).getAgentId(), is("30096cdf-d96b-4c73-a9d0-55b50c6ce37d"));
            assertThat(vmList.get(0).getCid(), is("5585315f-8280-4de6-6694-9b6ab2f21393"));
            assertThat(vmList.get(0).getIndex(), is(0));
            assertThat(vmList.get(0).getJob(), is("cfbackup"));
        });
    }

    @Test
    public void listDetails() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.set("Location", "https://10.174.52.151/tasks/3307");
        mockServer.expect(requestTo(url("/deployments/example/vms?format=full")))//
                .andRespond(withSuccess().headers(headers));

        mockServer.expect(requestTo(url("/tasks/3307")))//
                .andRespond(withSuccess(payload("tasks/in-progress-task.json"), MediaType.TEXT_HTML));
        mockServer
                .expect(requestTo(url("/tasks/3307")))
                .andRespond(
                        withSuccess(payload("vms/retrieve-vm-stats-complete-task.json"), MediaType.TEXT_HTML));
        mockServer
                .expect(requestTo(url("/tasks/3307/output?type=result")))
                .andRespond(
                        withSuccess(payload("vms/vms-full.json"), MediaType.TEXT_HTML));
        
        // When
        TestSubscriber<List<Vm>> subscriber = new TestSubscriber<List<Vm>>();
        vms.listDetails("example").subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        // Then
        subscriber.assertNoErrors();
        subscriber.assertCompleted();
        List<Vm> details = subscriber.getOnNextEvents().get(0);
        
        // Then
        assertThat(details.size(), is(2));
        
        assertThat(details.get(0).getVmCid(), is("vm-b1f8dfec-ecc3-44e8-8892-ea6cdcb75571"));
        assertThat(details.get(0).getIps().size(), is(1));
        assertThat(details.get(0).getIps().get(0), is("10.174.52.204"));
        assertThat(details.get(0).getDns().size(), is(1));
        assertThat(details.get(0).getDns().get(0), is("0.syslog-configurator-partition-6e7ff5ae3b5fed5a7435.default.cf-redis-61423dfddec885b6e28d.microbosh"));
        assertThat(details.get(0).getAgentId(), is("bb7ffdfa-d102-44bc-a4c1-b048d121a814"));
        assertThat(details.get(0).getIndex(), is(0));
        assertThat(details.get(0).getJobState(), is("running"));
        assertThat(details.get(0).getResourcePool(), is("syslog-configurator-partition-6e7ff5ae3b5fed5a7435"));
        assertThat(details.get(0).getVitals().getCpu().getSys(), is("0.1"));
        assertThat(details.get(0).getVitals().getCpu().getUser(), is("0.0"));
        assertThat(details.get(0).getVitals().getCpu().getWait(), is("0.2"));
        assertThat(details.get(0).getVitals().getDisk().getEphemeral().getInodePercent(), is(0));
        assertThat(details.get(0).getVitals().getDisk().getEphemeral().getPercent(), is(1));
        assertNull(details.get(0).getVitals().getDisk().getPersistent());
        assertThat(details.get(0).getVitals().getDisk().getSystem().getInodePercent(), is(36));
        assertThat(details.get(0).getVitals().getDisk().getSystem().getPercent(), is(49));
        assertThat(details.get(0).getVitals().getLoad().size(), is(3));
        assertThat(details.get(0).getVitals().getLoad().get(0), is("0.00"));
        assertThat(details.get(0).getVitals().getLoad().get(1), is("0.01"));
        assertThat(details.get(0).getVitals().getLoad().get(2), is("0.05"));
        assertThat(details.get(0).getVitals().getMem().getKb(), is(103976));
        assertThat(details.get(0).getVitals().getMem().getPercent(), is(5));
        assertThat(details.get(0).getVitals().getSwap().getKb(), is(1));
        assertThat(details.get(0).getVitals().getSwap().getPercent(), is(0));
        assertFalse(details.get(0).isResurrectionPaused());

        assertThat(details.get(1).getVmCid(), is("vm-82e4a7af-82e4-4bea-8f0f-db53c9d49f79"));
        assertTrue(details.get(1).isResurrectionPaused());
        assertThat(details.get(1).getVitals().getDisk().getPersistent().getInodePercent(), is(37));
        assertThat(details.get(1).getVitals().getDisk().getPersistent().getPercent(), is(50));        

        mockServer.verify();
    }

    // TODO test listDetails for errand only deployment
}
