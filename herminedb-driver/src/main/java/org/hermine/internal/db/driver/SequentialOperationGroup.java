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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

class SequentialOperationGroup<S, T> extends AbstractOperationGroup<S, T> {

    private static final Logger LOGGER = LogManager.getLogger(SequentialOperationGroup.class);

    boolean heldFinished = false;

    /**
     * completed when this OperationGroup is no longer held. Completion of this
     * OperationGroup depends on held.
     *
     * @see #submit,
     * @see #releaseProhibitingMoreMembers,
     * @see #submitHoldingForMoreMembers
     */
    private Mono held;

    /**
     * predecessor of all member Operations and the OperationGroup itself
     */
    private Mono head;

    /**
     * The last Mono of any submitted member Operation. Mutable until
     * not isHeld().
     */
    private Mono<S> memberTail;

    SequentialOperationGroup(ConnectionImpl conn, AbstractOperationGroup<? super T, ?> group) {
        super(conn, group);
        held = Mono.empty()
            .doOnSuccessOrError((o, throwable) -> heldFinished = true);
        head = Mono.empty();
        memberTail = head;
    }

    // Override OperationGroup methods

    @Override
    public SubmissionImpl<T> releaseProhibitingMoreMembers() {
        LOGGER.info("SequentialOperationGroup#releaseProhibitingMoreMembers");
        if ( ! isImmutable() || ! isHeld() ) throw new IllegalStateException("TODO");
        held.subscribe();
        immutable();  // having set isHeld to false this call will make this OpGrp immutable
        return submission;
    }

    @Override
    public SubmissionImpl<T> submit() {
        LOGGER.info("SequentialOperationGroup#submit");
        if ( isImmutable() ) throw new IllegalStateException("TODO");
        accumulator = collector.supplier().get();
        SubmissionImpl<T> sub = super.submit();
        held.subscribe();
        return sub;
    }

    // Internal methods

    @Override
    Mono<T> follows(Mono<?> predecessor, Scheduler scheduler) {
        LOGGER.info("SequentialOperationGroup#follows");
        return condition.flatMap(cond -> {
                    if (cond) {
                        head = head.thenReturn(predecessor);
                        held = held.then(memberTail.map(t -> (T)collector.finisher()
                                        .apply(accumulator))
                                .publishOn(scheduler)
                        );
                        return held;
                    }
                    else {
                        return Mono.empty();
                    }
                }
        );
    }

    @Override
    protected boolean isHeld() {
        LOGGER.info("SequentialOperationGroup#isHeld {}", !heldFinished);
        return !heldFinished;
    }
}
