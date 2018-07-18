/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol
 * and explicitly declared author of each file if specified.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.hermine.db.driver;

import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.DataSourceFactory;
import jdk.incubator.sql2.Session;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class JavaHermineDBDriverTest {

    private static final String URL = "<database connect string>";
    private static final String USER = "<database user name>";
    private static final String PASSWORD = "<database user password>";
    // Define this to be the most trivial SELECT possible
//    private static final String TRIVIAL = "SELECT 1 FROM DUAL";

    private static final String FACTORY_NAME = "org.hermine.db.driver.HermineDataSourceFactory";

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaHermineDBDriverTest.class);

    @Test
    @Disabled
    @DisplayName("Verify that DataSourceFactory.forName works. Can't do anything without that.")
    public void forName() {
        // When
        String name = DataSourceFactory.newFactory(FACTORY_NAME).getClass().getName();

        // Then
        assertThat(name)
                .as("Verify that the DataSourceFactory found is a HermineDataSourceFactory")
                .isEqualTo("org.hermine.db.driver.HermineDataSourceFactory");

        LOGGER.info("DataSourceFactory.forName class = {}", name);
    }

    @Test
    @DisplayName("Verify that can create a DataSource, though not a Session. Should work even if there is no database.")
    public void createDataSource() {
        // Given
        DataSourceFactory factory = new HermineDataSourceFactory();

        // When
        DataSource ds = factory.builder()
                .url(URL)
                .username(USER)
                .password(PASSWORD)
                .build();

        // Then
        assertThat(ds)
                .as("Verify that DataSource is created.")
                .isNotNull();
    }

    @Test
    @DisplayName("Verify that can create a Session. Should work even if there is no database.")
    public void createSession() {
        // Given
        DataSourceFactory factory = new HermineDataSourceFactory();

        // When
        DataSource ds = factory.builder()
                .url(URL)
                .username(USER)
                .password(PASSWORD)
                .build();
        try (Session session = ds.getSession(t -> System.out.println("ERROR: " + t.getMessage()))) {
            // Then
            assertThat(session)
                    .as("Verify that Session is created.")
                    .isNotNull();
            LOGGER.info("Session class = {}", session.getClass());
        }
    }
}
