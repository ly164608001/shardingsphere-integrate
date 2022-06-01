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

package org.apache.shardingsphere.sharding.merge.dql;

import com.alibaba.druid.proxy.jdbc.ResultSetMetaDataProxyImpl;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.binder.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.executor.sql.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.queryresult.MemoryQueryResult;
import org.apache.shardingsphere.infra.merge.engine.merger.ResultMerger;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.model.physical.model.schema.PhysicalSchemaMetaData;
import org.apache.shardingsphere.sharding.merge.dql.groupby.GroupByMemoryMergedResult;
import org.apache.shardingsphere.sharding.merge.dql.groupby.GroupByStreamMergedResult;
import org.apache.shardingsphere.sharding.merge.dql.iterator.IteratorStreamMergedResult;
import org.apache.shardingsphere.sharding.merge.dql.orderby.OrderByStreamMergedResult;
import org.apache.shardingsphere.sharding.merge.dql.pagination.LimitDecoratorMergedResult;
import org.apache.shardingsphere.sharding.merge.dql.pagination.RowNumberDecoratorMergedResult;
import org.apache.shardingsphere.sharding.merge.dql.pagination.TopAndRowNumberDecoratorMergedResult;
import org.apache.shardingsphere.sql.parser.sql.common.constant.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtil;

import java.sql.SQLException;
import java.util.*;

/**
 * DQL result merger for Sharding.
 */
@RequiredArgsConstructor
public final class ShardingDQLResultMerger implements ResultMerger {
    
    private final DatabaseType databaseType;

    private static final String PAGE_COUNT = "count(0)";

    private static final String PAGE_COUNT_KEY = "count";
    
    @Override
    public MergedResult merge(final List<QueryResult> queryResults, final SQLStatementContext<?> sqlStatementContext, final PhysicalSchemaMetaData schemaMetaData) throws SQLException {
        if (1 == queryResults.size()) {
            return new IteratorStreamMergedResult(queryResults);
        }
        Map<String, Integer> columnLabelIndexMap = getColumnLabelIndexMap(queryResults.get(0));
        SelectStatementContext selectStatementContext = (SelectStatementContext) sqlStatementContext;
        selectStatementContext.setIndexes(columnLabelIndexMap);
        MergedResult mergedResult = build(queryResults, selectStatementContext, columnLabelIndexMap, schemaMetaData);
        return decorate(queryResults, selectStatementContext, mergedResult);
    }
    
    private Map<String, Integer> getColumnLabelIndexMap(final QueryResult queryResult) throws SQLException {
        Map<String, Integer> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        if(queryResult.getColumnCount() == 1 && Objects.equals(queryResult.getColumnLabel(1),PAGE_COUNT_KEY)){
            result.put(PAGE_COUNT, 1);
        }else {
            for (int i = queryResult.getColumnCount(); i > 0; i--) {
                result.put(SQLUtil.getExactlyValue(queryResult.getColumnLabel(i)), i);
            }
        }
        return result;
    }
    
    private MergedResult build(final List<QueryResult> queryResults, final SelectStatementContext selectStatementContext,
                               final Map<String, Integer> columnLabelIndexMap, final PhysicalSchemaMetaData schemaMetaData) throws SQLException {
        if (isNeedProcessGroupBy(selectStatementContext)) {
            return getGroupByMergedResult(queryResults, selectStatementContext, columnLabelIndexMap, schemaMetaData);
        }
        if (isNeedProcessDistinctRow(selectStatementContext)) {
            setGroupByForDistinctRow(selectStatementContext);
            return getGroupByMergedResult(queryResults, selectStatementContext, columnLabelIndexMap, schemaMetaData);
        }
        if (isNeedProcessOrderBy(selectStatementContext)) {
            //edit by ly 2020-07-09 #5586 The count(1) result from subquery with order by or no order by is error
            boolean isCountSql = isCountSql(queryResults);
            if(isCountSql && selectStatementContext.getOrderByContext().getItems().size() > 1) {
                Collection<OrderByItem> items = selectStatementContext.getOrderByContext().getItems();
                OrderByItem orderByItem = items.iterator().next();
                selectStatementContext.getOrderByContext().getItems().clear();
                selectStatementContext.getOrderByContext().getItems().add(orderByItem);
            }
            return new OrderByStreamMergedResult(queryResults,selectStatementContext,schemaMetaData,isCountSql);
        }
        return new IteratorStreamMergedResult(queryResults);
    }

    private boolean isCountSql(List<QueryResult> queryResults) throws SQLException {
        if(queryResults.get(0) instanceof MemoryQueryResult) {
            MemoryQueryResult queryResult = (MemoryQueryResult)queryResults.get(0);
            ResultSetMetaDataProxyImpl resultSetMetaData = (ResultSetMetaDataProxyImpl)queryResult.getResultSetMetaData();
            return Objects.equals(PAGE_COUNT, resultSetMetaData.getResultSetMetaDataRaw().getColumnName(1));
        }
        return false;
    }
    
    private boolean isNeedProcessGroupBy(final SelectStatementContext selectStatementContext) {
        return !selectStatementContext.getGroupByContext().getItems().isEmpty() || !selectStatementContext.getProjectionsContext().getAggregationProjections().isEmpty();
    }
    
    private boolean isNeedProcessDistinctRow(final SelectStatementContext selectStatementContext) {
        return selectStatementContext.getProjectionsContext().isDistinctRow();
    }
    
    private void setGroupByForDistinctRow(final SelectStatementContext selectStatementContext) {
        for (int index = 1; index <= selectStatementContext.getProjectionsContext().getExpandProjections().size(); index++) {
            OrderByItem orderByItem = new OrderByItem(new IndexOrderByItemSegment(-1, -1, index, OrderDirection.ASC, OrderDirection.ASC));
            orderByItem.setIndex(index);
            selectStatementContext.getGroupByContext().getItems().add(orderByItem);
        }
    }
    
    private MergedResult getGroupByMergedResult(final List<QueryResult> queryResults, final SelectStatementContext selectStatementContext,
                                                final Map<String, Integer> columnLabelIndexMap, final PhysicalSchemaMetaData schemaMetaData) throws SQLException {
        return selectStatementContext.isSameGroupByAndOrderByItems()
                ? new GroupByStreamMergedResult(columnLabelIndexMap, queryResults, selectStatementContext, schemaMetaData)
                : new GroupByMemoryMergedResult(queryResults, selectStatementContext, schemaMetaData);
    }
    
    private boolean isNeedProcessOrderBy(final SelectStatementContext selectStatementContext) {
        return !selectStatementContext.getOrderByContext().getItems().isEmpty();
    }
    
    private MergedResult decorate(final List<QueryResult> queryResults, final SelectStatementContext selectStatementContext, final MergedResult mergedResult) throws SQLException {
        PaginationContext paginationContext = selectStatementContext.getPaginationContext();
        if (!paginationContext.isHasPagination() || 1 == queryResults.size()) {
            return mergedResult;
        }
        String trunkDatabaseName = DatabaseTypeRegistry.getTrunkDatabaseType(databaseType.getName()).getName();
        if ("MySQL".equals(trunkDatabaseName) || "PostgreSQL".equals(trunkDatabaseName)) {
            return new LimitDecoratorMergedResult(mergedResult, paginationContext);
        }
        if ("Oracle".equals(trunkDatabaseName)) {
            return new RowNumberDecoratorMergedResult(mergedResult, paginationContext);
        }
        if ("SQLServer".equals(trunkDatabaseName)) {
            return new TopAndRowNumberDecoratorMergedResult(mergedResult, paginationContext);
        }
        return mergedResult;
    }
}
