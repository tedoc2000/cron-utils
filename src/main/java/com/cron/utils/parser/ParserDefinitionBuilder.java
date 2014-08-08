package com.cron.utils.parser;

import com.cron.utils.CronParameter;
import com.cron.utils.parser.field.CronField;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/*
 * Copyright 2014 jmrozanec
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
public class ParserDefinitionBuilder {
    private Map<CronParameter, CronField> fields;
    private boolean lastFieldOptional;

    private ParserDefinitionBuilder() {
        fields = new HashMap<CronParameter, CronField>();
        lastFieldOptional = false;
    }

    public static ParserDefinitionBuilder defineParser() {
        return new ParserDefinitionBuilder();
    }

    public ParserDefinitionBuilder withSeconds() {
        register(CronField.seconds());
        return this;
    }

    public ParserDefinitionBuilder withMinutes() {
        register(CronField.minutes());
        return this;
    }

    public ParserDefinitionBuilder withHours() {
        register(CronField.hours());
        return this;
    }

    public ParserDefinitionBuilder withDayOfMonth() {
        register(CronField.daysOfMonth());
        return this;
    }

    public ParserDefinitionBuilder withMonth() {
        register(CronField.months());
        return this;
    }

    public ParserDefinitionBuilder withDayOfWeek() {
        register(CronField.daysOfWeek());
        return this;
    }

    public ParserDefinitionBuilder withYear() {
        register(CronField.years());
        return this;
    }

    public ParserDefinitionBuilder andLastFieldOptional() {
        lastFieldOptional = true;
        return this;
    }

    private void register(CronField cronField) {
        fields.put(cronField.getField(), cronField);
    }

    public CronParser instance() {
        return new CronParser(new HashSet<CronField>(fields.values()), lastFieldOptional);
    }

}
