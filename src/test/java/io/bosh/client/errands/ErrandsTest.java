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
package io.bosh.client.errands;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import io.bosh.client.AbstractDirectorTest;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;

/**
 * @author David Ehringer
 */
public class ErrandsTest extends AbstractDirectorTest{

    private Errands errands;

    @Before
    public void setup(){
        errands = client.errands();
    }

    @Test
    public void list() {
        // Given
        mockServer.expect(requestTo(url("/deployments/test/errands")))//
                .andRespond(withSuccess(payload("errands/errands.json"), MediaType.TEXT_HTML));
        // When
        errands.list("test").subscribe(summaries -> {
            // Then
            assertThat(summaries.size(), is(3));

            assertThat(summaries.get(0).getName(), is("broker-registrar"));
            assertThat(summaries.get(1).getName(), is("broker-deregistrar"));
            assertThat(summaries.get(2).getName(), is("smoke-tests"));
        });
    }

}
