package org.example;

import java.sql.*;

public class ResultService {

    private static final String DB_NAME = "calc_data";
    private static final String DB_USER = "app_user";
    private static final String DB_PASSWORD = "STRONG_PASSWORD";

    static {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            initSchema();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static String getDatabaseHost() {
        String host = System.getenv("DB_HOST");
        if (host == null || host.isEmpty()) {
            host = "db"; // docker-compose service name
        }
        return host;
    }

    private static String getDatabasePort() {
        String port = System.getenv("DB_PORT");
        if (port == null || port.isEmpty()) {
            port = "3306";
        }
        return port;
    }

    private static String getDatabaseUrl() {
        return "jdbc:mariadb://" + getDatabaseHost() + ":" + getDatabasePort() + "/" + DB_NAME +
                "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }

    /**
     * Creates database and table automatically when the application starts.
     * This is required so Jenkins pipeline can verify DB + table without UI interaction.
     */
    public static void initSchema() {

        String baseUrl = "jdbc:mariadb://" + getDatabaseHost() + ":" + getDatabasePort() +
                "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        try (Connection conn = DriverManager.getConnection(baseUrl, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (Connection conn = DriverManager.getConnection(getDatabaseUrl(), DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS calc_results (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    number1 DOUBLE NOT NULL,
                    number2 DOUBLE NOT NULL,
                    sum_result DOUBLE NOT NULL,
                    product_result DOUBLE NOT NULL,
                    subtraction_result DOUBLE NOT NULL,
                    division_result DOUBLE NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            System.out.println("Database and table verified/created successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void saveResult(double n1,
                                  double n2,
                                  double sum,
                                  double product,
                                  double subtraction,
                                  Double division) {

        try (Connection conn = DriverManager.getConnection(getDatabaseUrl(), DB_USER, DB_PASSWORD)) {

            String insert = """
                INSERT INTO calc_results
                (number1, number2, sum_result, product_result, subtraction_result, division_result)
                VALUES (?, ?, ?, ?, ?, ?)
            """;

            try (PreparedStatement ps = conn.prepareStatement(insert)) {
                ps.setDouble(1, n1);
                ps.setDouble(2, n2);
                ps.setDouble(3, sum);
                ps.setDouble(4, product);
                ps.setDouble(5, subtraction);

                if (division == null) {
                    ps.setNull(6, Types.DOUBLE);
                } else {
                    ps.setDouble(6, division);
                }

                ps.executeUpdate();
            }

            System.out.println("Result saved to database.");

        } catch (SQLException e) {
            System.err.println("Failed to save result.");
            e.printStackTrace();
        }
    }
}