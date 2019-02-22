package io.bosh.client.vms;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author David Ehringer, Yannic Remmet.
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
