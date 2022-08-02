package org.apache.shardingsphere.infra.database.type.dialect;

import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.database.metadata.dialect.DmDataSourceMetaData;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.sql.common.constant.QuoteCharacter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * @author ly
 * @desc Database type of Dm
 * @date 2021-08-11
 */
public final class DmDatabaseType implements DatabaseType {

    @Override
    public String getName() {
        return "Oracle";
    }

    @Override
    public QuoteCharacter getQuoteCharacter() {
        return QuoteCharacter.QUOTE;
    }

    @Override
    public Collection<String> getJdbcUrlPrefixes() {
        return Collections.singleton(String.format("jdbc:%s:", "dm".toLowerCase()));
    }

    @Override
    public DataSourceMetaData getDataSourceMetaData(String url, String username) {
        return new DmDataSourceMetaData(url, username);
    }

    @Override
    public String getSchema(Connection connection) {
        try {
            return (String) Optional.ofNullable(connection.getMetaData().getUserName()).map(String::toUpperCase).orElse(null);
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public String formatTableNamePattern(String tableNamePattern) {
        return tableNamePattern.toUpperCase();
    }
}
