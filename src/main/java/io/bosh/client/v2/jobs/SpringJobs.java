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
package io.bosh.client.v2.jobs;

import io.bosh.client.v2.DirectorException;
import io.bosh.client.v2.internal.AbstractSpringOperations;
import io.bosh.client.v2.tasks.Tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

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
    
    public SpringJobs(RestOperations restOperations, URI root, Tasks tasks) {
        super(restOperations, root);
        this.tasks = tasks;
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
                 .flatMap(task -> {
                   System.out.println("task: " + task.getResult());
                   return  getGzip(builder -> builder.pathSegment("resources", task.getResult()));
                 })
                 .map(file -> {
                     System.out.println("file: " + file);
                     return createInputStream(file);
                 });
    }


    private final Observable<File> getGzip(Consumer<UriComponentsBuilder> builderCallback) {
        // For responses that have a Content-Type of application/x-gzip, we need to
        // decompress them. The RestTemplate and HttpClient don't handle this for
        // us
        return exchange(() -> {
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

}
