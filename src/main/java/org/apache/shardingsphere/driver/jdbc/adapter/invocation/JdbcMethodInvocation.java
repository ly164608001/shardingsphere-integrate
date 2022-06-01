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

package org.apache.shardingsphere.driver.jdbc.adapter.invocation;

import com.alibaba.druid.pool.DruidPooledPreparedStatement;
import com.alibaba.druid.proxy.jdbc.JdbcParameterImpl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.sql.SQLException;

/**
 * Invocation that reflected call for JDBC method.
 */
@RequiredArgsConstructor
public class JdbcMethodInvocation {
    
    @Getter
    private final Method method;
    
    @Getter
    private final Object[] arguments;
    
    /**
     * Invoke JDBC method.
     * 
     * @param target target object
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public void invoke(final Object target) {
        if(arguments.length > 1 && arguments[1] instanceof JdbcParameterImpl) {
            JdbcParameterImpl argument = (JdbcParameterImpl) arguments[1];
            arguments[1] = argument.getValue();
            try {
                ((DruidPooledPreparedStatement) target).getRawStatement().setNString((Integer) this.arguments[0],argument.getValue().toString());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }else {
            method.invoke(target, arguments);
        }
    }
}
