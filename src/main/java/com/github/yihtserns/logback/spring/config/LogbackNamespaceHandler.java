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

import ch.qos.logback.ext.spring.ApplicationContextHolder;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * @author yihtserns
 */
public class LogbackNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        final String contextHolderBeanName = "logback.applicationContextHolder";

        if (!parserContext.getRegistry().containsBeanDefinition(contextHolderBeanName)) {
            BeanDefinition bd = BeanDefinitionBuilder.rootBeanDefinition(ApplicationContextHolder.class).getBeanDefinition();
            parserContext.registerBeanComponent(new BeanComponentDefinition(bd, contextHolderBeanName));
        }

        return super.parse(element, parserContext);
    }

    public void init() {
        registerBeanDefinitionParser("appender", new LogbackObjectBeanDefinitionParser());
    }

    private class LogbackObjectBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

        /**
         * Only called when not nested element.
         */
        @Override
        protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) {
            return element.getAttribute("name");
        }

        @Override
        protected Class<?> getBeanClass(Element element) {
            return LogbackObjectPropertiesAssembler.class;
        }

        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            builder.setFactoryMethod("assemble");

            String logbackObjectClassName = element.getAttribute("class");
            BeanDefinitionBuilder logbackObjectBuilder = BeanDefinitionBuilder.genericBeanDefinition(logbackObjectClassName);
            String name = element.getAttribute("name");
            if (StringUtils.hasText(name)) {
                logbackObjectBuilder.addPropertyValue("name", name);
            }
            builder.addConstructorArgValue(logbackObjectBuilder.getBeanDefinition());

            ManagedList<Pair> property2ValueList = new ManagedList<Pair>();
            for (Element childElement : DomUtils.getChildElements(element)) {
                Pair property2Value = parsePropertyValue(childElement, parserContext, builder);

                if (property2Value != null) {
                    property2ValueList.add(property2Value);
                }
            }
            builder.addConstructorArgValue(property2ValueList);
        }

        private Pair parsePropertyValue(
                Element propertyElement,
                ParserContext parserContext,
                BeanDefinitionBuilder builder) {
            String localName = propertyElement.getLocalName();

            if ("param".equals(localName)) {
                String name = propertyElement.getAttribute("name");
                String value = propertyElement.getAttribute("value").trim();

                if (!StringUtils.hasText(name)) {
                    parserContext.getReaderContext().error("<param> must have 'name' attribute.", propertyElement);

                    return null;
                }
                if (!StringUtils.hasText(value)) {
                    parserContext.getReaderContext().error("<param> must have 'value' attribute.", propertyElement);

                    return null;
                }

                return new Pair(name, value);
            }

            if ("appender-ref".equals(localName)) {
                String appenderName = propertyElement.getAttribute("ref");

                return new Pair("appender", new RuntimeBeanReference(appenderName));
            }

            if (StringUtils.hasText(propertyElement.getAttribute("class"))) {
                ParserContext childParserContext = new ParserContext(
                        parserContext.getReaderContext(),
                        parserContext.getDelegate(),
                        builder.getRawBeanDefinition());

                return new Pair(localName, parse(propertyElement, childParserContext));
            }

            String body = DomUtils.getTextValue(propertyElement);
            if (!StringUtils.hasText(body)) {
                String msg = String.format("<%s> property should have either 'class' attribute or text body.", localName);
                parserContext.getReaderContext().error(msg, propertyElement);

                return null;
            }

            return new Pair(localName, body);
        }
    }

    /**
     * Pair of property key -to- property value.
     * <p/>
     * Extends {@link ManagedMap} so that Spring would resolve the key and value.
     */
    private static final class Pair extends ManagedMap<String, Object> {

        public Pair(String key, Object value) {
            put(key, value);
        }
    }
}
