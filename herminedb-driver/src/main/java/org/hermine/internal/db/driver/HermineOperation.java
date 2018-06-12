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

import jdk.incubator.sql2.AdbaType;
import jdk.incubator.sql2.Operation;
import jdk.incubator.sql2.SqlType;
import jdk.incubator.sql2.Submission;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.time.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * TODO : not based on Completion Stage but on close of previous Flow Publisher
 */
abstract class HermineOperation<T> implements Operation<T> {

    private static final Map<Class, AdbaType> CLASS_TO_ADBATYPE = new HashMap<>(20);
    static {
        try {
            CLASS_TO_ADBATYPE.put(BigInteger.class, AdbaType.BIGINT);
            CLASS_TO_ADBATYPE.put(Boolean.class, AdbaType.BOOLEAN);
            CLASS_TO_ADBATYPE.put(Byte.class, AdbaType.TINYINT);
            CLASS_TO_ADBATYPE.put(Class.forName("[B"), AdbaType.VARBINARY);
            CLASS_TO_ADBATYPE.put(Character.class, AdbaType.CHAR);
            CLASS_TO_ADBATYPE.put(Double.class, AdbaType.DOUBLE);
            CLASS_TO_ADBATYPE.put(Float.class, AdbaType.FLOAT);
            CLASS_TO_ADBATYPE.put(Integer.class, AdbaType.INTEGER);
            CLASS_TO_ADBATYPE.put(LocalDate.class, AdbaType.DATE);
            CLASS_TO_ADBATYPE.put(LocalDateTime.class, AdbaType.TIMESTAMP);
            CLASS_TO_ADBATYPE.put(LocalTime.class, AdbaType.TIME);
            CLASS_TO_ADBATYPE.put(OffsetDateTime.class, AdbaType.TIMESTAMP_WITH_TIMEZONE);
            CLASS_TO_ADBATYPE.put(OffsetTime.class, AdbaType.TIME_WITH_TIMEZONE);
            CLASS_TO_ADBATYPE.put(Short.class, AdbaType.SMALLINT);
            CLASS_TO_ADBATYPE.put(String.class, AdbaType.VARCHAR);
        }
        catch (ClassNotFoundException ex) { /* should never happen */ }
    }

    /**
     * Find the default SQLType to represent a Java type.
     *
     * @param c a Java type
     * @return the default SQLType to represent the Java type
     */
    static SqlType toSQLType(Class c) {
        var s = CLASS_TO_ADBATYPE.get(c);
        if (s == null) {
            throw new UnsupportedOperationException(
                    MessageFormat.format("{0} is not Not supported yet.", c.getSimpleName()));
        }
        return s;
    }


    // attributes
    private Duration timeout = null;
    private Consumer<Throwable> errorHandler = null;

    // internal state
    private final HermineConnection connection;
    private final HermineOperationGroup<T, ?> group;

    HermineOperation(HermineConnection conn, HermineOperationGroup operationGroup) {
        // passing null for connection and operationGroup is a hack. It is not
        // possible to pass _this_ to a super constructor so we define null to mean
        // _this_. Yuck. Only used by Connection.
        connection = conn == null ? (HermineConnection) this : conn;
        group = operationGroup == null ? (HermineOperationGroup) this : operationGroup;
    }

    @Override
    public HermineOperation<T> onError(Consumer<Throwable> handler) {
        return this;
    }

    @Override
    public HermineOperation<T> timeout(Duration minTime) {
        return this;
    }

    @Override
    public Submission<T> submit() {
        return null;
    }

    Executor getExecutor() {
        return connection.getExecutor();
    }
}
