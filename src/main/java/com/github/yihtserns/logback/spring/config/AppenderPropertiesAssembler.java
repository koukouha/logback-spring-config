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
import java.util.Map;
import org.slf4j.LoggerFactory;

/**
 * @author yihtserns
 * @see #assemble(Appender, Map)
 */
public class AppenderPropertiesAssembler {

    /**
     * Use Logback's API to set values to setXXX(value) and addXXX(value) methods because Spring only supports the former.
     *
     * @param appender appender to set property values to
     * @param property2Value property values to set to appender
     * @return the given appender
     */
    public static Appender assemble(Appender appender, Map<String, Object> property2Value) {
        PropertySetter setter = new PropertySetter(appender);
        setter.setContext((Context) LoggerFactory.getILoggerFactory());

        for (Map.Entry<String, Object> entry : property2Value.entrySet()) {
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

        appender.start();
        return appender;
    }
}