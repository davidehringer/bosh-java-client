package io.bosh.client.vms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import io.bosh.client.DirectorException;
import io.bosh.client.deployments.SSHConfig;
import io.bosh.client.internal.AbstractSpringOperations;
import io.bosh.client.tasks.Task;
import io.bosh.client.tasks.Tasks;
import org.springframework.web.client.RestOperations;
import rx.Observable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author David Ehringer, Johannes Hiemer.
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
        config = new SSHConfig(config, new String(keyPair.getPublic().getEncoded()));
        return this.ssh(config, new String(keyPair.getPrivate().getEncoded()));
    }

    private File writePrivateKeyFile(String privateKey) {
        try {
            String filename = String.valueOf(new Date().getTime());
            File temp = File.createTempFile(filename, ".tmp");

            BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
            bw.write(privateKey);
            bw.close();

            return temp;
        } catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Observable<Session> ssh(SSHConfig config, String privateKey) {
        return postForEntity(Task.class, config,
                builder -> builder.pathSegment("deployments", config.getDeploymentName(), "ssh"))
                .flatMap(response -> tasks.trackToCompletion(getTaskId(response)))
                .map(body -> {
                    Vm vm = listDetails(config.getDeploymentName()).toBlocking().first().stream().filter(filterVm -> {
                        if (filterVm.getJobName().equals(config.getTarget().getJob()) &&
                                filterVm.getIndex() == config.getTarget().getIndexes())
                            return true;
                        return false;
                    }).findFirst().get();

                    JSch jsch = new JSch();
                    Session session = null;
                    File privateKeyFile = null;
                    try {
                        privateKeyFile = this.writePrivateKeyFile(privateKey);
                        jsch.addIdentity(privateKeyFile.getAbsolutePath());
                        session = jsch.getSession(config.getParams().getUser(), vm.getIps().get(0), 22);
                        session.setConfig("StrictHostKeyChecking", "no");
                    } catch (JSchException e) {
                        throw new DirectorException("Unable to create ssh connection to " + vm.getJobName() + vm.getIndex(), e);
                    } finally {
                        if (privateKeyFile.exists())
                            privateKeyFile.delete();
                    }
                    return session;
                });
    }

}