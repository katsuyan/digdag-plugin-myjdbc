package com.github.katsuyan.digdag.plugin.myjdbc;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import io.digdag.client.config.Config;
import io.digdag.spi.SecretProvider;
import io.digdag.standards.operator.jdbc.DatabaseException;
import io.digdag.util.DurationParam;
import org.immutables.value.Value;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Properties;

import io.digdag.standards.operator.jdbc.AbstractJdbcConnectionConfig;
import io.digdag.core.plugin.PluginClassLoader;

@Value.Immutable
public abstract class MyjdbcConnectionConfig
        extends AbstractJdbcConnectionConfig
{
    public abstract Optional<String> schema();
    public abstract String jdbcDriverPath();

    @VisibleForTesting
    public static MyjdbcConnectionConfig configure(SecretProvider secrets, Config params)
    {
        return ImmutableMyjdbcConnectionConfig.builder()
                .host(secrets.getSecretOptional("host").or(() -> params.get("host", String.class)))
                .port(secrets.getSecretOptional("port").transform(Integer::parseInt).or(() -> params.get("port", int.class, 3306)))
                .user(secrets.getSecretOptional("user").or(() -> params.get("user", String.class)))
                .password(secrets.getSecretOptional("password"))
                .database(secrets.getSecretOptional("database").or(() -> params.get("database", String.class)))
                .jdbcDriverPath(secrets.getSecretOptional("driver_path").or(() -> params.get("driver_path", String.class)))
                .jdbcDriverName(secrets.getSecretOptional("driver_name").or(() -> params.get("driver_name", String.class)))
                .jdbcProtocolName(secrets.getSecretOptional("protocol").or(() -> params.get("protocol", String.class)))
                .ssl(secrets.getSecretOptional("ssl").transform(Boolean::parseBoolean).or(() -> params.get("ssl", boolean.class, false)))
                .connectTimeout(secrets.getSecretOptional("connect_timeout").transform(DurationParam::parse).or(() ->
                        params.get("connect_timeout", DurationParam.class, DurationParam.of(Duration.ofSeconds(30)))))
                .socketTimeout(secrets.getSecretOptional("socket_timeout").transform(DurationParam::parse).or(() ->
                        params.get("socket_timeout", DurationParam.class, DurationParam.of(Duration.ofSeconds(1800)))))
                .schema(secrets.getSecretOptional("schema").or(params.getOptional("schema", String.class)))
                .build();
    }


    @Override
    public Properties buildProperties() {
        Properties props = new Properties();

        props.setProperty("user", user());
        if (password().isPresent()) {
            props.setProperty("password", password().get());
        }
        if (schema().isPresent()) {
            props.setProperty("currentSchema", schema().get());
        }
        props.setProperty("loginTimeout", String.valueOf(connectTimeout().getDuration().getSeconds()));
        props.setProperty("connectTimeout", String.valueOf(connectTimeout().getDuration().getSeconds()));
        props.setProperty("socketTimeout", String.valueOf(socketTimeout().getDuration().getSeconds()));
        props.setProperty("tcpKeepAlive", "true");
        if (ssl()) {
            props.setProperty("ssl", "true");
            props.setProperty("sslfactory", "org.postgresql.ssl.NonValidatingFactory");
        }
        props.setProperty("applicationName", "digdag");

        return props;
    }

    @Override
    public String toString()
    {
        // Omit credentials in toString output
        return url();
    }

    @Override
    public Connection openConnection() {
        loadDriver(jdbcDriverName(), jdbcDriverPath());

        try {
            return DriverManager.getConnection(url(), buildProperties());
        } catch (SQLException ex) {
            throw new DatabaseException("Failed to connect to the database", ex);
        }
    }

    public void loadDriver(String className, String driverPath) {
        addDriverJarToClasspath(driverPath);

        try {
            Class.forName(className);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void addDriverJarToClasspath(String glob) {
        PluginClassLoader loader = (PluginClassLoader) getClass().getClassLoader();
        Path path = Paths.get(glob);
        loader.addPath(Paths.get(glob));
    }

}


