///*
// * Copyright 2018 HermineDB's author : Frédéric Montariol. Use of this source code is governed by the Apache 2.0 license.
// */
//
//package org.hermine.internal.db.driver
//
//import jdk.incubator.sql2.ParameterizedOperation
//import jdk.incubator.sql2.SqlType
//import java.util.concurrent.CompletionStage
//import jdk.incubator.sql2.AdbaType
//
//
//
//
//internal abstract class AbstractParameterizedOperation<T>(
//        session: SessionImpl,
//        group: OperationGroupImpl<T, *>
//) : AbstractOperation<T>(session, group), ParameterizedOperation<T> {
//
//    protected val setParameters: Map<String, ParameterValue> = mutableMapOf()
//
//    override fun set(id: String?, value: Any, type: SqlType?): ParameterizedOperation<T> {
//        if (isImmutable() || setParameters.containsKey(id)) {
//            throw IllegalStateException("TODO")
//        }
//        if (id == null || type != null && type !is AdbaType) {
//            throw IllegalArgumentException("TODO")
//        }
//        if (value is CompletionStage<*>) {
//            if (futureParameters == null) {
//                futureParameters = value
//                        .thenAccept { v -> setParameters.put(id, ParameterValue(v, type!!)) }
//            } else {
//                futureParameters = value
//                        .thenAcceptBoth(futureParameters
//                        ) { v, f -> setParameters.put(id, ParameterValue(v, type!!)) }
//            }
//        } else {
//            setParameters.put(id, ParameterValue(value, type!!))
//        }
//        return this
//    }
//
//    override fun set(id: String, source: CompletionStage<*>, type: SqlType): ParameterizedOperation<T> {
//        return set(id, source as Any, type)
//    }
//
//    override fun set(id: String, source: CompletionStage<*>): ParameterizedOperation<T> {
//        return set(id, source as Any, null)
//    }
//
//    override fun set(id: String, value: Any): ParameterizedOperation<T> {
//        return set(id, value, null)
//    }
//}
//
//internal data class ParameterValue(val value: Any, val type: SqlType) {
//
//}
