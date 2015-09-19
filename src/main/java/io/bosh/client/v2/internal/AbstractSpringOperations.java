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
package io.bosh.client.v2.internal;

import io.bosh.client.v2.DirectorException;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.util.UriComponentsBuilder;

import rx.Observable;

/**
 * @author David Ehringer
 */
public abstract class AbstractSpringOperations {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final int TASK_TRACKING_POLL_INTERVAL = 1000;
    private static final List<String> COMPLETED_STATES = Arrays
            .asList("done", "error", "cancelled");

    protected final RestOperations restOperations;
    protected final URI root;

    protected AbstractSpringOperations(RestOperations restOperations, URI root) {
        this.restOperations = restOperations;
        this.root = root;
    }

    protected final <T> Observable<T> get(Class<T> responseType,
            Consumer<UriComponentsBuilder> builderCallback) {
        return exchange(() -> {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUri(this.root);
            builderCallback.accept(builder);
            URI uri = builder.build().toUri();

            this.logger.debug("GET {}", uri);
            return this.restOperations.getForObject(uri, responseType);
        });
    }

    protected final <T> Observable<ResponseEntity<T>> getEntity(Class<T> responseType,
            Consumer<UriComponentsBuilder> builderCallback) {
        return exchange(() -> {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUri(this.root);
            builderCallback.accept(builder);
            URI uri = builder.build().toUri();

            this.logger.debug("GET {}", uri);
            return this.restOperations.getForEntity(uri, responseType);
        });
    }
    
    protected final <T> Observable<T> post(Class<T> responseType, Object request,
            Consumer<UriComponentsBuilder> builderCallback) {
        return exchange(() -> {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUri(this.root);
            builderCallback.accept(builder);
            URI uri = builder.build().toUri();

            this.logger.debug("GET {}", uri);
            return this.restOperations.postForObject(uri, request, responseType);
        });
    }
    
    protected final <T> Observable<ResponseEntity<T>> postForEntity(Class<T> responseType, Object request,
            Consumer<UriComponentsBuilder> builderCallback) {
        return exchange(() -> {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUri(this.root);
            builderCallback.accept(builder);
            URI uri = builder.build().toUri();

            this.logger.debug("GET {}", uri);
            return this.restOperations.postForEntity(uri, request, responseType);
        });
    }

    protected final <T> Observable<T> exchange(Supplier<T> exchange) {
        return Observable.create(subscriber -> {

            try {
                subscriber.onNext(exchange.get());
                subscriber.onCompleted();
            } catch (HttpStatusCodeException e) {
                 subscriber.onError(new DirectorException(e.getMessage(), e));
                // TODO
            }
        });
    }

    @SuppressWarnings("unchecked")
    protected String trackTask(ResponseEntity<?> response) {
        // TODO assert redirect
        // https://10.174.52.151/tasks/3307
        Pattern pattern = Pattern.compile(".*/tasks/(.*)$");
        Matcher matcher = pattern.matcher(response.getHeaders().getLocation().toString());
        if (matcher.matches()) {
            String taskId = matcher.group(1);
            logger.debug("Tracking task {}", taskId);
            URI taskUri = UriComponentsBuilder.fromUri(this.root).pathSegment("tasks", taskId)
                    .build().toUri();
            String state = "unknown";
            while (inProgress(state)) {
                // TODO put in a max polls?
                try {
                    Thread.sleep(TASK_TRACKING_POLL_INTERVAL);
                } catch (InterruptedException e) {
                }
                Map<String, String> result = this.restOperations.getForObject(taskUri, Map.class);
                state = result.get("state");
                logger.debug("Task {}: state = {}", taskId, state);
            }
            // TODO how to handle error states?
            return taskId;
        }
        // TODO how to handle error states?
        return null;
    }

    private boolean inProgress(String state) {
        return !COMPLETED_STATES.contains(state);
    }
}
