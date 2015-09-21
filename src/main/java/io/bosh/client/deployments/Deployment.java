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
package io.bosh.client.deployments;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author David Ehringer
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Deployment {

    private String name;
    private List<ReleaseSummary> releases = new ArrayList<ReleaseSummary>();
    private List<StemcellSummary> stemcells = new ArrayList<StemcellSummary>();

    public String getName() {
        return name;
    }

    public List<ReleaseSummary> getReleases() {
        return releases;
    }

    public List<StemcellSummary> getStemcells() {
        return stemcells;
    }

    @Override
    public String toString() {
        return "Deployment [name=" + name + "]";
    }

}
