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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author David Ehringer
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Task {

    private String id;
    private String state;
    private String description;
    private String timestamp;
    private String result;
    private String user;

    public String getId() {
        return id;
    }

    public String getState() {
        return state;
    }

    public String getDescription() {
        return description;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getResult() {
        return result;
    }

    public String getUser() {
        return user;
    }

    @Override
    public String toString() {
        return "Task [id=" + id + ", state=" + state + ", description=" + description
                + ", timestamp=" + timestamp + ", result=" + result + ", user=" + user + "]";
    }

}
