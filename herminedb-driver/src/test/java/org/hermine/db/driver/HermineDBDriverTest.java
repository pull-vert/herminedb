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

import jdk.incubator.sql2.DataSourceFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HermineDBDriverTest {

    private static final String FACTORY_NAME = "org.hermine.db.driver.HermineDataSourceFactory";

    private static final Logger logger = LogManager.getLogger(HermineDBDriverTest.class);

    /**
     * Verify that DataSourceFactory.forName works. Can't do anything without that.
     */
    @Test
    public void forName() {
        assertEquals("org.hermine.db.driver.HermineDataSourceFactory",
                DataSourceFactory.forName(FACTORY_NAME).getClass().getName());
        logger.info("DataSourceFactory.forName class = {}", DataSourceFactory.forName(FACTORY_NAME).getClass().getName());
    }
}
