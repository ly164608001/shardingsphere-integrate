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

package org.apache.shardingsphere.sharding.metadata;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datanode.DataNodes;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.model.logic.spi.LogicMetaDataLoader;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.infra.metadata.model.physical.model.schema.PhysicalSchemaMetaData;
import org.apache.shardingsphere.infra.metadata.model.physical.model.table.PhysicalTableMetaData;
import org.apache.shardingsphere.infra.metadata.model.physical.model.table.PhysicalTableMetaDataLoader;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Meta data loader for sharding.
 */
public final class ShardingMetaDataLoader implements LogicMetaDataLoader<ShardingRule> {

    private static final int CPU_CORES = Runtime.getRuntime().availableProcessors();

    private static final int FUTURE_GET_TIME_OUT_SECOND = 5;

    @Override
    public PhysicalSchemaMetaData load(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, final DataNodes dataNodes,
                                       final ShardingRule rule, final ConfigurationProperties props, final Collection<String> excludedTableNames) throws SQLException {
        PhysicalSchemaMetaData result = new PhysicalSchemaMetaData(new HashMap<>(rule.getTableRules().size(), 1));
        for (TableRule each : rule.getTableRules()) {
            if (!excludedTableNames.contains(each.getLogicTable())) {
                load(databaseType, dataSourceMap, dataNodes, each.getLogicTable(), rule, props).ifPresent(tableMetaData -> result.put(each.getLogicTable(), tableMetaData));
            }
        }
        return result;
    }

    @Override
    public Optional<PhysicalTableMetaData> load(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, final DataNodes dataNodes,
                                                final String tableName, final ShardingRule rule, final ConfigurationProperties props) throws SQLException {
        if (!rule.findTableRule(tableName).isPresent()) {
            return Optional.empty();
        }
        boolean isCheckingMetaData = props.getValue(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED);
        int maxConnectionsSizePerQuery = props.getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        TableRule tableRule = rule.getTableRule(tableName);
        if (!isCheckingMetaData) {
            DataNode dataNode = dataNodes.getDataNodes(tableName).iterator().next();
            return PhysicalTableMetaDataLoader.load(dataSourceMap.get(dataNode.getDataSourceName()), dataNode.getTableName(), databaseType);
        }
        Map<String, PhysicalTableMetaData> actualTableMetaDataMap = parallelLoadTables(databaseType, dataSourceMap, dataNodes, tableName, maxConnectionsSizePerQuery);
        if (actualTableMetaDataMap.isEmpty()) {
            return Optional.empty();
        }
        checkUniformed(tableRule.getLogicTable(), actualTableMetaDataMap, rule);
        return Optional.of(actualTableMetaDataMap.values().iterator().next());
    }

    private Map<String, PhysicalTableMetaData> parallelLoadTables(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, final DataNodes dataNodes,
                                                                  final String tableName, final int maxConnectionsSizePerQuery) {
        Map<String, List<DataNode>> dataNodeGroups = dataNodes.getDataNodeGroups(tableName);
        Map<String, PhysicalTableMetaData> result = new HashMap<>(dataNodeGroups.size(), 1);
        Map<String, Future<Optional<PhysicalTableMetaData>>> tableFutureMap = new HashMap<>(dataNodeGroups.size(), 1);
        ExecutorService executorService = Executors.newFixedThreadPool(Math.min(CPU_CORES * 2, dataNodeGroups.size() * maxConnectionsSizePerQuery));
        for (Entry<String, List<DataNode>> entry : dataNodeGroups.entrySet()) {
            for (DataNode each : entry.getValue()) {
                Future<Optional<PhysicalTableMetaData>> futures = executorService.submit(() -> loadTableByDataNode(each, databaseType, dataSourceMap));
                tableFutureMap.put(each.getTableName(), futures);
            }
        }
        tableFutureMap.forEach((key, value) -> {
            try {
                getTableMetaData(value).ifPresent(tableMetaData -> result.put(key, tableMetaData));
            } catch (final InterruptedException | ExecutionException | TimeoutException ex) {
                throw new IllegalStateException(String.format("Error while fetching tableMetaData with key= %s and Value=%s", key, value), ex);
            }
        });
        executorService.shutdownNow();
        return result;
    }

    private Optional<PhysicalTableMetaData> getTableMetaData(final Future<Optional<PhysicalTableMetaData>> value) throws InterruptedException, ExecutionException, TimeoutException {
        return value.get(FUTURE_GET_TIME_OUT_SECOND, TimeUnit.SECONDS);
    }

    private Optional<PhysicalTableMetaData> loadTableByDataNode(final DataNode dataNode, final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap) {
        try {
            return PhysicalTableMetaDataLoader.load(dataSourceMap.get(dataNode.getDataSourceName()), dataNode.getTableName(), databaseType);
        } catch (final SQLException ex) {
            throw new IllegalStateException(String.format("SQLException for DataNode=%s and databaseType=%s", dataNode, databaseType.getName()), ex);
        }
    }

    private void checkUniformed(final String logicTableName, final Map<String, PhysicalTableMetaData> actualTableMetaDataMap, final ShardingRule shardingRule) {
        ShardingMetaDataDecorator decorator = new ShardingMetaDataDecorator();
        PhysicalTableMetaData sample = decorator.decorate(logicTableName, actualTableMetaDataMap.values().iterator().next(), shardingRule);
        Collection<TableMetaDataViolation> violations = actualTableMetaDataMap.entrySet().stream()
                .filter(entry -> !sample.equals(decorator.decorate(logicTableName, entry.getValue(), shardingRule)))
                .map(entry -> new TableMetaDataViolation(entry.getKey(), entry.getValue())).collect(Collectors.toList());
        throwExceptionIfNecessary(violations, logicTableName);
    }

    private void throwExceptionIfNecessary(final Collection<TableMetaDataViolation> violations, final String logicTableName) {
        if (!violations.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder(
                    "Cannot get uniformed table structure for logic table `%s`, it has different meta data of actual tables are as follows:").append(System.lineSeparator());
            for (TableMetaDataViolation each : violations) {
                errorMessage.append("actual table: ").append(each.getActualTableName()).append(", meta data: ").append(each.getTableMetaData()).append(System.lineSeparator());
            }
            throw new ShardingSphereException(errorMessage.toString(), logicTableName);
        }
    }

    @Override
    public int getOrder() {
        return ShardingOrder.ORDER;
    }

    @Override
    public Class<ShardingRule> getTypeClass() {
        return ShardingRule.class;
    }

    @RequiredArgsConstructor
    @Getter
    private static final class TableMetaDataViolation {

        private final String actualTableName;

        private final PhysicalTableMetaData tableMetaData;
    }
}
