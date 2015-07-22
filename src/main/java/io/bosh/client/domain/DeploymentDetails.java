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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author David Ehringer
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeploymentDetails {

    private String name;
    private String manifest;
    private Map<String, Object> manifestMap = new HashMap<String, Object>();

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getRawManifest() {
        return manifest;
    }

    public Map<String, Object> getManifest() {
        return Collections.unmodifiableMap(manifestMap);
    }

    public void setManifestMap(Map<String, Object> manifestMap) {
        this.manifestMap.clear();
        this.manifestMap.putAll(manifestMap);
    }

    @Override
    public String toString() {
        return "DeploymentDetails [name=" + name + ", manifest=" + manifest + "]";
    }

}
