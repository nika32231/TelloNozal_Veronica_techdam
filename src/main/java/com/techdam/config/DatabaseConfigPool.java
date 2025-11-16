package com.techdam.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConfigPool {
    private static HikariDataSource dataSource;

    static {
        try (InputStream in = DatabaseConfigPool.class.getClassLoader().getResourceAsStream("db.properties")) {
            Properties props = new Properties();
            props.load(in);

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("jdbc.url"));
            config.setUsername(props.getProperty("jdbc.username"));
            config.setPassword(props.getProperty("jdbc.password"));
            String driver = props.getProperty("jdbc.driverClassName");
            if (driver != null) config.setDriverClassName(driver);

            String maxPool = props.getProperty("maximumPoolSize");
            if (maxPool != null) config.setMaximumPoolSize(Integer.parseInt(maxPool));

            String minIdle = props.getProperty("minimumIdle");
            if (minIdle != null) config.setMinimumIdle(Integer.parseInt(minIdle));

            String connectionTimeout = props.getProperty("connectionTimeout");
            if (connectionTimeout != null) config.setConnectionTimeout(Long.parseLong(connectionTimeout));

            config.addDataSourceProperty("cachePrepStmts", props.getProperty("dataSource.cachePrepStmts"));
            config.addDataSourceProperty("prepStmtCacheSize", props.getProperty("dataSource.prepStmtCacheSize"));
            config.addDataSourceProperty("prepStmtCacheSqlLimit", props.getProperty("dataSource.prepStmtCacheSqlLimit"));
            config.addDataSourceProperty("useServerPrepStmts", props.getProperty("dataSource.useServerPrepStmts"));

            dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ExceptionInInitializerError("Failed to initialize HikariCP: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}