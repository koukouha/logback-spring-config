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
import ch.qos.logback.core.filter.Filter;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author yihtserns
 */
public class AppenderFactoryBean implements InitializingBean, FactoryBean {

    private Appender appender;
    private Filter filter;

    public void setAppender(Appender appender) {
        this.appender = appender;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public void afterPropertiesSet() {
        appender.addFilter(filter);
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
