/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol,
 * and explicitly declared author of this file if provided.
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
package org.hermine.internal.db.driver;

import jdk.incubator.sql2.ConnectionProperty;
import reactor.core.scheduler.Scheduler;
import reactor.scheduler.forkjoin.ForkJoinPoolScheduler;

import java.util.function.Function;

public enum HermineConnectionProperty implements ConnectionProperty {

    /**
     *
     */
    SCHEDULER(Scheduler.class,
            v -> v instanceof Scheduler,
            ForkJoinPoolScheduler.create("HermineScheduler"),
            false);

    private final Class<?> range;
    private final Function<Object, Boolean> validator;
    private final Object defaultValue;
    private final boolean isSensitive;

    private HermineConnectionProperty(Class<?> range,
                                   Function<Object, Boolean> validator,
                                   Object value,
                                   boolean isSensitive) {
        this.range = range;
        this.validator = validator;
        this.defaultValue = value;
        this.isSensitive = isSensitive;
    }

    @Override
    public Class<?> range() {
        return range;
    }

    @Override
    public boolean validate(Object value) {
        return validator.apply(value);
    }

    @Override
    public Object defaultValue() {
        return defaultValue;
    }

    @Override
    public boolean isSensitive() {
        return isSensitive;
    }
}
