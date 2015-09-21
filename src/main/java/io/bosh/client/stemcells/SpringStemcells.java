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
package io.bosh.client.stemcells;

import io.bosh.client.internal.AbstractSpringOperations;

import java.net.URI;
import java.util.Arrays;

import org.springframework.web.client.RestOperations;

import rx.Observable;

/**
 * @author David Ehringer
 */
public class SpringStemcells extends AbstractSpringOperations implements Stemcells {

    public SpringStemcells(RestOperations restOperations, URI root) {
        super(restOperations, root);
    }

    @Override
    public Observable<ListStemcellsResponse> list() {
        return get(StemcellDetails[].class, 
                builder -> builder.pathSegment("stemcells"))
               .map(results -> new ListStemcellsResponse().withStemcells(Arrays.asList(results)));
    }

}
