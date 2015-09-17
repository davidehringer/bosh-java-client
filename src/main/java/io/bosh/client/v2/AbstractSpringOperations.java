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
package io.bosh.client.v2;

import java.net.URI;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.util.UriComponentsBuilder;

import rx.Observable;

/**
 * @author David Ehringer
 */
public abstract class AbstractSpringOperations {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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

    /**
     * A specialize GET method that can convert the BOSH Director responses that are a list of JSON responses (rather than a JSON array).
     * 
     * @param responseType the response type of the method
     * @param builderCallback a callback for configuring the URI
     * @param apiType the type that is returned from the API calls
     * @param mapper a function to convert the apiType to the responseType
     * @return
     */
    protected final <T,R> Observable<T> getArray(Class<T> responseType,
            Consumer<UriComponentsBuilder> builderCallback, Class<R> apiType, Function <R, T> mapper) {
        return exchange(() -> {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUri(this.root);
            builderCallback.accept(builder);
            URI uri = builder.build().toUri();

            this.logger.debug("GET {}", uri);
            R response = this.restOperations.getForObject(uri, apiType);
            return mapper.apply(response);
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

}
