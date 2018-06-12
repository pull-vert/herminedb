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
package org.hermine.db.driver;

import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.DataSourceFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HermineDBDriverTest {

    private static final String URL = "<database connect string>";
    private static final String USER = "<database user name>";
    private static final String PASSWORD = "<database user password>";
    // Define this to be the most trivial SELECT possible
//    private static final String TRIVIAL = "SELECT 1 FROM DUAL";

    private static final String FACTORY_NAME = "org.hermine.db.driver.HermineDataSourceFactory";

    private static final Logger logger = LogManager.getLogger(HermineDBDriverTest.class);

    @Test
    @Disabled
    @DisplayName("Verify that DataSourceFactory.forName works. Can't do anything without that.")
    public void forName() {
        // When
        var name = DataSourceFactory.forName(FACTORY_NAME).getClass().getName();

        // Then
        assertThat(name)
                .as("Verify that the DataSourceFactory found is a HermineDataSourceFactory")
                .isEqualTo("org.hermine.db.driver.HermineDataSourceFactory");

        logger.info("DataSourceFactory.forName class = {}", name);
    }

    @Test
    @DisplayName("Verify that can create a DataSource, though not a Connection. Should work even if there is no database.")
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
}
