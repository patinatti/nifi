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
package org.apache.nifi.processors.standard.db.impl;

import org.apache.nifi.processors.standard.db.DatabaseAdapter;
import org.junit.Assert;
import org.junit.Test;

public class TestMSSQL2008DatabaseAdapter {
    private final DatabaseAdapter db = new MSSQL2008DatabaseAdapter();

    @Test
    public void testGeneration() throws Exception {
        String sql = db.getSelectStatement("database.tablename", "some(set),of(columns),that,might,contain,methods,a.*", "", "", null, null);
        String expected1 = "SELECT some(set),of(columns),that,might,contain,methods,a.* FROM database.tablename";
        Assert.assertEquals(expected1, sql);

        sql = db.getSelectStatement("database.tablename", "some(set),of(columns),that,might,contain,methods,a.*", "that=\'some\"\' value\'", "", null, null);
        String expected2 = "SELECT some(set),of(columns),that,might,contain,methods,a.* FROM database.tablename WHERE that=\'some\"\' value\'";
        Assert.assertEquals(expected2, sql);

        sql = db.getSelectStatement("database.tablename", "some(set),of(columns),that,might,contain,methods,a.*", "that=\'some\"\' value\'", "might DESC", null, null);
        String expected3 = "SELECT some(set),of(columns),that,might,contain,methods,a.* FROM database.tablename WHERE that=\'some\"\' value\' ORDER BY might DESC";
        Assert.assertEquals(expected3, sql);

        sql = db.getSelectStatement("database.tablename", "", "that=\'some\"\' value\'", "might DESC", null, null);
        String expected4 = "SELECT * FROM database.tablename WHERE that=\'some\"\' value\' ORDER BY might DESC";
        Assert.assertEquals(expected4, sql);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoTableName() throws Exception {
        db.getSelectStatement("", "some(set),of(columns),that,might,contain,methods,a.*", "", "", null, null);
    }

    @Test
    public void testTOPQuery() throws Exception {
        String sql = db.getSelectStatement("database.tablename", "some(set),of(columns),that,might,contain,methods,a.*", "", "", 100L, null);
        String expected1 = "SELECT TOP 100 some(set),of(columns),that,might,contain,methods,a.* FROM database.tablename";
        Assert.assertEquals(expected1, sql);

        sql = db.getSelectStatement("database.tablename", "some(set),of(columns),that,might,contain,methods,a.*", "", "contain", 100L, null);
        String expected2 = "SELECT TOP 100 some(set),of(columns),that,might,contain,methods,a.* FROM database.tablename ORDER BY contain";
        Assert.assertEquals(expected2, sql);

        sql = db.getSelectStatement("database.tablename", "", "that=\'some\"\' value\'", "might DESC", 123456L, null);
        String expected4 = "SELECT TOP 123456 * FROM database.tablename WHERE that=\'some\"\' value\' ORDER BY might DESC";
        Assert.assertEquals(expected4, sql);
    }

    @Test
    public void testPagingQuery() throws Exception {
        String sql = db.getSelectStatement("database.tablename", "some(set),of(columns),that,might,contain,methods,a.*", "", "contain", 100L, 0L);
        String expected1 = "SELECT * FROM (SELECT TOP 100 some(set),of(columns),that,might,contain,methods,a.*, ROW_NUMBER() OVER(ORDER BY contain asc) "
                + "rnum FROM database.tablename ORDER BY contain) A WHERE rnum > 0 AND rnum <= 100";
        Assert.assertEquals(expected1, sql);

        sql = db.getSelectStatement("database.tablename", "some(set),of(columns),that,might,contain,methods,a.*", "", "contain", 10000L, 123456L);
        String expected2 = "SELECT * FROM (SELECT TOP 133456 some(set),of(columns),that,might,contain,methods,a.*, ROW_NUMBER() OVER(ORDER BY contain asc) "
                + "rnum FROM database.tablename ORDER BY contain) A WHERE rnum > 123456 AND rnum <= 133456";
        Assert.assertEquals(expected2, sql);

        sql = db.getSelectStatement("database.tablename", "some(set),of(columns),that,might,contain,methods,a.*", "methods='strange'", "contain", 10000L, 123456L);
        String expected3 = "SELECT * FROM (SELECT TOP 133456 some(set),of(columns),that,might,contain,methods,a.*, ROW_NUMBER() OVER(ORDER BY contain asc) rnum FROM database.tablename "
                + "WHERE methods='strange' ORDER BY contain) A WHERE rnum > 123456 AND rnum <= 133456";
        Assert.assertEquals(expected3, sql);
    }
}
