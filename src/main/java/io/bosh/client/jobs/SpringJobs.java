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
package io.bosh.client.jobs;

import io.bosh.client.DirectorException;
import io.bosh.client.deployments.Deployments;
import io.bosh.client.internal.AbstractSpringOperations;
import io.bosh.client.tasks.Task;
import io.bosh.client.tasks.Tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestOperations;
import org.springframework.web.util.UriComponentsBuilder;

import rx.Observable;

/**
 * @author David Ehringer
 */
public class SpringJobs extends AbstractSpringOperations implements Jobs{

    private final Tasks tasks;
    private final Deployments deployments;
    
    public SpringJobs(RestOperations restOperations, URI root, Tasks tasks, Deployments deployments) {
        super(restOperations, root);
        this.tasks = tasks;
        this.deployments = deployments;
    }

    @Override
    public Observable<InputStream> fetchLogs(FetchLogsRequest request) {
        return getEntity(Void.class, builder -> 
                    builder.pathSegment("deployments", request.getDeploymentName())
                           .pathSegment("jobs", request.getJobName(), String.valueOf(request.getJobIndex()))
                           .pathSegment("logs")
                           .queryParam("type", request.getLogType().getType())
                           .queryParam("filters", String.join(",", request.getFilters())))
               .flatMap(response -> tasks.trackToCompletion(getTaskId(response)))
               .flatMap(task -> getGzip(builder -> builder.pathSegment("resources", task.getResult())))
               .map(file -> createInputStream(file));
    }


    private final Observable<File> getGzip(Consumer<UriComponentsBuilder> builderCallback) {
        // For responses that have a Content-Type of application/x-gzip, we need to
        // decompress them. The RestTemplate and HttpClient don't handle this for
        // us
        return createObservable(() -> {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUri(this.root);
            builderCallback.accept(builder);
            URI uri = builder.build().toUri();
            return this.restOperations.execute(uri, HttpMethod.GET, null, new ResponseExtractor<File>() {
                @Override
                public File extractData(ClientHttpResponse response) throws IOException {
                    return decompress(response.getBody());
                }
            });
        });
    }
    
    private InputStream createInputStream(File file) {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new DirectorException("Unable to create InputStream from file", e);
        }
    }

    private File decompress(InputStream compressed) {
        File file = null;
        try {
            file = File.createTempFile(UUID.randomUUID().toString(), ".log");
        } catch (IOException e) {
            throw new DirectorException("Unable to create temporary file to decompress log data", e);
        }
        try (InputStream ungzippedResponse = new GZIPInputStream(compressed);
                FileWriter writer = new FileWriter(file);
                Reader reader = new InputStreamReader(ungzippedResponse, "UTF-8")) {
            char[] buffer = new char[10240];
            for (int length = 0; (length = reader.read(buffer)) > 0;) {
                writer.write(buffer, 0, length);
            }
            return file;
        } catch (IOException e) {
            throw new DirectorException("Unable to decompress log data", e);
        }
    }
    
    private Observable<Task> changeJobState(String deploymentName, Consumer<UriComponentsBuilder> builderCallback) {
        HttpHeaders headers = new  HttpHeaders();
        headers.put("content-type",Arrays.asList("text/yaml"));
        
        return deployments.get(deploymentName)
        .flatMap(deployment -> exchangeForEntity(deployment.getRawManifest(), Void.class, headers, HttpMethod.PUT, 
                                builder -> builderCallback.accept(builder)))
        .flatMap(response -> tasks.trackToCompletion(getTaskId(response)));
    }
    
    private void buildChangeJobStateUri(String deployment, String job, Integer index, String newState, boolean skipDrain, UriComponentsBuilder builder) {
        builder.pathSegment("deployments", deployment, "jobs", job);
        if(index != null){
            builder.pathSegment(String.valueOf(index));
        }
        builder.queryParam("state", newState);
        if(skipDrain){
            builder.queryParam("skip_drain", "true");
        }
    }

    @Override
    public Observable<Task> stopJob(StopJobRequest request) {
        return changeJobState(request.getDeploymentName(), builder -> {
            String newState = "stopped";
            if(request.isPowerOffVm()){
                newState = "detached";
            }
            buildChangeJobStateUri(request.getDeploymentName(), request.getJobName(), request.getJobIndex(), newState, request.isSkipDrain(), builder);
        });
    }
    
    @Override
    public Observable<Task> startJob(StartJobRequest request) {
        return changeJobState(request.getDeploymentName(), builder -> {
            buildChangeJobStateUri(request.getDeploymentName(), request.getJobName(), request.getJobIndex(), "started", false, builder);
        });
    }

    @Override
    public Observable<Task> restartJob(RestartJobRequest request) {
        return changeJobState(request.getDeploymentName(), builder -> {
           buildChangeJobStateUri(request.getDeploymentName(), request.getJobName(), request.getJobIndex(), "restart", false, builder);
        });
    }

    @Override
    public Observable<Task> recreateJob(RecreateJobRequest request) {
        return changeJobState(request.getDeploymentName(), builder -> {
            buildChangeJobStateUri(request.getDeploymentName(), request.getJobName(), request.getJobIndex(), "recreate", false, builder);
         });
    }
}
