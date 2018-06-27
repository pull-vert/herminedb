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
import jdk.incubator.sql2.SqlSkippedException;
import jdk.incubator.sql2.SqlType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

abstract class AbstractOperation<T> implements Operation<T> {

    private static final Logger LOGGER = LogManager.getLogger(AbstractOperation.class);

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
            CLASS_TO_ADBATYPE.put(OffsetDateTime.class, AdbaType.TIMESTAMP_WITH_TIME_ZONE);
            CLASS_TO_ADBATYPE.put(OffsetTime.class, AdbaType.TIME_WITH_TIME_ZONE);
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
    Consumer<Throwable> errorHandler = null;

    // internal state
    private final ConnectionImpl connection;
    private final AbstractOperationGroup<T, ?> group;
    protected OperationLifecycle operationLifecycle = OperationLifecycle.MUTABLE;

    AbstractOperation(ConnectionImpl conn, AbstractOperationGroup operationGroup) {
        // passing null for connection and operationGroup is a hack. It is not
        // possible to pass _this_ to a super constructor so we define null to mean
        // _this_. Yuck. Only used by Connection.
        connection = conn == null ? (ConnectionImpl) this : conn;
        group = operationGroup == null ? (AbstractOperationGroup) this : operationGroup;
    }

    @Override
    public AbstractOperation<T> onError(Consumer<Throwable> handler) {
        if (isImmutable() || errorHandler != null) {
            throw new IllegalStateException("TODO");
        }
        if (handler == null) {
            throw new IllegalArgumentException("TODO");
        }
        errorHandler = handler;
        return this;
    }

    @Override
    public AbstractOperation<T> timeout(Duration minTime) {
        if (isImmutable() || timeout != null) {
            throw new IllegalStateException("TODO");
        }
        if (minTime == null || minTime.isNegative() || minTime.isZero()) {
            throw new IllegalArgumentException("minTime must be > 0");
        }
        timeout = minTime;
        return this;
    }

    @Override
    public SubmissionImpl<T> submit() {
        LOGGER.info("AbstractOperation#submit");
        if (isImmutable()) {
            throw new IllegalStateException("TODO");
        }
        immutable();
        return group.submit(this);
    }

    /**
     * Returns true if this Operation is immutable. An Operation is immutable if
     * it has been submitted. Held OperationGroups are an exception.
     *
     * @return return true if immutable
     */
    boolean isImmutable() {
        LOGGER.info("AbstractOperation#isImmutable");
        return operationLifecycle.isImmutable();
    }

    protected AbstractOperation<T> immutable() {
        operationLifecycle = OperationLifecycle.RELEASED;
        return this;
    }

    long getTimeoutMillis() {
        if (timeout == null) {
            return 0L;
        }
        else {
            return timeout.get(ChronoUnit.MILLIS);
        }
    }

    Scheduler getScheduler() {
        return connection.getScheduler();
    }

    /**
     * Attaches the Mono that starts this Operation to the tail and
     * return a Mono that represents completion of this Operation.
     * The returned Mono may not be directly attached to the tail,
     * but completion of the tail should result in completion of the returned
     * Mono. (Note: Not quite true for OperationGroups submitted by
     * calling submitHoldingForMoreMembers. While the returned Mono
     * does depend on the tail, it also depends on user code calling
     * releaseProhibitingMoreMembers.)
     *
     * @param tail the predecessor of this operation. Completion of tail starts
     * execution of this Operation
     * @param scheduler used for asynchronous execution
     * @return completion of this Mono means this Operation is
     * complete. The value of the Operation is the value of the Mono.
     */
    abstract Mono<T> follows(Mono<?> tail, Scheduler scheduler);

    boolean cancel() {
        if (operationLifecycle.isFinished()) {
            return false;
        }
        else {
            operationLifecycle = OperationLifecycle.CANCELED;
            return true;
        }
    }

    boolean isCanceled() {
        return operationLifecycle.isCanceled();
    }

    AbstractOperation<T> checkCanceled() {
        if (isCanceled()) {
            throw new SqlSkippedException("TODO", null, null, -1, null, -1);
        }
        return this;
    }


    /**
     * If an errorHandler is specified, attach a CompletableFuture to the argument
     * that will call the errorHandler in event the argument completes
     * exceptionally and return that CompletableFuture. If there is no errorHandle
     * specified, return the argument.
     *
     * @param result A CompletionStage that may complete exceptionally
     * @return a CompletableFuture that will call the errorHandle if any.
     */
    Mono<T> attachErrorHandler(Mono<T> result) {
        LOGGER.info("AbstractOperation#attachErrorHandler");
        if (errorHandler != null) {
            return result.onErrorMap(t -> {
                Throwable ex = Exceptions.unwrapException(t);
                errorHandler.accept(ex);
                if (ex instanceof SqlSkippedException) return ex;
                else return new SqlSkippedException("TODO", ex, null, -1, null, -1);
            });
        }
        else {
            return result;
        }
    }

    static enum OperationLifecycle {
        MUTABLE,
        HELD,
        RELEASED,
        COMPLETED,
        CANCELED;

        /**
         * @return true if op has been submitted which means no more configuration
         */
        boolean isSubmitted() {
            return this != MUTABLE;
        }

        /**
         * @return return true if no new members may be added. Implies isSubmitted
         */
        boolean isImmutable() { //TODO better name?
            return this == RELEASED || this == COMPLETED || this == CANCELED;
        }

        boolean isFinished() {
            return this == COMPLETED || this == CANCELED;
        }

        boolean isCanceled() {
            return this == CANCELED;
        }
    }
}
