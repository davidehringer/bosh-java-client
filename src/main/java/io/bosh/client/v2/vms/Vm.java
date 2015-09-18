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
package io.bosh.client.v2.vms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author David Ehringer
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Vm {

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
