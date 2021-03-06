package com.cronutils.model.time;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
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
public class ExecutionTimeQuartzIntegrationTest {
    private CronParser quartzCronParser;
    private static final String EVERY_SECOND = "* * * * * * *";

    @Before
    public void setUp(){
        quartzCronParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
    }

    @Test
    public void testForCron() throws Exception {
        assertEquals(ExecutionTime.class, ExecutionTime.forCron(quartzCronParser.parse(EVERY_SECOND)).getClass());
    }

    @Test
    public void testNextExecutionEverySecond() throws Exception {
        DateTime now = truncateToSeconds(DateTime.now());
        DateTime expected = truncateToSeconds(now.plusSeconds(1));
        ExecutionTime executionTime = ExecutionTime.forCron(quartzCronParser.parse(EVERY_SECOND));
        assertEquals(expected, executionTime.nextExecution(now));
    }

    @Test
    public void testTimeToNextExecution() throws Exception {
        DateTime now = truncateToSeconds(DateTime.now());
        DateTime expected = truncateToSeconds(now.plusSeconds(1));
        ExecutionTime executionTime = ExecutionTime.forCron(quartzCronParser.parse(EVERY_SECOND));
        assertEquals(new Interval(now, expected).toDuration(), executionTime.timeToNextExecution(now));
    }

    @Test
    public void testLastExecution() throws Exception {
        DateTime now = truncateToSeconds(DateTime.now());
        DateTime expected = truncateToSeconds(now.minusSeconds(1));
        ExecutionTime executionTime = ExecutionTime.forCron(quartzCronParser.parse(EVERY_SECOND));
        assertEquals(expected, executionTime.lastExecution(now));
    }

    @Test
    public void testTimeFromLastExecution() throws Exception {
        DateTime now = truncateToSeconds(DateTime.now());
        DateTime expected = truncateToSeconds(now.minusSeconds(1));
        ExecutionTime executionTime = ExecutionTime.forCron(quartzCronParser.parse(EVERY_SECOND));
        assertEquals(new Interval(expected, now).toDuration(), executionTime.timeFromLastExecution(now));
    }

    /**
     * Test for issue #9
     * https://github.com/jmrozanec/cron-utils/issues/9
     * Reported case: If you write a cron expression that contains a month or day of week, nextExection() ignores it.
     * Expected: should not ignore month or day of week field
     */
    @Test
    public void testDoesNotIgnoreMonthOrDayOfWeek(){
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        CronParser cronParser = new CronParser(cronDefinition);
        //seconds, minutes, hours, dayOfMonth, month, dayOfWeek
        ExecutionTime executionTime = ExecutionTime.forCron(cronParser.parse("0 11 11 11 11 ?"));
        DateTime now = new DateTime(2015, 4, 15, 0, 0, 0);
        DateTime whenToExecuteNext = executionTime.nextExecution(now);
        assertEquals(2015, whenToExecuteNext.getYear());
        assertEquals(11, whenToExecuteNext.getMonthOfYear());
        assertEquals(1, whenToExecuteNext.getDayOfMonth());
        assertEquals(11, whenToExecuteNext.getHourOfDay());
        assertEquals(11, whenToExecuteNext.getMinuteOfHour());
        assertEquals(0, whenToExecuteNext.getSecondOfMinute());
    }

    /**
     * Test for issue #18
     * @throws Exception
     */
    @Test
    public void testHourlyIntervalTimeFromLastExecution() throws Exception {
        DateTime now = DateTime.now();
        DateTime previousHour = now.minusHours(1);
        String quartzCronExpression = String.format("0 0 %s * * ?", previousHour.getHourOfDay());
        ExecutionTime executionTime = ExecutionTime.forCron(quartzCronParser.parse(quartzCronExpression));

        assertTrue(executionTime.timeFromLastExecution(now).getStandardMinutes() <= 120);
    }

    /**
     * Test for issue #19
     * https://github.com/jmrozanec/cron-utils/issues/19
     * Reported case: When nextExecution shifts to the 24th hour (e.g. 23:59:59 + 00:00:01), JodaTime will throw an exception
     * Expected: should shift one day
     */
    @Test
    public void testShiftTo24thHour() {
        String expression = "0/1 * * 1/1 * ? *";  // every second every day
        ExecutionTime executionTime = ExecutionTime.forCron(quartzCronParser.parse(expression));

        DateTime now = new DateTime().withTime(23, 59, 59, 0);
        DateTime expected = now.plusSeconds(1);
        DateTime nextExecution = executionTime.nextExecution(now);

        assertEquals(expected, nextExecution);
    }

    /**
     * Test for issue #19
     * https://github.com/jmrozanec/cron-utils/issues/19
     * Reported case: When nextExecution shifts to 32nd day (e.g. 2015-01-31 23:59:59 + 00:00:01), JodaTime will throw an exception
     * Expected: should shift one month
     */
    @Test
    public void testShiftTo32ndDay() {
        String expression = "0/1 * * 1/1 * ? *";  // every second every day
        ExecutionTime executionTime = ExecutionTime.forCron(quartzCronParser.parse(expression));

        DateTime now = new DateTime(2015, 1, 31, 23, 59, 59, 0);
        DateTime expected = now.plusSeconds(1);
        DateTime nextExecution = executionTime.nextExecution(now);

        assertEquals(expected, nextExecution);
    }

    /**
     * Issue #24: next execution not properly calculated
     */
    @Test
    public void testTimeShiftingProperlyDone() throws Exception {
        ExecutionTime executionTime = ExecutionTime.forCron(quartzCronParser.parse("0 0/10 22 * * *"));
        DateTime nextExecution =
                executionTime.nextExecution(
                        DateTime.now()
                                .withHourOfDay(15)
                                .withMinuteOfHour(27)
                );
        assertEquals(22, nextExecution.getHourOfDay());
        assertEquals(0, nextExecution.getMinuteOfHour());
    }

    private DateTime truncateToSeconds(DateTime dateTime){
        return new DateTime(
                dateTime.getYear(),
                dateTime.getMonthOfYear(),
                dateTime.getDayOfMonth(),
                dateTime.getHourOfDay(),
                dateTime.getMinuteOfHour(),
                dateTime.getSecondOfMinute()
        );
    }
}