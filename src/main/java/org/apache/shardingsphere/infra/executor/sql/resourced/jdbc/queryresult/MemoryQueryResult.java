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

package org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.queryresult;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.executor.sql.QueryResult;

import java.io.*;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

/**
 * Query result for memory loading.
 */
public final class MemoryQueryResult implements QueryResult {
    
    private final ResultSetMetaData resultSetMetaData;
    
    private final Iterator<List<Object>> rows;
    
    private List<Object> currentRow;
    
    public MemoryQueryResult(final ResultSet resultSet) throws SQLException {
        resultSetMetaData = resultSet.getMetaData();
        rows = getRows(resultSet);
    }
    
    private Iterator<List<Object>> getRows(final ResultSet resultSet) throws SQLException {
        Collection<List<Object>> result = new LinkedList<>();
        while (resultSet.next()) {
            List<Object> rowData = new ArrayList<>(resultSet.getMetaData().getColumnCount());
            for (int columnIndex = 1; columnIndex <= resultSet.getMetaData().getColumnCount(); columnIndex++) {
                Object rowValue = getRowValue(resultSet, columnIndex);
                rowData.add(resultSet.wasNull() ? null : rowValue);
            }
            result.add(rowData);
        }
        return result.iterator();
    }
    
    private Object getRowValue(final ResultSet resultSet, final int columnIndex) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        switch (metaData.getColumnType(columnIndex)) {
            case Types.BOOLEAN:
                return resultSet.getBoolean(columnIndex);
            case Types.TINYINT:
            case Types.SMALLINT:
                return resultSet.getInt(columnIndex);
            case Types.INTEGER:
                if (metaData.isSigned(columnIndex)) {
                    return resultSet.getInt(columnIndex);
                }
                return resultSet.getLong(columnIndex);
            case Types.BIGINT:
                if (metaData.isSigned(columnIndex)) {
                    return resultSet.getLong(columnIndex);
                }
                BigDecimal bigDecimal = resultSet.getBigDecimal(columnIndex);
                return bigDecimal == null ? null : bigDecimal.toBigInteger();
            case Types.NUMERIC:
            case Types.DECIMAL:
                return resultSet.getBigDecimal(columnIndex);
            case Types.FLOAT:
            case Types.DOUBLE:
                return resultSet.getDouble(columnIndex);
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
                return resultSet.getString(columnIndex);
            case Types.DATE:
                return resultSet.getDate(columnIndex);
            case Types.TIME:
                return resultSet.getTime(columnIndex);
            case Types.TIMESTAMP:
                return resultSet.getTimestamp(columnIndex);
            case Types.CLOB:
                return resultSet.getClob(columnIndex);
            case Types.BLOB:
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return resultSet.getBlob(columnIndex);
            case Types.ARRAY:
                return resultSet.getArray(columnIndex);
            default:
                return resultSet.getObject(columnIndex);
        }
    }

    @Override
    public ResultSetMetaData getResultSetMetaData() {
        return resultSetMetaData;
    }

    @Override
    public boolean next() {
        if (rows.hasNext()) {
            currentRow = rows.next();
            return true;
        }
        currentRow = null;
        return false;
    }
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) {
        return currentRow.get(columnIndex - 1);
    }
    
    @Override
    public Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) {
        return currentRow.get(columnIndex - 1);
    }
    
    @Override
    public InputStream getInputStream(final int columnIndex, final String type) {
        return getInputStream(currentRow.get(columnIndex - 1));
    }
    
    @SneakyThrows(IOException.class)
    private InputStream getInputStream(final Object value) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(value);
        objectOutputStream.flush();
        objectOutputStream.close();
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }
    
    @Override
    public boolean wasNull() {
        return null == currentRow;
    }
    
    @Override
    public int getColumnCount() throws SQLException {
        return resultSetMetaData.getColumnCount();
    }
    
    @Override
    public String getColumnName(final int columnIndex) throws SQLException {
        return resultSetMetaData.getColumnName(columnIndex);
    }
    
    @Override
    public String getColumnLabel(final int columnIndex) throws SQLException {
        return resultSetMetaData.getColumnLabel(columnIndex);
    }

    public List<Object> getCurrentRow() {
        return currentRow;
    }

}
