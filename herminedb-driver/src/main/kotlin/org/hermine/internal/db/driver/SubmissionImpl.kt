/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol. Use of this source code is governed by the Apache 2.0 license.
 */

package org.hermine.internal.db.driver

import jdk.incubator.sql2.Submission
import kotlinx.coroutines.experimental.Deferred
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

internal class SubmissionImpl<T>(
        val cancel: () -> Boolean,
        val result: Deferred<T?>
) : Submission<T> {
    private var publicStage: CompletionStage<T?>? = null

    override fun cancel(): CompletionStage<Boolean> {
        return CompletableFuture<Boolean>().completeAsync(cancel)
    }

    override fun getCompletionStage(): CompletionStage<T?> {
        if (publicStage == null) publicStage = DeferredCompletionStage(result)
        return publicStage!!
    }
}
