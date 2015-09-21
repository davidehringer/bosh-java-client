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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author David Ehringer
 */
public class FetchLogsRequest extends AbstractJobRequest<FetchLogsRequest>{

    private LogType logType;
    private List<String> filters = new ArrayList<String>();

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
