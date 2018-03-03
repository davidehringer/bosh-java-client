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

import java.io.IOException;
import java.io.InputStreamReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.FileCopyUtils;

/**
 * @author David Ehringer
 */
public abstract class AbstractDirectorTest {

    protected MockRestServiceServer mockServer;
    protected DirectorClient client;

    {
        SpringDirectorClient springClient = new SpringDirectorClientBuilder()
                .withScheme(Scheme.https)
                .withHost("192.168.50.4")
                .withPort(25555)
                .withCredentials("admin", "admin", Authentication.BASIC).build();
        mockServer = MockRestServiceServer.createServer(springClient.restTemplate());
        client = springClient;
    }


    protected String url(String url) {
        return "https://192.168.50.4:25555" + url;
    }

    protected String payload(String filename) {
        ClassPathResource resource = new ClassPathResource(filename);
        try {
            return FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(
                    "TEST SETUP ERROR: unable to find test resources file with name " + filename, e);
        }

    }
}
