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
package io.bosh.client;

import static org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter.DEFAULT_CHARSET;
import io.bosh.client.domain.DeploymentDetails;
import io.bosh.client.domain.ErrandSummary;
import io.bosh.client.domain.LogType;
import io.bosh.client.domain.Problem;
import io.bosh.client.domain.Release;
import io.bosh.client.domain.Task;
import io.bosh.client.domain.Vm;
import io.bosh.client.domain.VmDetails;
import io.bosh.client.v2.deployments.Deployment;
import io.bosh.client.v2.info.DirectorInfo;
import io.bosh.client.v2.releases.GetReleaseResponse;
import io.bosh.client.v2.stemcells.StemcellDetails;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLContext;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * @author David Ehringer
 */
public class DirectorClient implements DirectorOperations {

    private static final int DEFAULT_RECENT_TASK_COUNT = 30;
    private static final int TASK_TRACKING_POLL_INTERVAL = 1000;

    private static final Logger LOG = LoggerFactory.getLogger(DirectorClient.class);

    private static final List<String> COMPLETED_STATES = Arrays
            .asList("done", "error", "cancelled");

    private final URI root;
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    public DirectorClient(String host, String username, String password)
            throws GeneralSecurityException {
        this.root = UriComponentsBuilder.newInstance().scheme("https").host(host).port(25555)
                .build().toUri();
        this.restTemplate = new RestTemplate(createRequestFactory(host, username, password));
        this.restTemplate.getInterceptors().add(new ContentTypeClientHttpRequestInterceptor());
        handleTextHtmlResponses(restTemplate);
    }

    DirectorClient(String host, RestTemplate restTemplate) {
        this.root = UriComponentsBuilder.newInstance().scheme("https").host(host).port(25555)
                .build().toUri();
        this.restTemplate = restTemplate;
        this.restTemplate.getInterceptors().add(new ContentTypeClientHttpRequestInterceptor());
        handleTextHtmlResponses(restTemplate);
    }

    private ClientHttpRequestFactory createRequestFactory(String host, String username,
            String password) throws GeneralSecurityException {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(new AuthScope(host, 25555),
                new UsernamePasswordCredentials(username, password));

        SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial(null, new TrustSelfSignedStrategy()).useTLS().build();

        SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext,
                new AllowAllHostnameVerifier());

        // disabling redirect handling is critical for the way BOSH uses 302's
        HttpClient httpClient = HttpClientBuilder.create().disableRedirectHandling()
                .setDefaultCredentialsProvider(credentialsProvider)
                .setSSLSocketFactory(connectionFactory).build();

        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }

    private void handleTextHtmlResponses(RestTemplate restTemplate) {
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
        messageConverters.add(new StringHttpMessageConverter());
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        messageConverter.setSupportedMediaTypes(Arrays.asList(new MediaType("application", "json",
                DEFAULT_CHARSET), new MediaType("application", "*+json", DEFAULT_CHARSET),
                new MediaType("text", "html", DEFAULT_CHARSET)));
        messageConverters.add(messageConverter);
        restTemplate.setMessageConverters(messageConverters);
    }

    public URI getRoot() {
        return root;
    }

    @Override
    public DirectorInfo getInfo() {
        LOG.debug("Getting Director info");
        URI uri = UriComponentsBuilder.fromUri(this.root).pathSegment("info").build().toUri();
        return restTemplate.getForObject(uri, DirectorInfo.class);
    }

    @Override
    public List<Deployment> getDeployments() {
        LOG.debug("Getting all Deployments");
        URI uri = UriComponentsBuilder.fromUri(this.root).pathSegment("deployments").build()
                .toUri();
        Deployment[] results = restTemplate.getForObject(uri, Deployment[].class);
        return Arrays.asList(results);
    }

    @Override
    public List<Release> getReleases() {
        LOG.debug("Getting all Releases");
        URI uri = UriComponentsBuilder.fromUri(this.root).pathSegment("releases").build().toUri();
        Release[] results = restTemplate.getForObject(uri, Release[].class);
        return Arrays.asList(results);
    }

    @Override
    public List<StemcellDetails> getStemcells() {
        LOG.debug("Getting all Stemcells");
        URI uri = UriComponentsBuilder.fromUri(this.root).pathSegment("stemcells").build().toUri();
        StemcellDetails[] results = restTemplate.getForObject(uri, StemcellDetails[].class);
        return Arrays.asList(results);
    }

    @Override
    public GetReleaseResponse getRelease(String name) {
        LOG.debug("Getting Release '{}'", name);
        URI uri = UriComponentsBuilder.fromUri(this.root).pathSegment("releases", name).build()
                .toUri();
        GetReleaseResponse release = restTemplate.getForObject(uri, GetReleaseResponse.class);
        release.setName(name);
        return release;
    }

    @Override
    public DeploymentDetails getDeployment(String name) {
        LOG.debug("Getting Deployment '{}'", name);
        URI uri = UriComponentsBuilder.fromUri(this.root).pathSegment("deployments", name).build()
                .toUri();
        DeploymentDetails deployment = restTemplate.getForObject(uri, DeploymentDetails.class);
        deployment.setName(name);

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Map manifestMap = null;
        try {
            manifestMap = mapper.readValue(deployment.getRawManifest(), Map.class);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        deployment.setManifestMap(manifestMap);
        return deployment;
    }

    @Override
    public List<Vm> getVms(String deploymentName) {
        LOG.debug("Getting VMs for Deployment '{}'", deploymentName);
        URI uri = UriComponentsBuilder.fromUri(this.root)
                .pathSegment("deployments", deploymentName, "vms").build().toUri();
        Vm[] results = restTemplate.getForObject(uri, Vm[].class);
        return Arrays.asList(results);
    }

    @Override
    public List<VmDetails> getVmDetails(String deploymentName) {
        LOG.debug("Getting VM details for Deployment '{}'", deploymentName);
        URI vmsUri = UriComponentsBuilder.fromUri(this.root)
                .pathSegment("deployments", deploymentName, "vms").queryParam("format", "full")
                .build().toUri();
        ResponseEntity<Void> response = this.restTemplate.getForEntity(vmsUri, Void.class);
        String taskId = trackTask(response);

        URI taskOutputUri = UriComponentsBuilder.fromUri(this.root)
                .pathSegment("tasks", taskId, "output").queryParam("type", "result").build()
                .toUri();
        String taskResult = this.restTemplate.getForObject(taskOutputUri, String.class);

        // TODO refactor mess

        List<VmDetails> details = new ArrayList<VmDetails>();
        // not all deployments have running vms... for example errand only
        // deployments
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
        return details;
    }

    @SuppressWarnings("unchecked")
    private String trackTask(ResponseEntity<?> response) {
        // TODO assert redirect
        // https://10.174.52.151/tasks/3307
        Pattern pattern = Pattern.compile(".*/tasks/(.*)$");
        Matcher matcher = pattern.matcher(response.getHeaders().getLocation().toString());
        if (matcher.matches()) {
            String taskId = matcher.group(1);
            LOG.debug("Tracking task {}", taskId);
            URI taskUri = UriComponentsBuilder.fromUri(this.root).pathSegment("tasks", taskId)
                    .build().toUri();
            String state = "unknown";
            while (inProgress(state)) {
                // TODO put in a max polls?
                try {
                    Thread.sleep(TASK_TRACKING_POLL_INTERVAL);
                } catch (InterruptedException e) {
                }
                Map<String, String> result = this.restTemplate.getForObject(taskUri, Map.class);
                state = result.get("state");
                LOG.debug("Task {}: state = {}", taskId, state);
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

    @Override
    public List<Problem> getProblems(String deploymentName) {
        LOG.debug("Getting Problems for Deploment '{}'", deploymentName);
        URI uri = UriComponentsBuilder.fromUri(this.root)
                .pathSegment("deployments", deploymentName, "problems").build().toUri();
        Problem[] results = restTemplate.getForObject(uri, Problem[].class);
        return Arrays.asList(results);
    }

    @Override
    public List<Problem> performCloudScan(String deploymentName) {
        LOG.debug("Performing Cloud Scan on Deploment '{}'", deploymentName);
        URI vmsUri = UriComponentsBuilder.fromUri(this.root)
                .pathSegment("deployments", deploymentName, "scans").build().toUri();
        ResponseEntity<Void> response = this.restTemplate.postForEntity(vmsUri, null, Void.class);
        trackTask(response);

        return getProblems(deploymentName);
    }

    @Override
    public List<ErrandSummary> getErrands(String deploymentName) {
        LOG.debug("Getting Errands for Deploment '{}'", deploymentName);
        URI uri = UriComponentsBuilder.fromUri(this.root)
                .pathSegment("deployments", deploymentName, "errands").build().toUri();
        ErrandSummary[] results = restTemplate.getForObject(uri, ErrandSummary[].class);
        return Arrays.asList(results);
    }

    @Override
    public List<Task> getRunningTasks() {
        LOG.debug("Getting running Tasks");
        URI uri = UriComponentsBuilder.fromUri(this.root).pathSegment("tasks")
                .queryParam("state", "processing,cancelling,queued").build().toUri();
        Task[] results = restTemplate.getForObject(uri, Task[].class);
        return Arrays.asList(results);
    }

    @Override
    public List<Task> getRecentTasks() {
        return getRecentTasks(DEFAULT_RECENT_TASK_COUNT);
    }

    @Override
    public List<Task> getRecentTasks(int count) {
        LOG.debug("Getting the {} most recent Tasks", count);
        URI uri = UriComponentsBuilder.fromUri(this.root).pathSegment("tasks")
                .queryParam("limit", count).queryParam("verbose", 1).build().toUri();
        Task[] results = restTemplate.getForObject(uri, Task[].class);
        return Arrays.asList(results);
    }

    @Override
    public InputStream fetchLogs(String deploymentName, String jobName, int jobIndex,
            LogType logType, String... filters) {
        LOG.debug("Fetching {} logs for {}/{}/{}", logType, deploymentName, jobName, jobIndex);
        URI vmsUri = UriComponentsBuilder
                .fromUri(this.root)
                .pathSegment("deployments", deploymentName, "jobs", jobName,
                        String.valueOf(jobIndex), "logs").queryParam("type", logType.getType())
                .queryParam("filters", String.join(",", filters)).build().toUri();
        ResponseEntity<Void> response = this.restTemplate.getForEntity(vmsUri, Void.class);
        String taskId = trackTask(response);
        // TODO below is a quick hack to get this working. Revisit
        File file = writeToFile(downloadResource(getTask(taskId).getResult()));
        return createInputStream(file);
    }

    private InputStream createInputStream(File file) {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private File writeToFile(String content) {
        String name = UUID.randomUUID().toString();
        try {
            File file = File.createTempFile(name, ".tgz");
            Files.write(Paths.get(file.getPath()), content.getBytes());
            return file;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private String decompress(InputStream compressed) {
        // TODO Use a FileWriter to avoid entire file being in memory
        try (InputStream ungzippedResponse = new GZIPInputStream(compressed);
                StringWriter writer = new StringWriter();
                Reader reader = new InputStreamReader(ungzippedResponse, "UTF-8")) {
            char[] buffer = new char[10240];
            for (int length = 0; (length = reader.read(buffer)) > 0;) {
                writer.write(buffer, 0, length);
            }
            return writer.toString();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Task getTask(String id) {
        LOG.debug("Getting Task {}", id);
        URI uri = UriComponentsBuilder.fromUri(this.root).pathSegment("tasks", id).build().toUri();
        return restTemplate.getForObject(uri, Task.class);
    }

    public String downloadResource(String id) {
        LOG.debug("Downloading resource {}", id);
        URI uri = UriComponentsBuilder.fromUri(this.root).pathSegment("resources", id).build()
                .toUri();
        // Response has a Content-Type of application/x-gzip so we need to
        // decompress it. The RestTemplate and HttpClient don't handle this for
        // us
        String r = restTemplate.execute(uri, HttpMethod.GET, null, new ResponseExtractor<String>() {

            @Override
            public String extractData(ClientHttpResponse response) throws IOException {
                return decompress(response.getBody());
            }
        });
        return r;
    }

    public static void main(String args[]) throws GeneralSecurityException, IOException {
        DirectorOperations director = new DirectorClient("192.168.50.4", "admin", "admin");
         System.out.println(director.performCloudScan("platform-backup"));
        System.out.println(director.getDeployments());
        System.out.println(director.getRecentTasks());
         System.out.println(director.getTask("18"));
        InputStream is = director.fetchLogs("platform-backup", "cfbackup", 0, LogType.AGENT);
        Files.copy(is, Paths.get("./test.tgz"));
         System.out.println(director.getDeployment("platform-backup").getRawManifest());
        // System.out.println(director.getDeployment("platform-backup").getManifest().get("name"));

    }

    @Override
    public void stopJob(String deploymentName, String jobName, boolean powerOffVm) {
        // TODO Auto-generated method stub
        DeploymentDetails deployment = getDeployment(deploymentName);
        // assert deployment exists, assert job exists?
        
        String manifest = deployment.getRawManifest();
        
//        url = "/deployments/#{deployment_name}/jobs/#{job_name}"
//                url += "/#{index}" if index
//                url += "?state=#{new_state}"
//                url += "&skip_drain=true" if skip_drain
        LOG.debug("Stopping job '{}' in deployment '{}'", jobName, deploymentName);
        UriComponentsBuilder uriBuilder = 
       UriComponentsBuilder.fromUri(this.root)
                .pathSegment("deployments", deploymentName, "jobs", jobName);
//        if(index != null){
//            uriBuilder.pathSegment(index);
//        }
        if(powerOffVm){
            uriBuilder.queryParam("state", "stopped");
        }else{
            uriBuilder.queryParam("state", "detached");
        }
//        if(skipDrain){
//            uriBuilder.queryParam("skip_drain", "true");
//        }
        URI uri = uriBuilder.build().toUri();
        MultiValueMap<String, String> headers = new  HttpHeaders();
        headers.put("content-type",Arrays.asList("text-yaml"));
        RequestEntity<String> requestEntity = new RequestEntity<String>(manifest, headers, HttpMethod.PUT, uri);
        ResponseEntity<Void> response = restTemplate.exchange( requestEntity, Void.class);
        String taskId = trackTask(response);
        
        // set content-type to text/yaml
        
        //https://github.com/cloudfoundry/bosh/blob/master/bosh_cli/lib/cli/client/director.rb#L299
//        https://github.com/cloudfoundry/bosh/blob/master/bosh_cli/lib/cli/commands/job_management.rb
        
//        OPERATION_DESCRIPTIONS = {
//                start: 'start %s',
//                stop: 'stop %s',
//                detach: 'stop %s and power off its VM(s)',
//                restart: 'restart %s',
//                recreate: 'recreate %s'
//            }
//
//            NEW_STATES = {
//                start: 'started',
//                stop: 'stopped',
//                detach: 'detached',
//                restart: 'restart',
//                recreate: 'recreate'
//            }
//
//            COMPLETION_DESCRIPTIONS = {
//                start: '%s has been started',
//                stop: '%s has been stopped, VM(s) still running',
//                detach: '%s has been detached, VM(s) powered off',
//                restart: '%s has been restarted',
//                recreate: '%s has been recreated'
//            }
    }

    @Override
    public void stopJob(String deploymentName, String jobName, int index, boolean powerOffVm) {
        // TODO Auto-generated method stub
        
    }
}
