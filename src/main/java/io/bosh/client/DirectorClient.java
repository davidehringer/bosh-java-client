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
package io.bosh.client;

import io.bosh.client.deployments.Deployments;
import io.bosh.client.errands.Errands;
import io.bosh.client.info.Info;
import io.bosh.client.jobs.Jobs;
import io.bosh.client.releases.Releases;
import io.bosh.client.stemcells.Stemcells;
import io.bosh.client.tasks.Tasks;
import io.bosh.client.vms.Vms;

/**
 * @author David Ehringer
 */
public interface DirectorClient {

    Info info();
    
    Stemcells stemcells();
    
    Releases releases();
    
    Deployments deployments();

    Jobs jobs();
    
    Vms vms();
    
    Errands errands();
    
    Tasks tasks();
}
