/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol. Use of this source code is governed by the Apache 2.0 license.
 */

package org.hermine.internal.db.driver

import jdk.incubator.sql2.ParameterizedOperation

internal abstract class AbstractParameterizedOperation<T>(
        session: SessionImpl,
        group: OperationGroupImpl<T, *>
) : AbstractOperation<T>(session, group), ParameterizedOperation<T> {

}