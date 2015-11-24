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
package com.github.yihtserns.logback.spring.config.testutil;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * @author yihtserns
 * @see #decide(ILoggingEvent)
 */
public class SpecialWordFilter extends Filter<ILoggingEvent> {

    private static final String SPECIAL_WORD = "[DROP]";

    /**
     * @param event to get the message
     * @return deny if message contains {@value #SPECIAL_WORD}, accept otherwise
     */
    @Override
    public FilterReply decide(ILoggingEvent event) {
        return event.getMessage().contains(SPECIAL_WORD)
                ? FilterReply.DENY
                : FilterReply.ACCEPT;
    }
}