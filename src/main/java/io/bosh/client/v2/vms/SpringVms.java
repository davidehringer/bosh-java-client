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

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.util.UriComponentsBuilder;

import rx.Observable;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author David Ehringer
 */
public class SpringVms extends AbstractSpringOperations implements Vms {

    private final ObjectMapper mapper = new ObjectMapper();
    
    public SpringVms(RestOperations restOperations, URI root) {
        super(restOperations, root);
    }

    @Override
    public Observable<ListVmsResponse> list(ListVmsRequest request) {
        return get(Vm[].class,
                builder -> builder.pathSegment("deployments", request.getDeploymentName(), "vms"))
                .map(results -> new ListVmsResponse().withVms(Arrays.asList(results)));
    }

    @Override
    public Observable<ListVmDetailsResponse> listDetails(final ListVmDetailsRequest request) {
        return Observable.create(subscriber -> {
            try {
                URI vmsUri = UriComponentsBuilder.fromUri(this.root)
                        .pathSegment("deployments", request.getDeploymentName(), "vms")
                        .queryParam("format", "full").build().toUri();
                ResponseEntity<Void> response = this.restOperations
                        .getForEntity(vmsUri, Void.class);
                String taskId = trackTask(response);

                URI taskOutputUri = UriComponentsBuilder.fromUri(this.root)
                        .pathSegment("tasks", taskId, "output").queryParam("type", "result")
                        .build().toUri();
                String taskResult = this.restOperations.getForObject(taskOutputUri, String.class);

                // TODO refactor mess

                List<VmDetails> details = new ArrayList<VmDetails>();
                // not all deployments have running vms... for example errand
                // only deployments
                if (taskResult != null) {
                    for (String vm : taskResult.split("\n")) {
                        try {
                            details.add(mapper.readValue(vm.getBytes(), VmDetails.class));
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
                subscriber.onNext(new ListVmDetailsResponse().withVms(details));
                subscriber.onCompleted();
            } catch (HttpStatusCodeException e) {
                subscriber.onError(new DirectorException(e.getMessage(), e));
            }
        });
    }

}