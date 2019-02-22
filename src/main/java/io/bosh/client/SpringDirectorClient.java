package io.bosh.client;

import io.bosh.client.deployments.Deployments;
import io.bosh.client.deployments.SpringDeployments;
import io.bosh.client.errands.Errands;
import io.bosh.client.errands.SpringErrands;
import io.bosh.client.info.Info;
import io.bosh.client.info.SpringInfo;
import io.bosh.client.jobs.Jobs;
import io.bosh.client.jobs.SpringJobs;
import io.bosh.client.releases.Releases;
import io.bosh.client.releases.SpringReleases;
import io.bosh.client.stemcells.SpringStemcells;
import io.bosh.client.stemcells.Stemcells;
import io.bosh.client.tasks.SpringTasks;
import io.bosh.client.tasks.Tasks;
import io.bosh.client.vms.SpringVms;
import io.bosh.client.vms.Vms;

import java.net.URI;

import org.springframework.web.client.RestTemplate;

/**
 * @author David Ehringer, Jannik Heyl.
 */
public class SpringDirectorClient implements DirectorClient {

    private final RestTemplate restTemplate;

    private final Info info;
    private final Releases releases;
    private final Stemcells stemcells;
    private final Deployments deployments;
    private final Jobs jobs;
    private final Vms vms;
    private final Errands errands;
    private final Tasks tasks;

    SpringDirectorClient(URI root, RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.info = new SpringInfo(restTemplate, root);
        this.releases = new SpringReleases(restTemplate, root);
        this.stemcells = new SpringStemcells(restTemplate, root);
        this.errands = new SpringErrands(restTemplate, root);
        this.tasks = new SpringTasks(restTemplate, root);
        this.deployments = new SpringDeployments(restTemplate, root, tasks);
        this.jobs = new SpringJobs(restTemplate, root, tasks, deployments);
        this.vms = new SpringVms(restTemplate, root, tasks);
    }

    public RestTemplate restTemplate() {
        return restTemplate;
    }

    @Override
    public Stemcells stemcells() {
        return stemcells;
    }

    @Override
    public Releases releases() {
        return releases;
    }

    @Override
    public Info info() {
        return info;
    }

    @Override
    public Deployments deployments() {
        return deployments;
    }

    @Override
    public Vms vms() {
        return vms;
    }

    @Override
    public Errands errands() {
        return errands;
    }

    @Override
    public Tasks tasks() {
        return tasks;
    }


    @Override
    public Jobs jobs() {
        return jobs;
    }

}
