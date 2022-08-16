/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.infra.database.type.dialect;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.database.metadata.dialect.DmDataSourceMetaData;
import org.apache.shardingsphere.infra.database.metadata.dialect.KingBaseDataSourceMetaData;
import org.apache.shardingsphere.infra.database.metadata.dialect.OracleDataSourceMetaData;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.sql.common.constant.QuoteCharacter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Database type of Oracle.
 */
public final class OracleDatabaseType implements DatabaseType {

    private static final String DM_PREFIX = "jdbc:dm";

    private static final String KINGBASE_PREFIX = "jdbc:kingbase";

    @Override
    public String getType() {
        return "Oracle";
    }

    @Override
    public QuoteCharacter getQuoteCharacter() {
        return QuoteCharacter.QUOTE;
    }

    @Override
    public Collection<String> getJdbcUrlPrefixes() {
        return Lists.newArrayList(String.format("jdbc:%s:", getType().toLowerCase()),
                String.format("jdbc:%s:", "dm".toLowerCase()),String.format("jdbc:%s", "kingbase".toLowerCase()));
    }

    @Override
    public DataSourceMetaData getDataSourceMetaData(final String url, final String username) {
        if(url.startsWith(DM_PREFIX)) {
            return new DmDataSourceMetaData(url, username);
        }else if(url.startsWith(KINGBASE_PREFIX)) {
            return new KingBaseDataSourceMetaData(url, username);
        }
        return new OracleDataSourceMetaData(url, username);
    }

    @Override
    public Map<String, Collection<String>> getSystemDatabaseSchemaMap() {
        return Collections.emptyMap();
    }

    @Override
    public Collection<String> getSystemSchemas() {
        return Collections.emptyList();
    }

    @Override
    public String getSchema(Connection connection) {
        try {
            return (String) Optional.ofNullable(connection.getMetaData().getUserName()).map(String::toUpperCase).orElse(null);
        } catch (SQLException var3) {
            return null;
        }
    }

    @Override
    public String formatTableNamePattern(String tableNamePattern) {
        return tableNamePattern.toUpperCase();
    }
}
