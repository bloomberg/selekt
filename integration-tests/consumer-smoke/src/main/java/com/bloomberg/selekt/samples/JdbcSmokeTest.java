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

import com.bloomberg.selekt.jdbc.driver.SelektDataSource;
import com.bloomberg.selekt.jdbc.micrometer.SelektDataSourceMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class JdbcSmokeTest {
    private JdbcSmokeTest() {
    }

    public static void main(final String[] args) {
        BackendSelection.assertSelectedBackend(args);

        final SelektDataSource dataSource = new SelektDataSource();
        dataSource.setDatabasePath(":memory:");
        final SimpleMeterRegistry registry = new SimpleMeterRegistry();
        new SelektDataSourceMetrics(dataSource).bindTo(registry);
        try (
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT 42")
        ) {
            if (!resultSet.next() || resultSet.getInt(1) != 42 || resultSet.next()) {
                throw new AssertionError("Unexpected JDBC query result");
            }
            final double activeConnections = registry.get("selekt.jdbc.connections.active").gauge().value();
            if (Math.abs(activeConnections - 1.0) > 1e-6) {
                throw new AssertionError("Unexpected active connection metric");
            }
        } catch (SQLException exception) {
            throw new AssertionError("JDBC smoke test failed", exception);
        } finally {
            dataSource.close();
            registry.close();
        }
    }
}
