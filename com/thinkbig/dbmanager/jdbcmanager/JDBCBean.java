package com.thinkbig.dbmanager.jdbcmanager;

import java.sql.Connection;
import java.sql.Statement;

public class JDBCBean {
    public Statement getStatement() {
        return statement;
    }

    public void setStatement(Statement statement) {
        this.statement = statement;
    }

    private Statement statement;

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    private Connection connection;

}
