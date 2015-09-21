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

import io.bosh.client.v2.tasks.Task;

import java.io.InputStream;

import rx.Observable;

/**
 * @author David Ehringer
 */
public interface Jobs {

    Observable<InputStream> fetchLogs(FetchLogsRequest request);

    /**
     * Check {@link Task#getState()} and {@link Task#getResult()} for the
     * outcome of this operation. This operation may timeout after a defined interval based on server-side
     * logic in the BOSH Director.
     * 
     * @param request
     * @return the BOSH {@link Task} to 
     */
    Observable<Task> stopJob(StopJobRequest request);
    
    Observable<Task> startJob(StartJobRequest request);
    
    Observable<Task> restartJob(RestartJobRequest request);
    
    Observable<Task> recreateJob(RecreateJobRequest request);
}
