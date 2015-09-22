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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author David Ehringer
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Vm {

    @JsonProperty("vm_cid")
    private String vmCid;
    private List<String> ips = new ArrayList<String>();
    private List<String> dns = new ArrayList<String>();
    @JsonProperty("agent_id")
    private String agentId;
    @JsonProperty("job_name")
    private String jobName;
    @JsonProperty("job_state")
    private String jobState;
    private int index;
    @JsonProperty("resource_pool")
    private String resourcePool;
    private VmVitals vitals;
    @JsonProperty("resurrection_paused")
    private boolean resurrectionPaused;

    public String getVmCid() {
        return vmCid;
    }

    public List<String> getIps() {
        return ips;
    }

    public List<String> getDns() {
        return dns;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getJobName() {
        return jobName;
    }

    public String getJobState() {
        return jobState;
    }

    public int getIndex() {
        return index;
    }

    public String getResourcePool() {
        return resourcePool;
    }

    public VmVitals getVitals() {
        return vitals;
    }
    
    public boolean isResurrectionPaused() {
        return resurrectionPaused;
    }

    @Override
    public String toString() {
        return "VmDetails [vmCid=" + vmCid + ", ips=" + ips + ", dns=" + dns + ", agentId="
                + agentId + ", jobName=" + jobName + ", jobState=" + jobState + ", index=" + index
                + ", resourcePool=" + resourcePool + ", vitals=" + vitals + "]";
    }

}
