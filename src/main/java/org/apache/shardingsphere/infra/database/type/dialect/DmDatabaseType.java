package org.apache.shardingsphere.infra.database.type.dialect;

import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.database.metadata.dialect.DmDataSourceMetaData;
import org.apache.shardingsphere.infra.database.type.DatabaseType;

import java.util.Collection;
import java.util.Collections;

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
    public Collection<String> getJdbcUrlPrefixes() {
        return Collections.singleton(String.format("jdbc:%s:", "dm".toLowerCase()));
    }

    @Override
    public DataSourceMetaData getDataSourceMetaData(String url, String username) {
        return new DmDataSourceMetaData(url, username);
    }
}
