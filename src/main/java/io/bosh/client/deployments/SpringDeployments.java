package io.bosh.client.deployments;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import io.bosh.client.DirectorException;
import io.bosh.client.internal.AbstractSpringOperations;
import io.bosh.client.tasks.Task;
import io.bosh.client.tasks.Tasks;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import rx.Observable;

import java.io.IOException;
import java.net.URI;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author David Ehringer, Jannik Heyl, Johannes Hiemer.
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
                   if(response.getManifest() != null) {
                       ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                       Map manifestMap = null;
                       try {
                           manifestMap = mapper.readValue(response.getRawManifest(), Map.class);
                       } catch (IOException e) {
                           throw new DirectorException("Unable to parse deployment manifest", e);
                       }
                       response.setManifestMap(manifestMap);
                   }
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
        return exchangeWithTaskRedirect(deployment.getRawManifest(),
                                        Task.class,
                                        headers,
                                        HttpMethod.POST,
                    builder -> builder.path("deployments"))
                .map(exchange -> exchange.getBody());
    }

    @Override
    public Observable<Task> delete(Deployment deployment) {
        return exchangeWithTaskRedirect("", Task.class, null, HttpMethod.DELETE,
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

}
