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
package io.bosh.client.vms;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import io.bosh.client.DirectorException;
import io.bosh.client.deployments.SSHConfig;
import io.bosh.client.internal.AbstractSpringOperations;
import io.bosh.client.tasks.Task;
import io.bosh.client.tasks.Tasks;

import java.io.IOException;
import java.net.URI;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpMethod;
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
    public Observable<List<VmSummary>> list(String deploymentName) {
        return get(VmSummary[].class,
                builder -> builder.pathSegment("deployments", deploymentName, "vms"))
                .map(results -> Arrays.asList(results));
    }
    
    @Override
    public Observable<List<Vm>> listDetails(String deploymentName) {
        return getEntity(Void.class, builder -> builder.pathSegment("deployments", deploymentName, "vms")
                        .queryParam("format", "full"))
            .flatMap(response -> tasks.trackToCompletion(getTaskId(response)))
            .flatMap(task -> get(String.class, builder -> builder.pathSegment("tasks", task.getId(), "output")
                                                           .queryParam("type", "result"))
            .filter(rawDetails -> rawDetails != null)
            .map(rawDetails -> rawDetails.split("\n"))
            .map(rawDetails -> {
                List<Vm> details = new ArrayList<Vm>();
                for (String vm : rawDetails) {
                    try {
                        details.add(mapper.readValue(vm.getBytes(), Vm.class));
                    } catch (IOException e) {
                        throw new DirectorException("Unable to read VM data into VmDetails: " + vm, e);
                    }
                }
                return details;
             }));
    }


    public Observable<Session> ssh(SSHConfig config) {
        KeyPairGenerator keyGen = null;
        try {
            keyGen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new DirectorException("Unable to generate SSH-Keypair" , e);
        }
        keyGen.initialize(1024);
        KeyPair keyPair =keyGen.generateKeyPair();
        config = new SSHConfig(config, keyPair.getPublic().getEncoded().toString());
        return this.ssh(config, keyPair.getPrivate().getEncoded().toString());
    }

    public Observable<Session> ssh(SSHConfig config, String privateKey){
        return exchangeWithTaskRedirect(config,
                                        Task.class,
                                        null,
                                        HttpMethod.POST,
                                        builder -> builder.pathSegment("deployments", config.getDeploymentName(), "ssh"))
                     .map(exchange -> exchange.getBody())
                     .map(body -> {
                         List<Vm> vms = listDetails(config.getDeploymentName()).toBlocking().first();
                         Vm vm = vms.get(config.getTarget().getIndexes());

                         JSch jsch=new JSch();
                         Session session = null;
                         try {
                             jsch.addIdentity(privateKey);
                             session = jsch.getSession(config.getParams().getUser(), vm.getIps().get(0), 22);
                         } catch (JSchException e) {
                             throw new DirectorException("Unable to create ssh connection to " + vm.getJobName() + vm.getIndex(), e);
                         }
                         return session;
                     });
    }

}