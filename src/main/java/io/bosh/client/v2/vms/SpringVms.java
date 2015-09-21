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
package io.bosh.client.v2.vms;

import io.bosh.client.v2.DirectorException;
import io.bosh.client.v2.internal.AbstractSpringOperations;
import io.bosh.client.v2.tasks.Tasks;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.web.client.RestOperations;

import rx.Observable;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author David Ehringer
 */
public class SpringVms extends AbstractSpringOperations implements Vms {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Tasks tasks;
    
    public SpringVms(RestOperations restOperations, URI root, Tasks tasks) {
        super(restOperations, root);
        this.tasks = tasks;
    }

    @Override
    public Observable<ListVmsResponse> list(ListVmsRequest request) {
        return get(Vm[].class,
                builder -> builder.pathSegment("deployments", request.getDeploymentName(), "vms"))
                .map(results -> new ListVmsResponse().withVms(Arrays.asList(results)));
    }
    
    @Override
    public Observable<ListVmDetailsResponse> listDetails(final ListVmDetailsRequest request) {
        return getEntity(Void.class, builder -> builder.pathSegment("deployments", request.getDeploymentName(), "vms")
                        .queryParam("format", "full"))
            .flatMap(response -> tasks.trackToCompletion(getTaskId(response)))
            .flatMap(task -> get(String.class, builder -> builder.pathSegment("tasks", task.getId(), "output")
                                                           .queryParam("type", "result")))
            .filter(rawDetails -> rawDetails != null)
            .map(rawDetails -> rawDetails.split("\n"))
            .map(rawDetails -> {
                List<VmDetails> details = new ArrayList<VmDetails>();
                for (String vm : rawDetails) {
                    try {
                        details.add(mapper.readValue(vm.getBytes(), VmDetails.class));
                    } catch (IOException e) {
                        throw new DirectorException("Unable to read VM data into VmDetails: " + vm, e);
                    }
                }
                return details;
             })
             .map(vms -> new ListVmDetailsResponse().withVms(vms));
    }

}