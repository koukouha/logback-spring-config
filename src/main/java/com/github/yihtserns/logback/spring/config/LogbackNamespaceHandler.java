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
import java.util.List;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.beans.factory.xml.ParserContext;
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

                String appenderClassName = element.getAttribute("class");
                BeanDefinition appenderBd = BeanDefinitionBuilder.genericBeanDefinition(appenderClassName)
                        .setDestroyMethodName("stop")
                        .getBeanDefinition();
                builder.addConstructorArgValue(appenderBd);

                ManagedMap<String, Object> property2Value = new ManagedMap<String, Object>();
                List<Element> childElements = DomUtils.getChildElements(element);

                for (Element childElement : childElements) {
                    String childClassName = childElement.getAttribute("class");
                    BeanDefinition childBd = BeanDefinitionBuilder.genericBeanDefinition(childClassName).getBeanDefinition();

                    property2Value.put(childElement.getLocalName(), childBd);
                }
                builder.addConstructorArgValue(property2Value);
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
                return AppenderPropertiesAssembler.class;
            }
        });
    }

}
