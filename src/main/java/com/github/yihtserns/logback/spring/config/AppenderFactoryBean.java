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

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.util.PropertySetter;
import ch.qos.logback.core.util.AggregationType;
import static ch.qos.logback.core.util.AggregationType.AS_COMPLEX_PROPERTY_COLLECTION;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author yihtserns
 */
public class AppenderFactoryBean implements InitializingBean, FactoryBean {

    private Appender appender;
    private Map<String, Object> property2Value = null;

    public void setAppender(Appender appender) {
        this.appender = appender;
    }

    public void setPropertyValues(Map<String, Object> property2Value) {
        this.property2Value = property2Value;
    }

    public void afterPropertiesSet() {
        if (property2Value == null) {
            return;
        }

        PropertySetter setter = new PropertySetter(appender);
        setter.setContext((Context) LoggerFactory.getILoggerFactory());

        for (Entry<String, Object> entry : property2Value.entrySet()) {
            String propertyName = entry.getKey();
            Object value = entry.getValue();

            AggregationType setterType = setter.computeAggregationType(propertyName);
            switch (setterType) {
                case AS_COMPLEX_PROPERTY_COLLECTION:
                    setter.addComplexProperty(propertyName, value);
                    break;
                default:
                    throw new UnsupportedOperationException("Not implemented yet for AggregationType: " + setterType);
            }
        }
    }

    public Object getObject() throws Exception {
        return appender;
    }

    public Class getObjectType() {
        return appender.getClass();
    }

    public boolean isSingleton() {
        return true;
    }
}
