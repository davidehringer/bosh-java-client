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
package io.bosh.client.v2.jobs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author David Ehringer
 */
public class FetchLogsRequest {

    private String deploymentName;
    private String jobName;
    private int jobIndex;
    private LogType logType;
    private List<String> filters = new ArrayList<String>();

    public String getDeploymentName() {
        return deploymentName;
    }

    public FetchLogsRequest withDeploymentName(String deploymentName) {
        this.deploymentName = deploymentName;
        return this;
    }

    public String getJobName() {
        return jobName;
    }

    public FetchLogsRequest withJobName(String jobName) {
        this.jobName = jobName;
        return this;
    }

    public int getJobIndex() {
        return jobIndex;
    }

    public FetchLogsRequest withJobIndex(int jobIndex) {
        this.jobIndex = jobIndex;
        return this;
    }

    public LogType getLogType() {
        return logType;
    }

    public FetchLogsRequest withLogType(LogType logType) {
        this.logType = logType;
        return this;
    }

    public List<String> getFilters() {
        return filters;
    }

    public FetchLogsRequest withFilters(List<String> filters) {
        this.filters.addAll(filters);
        return this;
    }

    public FetchLogsRequest withFilters(String... filters) {
        this.filters.addAll(Arrays.asList(filters));
        return this;
    }

}
