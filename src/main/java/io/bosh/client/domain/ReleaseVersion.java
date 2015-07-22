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
package io.bosh.client.domain;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author David Ehringer
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReleaseVersion {

    private String version;
    @JsonProperty("commit_hash")
    private String commitHash;
    @JsonProperty("uncommitted_changes")
    private boolean uncommittedChanges;
    @JsonProperty("currently_deployed")
    private boolean currentlyDeployed;
    @JsonProperty("job_names")
    private List<String> jobNames = new ArrayList<String>();

    public String getVersion() {
        return version;
    }

    public String getCommitHash() {
        return commitHash;
    }

    public boolean isUncommittedChanges() {
        return uncommittedChanges;
    }

    public boolean isCurrentlyDeployed() {
        return currentlyDeployed;
    }

    public List<String> getJobNames() {
        return jobNames;
    }

    @Override
    public String toString() {
        return "ReleaseVersion [version=" + version + ", commitHash=" + commitHash
                + ", uncommittedChanges=" + uncommittedChanges + ", currentlyDeployed="
                + currentlyDeployed + ", jobNames=" + jobNames + "]";
    }

}
