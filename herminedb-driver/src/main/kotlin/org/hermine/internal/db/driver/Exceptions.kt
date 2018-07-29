/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol. Use of this source code is governed by the Apache 2.0 license.
 */

package org.hermine.internal.db.driver

import java.util.concurrent.CompletionException

internal fun Throwable.unwrapException() = if (this is CompletionException) this.cause else this
