/*
 * Copyright (c) 2011-2014 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.jdbc;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.test.core.VertxTestBase;
import org.junit.BeforeClass;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public abstract class JDBCClientTestBase extends VertxTestBase {

  private static final List<String> SQL = new ArrayList<>();

  static {
    System.setProperty("textdb.allow_full_path", "true");
    //TODO: Create table with more types for testing
    SQL.add("drop table if exists select_table;");
    SQL.add("drop table if exists insert_table;");
    SQL.add("drop table if exists insert_table2;");
    SQL.add("drop table if exists update_table;");
    SQL.add("drop table if exists delete_table;");
    SQL.add("drop table if exists blob_table;");
    SQL.add("create table select_table (id int, lname varchar(255), fname varchar(255) );");
    SQL.add("insert into select_table values (1, 'doe', 'john');");
    SQL.add("insert into select_table values (2, 'doe', 'jane');");
    SQL.add("create table insert_table (id int generated by default as identity (start with 1 increment by 1) not null, lname varchar(255), fname varchar(255), dob date );");
    SQL.add("create table insert_table2 (id int not null, lname varchar(255), fname varchar(255), dob date );");
    SQL.add("create table update_table (id int, lname varchar(255), fname varchar(255), dob date );");
    SQL.add("insert into update_table values (1, 'doe', 'john', '2001-01-01');");
    SQL.add("create table delete_table (id int, lname varchar(255), fname varchar(255), dob date );");
    SQL.add("insert into delete_table values (1, 'doe', 'john', '2001-01-01');");
    SQL.add("insert into delete_table values (2, 'doe', 'jane', '2002-02-02');");
    SQL.add("create table blob_table (b blob, c clob, a int array default array[]);");
    SQL.add("insert into blob_table (b, c, a) values (load_file('pom.xml'), convert('Hello', clob),  ARRAY[1,2,3])");
  }

  @BeforeClass
  public static void createDb() throws Exception {
    Connection conn = DriverManager.getConnection(config().getString("url"));
    for (String sql : SQL) {
      conn.createStatement().execute(sql);
    }
  }

  protected static JsonObject config() {
    return new JsonObject()
      .put("url", "jdbc:hsqldb:mem:test?shutdown=true")
      .put("driver_class", "org.hsqldb.jdbcDriver");
  }

  protected void assertUpdate(UpdateResult result, int updated) {
    assertUpdate(result, updated, false);
  }

  protected void assertUpdate(UpdateResult result, int updated, boolean generatedKeys) {
    assertNotNull(result);
    assertEquals(updated, result.getUpdated());
    if (generatedKeys) {
      JsonArray keys = result.getKeys();
      assertNotNull(keys);
      assertEquals(updated, keys.size());
      Set<Integer> numbers = new HashSet<>();
      for (int i = 0; i < updated; i++) {
        assertTrue(keys.getValue(i) instanceof Integer);
        assertTrue(numbers.add(i));
      }
    }
  }

  protected static void setLogLevel(String name, Level level) {
    Logger logger = Logger.getLogger(name);
    if (logger != null) {
      logger.setLevel(level);
    }
  }
}
