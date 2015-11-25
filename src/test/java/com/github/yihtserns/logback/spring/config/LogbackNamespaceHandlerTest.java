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

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.util.StatusPrinter;
import com.github.yihtserns.logback.spring.config.testutil.MockAppender;
import java.io.StringReader;
import java.nio.charset.Charset;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.After;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.xml.sax.InputSource;

/**
 * @author yihtserns
 */
public class LogbackNamespaceHandlerTest {

    private static final String LOGGER_NAME = "test";
    private Logger log = LoggerFactory.getLogger(LOGGER_NAME);
    private GenericApplicationContext appContext;

    @Before
    public void initLogback() throws Exception {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator joranConfigurator = new JoranConfigurator();
        joranConfigurator.setContext(loggerContext);

        loggerContext.reset();
        joranConfigurator.doConfigure(new InputSource(new StringReader(
                "<configuration debug=\"true\">\n"
                + "\n"
                + "    <appender name=\"mock\" class=\"ch.qos.logback.ext.spring.DelegatingLogbackAppender\"/>\n"
                + "\n"
                + "    <appender name=\"stdout\" class=\"ch.qos.logback.core.ConsoleAppender\">\n"
                + "        <encoder>\n"
                + "            <pattern>%-4relative [%thread] %-5level %logger{35} - %msg %n</pattern>\n"
                + "        </encoder>\n"
                + "    </appender>\n"
                + "\n"
                + "    <logger name=\"" + LOGGER_NAME + "\" level=\"INFO\" additivity=\"false\">\n"
                + "        <appender-ref ref=\"mock\"/>\n"
                + "    </logger>\n"
                + "\n"
                + "    <root level=\"INFO\">\n"
                + "        <appender-ref ref=\"stdout\"/>\n"
                + "    </root>\n"
                + "\n"
                + "</configuration>")));

        StatusPrinter.printInCaseOfErrorsOrWarnings(loggerContext);
    }

    @After
    public void closeAppContext() {
        if (appContext != null) {
            appContext.close();
            appContext.stop();
        }
    }

    @Test
    public void canConfigureAppenderInSpringXml() {
        appContext = newApplicationContextFor(
                "<appender name=\"mock\" class=\"com.github.yihtserns.logback.spring.config.testutil.MockAppender\"\n"
                + " xmlns=\"http://logback.qos.ch\"\n"
                + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                + " xsi:schemaLocation=\"http://logback.qos.ch logback-lenient.xsd\"/>\n");

        final String expectedMessage = "Configured successfully!";
        log.info(expectedMessage);

        MockAppender mock = appContext.getBean(MockAppender.class);
        mock.assertLogged(expectedMessage);
    }

    @Test
    public void shouldInjectLoggerContextIntoAppender() throws Exception {
        appContext = newApplicationContextFor(
                "<appender name=\"mock\" class=\"com.github.yihtserns.logback.spring.config.testutil.MockAppender\"\n"
                + " xmlns=\"http://logback.qos.ch\"\n"
                + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                + " xsi:schemaLocation=\"http://logback.qos.ch logback-lenient.xsd\"/>\n");

        MockAppender mock = appContext.getBean(MockAppender.class);
        Context loggerContext = mock.getContext();

        assertThat(loggerContext, is(not(nullValue())));
        assertThat(loggerContext, is((Context) LoggerFactory.getILoggerFactory()));
    }

    @Test
    public void canSetSimplePropertyIntoAppender() throws Exception {
        appContext = newApplicationContextFor(
                "<appender name=\"mock\" class=\"com.github.yihtserns.logback.spring.config.testutil.MockAppender\"\n"
                + " xmlns=\"http://logback.qos.ch\"\n"
                + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                + " xsi:schemaLocation=\"http://logback.qos.ch logback-lenient.xsd\">\n"
                + "    <id>123</id>\n"
                + "</appender>");

        MockAppender mock = appContext.getBean(MockAppender.class);
        assertThat(mock.id, is(123L));
    }

    @Test
    public void canAddMultipleSimplePropertyIntoAppender() throws Exception {
        appContext = newApplicationContextFor(
                "<appender name=\"mock\" class=\"com.github.yihtserns.logback.spring.config.testutil.MockAppender\"\n"
                + " xmlns=\"http://logback.qos.ch\"\n"
                + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                + " xsi:schemaLocation=\"http://logback.qos.ch logback-lenient.xsd\">\n"
                + "    <alias>fake</alias>\n"
                + "    <alias>unreal</alias>\n"
                + "</appender>");

        MockAppender mock = appContext.getBean(MockAppender.class);
        assertThat(mock.getAliases(), is(new String[]{"fake", "unreal"}));
    }

    @Test
    public void canConfigureFilter() throws Exception {
        appContext = newApplicationContextFor(
                "<appender name=\"mock\" class=\"com.github.yihtserns.logback.spring.config.testutil.MockAppender\"\n"
                + " xmlns=\"http://logback.qos.ch\"\n"
                + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                + " xsi:schemaLocation=\"http://logback.qos.ch logback-lenient.xsd\">\n"
                + "    <filter class=\"ch.qos.logback.classic.filter.LevelFilter\">\n"
                + "        <level>ERROR</level>\n"
                + "        <onMatch>DENY</onMatch>\n"
                + "        <onMismatch>NEUTRAL</onMismatch>\n"
                + "    </filter>\n"
                + "</appender>");

        final String expectedMessage1 = "Accepted";
        final String expectedMessage2 = "Accepted 2";

        log.info(expectedMessage1);
        log.error("Rejected");
        log.info(expectedMessage2);

        MockAppender mock = appContext.getBean(MockAppender.class);
        mock.assertLogged(expectedMessage1, expectedMessage2);
    }

    @Test
    public void canAddMultipleComplexValueIntoAppender() throws Exception {
        appContext = newApplicationContextFor(
                "<appender name=\"mock\" class=\"com.github.yihtserns.logback.spring.config.testutil.MockAppender\"\n"
                + " xmlns=\"http://logback.qos.ch\"\n"
                + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                + " xsi:schemaLocation=\"http://logback.qos.ch logback-lenient.xsd\">\n"
                + "    <filter class=\"ch.qos.logback.classic.filter.LevelFilter\">\n"
                + "        <level>WARN</level>\n"
                + "        <onMatch>DENY</onMatch>\n"
                + "        <onMismatch>NEUTRAL</onMismatch>\n"
                + "    </filter>\n"
                + "    <filter class=\"ch.qos.logback.classic.filter.LevelFilter\">\n"
                + "        <level>ERROR</level>\n"
                + "        <onMatch>DENY</onMatch>\n"
                + "        <onMismatch>NEUTRAL</onMismatch>\n"
                + "    </filter>\n"
                + "</appender>");

        final String expectedMessage1 = "Accepted";
        final String expectedMessage2 = "Accepted 2";

        log.info(expectedMessage1);
        log.warn("Rejected");
        log.info(expectedMessage2);
        log.error("Rejected 2");

        MockAppender mock = appContext.getBean(MockAppender.class);
        mock.assertLogged(expectedMessage1, expectedMessage2);
    }

    @Test
    public void canAttachAppenderIntoAppender() throws Exception {
        appContext = newApplicationContextFor(
                "<beans xmlns=\"http://www.springframework.org/schema/beans\"\n"
                + "       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                + "       xsi:schemaLocation=\"\n"
                + "            http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\n"
                + "            http://logback.qos.ch logback-lenient.xsd\n"
                + "\">\n"
                + "    <appender name=\"mock\" class=\"com.github.yihtserns.logback.spring.config.testutil.MockAppender\" xmlns=\"http://logback.qos.ch\"/>\n"
                + "    <appender name=\"async\" class=\"ch.qos.logback.classic.AsyncAppender\" xmlns=\"http://logback.qos.ch\">\n"
                + "        <appender-ref ref=\"mock\"/>\n"
                + "    </appender>\n"
                + "</beans>");

        AsyncAppender asyncAppender = appContext.getBean(AsyncAppender.class);
        Appender mockAppender = appContext.getBean(MockAppender.class);

        assertThat(asyncAppender.getAppender("mock"), is(sameInstance(mockAppender)));
    }

    @Test
    public void canSetComplexValueIntoAppender() throws Exception {
        appContext = newApplicationContextFor(
                "<appender name=\"mock\" class=\"com.github.yihtserns.logback.spring.config.testutil.MockAppender\"\n"
                + " xmlns=\"http://logback.qos.ch\"\n"
                + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                + " xsi:schemaLocation=\"http://logback.qos.ch logback-lenient.xsd\">\n"
                + "    <layout class=\"ch.qos.logback.classic.PatternLayout\">\n"
                + "        <pattern>%level - %msg</pattern>\n"
                + "    </layout>\n"
                + "</appender>");

        log.info("Information");
        log.warn("Warning");
        log.error("Problem");

        MockAppender mock = appContext.getBean(MockAppender.class);
        mock.assertLogged(
                "INFO - Information",
                "WARN - Warning",
                "ERROR - Problem");
    }

    private static GenericApplicationContext newApplicationContextFor(String xml) {
        GenericApplicationContext applicationContext = new GenericApplicationContext();
        XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(applicationContext);
        xmlReader.loadBeanDefinitions(new ByteArrayResource(xml.getBytes(Charset.forName("UTF-8"))));

        applicationContext.refresh();
        applicationContext.start();

        return applicationContext;
    }
}
