package com.cronutils.model.definition;

import com.cronutils.model.Cron;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.cronutils.validator.CronValidator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/*
 * Copyright 2015 jmrozanec
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class CronDefinitionIssue25IntegrationTest {
    private CronDefinition cronDefinition;
    final String CRON_EXPRESSION = "0 18 1";

    @Before
    public void setUp(){
        cronDefinition =
                CronDefinitionBuilder.defineCron()
                        .withMinutes().and()
                        .withHours().and()
                        .withDayOfWeek().and()
                        .instance();
    }

    @Test
    public void testValidator(){
        CronValidator validator = new CronValidator(cronDefinition);
        assertTrue(validator.isValid(CRON_EXPRESSION));
    }

    @Test
    public void testParser(){
        CronParser parser = new CronParser(cronDefinition);
        Cron cron = parser.parse(CRON_EXPRESSION);
    }

    /**
     * Issue #25: next execution time produces NullPointerException for custom cron definitions.
     */
    public void testExecutionTime(){
        CronParser parser = new CronParser(cronDefinition);
        Cron cron = parser.parse(CRON_EXPRESSION);
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
    }
}
