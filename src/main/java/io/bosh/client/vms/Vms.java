package io.bosh.client.vms;

import java.util.List;

import com.jcraft.jsch.Session;
import io.bosh.client.deployments.SSHConfig;
import rx.Observable;

/**
 * @author David Ehringer, Yannic Remmet.
 */
public interface Vms {

    Observable<List<VmSummary>> list(String deploymentName);
    
    Observable<List<Vm>> listDetails(String deploymentName);

    Observable<Session> ssh(SSHConfig config, String privateKey);

    Observable<Session> ssh(SSHConfig config);
}
