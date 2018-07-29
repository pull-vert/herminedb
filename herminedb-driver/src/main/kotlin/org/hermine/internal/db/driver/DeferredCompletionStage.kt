/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol. Use of this source code is governed by the Apache 2.0 license.
 */

package org.hermine.internal.db.driver

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.future.asCompletableFuture
import java.util.concurrent.CompletionStage

class DeferredCompletionStage<T>(delegate: Deferred<T>) : Deferred<T> by delegate, CompletionStage<T> by delegate.asCompletableFuture()
