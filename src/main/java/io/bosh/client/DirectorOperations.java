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

import io.bosh.client.domain.Deployment;
import io.bosh.client.domain.DeploymentDetails;
import io.bosh.client.domain.DirectorInfo;
import io.bosh.client.domain.ErrandSummary;
import io.bosh.client.domain.LogType;
import io.bosh.client.domain.Problem;
import io.bosh.client.domain.StemcellDetails;
import io.bosh.client.domain.Task;
import io.bosh.client.domain.Vm;
import io.bosh.client.domain.VmDetails;
import io.bosh.client.v2.releases.Release;
import io.bosh.client.v2.releases.ReleaseDetails;

import java.io.InputStream;
import java.util.List;

/**
 * @author David Ehringer
 */
public interface DirectorOperations {

    // TODO
    // getTaskResultLog
    // getTaskOutput
    // getLocks

    DirectorInfo getInfo();

    List<Release> getReleases();

    ReleaseDetails getRelease(String name);

    List<StemcellDetails> getStemcells();

    List<Deployment> getDeployments();

    DeploymentDetails getDeployment(String name);

    List<Vm> getVms(String deploymentName);

    List<VmDetails> getVmDetails(String deploymentName);

    List<ErrandSummary> getErrands(String deploymentName);

    List<Task> getRunningTasks();

    List<Task> getRecentTasks();

    List<Task> getRecentTasks(int count);
    
    Task getTask(String id);

    List<Problem> performCloudScan(String deploymentName);
    
    /**
     * Typically preceeded by {@link #performCloudScan(String)}
     */
    List<Problem> getProblems(String deploymentName);
    
    InputStream fetchLogs(String deploymentName, String jobName, int jobIndex, LogType logType, String... filters);

    
    // TODO add skipDrain option
    void stopJob(String deploymentName, String jobName, boolean powerOffVm);
    
    void stopJob(String deploymentName, String jobName, int index, boolean powerOffVm);
}
