package com.librerialuz.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gestiona la conexión de la aplicación con MySQL.
 * Las credenciales se cargan desde db.properties.
 */
public class DatabaseConnection {

    private static final Properties PROPIEDADES = cargarPropiedades();

    private static Properties cargarPropiedades() {
        Properties propiedades = new Properties();

        try (InputStream entrada = DatabaseConnection.class
                .getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (entrada == null) {
                throw new IllegalStateException(
                        "No se encontró el archivo db.properties en src/main/resources."
                );
            }

            propiedades.load(entrada);
            return propiedades;

        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo cargar db.properties.",
                    e
            );
        }
    }

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "No se pudo cargar el driver de MySQL.",
                    e
            );
        }
    }

    public static Connection getConnection() throws SQLException {
        String url = PROPIEDADES.getProperty("db.url");
        String user = PROPIEDADES.getProperty("db.user");
        String password = PROPIEDADES.getProperty("db.password");

        return DriverManager.getConnection(url, user, password);
    }
}