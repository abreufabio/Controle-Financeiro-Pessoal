// repository/DatabaseManager.java
package repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/controle_financeiro";
    private static final String USER = "root";
    private static final String PASSWORD = "Fabio123@";
    
    private static Connection connection = null;
    
    // Construtor privado para Singleton
    private DatabaseManager() {}
    
    // Obtém conexão com o banco (Singleton)
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✓ Nova conexão estabelecida com o banco de dados");
            } catch (ClassNotFoundException e) {
                System.err.println("Driver MySQL não encontrado! Adicione o mysql-connector-java.jar ao classpath.");
                throw new SQLException("Driver MySQL não encontrado", e);
            }
        } else {
            System.out.println("✓ Reutilizando conexão existente");
        }
        return connection;
    }
    
    // Fecha a conexão
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("✓ Conexão fechada com sucesso");
                connection = null;
            } catch (SQLException e) {
                System.err.println("Erro ao fechar conexão: " + e.getMessage());
            }
        } else {
            System.out.println("ℹ Nenhuma conexão ativa para fechar");
        }
    }
    
    // Testa a conexão com o banco
    public static boolean testConnection() {
        System.out.println("\n--- Testando conexão com o banco de dados ---");
        try (Connection conn = getConnection()) {
            boolean conectado = conn != null && !conn.isClosed();
            if (conectado) {
                System.out.println("✓ Conexão bem-sucedida!");
                System.out.println("  Banco: " + conn.getMetaData().getDatabaseProductName());
                System.out.println("  Versão: " + conn.getMetaData().getDatabaseProductVersion());
                System.out.println("  URL: " + URL);
            }
            return conectado;
        } catch (SQLException e) {
            System.err.println("✗ Erro ao conectar ao MySQL: " + e.getMessage());
            return false;
        }
    }
}