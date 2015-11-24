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
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.util.AggregationType;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.LoggerFactory;

/**
 * @author yihtserns
 * @see #assemble(Appender, Map)
 */
public class LogbackObjectPropertiesAssembler {

    /**
     * Use Logback's API to set values to setXXX(value) and addXXX(value) methods because Spring only supports the former.
     *
     * @param logbackObject object to set property values to
     * @param property2ValueList list with even items,
     * @param context Logback context
     * @return the given object
     */
    public static Object assemble(Object logbackObject, List<Map<String, Object>> property2ValueList, Context context) {
        PropertySetter setter = new PropertySetter(logbackObject);
        setter.setContext((Context) LoggerFactory.getILoggerFactory());

        for (Map<String, Object> property2Value : property2ValueList) {
            for (Entry<String, Object> entry : property2Value.entrySet()) {
                String propertyName = entry.getKey();
                Object value = entry.getValue();

                AggregationType setterType = setter.computeAggregationType(propertyName);
                switch (setterType) {
                    case AS_BASIC_PROPERTY:
                        setter.setProperty(propertyName, (String) value);
                        break;
                    case AS_COMPLEX_PROPERTY:
                        setter.setComplexProperty(propertyName, value);
                        break;
                    case AS_COMPLEX_PROPERTY_COLLECTION:
                        setter.addComplexProperty(propertyName, value);
                        break;
                    default:
                        String msg = String.format("[Property: %s, AggregationType: %s]", propertyName, setterType);
                        throw new UnsupportedOperationException(msg);
                }
            }
        }

        if (logbackObject instanceof ContextAware) {
            ((ContextAware) logbackObject).setContext(context);
        }
        if (logbackObject instanceof LifeCycle) {
            ((LifeCycle) logbackObject).start();
        }
        return logbackObject;
    }
}
