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
package io.bosh.client.v2.releases;

import io.bosh.client.v2.AbstractSpringOperations;

import java.net.URI;
import java.util.Arrays;

import org.springframework.web.client.RestTemplate;

import rx.Observable;

/**
 * @author David Ehringer
 */
public class SpringReleases extends AbstractSpringOperations implements Releases {

    public SpringReleases(RestTemplate restTemplate, URI root) {
        super(restTemplate, root);
    }

    @Override
    public Observable<ListReleasesResponse> list() {
        return getArray(ListReleasesResponse.class, 
                builder -> builder.pathSegment("releases"),
                Release[].class,
                results -> new ListReleasesResponse().withReleases(Arrays.asList(results)));
    }

    @Override
    public Observable<ReleaseDetails> get(GetReleaseRequest request) {
        return get(ReleaseDetails.class,
                builder -> builder.pathSegment("releases", request.getName()));
        // release.setName(name);
        // return release;
        // TODO
    }
}
