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
package io.bosh.client.jobs;

/**
 * @author David Ehringer
 */
abstract class AbstractJobRequest<T extends AbstractJobRequest<T>> {
    
    private String deploymentName;
    private String jobName;
    private Integer jobIndex;

    public String getDeploymentName() {
        return deploymentName;
    }

    @SuppressWarnings("unchecked")
    public T withDeploymentName(String deploymentName) {
        this.deploymentName = deploymentName;
        return (T) this;
    }

    public String getJobName() {
        return jobName;
    }

    @SuppressWarnings("unchecked")
    public T withJobName(String jobName) {
        this.jobName = jobName;
        return (T) this;
    }

    public Integer getJobIndex() {
        return jobIndex;
    }

    @SuppressWarnings("unchecked")
    public T withJobIndex(Integer jobIndex) {
        this.jobIndex = jobIndex;
        return (T) this;
    }
}
