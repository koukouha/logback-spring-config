/*
 * Copyright 2015 yihtserns.
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
package com.github.yihtserns.logback.spring.config;

import com.github.yihtserns.logback.spring.config.util.MockAppender;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author yihtserns
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class LogbackNamespaceHandlerTest {

    private Logger log = LoggerFactory.getLogger(getClass());
    @Autowired
    private MockAppender mock;

    @Before
    public void resetMock() {
        mock.reset();
    }

    @Test
    public void canConfigureAppenderInSpringXml() {
        final String expectedMessage = "Configured successfully!";
        log.info(expectedMessage);

        mock.assertLogged(expectedMessage);
    }
}
