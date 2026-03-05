package org.example;

import java.sql.*;

public class ResultService {

    private static final String DB_NAME = "calc_data";
    private static final String DB_USER = "app_user";
    private static final String DB_PASSWORD = "STRONG_PASSWORD";

    static {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static String getDatabaseHost() {
        String host = System.getenv("DB_HOST");
        if (host == null || host.isEmpty()) host = "db"; // docker-compose service name
        return host;
    }

    private static String getDatabasePort() {
        String port = System.getenv("DB_PORT");
        if (port == null || port.isEmpty()) port = "3306";
        return port;
    }

    private static String getDatabaseUrl() {
        return "jdbc:mariadb://" + getDatabaseHost() + ":" + getDatabasePort() + "/" + DB_NAME +
                "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }

    public static void saveResult(double n1,
                                  double n2,
                                  double sum,
                                  double product,
                                  double subtraction,
                                  Double division) {

        String dbUrl = getDatabaseUrl();

        try (Connection conn = DriverManager.getConnection(dbUrl, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {

            // Create table if it doesn't exist (lisätty subtraction + division)
            String createTable = """
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
                """;
            stmt.executeUpdate(createTable);

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

            System.out.println("✅ Result saved: " + n1 + ", " + n2 +
                    " → Sum=" + sum +
                    ", Product=" + product +
                    ", Subtraction=" + subtraction +
                    ", Division=" + (division == null ? "NULL" : division));

        } catch (SQLException e) {
            System.err.println("❌ Failed to save result to DB: " + dbUrl);
            e.printStackTrace();
        }
    }
}