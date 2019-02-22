package io.bosh.client.deployments;

import io.bosh.client.tasks.Task;
import org.springframework.http.HttpHeaders;
import rx.Observable;

import java.util.List;

/**
 * @author David Ehringer, Jannik Heyl.
 */
public interface Deployments {

    Observable<List<DeploymentSummary>> list();
    
    Observable<Deployment> get(String deploymentName);

    Observable<Task> create(Deployment deployment, HttpHeaders headers);

    Observable<Task> create(Deployment deployment);

    Observable<Task> update(Deployment deployment);

    Observable<Task> delete(Deployment deployment);
    
    Observable<List<Problem>> cloudcheck(String deploymentName);
}
