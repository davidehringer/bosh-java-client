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
package io.bosh.client.deployments;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.bosh.client.DirectorException;
import io.bosh.client.internal.AbstractSpringOperations;
import io.bosh.client.tasks.Task;
import io.bosh.client.tasks.Tasks;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import rx.Observable;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author David Ehringer
 */
public class SpringDeployments extends AbstractSpringOperations implements Deployments {

    private final Tasks tasks;
    
    public SpringDeployments(RestTemplate restTemplate, URI root, Tasks tasks) {
        super(restTemplate, root);
        restTemplate.getRequestFactory();
        this.tasks = tasks;
    }

    @Override
    public Observable<List<DeploymentSummary>> list() {
        return get(DeploymentSummary[].class, 
                builder -> builder.pathSegment("deployments"))
               .map(results -> Arrays.asList(results));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Observable<Deployment> get(String deploymentName) {
        return get(Deployment.class, 
                   builder -> builder.pathSegment("deployments", deploymentName))
               .map(response -> {
                   response.setName(deploymentName);

                   ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                   Map manifestMap = null;
                   try {
                       manifestMap = mapper.readValue(response.getRawManifest(), Map.class);
                   } catch (IOException e) {
                       throw new DirectorException("Unable to parse deployment manifest", e);
                   }
                   response.setManifestMap(manifestMap);
                   return response;
               });
    }

    @Override
    public Observable<Task> create(Deployment deployment) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "text/yaml");
        return create(deployment, headers);
    }

    public Observable<Task> update(Deployment deployment) {
        return create(deployment);
    }

    @Override
    public Observable<Task> create(Deployment deployment, HttpHeaders headers) {
        return exchange(deployment.getRawManifest(),
                                 Task.class,
                                 headers,
                                 HttpMethod.POST,
                    builder -> builder.path("deployments"))
                .map(exchange -> exchange.getBody());
    }

    @Override
    public Observable<Task> delete(Deployment deployment) {
        return exchangeForEntity("", Task.class, null, HttpMethod.DELETE,
                builder -> builder.pathSegment("deployments", deployment.getName()))
                .map(exchange -> exchange.getBody());
    }

    @Override
    public Observable<List<Problem>> cloudcheck(String deploymentName) {
        return postForEntity(Void.class, null, builder -> builder.pathSegment("deployments", deploymentName, "scans"))
                .flatMap(response -> tasks.trackToCompletion(getTaskId(response)))
                .flatMap(task -> get(Problem[].class, builder -> builder.pathSegment("deployments", deploymentName, "problems")))
                .map(problems -> Arrays.asList(problems));
    }

    protected final <T, R> Observable<ResponseEntity<R>> exchange(T request,
                                                                           Class<R> responseType, HttpHeaders headers, HttpMethod method, Consumer<UriComponentsBuilder> builderCallback) {
        return super.exchangeForEntity(request,responseType,headers,method,builderCallback).map(r -> {
            String taskId = getTaskId(r);
            return getEntity(responseType, builder -> builder.pathSegment("tasks", taskId)).toBlocking().first();
        });
    }
}
