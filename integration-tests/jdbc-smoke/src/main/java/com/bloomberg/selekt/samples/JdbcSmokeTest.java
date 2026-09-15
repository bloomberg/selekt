/*
 * Copyright 2026 Bloomberg Finance L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bloomberg.selekt.samples;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class JdbcSmokeTest {
    private JdbcSmokeTest() {
    }

    public static void main(final String[] args) {
        if (args.length != 0) {
            throw new IllegalArgumentException("This smoke test does not accept arguments");
        }

        try (
            Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT 42")
        ) {
            if (!resultSet.next() || resultSet.getInt(1) != 42 || resultSet.next()) {
                throw new AssertionError("Unexpected JDBC query result");
            }
        } catch (SQLException exception) {
            throw new AssertionError("JDBC smoke test failed", exception);
        }
    }
}
