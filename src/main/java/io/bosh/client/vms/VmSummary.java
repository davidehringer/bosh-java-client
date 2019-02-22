package io.bosh.client.vms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author David Ehringer
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VmSummary {

    @JsonProperty("agent_id")
    private String agentId;
    private String cid;
    private String job;
    private int index;

    public String getAgentId() {
        return agentId;
    }

    public String getCid() {
        return cid;
    }

    public String getJob() {
        return job;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return "Vm [agent_id=" + agentId + ", cid=" + cid + ", job=" + job + ", index=" + index + "]";
    }

}
