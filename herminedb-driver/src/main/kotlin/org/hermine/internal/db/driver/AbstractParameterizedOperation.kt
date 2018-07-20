package org.hermine.internal.db.driver

import jdk.incubator.sql2.ParameterizedOperation

internal abstract class AbstractParameterizedOperation<T>(
        session: SessionImpl,
        group: OperationGroupImpl<T, *>
) : AbstractOperation<T>(session, group), ParameterizedOperation<T> {

}