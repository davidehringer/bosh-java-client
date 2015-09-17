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
package io.bosh.client.v2.info;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author David Ehringer
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DirectorInfo {

    private String cpi;
    private String uuid;
    private String version;
    private String name;
    private String user;
    private Map<String, Object> features;

    public String getCpi() {
        return cpi;
    }

    public String getUuid() {
        return uuid;
    }

    public String getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public String getUser() {
        return user;
    }

    public Map<String, Object> getFeatures() {
        return features;
    }

    @Override
    public String toString() {
        return "Info [uuid=" + uuid + ", name=" + name + ", version=" + version + ", cpi=" + cpi
                + ", user=" + user + ", features=" + features + "]";
    }

}