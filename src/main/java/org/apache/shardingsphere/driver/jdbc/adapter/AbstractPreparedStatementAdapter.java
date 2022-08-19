//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.apache.shardingsphere.driver.jdbc.adapter;

import com.alibaba.druid.proxy.jdbc.JdbcParameter;
import com.alibaba.druid.proxy.jdbc.JdbcParameterImpl;
import com.alibaba.druid.proxy.jdbc.JdbcParameterNull;
import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import lombok.Generated;
import org.apache.shardingsphere.driver.jdbc.unsupported.AbstractUnsupportedOperationPreparedStatement;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;

public abstract class AbstractPreparedStatementAdapter extends AbstractUnsupportedOperationPreparedStatement {

    private final List<AbstractPreparedStatementAdapter.PreparedStatementInvocationReplayer> setParameterMethodInvocations = new LinkedList();

    private final List<Object> parameters = new ArrayList();

    public AbstractPreparedStatementAdapter() {
    }

    @Override
    public final void setNull(int parameterIndex, int sqlType) {
        this.setParameter(parameterIndex, (Object)null);
    }

    @Override
    public final void setNull(int parameterIndex, int sqlType, String typeName) {
        this.setParameter(parameterIndex, (Object)null);
    }

    @Override
    public final void setBoolean(int parameterIndex, boolean x) {
        this.setParameter(parameterIndex, x);
    }

    @Override
    public final void setByte(int parameterIndex, byte x) {
        this.setParameter(parameterIndex, x);
    }

    @Override
    public final void setShort(int parameterIndex, short x) {
        this.setParameter(parameterIndex, x);
    }

    @Override
    public final void setInt(int parameterIndex, int x) {
        this.setParameter(parameterIndex, x);
    }

    @Override
    public final void setLong(int parameterIndex, long x) {
        this.setParameter(parameterIndex, x);
    }

    @Override
    public final void setFloat(int parameterIndex, float x) {
        this.setParameter(parameterIndex, x);
    }

    @Override
    public final void setDouble(int parameterIndex, double x) {
        this.setParameter(parameterIndex, x);
    }

    @Override
    public final void setString(int parameterIndex, String x) {
        this.setParameter(parameterIndex, x);
    }

    @Override
    public final void setBigDecimal(int parameterIndex, BigDecimal x) {
        this.setParameter(parameterIndex, x);
    }

    @Override
    public final void setDate(int parameterIndex, Date x) {
        this.setParameter(parameterIndex, x);
    }

    @Override
    public final void setDate(int parameterIndex, Date x, Calendar cal) {
        this.setParameter(parameterIndex, x);
    }

    @Override
    public final void setTime(int parameterIndex, Time x) {
        this.setParameter(parameterIndex, x);
    }

    @Override
    public final void setTime(int parameterIndex, Time x, Calendar cal) {
        this.setParameter(parameterIndex, x);
    }

    @Override
    public final void setTimestamp(int parameterIndex, Timestamp x) {
        this.setParameter(parameterIndex, x);
    }

    @Override
    public final void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) {
        this.setParameter(parameterIndex, x);
    }

    @Override
    public final void setBytes(int parameterIndex, byte[] x) {
        this.setParameter(parameterIndex, x);
    }

    @Override
    public final void setBlob(int parameterIndex, Blob x) {
        this.setParameter(parameterIndex, x);
    }

    @Override
    public final void setBlob(int parameterIndex, InputStream x) {
        this.setParameter(parameterIndex, x);
    }

    @Override
    public final void setBlob(int parameterIndex, InputStream x, long length) {
        this.setParameter(parameterIndex, x);
    }

    @Override
    public final void setClob(int parameterIndex, Clob x) {
        this.setParameter(parameterIndex, x);
    }

    @Override
    public final void setClob(int parameterIndex, Reader x) {
        this.setParameter(parameterIndex, x);
    }

    @Override
    public final void setClob(int parameterIndex, Reader x, long length) {
        this.setParameter(parameterIndex, x);
    }

    public void setArray(int parameterIndex, Array x) {
        this.setParameter(parameterIndex, x);
    }

    @Override
    public final void setAsciiStream(int parameterIndex, InputStream x) {
        this.setParameter(parameterIndex, x);
    }

    @Override
    public final void setAsciiStream(int parameterIndex, InputStream x, int length) {
        this.setParameter(parameterIndex, x);
    }

    @Override
    public final void setAsciiStream(int parameterIndex, InputStream x, long length) {
        this.setParameter(parameterIndex, x);
    }

    @Override
    public final void setUnicodeStream(int parameterIndex, InputStream x, int length) {
        this.setParameter(parameterIndex, x);
    }

    @Override
    public final void setBinaryStream(int parameterIndex, InputStream x) {
        this.setParameter(parameterIndex, x);
    }

    @Override
    public final void setBinaryStream(int parameterIndex, InputStream x, int length) {
        this.setParameter(parameterIndex, x);
    }

    @Override
    public final void setBinaryStream(int parameterIndex, InputStream x, long length) {
        this.setParameter(parameterIndex, x);
    }

    @Override
    public final void setCharacterStream(int parameterIndex, Reader x) {
        try {
            this.setParameter(parameterIndex, CharStreams.toString(x));
        } catch (IOException var4) {
            throw new ShardingSphereException(var4);
        }
    }

    @Override
    public final void setCharacterStream(int parameterIndex, Reader x, int length) {
        try {
            this.setParameter(parameterIndex, CharStreams.toString(x));
        } catch (IOException var5) {
            throw new ShardingSphereException(var5);
        }
    }

    @Override
    public final void setCharacterStream(int parameterIndex, Reader x, long length) {
        try {
            this.setParameter(parameterIndex, CharStreams.toString(x));
        } catch (IOException var6) {
            throw new ShardingSphereException(var6);
        }
    }

    @Override
    public final void setURL(int parameterIndex, URL x) {
        this.setParameter(parameterIndex, x);
    }

    @Override
    public final void setSQLXML(int parameterIndex, SQLXML x) {
        this.setParameter(parameterIndex, x);
    }

    @Override
    public final void setObject(int parameterIndex, Object x) {
        this.setParameter(parameterIndex, x);
    }

    @Override
    public final void setObject(int parameterIndex, Object x, int targetSqlType) {
        this.setParameter(parameterIndex, x);
    }

    @Override
    public final void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) {
        this.setParameter(parameterIndex, x);
    }

    private void setParameter(int parameterIndex, Object value) {
        if (this.parameters.size() == parameterIndex - 1) {
            this.parameters.add(value);
        } else {
            for(int i = this.parameters.size(); i <= parameterIndex - 1; ++i) {
                this.parameters.add((Object)null);
            }

            this.parameters.set(parameterIndex - 1, value);
        }
    }

    @Override
    public final void setNString(final int parameterIndex, final String x) {
        //setParameter(parameterIndex, createParameter(Types.NVARCHAR, x));
        setParameter(parameterIndex, x);
    }

    private JdbcParameter createParameter(int sqlType, Object value) {
        if (value == null) {
            return JdbcParameterNull.valueOf(sqlType);
        }
        return new JdbcParameterImpl(sqlType, value);
    }

    protected final void replaySetParameter(PreparedStatement preparedStatement, List<Object> parameters) throws SQLException {
        this.setParameterMethodInvocations.clear();
        this.addParameters(parameters);
        Iterator var3 = this.setParameterMethodInvocations.iterator();

        while(var3.hasNext()) {
            AbstractPreparedStatementAdapter.PreparedStatementInvocationReplayer each = (AbstractPreparedStatementAdapter.PreparedStatementInvocationReplayer)var3.next();
            each.replayOn(preparedStatement);
        }

    }

    private void addParameters(List<Object> parameters) {
        int i = 0;
        Iterator var3 = parameters.iterator();

        while(var3.hasNext()) {
            Object each = var3.next();
            int index = i++ + 1;
            this.setParameterMethodInvocations.add((preparedStatement) -> {
                preparedStatement.setObject(index, each);
            });
        }

    }

    @Override
    public final void clearParameters() {
        this.parameters.clear();
        this.setParameterMethodInvocations.clear();
    }

    @Generated
    public List<Object> getParameters() {
        return this.parameters;
    }

    @FunctionalInterface
    interface PreparedStatementInvocationReplayer {
        void replayOn(PreparedStatement var1) throws SQLException;
    }
}
