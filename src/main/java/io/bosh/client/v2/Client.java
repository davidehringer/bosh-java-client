/*
 * Copyright (C) 2015, Liberty Mutual Group
 *
 * Created on Sep 21, 2015
 */
package io.bosh.client.v2;

import io.bosh.client.v2.deployments.Deployment;
import io.bosh.client.v2.deployments.ListDeploymentsResponse;
import io.bosh.client.v2.errands.ErrandSummary;
import io.bosh.client.v2.errands.ListErrandsRequest;
import io.bosh.client.v2.errands.ListErrandsResponse;
import io.bosh.client.v2.jobs.JobRequest;
import io.bosh.client.v2.jobs.StopJobRequest;
import io.bosh.client.v2.tasks.Task;
import io.bosh.client.v2.vms.ListVmDetailsRequest;
import io.bosh.client.v2.vms.VmDetails;

/**
 * @author David Ehringer (n0119737)
 */
public class Client {

    public static void main(String[] args) {
        DirectorClient client = new SpringDirectorClientBuilder().withHost("10.174.52.151").withCredentials("director", "c132c5e4644963716be0").build();
        
        ListDeploymentsResponse resposne = client.deployments().list().toBlocking().first();
        for(Deployment deployment: resposne.getDeployments()){
            System.out.println("deployment: " + deployment.getName());
        }
        
        StopJobRequest request = new StopJobRequest()
            .withDeploymentName("metrics-nozzle-d210a05a419256e66b39")
            .withJobName("metrics-nozzle-partition-6e7ff5ae3b5fed5a7435").withPowerOffVm(true);
        Task task = client.jobs().stopJob(request).toBlocking().first();
        System.out.println("task: " + task.getResult());
        System.out.println("task: " + task.getState());
        
        ListErrandsRequest listErrandsRequest = new ListErrandsRequest().withDeploymentName("cf-b3b2ad7221bc0c02c5e0");
        ListErrandsResponse listErrandsResponse = client.errands().list(listErrandsRequest).toBlocking().first();
        for(ErrandSummary errand: listErrandsResponse.getErrands()){
            System.out.println("errand: " + errand);
        }
        
        ListVmDetailsRequest listVmDetailsRequest = new ListVmDetailsRequest().withDeploymentName("cf-b3b2ad7221bc0c02c5e0");
        for(VmDetails details: client.vms().listDetails(listVmDetailsRequest).toBlocking().first().getVmDetails()){
            System.out.println("vm: " + details);
        }
        
    }
}
