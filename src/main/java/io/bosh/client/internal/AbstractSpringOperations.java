package io.bosh.client.internal;

import io.bosh.client.DirectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.util.UriComponentsBuilder;
import rx.Observable;

import java.net.URI;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author David Ehringer, Yannic Remmet.
 */
public abstract class AbstractSpringOperations {

    private final Logger logger = LoggerFactory.getLogger("BOSH_DIRECTOR_API");

    protected final RestOperations restOperations;
    protected final URI root;

    protected AbstractSpringOperations(RestOperations restOperations, URI root) {
        this.restOperations = restOperations;
        this.root = root;
    }

    protected final <T> Observable<T> get(Class<T> responseType,
            Consumer<UriComponentsBuilder> builderCallback) {
        return createObservable(() -> {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUri(this.root);
            builderCallback.accept(builder);
            URI uri = builder.build().toUri();

            this.logger.debug("GET {}", uri);
            return this.restOperations.getForObject(uri, responseType);
        });
    }

    protected final <T> Observable<ResponseEntity<T>> getEntity(Class<T> responseType,
            Consumer<UriComponentsBuilder> builderCallback) {
        return createObservable(() -> {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUri(this.root);
            builderCallback.accept(builder);
            URI uri = builder.build().toUri();

            this.logger.debug("GET {}", uri);
            return this.restOperations.getForEntity(uri, responseType);
        });
    }

    protected final <T> Observable<T> post(Class<T> responseType, Object request,
            Consumer<UriComponentsBuilder> builderCallback) {
        return createObservable(() -> {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUri(this.root);
            builderCallback.accept(builder);
            URI uri = builder.build().toUri();

            this.logger.debug("POST {}", uri);
            return this.restOperations.postForObject(uri, request, responseType);
        });
    }

    protected final <T> Observable<ResponseEntity<T>> postForEntity(Class<T> responseType,
            Object request, Consumer<UriComponentsBuilder> builderCallback) {
        return createObservable(() -> {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUri(this.root);
            builderCallback.accept(builder);
            URI uri = builder.build().toUri();

            this.logger.debug("POST {}", uri);
            return this.restOperations.postForEntity(uri, request, responseType);
        });
    }

    protected final <T, R> Observable<ResponseEntity<R>> exchangeForEntity(T request,
            Class<R> responseType, HttpHeaders headers, HttpMethod method, Consumer<UriComponentsBuilder> builderCallback) {
        return createObservable(() -> {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUri(this.root);
            builderCallback.accept(builder);
            URI uri = builder.build().toUri();

            RequestEntity<T> requestEntity = new RequestEntity<T>(request, headers, method, uri);
            this.logger.debug("{} {}", method, uri);
            return this.restOperations.exchange( requestEntity, responseType);
        });
    }

    protected final <T> Observable<T> createObservable(Supplier<T> exchange) {
        return Observable.create(subscriber -> {

            try {
                subscriber.onNext(exchange.get());
                subscriber.onCompleted();
            } catch (HttpStatusCodeException e) {
                subscriber.onError(new DirectorException(e.getMessage(), e));
            }
        });
    }

    protected String getTaskId(ResponseEntity<?> response) {
        // https://10.174.52.151/tasks/3307
        Pattern pattern = Pattern.compile(".*/tasks/(.*)$");
        Matcher matcher = pattern.matcher(response.getHeaders().getLocation().toString());
        if (matcher.matches()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("Response does not have a redirect header for a task");
    }

    protected final <T, R> Observable<ResponseEntity<R>> exchangeWithTaskRedirect (T request,
                                                                                   Class<R> responseType, HttpHeaders headers, HttpMethod method, Consumer<UriComponentsBuilder> builderCallback) {
        return exchangeForEntity(request,responseType,headers,method,builderCallback).map(r -> {
            String taskId = getTaskId(r);
            return getEntity(responseType, builder -> builder.pathSegment("tasks", taskId)).toBlocking().first();
        });
    }
}
