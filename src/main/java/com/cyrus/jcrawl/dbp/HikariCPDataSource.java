package com.cyrus.jcrawl.dbp;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author cyrushiker
 * @since 2019/7/23 16:11
 */
public class HikariCPDataSource {
    private static HikariConfig config = new HikariConfig();
    private static HikariDataSource ds;


    static {
        config.setJdbcUrl("jdbc:mysql://localhost:3306/pigo");
        config.setUsername("root");
        config.setPassword("root");
        config.setMaximumPoolSize(2);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        ds = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public static HikariDataSource getDatasource() {
        return ds;
    }


    private HikariCPDataSource(){}
}
