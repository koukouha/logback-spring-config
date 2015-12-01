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
import ch.qos.logback.core.joran.spi.NoAutoStartUtil;
import ch.qos.logback.core.joran.util.PropertySetter;
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.util.AggregationType;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.LoggerFactory;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.beans.factory.BeanInitializationException;

/**
 * @author yihtserns
 * @see #assemble(Appender, Map)
 */
public class LogbackObjectPropertiesAssembler {

    /**
     * Use Logback's API to set values to setXXX(value) and addXXX(value) methods because Spring only supports the former.
     *
     * @param logbackObject object to set property values to
     * @param property2ValueList property-to-value pairs (see {@link LogbackNamespaceHandler.Pair})
     * @return the given object
     */
    public static Object assemble(Object logbackObject, List<Map<String, Object>> property2ValueList) {
        Context logbackContext = (Context) LoggerFactory.getILoggerFactory();
        LogbackComponent logbackComponent = new LogbackComponent(logbackObject, logbackContext);

        for (Map<String, Object> property2Value : property2ValueList) {
            for (Entry<String, Object> entry : property2Value.entrySet()) {
                String propertyName = entry.getKey();
                Object propertyValue = entry.getValue();

                logbackComponent.setOrAddProperty(propertyName, propertyValue);
            }
        }

        return logbackObject;
    }

    private static final class LogbackComponent extends PropertySetter {

        public LogbackComponent(Object obj, Context logbackContext) {
            super(obj);
            setContext(logbackContext);
            injectLogbackContextIntoObjIfApplicable();
        }

        public void setOrAddProperty(String name, Object value) {
            AggregationType propertyType = computeAggregationType(name);

            if (isComplexType(propertyType)) {
                LogbackComponent childComponent = new LogbackComponent(value, getContext());

                childComponent.setPropertyIfExists("parent", getObj());
                childComponent.startIfApplicable();
            }

            // Inject property value into Logback object
            switch (propertyType) {
                case AS_BASIC_PROPERTY:
                    setProperty(name, (String) value);
                    break;
                case AS_BASIC_PROPERTY_COLLECTION:
                    addBasicProperty(name, (String) value);
                    break;
                case AS_COMPLEX_PROPERTY:
                    setComplexProperty(name, value);
                    break;
                case AS_COMPLEX_PROPERTY_COLLECTION:
                    addComplexProperty(name, value);
                    break;
                case NOT_FOUND: {
                    String msg = String.format("Bean property '%s' is not writable or has an invalid setter/adder method.", name);
                    throw new NotWritablePropertyException(getObjClass(), name, msg);
                }
                default: {
                    String msg = String.format("[Property: %s, AggregationType: %s]", name, propertyType);
                    throw new UnsupportedOperationException(msg);
                }
            }
        }

        public void setPropertyIfExists(String name, Object value) {
            if (computeAggregationType(name) == AggregationType.AS_COMPLEX_PROPERTY) {
                setComplexProperty(name, value);
            }
        }

        public void startIfApplicable() {
            Object candidate = getObj();

            if (candidate instanceof LifeCycle && NoAutoStartUtil.notMarkedWithNoAutoStart(candidate)) {
                ((LifeCycle) candidate).start();
            }
        }

        @Override
        public void addWarn(String msg) {
            addError(msg);
        }

        @Override
        public void addWarn(String msg, Throwable ex) {
            addError(msg, ex);
        }

        @Override
        public void addError(String msg) {
            throw new BeanInitializationException(msg);
        }

        @Override
        public void addError(String msg, Throwable ex) {
            throw new BeanInitializationException(msg, ex);
        }

        private void injectLogbackContextIntoObjIfApplicable() {
            Object candidate = getObj();

            if (candidate instanceof ContextAware) {
                ((ContextAware) candidate).setContext(context);
            }
        }

        private static boolean isComplexType(AggregationType propertyType) {
            return propertyType == AggregationType.AS_COMPLEX_PROPERTY
                    || propertyType == AggregationType.AS_COMPLEX_PROPERTY_COLLECTION;
        }
    }
}
