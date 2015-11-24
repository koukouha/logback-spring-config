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
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanDefinitionStoreException;
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

    public void init() {
        registerBeanDefinitionParser("appender", new AbstractSingleBeanDefinitionParser() {

            @Override
            protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
                registerContextHolderIfNotYet(parserContext);

                builder.setFactoryMethod("assemble");

                String name = element.getAttribute("name");
                if (StringUtils.hasText(name)) {
                    builder.addPropertyValue("name", name);
                }

                String appenderClassName = element.getAttribute("class");
                BeanDefinition appenderBd = BeanDefinitionBuilder.genericBeanDefinition(appenderClassName)
                        .setDestroyMethodName("stop")
                        .getBeanDefinition();
                builder.addConstructorArgValue(appenderBd);

                ManagedList<ManagedMap<String, Object>> property2ValueList = new ManagedList<ManagedMap<String, Object>>();
                for (Element childElement : DomUtils.getChildElements(element)) {
                    String localName = childElement.getLocalName();

                    String propertyName;
                    Object propertyValue;
                    if ("appender-ref".equals(localName)) {
                        String appenderName = childElement.getAttribute("ref");

                        propertyName = "appender";
                        propertyValue = new RuntimeBeanReference(appenderName);
                    } else if (StringUtils.hasText(childElement.getAttribute("class"))) {
                        // Complex property
                        ParserContext childParserContext = new ParserContext(
                                parserContext.getReaderContext(),
                                parserContext.getDelegate(),
                                builder.getRawBeanDefinition());

                        propertyName = localName;
                        propertyValue = parse(childElement, childParserContext);
                    } else {
                        // Simple property
                        propertyName = localName;
                        propertyValue = childElement.getTextContent();
                    }

                    ManagedMap<String, Object> property2Value = new ManagedMap<String, Object>();
                    property2Value.put(propertyName, propertyValue);

                    property2ValueList.add(property2Value);
                }
                builder.addConstructorArgValue(property2ValueList);
                builder.addConstructorArgValue(LoggerFactory.getILoggerFactory());
            }

            private void registerContextHolderIfNotYet(ParserContext parserContext) {
                final String contextHolderBeanName = "logback.applicationContextHolder";

                if (!parserContext.getRegistry().containsBeanDefinition(contextHolderBeanName)) {
                    BeanDefinition bd = BeanDefinitionBuilder.rootBeanDefinition(ApplicationContextHolder.class).getBeanDefinition();
                    parserContext.registerBeanComponent(new BeanComponentDefinition(bd, contextHolderBeanName));
                }
            }

            @Override
            protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) throws BeanDefinitionStoreException {
                return element.getAttribute("name");
            }

            @Override
            protected Class<?> getBeanClass(Element element) {
                return LogbackObjectPropertiesAssembler.class;
            }
        });
    }

}
