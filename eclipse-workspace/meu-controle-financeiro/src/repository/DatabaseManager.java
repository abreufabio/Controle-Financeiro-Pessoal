// repository/DatabaseManager.java
package repository;  // Pacote responsável pela conexão com o banco de dados

import java.sql.Connection;      // Representa a conexão com o banco
import java.sql.DriverManager;   // Gerencia drivers JDBC e conexões
import java.sql.SQLException;    // Trata erros de SQL

public class DatabaseManager {
    // ========== CONFIGURAÇÕES DO BANCO ==========
    private static final String URL = "jdbc:mysql://localhost:3306/controle_financeiro";  // Endereço do banco
    private static final String USER = "root";                    // Usuário do MySQL
    private static final String PASSWORD = "Fabio123@";          // Senha do MySQL
    
    private static Connection connection = null;  // Conexão única (Singleton)
    
    // Construtor privado impede que outras classes criem instâncias
    private DatabaseManager() {}
    
    // Obtém a conexão com o banco (cria uma nova ou reutiliza a existente)
    public static Connection getConnection() throws SQLException {
        // Verifica se a conexão não existe ou está fechada
        if (connection == null || connection.isClosed()) {
            try {
                // 1. Carrega o driver do MySQL na memória
                Class.forName("com.mysql.cj.jdbc.Driver");
                
                // 2. Estabelece a conexão com o banco usando URL, usuário e senha
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                
                System.out.println("✓ Nova conexão estabelecida com o banco de dados");
            } catch (ClassNotFoundException e) {
                // Erro: driver MySQL não está no classpath
                System.err.println("Driver MySQL não encontrado! Adicione o mysql-connector-java.jar ao classpath.");
                throw new SQLException("Driver MySQL não encontrado", e);
            }
        } else {
            System.out.println("✓ Reutilizando conexão existente");
        }
        return connection;
    }
    // Fecha a conexão manualmente quando o sistema for encerrado
    public static void closeConnection() {
        if (connection != null) {  // Só tenta fechar se houver conexão
            try {
                connection.close();  // Fecha a conexão com o banco
                System.out.println("✓ Conexão fechada com sucesso");
                connection = null;   // Libera a referência para o GC
            } catch (SQLException e) {
                System.err.println("Erro ao fechar conexão: " + e.getMessage());
            }
        } else {
            System.out.println("ℹ Nenhuma conexão ativa para fechar");
        }
    }
    // Verifica se a conexão está funcionando (útil na inicialização do app)
    public static boolean testConnection() {
        System.out.println("\n--- Testando conexão com o banco de dados ---");
        
        // Usa try-with-resources para garantir que a conexão de teste seja fechada
        try (Connection conn = getConnection()) {
            boolean conectado = conn != null && !conn.isClosed();
            
            if (conectado) {
                System.out.println("✓ Conexão bem-sucedida!");
                // Exibe informações do banco (para debug)
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