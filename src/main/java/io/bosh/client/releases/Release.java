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
package io.bosh.client.releases;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Release {

    private String name;
    private List<Job> jobs = new ArrayList<Job>();
    private List<Package> packages = new ArrayList<Package>();
    private List<String> versions = new ArrayList<String>();

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public List<Package> getPackages() {
        return packages;
    }

    public List<String> getVersions() {
        return versions;
    }

    @Override
    public String toString() {
        return "ReleaseDetails [name=" + name + ", jobs=" + jobs + ", packages=" + packages
                + ", versions=" + versions + "]";
    }

}
