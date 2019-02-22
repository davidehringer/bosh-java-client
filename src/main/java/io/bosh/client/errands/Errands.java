package io.bosh.client.errands;

import java.util.List;

import io.bosh.client.tasks.Task;
import rx.Observable;

/**
 * @author David Ehringer, Yannic Remmet.
 */
public interface Errands {

    Observable<List<ErrandSummary>> list(String deploymentName);

    default Observable<Task> runErrand(String deploymentName, String errandName){
        return runErrand(deploymentName,errandName,false,false);
    }

    Observable<Task> runErrand(String deploymentName, String errandName, boolean keep_alive, boolean when_changed);
}
