/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol
 * and explicitly declared author of each file if specified.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.hermine.db.driver

import jdk.incubator.sql2.AdbaType
import jdk.incubator.sql2.DataSourceFactory
import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import kotlin.test.Test

private val URL = "<database connect string>"
private val USER = "<database user name>"
private val PASSWORD = "<database user password>"
private val FACTORY_NAME = "org.hermine.db.driver.HermineDataSourceFactory"

// Place definition above class declaration to make field static
private val logger = KotlinLogging.logger {}

class HermineDBDriverTest {

    @Test
    @Disabled
    fun `Verify that DataSourceFactory#forName works, Can't do anything without that`() {
        // When
        val name = DataSourceFactory.newFactory<DataSourceFactory>(FACTORY_NAME).javaClass.name

        // Then
        assertThat(name)
                .`as`("Verify that the DataSourceFactory found is a HermineDataSourceFactory")
                .isEqualTo("org.hermine.db.driver.HermineDataSourceFactory")

        logger.info("DataSourceFactory.forName class = {}", name)
    }

    @Test
    fun `Verify that can create a DataSource, though not a Session, Should work even if there is no database`() {
        // Given
        val factory = HermineDataSourceFactory()

        // When
        val ds = factory.builder()
                .url(URL)
                .username(USER)
                .password(PASSWORD)
                .build()

        // Then
        assertThat(ds)
                .`as`("Verify that DataSource is created.")
                .isNotNull()
    }

    @Test
    fun `Verify that can create a Session, Should work even if there is no database`() {
        // Given
        val factory = HermineDataSourceFactory()

        // When
        val ds = factory.builder()
                .url(URL)
                .username(USER)
                .password(PASSWORD)
                .build()
        ds.getSession { println("ERROR: " + it.message) }.use { session ->
            // Then
            assertThat(session)
                    .`as`("Verify that Session is created.")
                    .isNotNull()
            logger.info("Session class = {}", session.javaClass)
        }
    }

    // RowCountOperation

    @Test
    @Disabled // todo Enable when rowCountOperation is supported
    fun `Verify that can insert one row in a table`() {
        // Given
        val factory = HermineDataSourceFactory()
        val item = Item(12, "luke", "who's your father ?")

        // When
        val ds = factory.builder()
                .url(URL)
                .username(USER)
                .password(PASSWORD)
                .build()
        ds.getSession { println("ERROR: " + it.message) }.use { session ->
            session.rowCountOperation<Any>("insert into tab values (:id, :name, :answer)")
                    .set("id", item.id, AdbaType.NUMERIC)
                    .set("name", item.name, AdbaType.VARCHAR)
                    .set("answer", item.question, AdbaType.NUMERIC)
                    .submit()
        }
    }
}

data class Item(var id: Int, var name: String, var question: String)
