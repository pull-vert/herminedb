/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol. Use of this source code is governed by the Apache 2.0 license.
 */

package org.hermine.adba.kotlin.internal

import jdk.incubator.sql2.AdbaType
import jdk.incubator.sql2.DataSource
import jdk.incubator.sql2.DataSourceFactory
import org.hermine.adba.kotlin.getAt
import org.hermine.adba.kotlin.newDataSourceFactory

private fun getDataSource(url: String, user: String, pass: String): DataSource {
    return newDataSourceFactory<DataSourceFactory>() // top level function
            .builder()
            .url(url)
            .username(user)
            .password(pass)
            .build()
}

// RowCountOperation

private fun insertItem(ds: DataSource, item: Item) {
    ds.session.use { session -> // 'use' will close session automatically
        session.rowCountOperation<Any>("insert into tab values (:id, :name, :answer)")
                .set("id", item.id, AdbaType.NUMERIC)
                .set("name", item.name, AdbaType.VARCHAR)
                .set("answer", item.answer, AdbaType.NUMERIC)
                .submit()
    }
}

// RowOperation

private fun idsForAnswer(ds: DataSource, result: MutableList<Int>, correctAnswer: Int) {
    ds.session.use { session ->
        session.rowOperation<List<Int>>("select id, name, answer from tab where answer = :target")
                .set("target", correctAnswer, AdbaType.NUMERIC)
                .collect({ result },
                        { list, row -> list.add(row.getAt("id")) }) // Generic Int type is inferred ! No need here to write row.getAt<Int>("id")
                .submit()
    }
}

private data class Item(var id: Int, var name: String, var answer: Int)