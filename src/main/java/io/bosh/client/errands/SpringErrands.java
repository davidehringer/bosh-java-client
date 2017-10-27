/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.bosh.client.errands;

import io.bosh.client.internal.AbstractSpringOperations;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import io.bosh.client.tasks.Task;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestOperations;

import rx.Observable;

/**
 * @author David Ehringer
 */
public class SpringErrands extends AbstractSpringOperations implements Errands {

    public SpringErrands(RestOperations restOperations, URI root) {
        super(restOperations, root);
    }

    @Override
    public Observable<List<ErrandSummary>> list(String deploymentName) {
        return get( ErrandSummary[].class, builder -> builder.pathSegment("deployments", deploymentName, "errands"))
               .map(response -> Arrays.asList(response));
    }

    public Observable<Task> runErrand(String deploymentName, String errandName, boolean keep_alive, boolean when_changed) {
        HashMap<String, Boolean> body = new HashMap();
        body.put("keep_alive", keep_alive);
        body.put("when_changed", when_changed);
        return exchangeWithTaskRedirect(body,
                                        Task.class,
                                        new HttpHeaders(),
                                        HttpMethod.POST,
                                        builder -> builder.pathSegment("deployments", deploymentName, "errands", errandName, "runs")
                                       )
                .map(HttpEntity::getBody);
    }
}
