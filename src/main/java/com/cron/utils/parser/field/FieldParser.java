package com.cron.utils.parser.field;

import org.apache.commons.lang3.StringUtils;

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
/**
 * Parses a field from a cron expression.
 */
class FieldParser {
    private final char[] specialCharsMinusStar = new char[]{'/', '-', ','};

    private FieldConstraints constraints;

    FieldParser() {
        constraints = FieldConstraints.nullConstraints();
    }

    CronFieldExpression parse(String expression) {
        if (!StringUtils.containsAny(expression, specialCharsMinusStar)) {
            if ("*".equals(expression)) {
                return new Always(constraints);
            } else {
                return new On(constraints, expression);
            }
        } else {
            String[] array = expression.split(",");
            if (array.length > 1) {
                And and = new And();
                for (String exp : array) {
                    and.and(parse(exp));
                }
                return and;
            } else {
                array = expression.split("-");
                if (array.length > 1) {
                    if (array[1].contains("/")) {
                        String[] every = array[1].split("/");
                        return new Between(constraints, array[0], every[0], every[1]);
                    } else {
                        return new Between(constraints, array[0], array[1]);
                    }
                } else {
                    return new Every(constraints, expression.split("/")[1]);
                }
            }
        }
    }


    FieldParser withConstraints(FieldConstraints constraints) {
        this.constraints = constraints;
        return this;
    }
}
