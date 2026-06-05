package repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseManager {
    
    private static String URL;
    private static String USER;
    private static String PASSWORD;
    private static int MAX_RECONNECT_ATTEMPTS = 3;
    private static int CONNECTION_TIMEOUT = 30;
    
    private static Connection connection = null;
    private static final ReentrantLock lock = new ReentrantLock();
    private static final AtomicInteger totalConnectionsCreated = new AtomicInteger(0);
    private static final AtomicInteger totalReconnectionsAttempted = new AtomicInteger(0);
    private static long lastConnectionTime = 0;
    
    static {
        loadConfiguration();
    }
    
    private DatabaseManager() {}
    
    private static void loadConfiguration() {
        Properties props = new Properties();
        boolean configLoaded = false;
        
        // Tenta carregar de arquivo externo
        try (InputStream input = DatabaseManager.class.getClassLoader()
                .getResourceAsStream("database.properties")) {
            
            if (input != null) {
                props.load(input);
                URL = props.getProperty("db.url");
                USER = props.getProperty("db.user");
                PASSWORD = props.getProperty("db.password");
                MAX_RECONNECT_ATTEMPTS = Integer.parseInt(props.getProperty("db.max.reconnect.attempts", "3"));
                CONNECTION_TIMEOUT = Integer.parseInt(props.getProperty("db.connection.timeout", "30"));
                configLoaded = true;
                System.out.println("[DB] Configuracoes carregadas do arquivo database.properties");
            }
        } catch (Exception e) {
            System.err.println("[DB] Erro ao carregar config: " + e.getMessage());
        }
        
        // Fallback se arquivo não encontrado (USA VARIAVEL DE AMBIENTE)
        if (!configLoaded) {
            URL = "jdbc:mysql://localhost:3306/controle_financeiro?useTimezone=true&serverTimezone=UTC";
            USER = System.getenv().getOrDefault("DB_USER", "root");
            PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "Fabio123@");
            System.out.println("[DB] Usando configuracoes padrao + variaveis de ambiente");
        }
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("[DB] Driver MySQL registrado");
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] Driver MySQL nao encontrado!");
        }
    }
    
    public static Connection getConnection() throws SQLException {
        lock.lock();
        try {
            if (needsNewConnection()) {
                return createNewConnection();
            }
            
            if (!isConnectionValid(connection)) {
                System.out.println("[DB] Conexao invalida. Reconectando...");
                closeConnection();
                return createNewConnection();
            }
            
            return connection;
        } finally {
            lock.unlock();
        }
    }
    
    private static boolean needsNewConnection() {
        if (connection == null) return true;
        try {
            return connection.isClosed();
        } catch (SQLException e) {
            return true;
        }
    }
    
    private static boolean isConnectionValid(Connection conn) {
        if (conn == null) return false;
        try {
            return conn.isValid(CONNECTION_TIMEOUT);
        } catch (SQLException e) {
            return false;
        }
    }
    
    private static Connection createNewConnection() throws SQLException {
        SQLException lastException = null;
        
        for (int attempt = 1; attempt <= MAX_RECONNECT_ATTEMPTS; attempt++) {
            try {
                System.out.println("[DB] Tentativa " + attempt + "/" + MAX_RECONNECT_ATTEMPTS);
                DriverManager.setLoginTimeout(CONNECTION_TIMEOUT);
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                
                // CORRIGIDO: AutoCommit true por padrão (compatível com DAOs existentes)
                connection.setAutoCommit(true);
                connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                
                if (attempt > 1) {
                    totalReconnectionsAttempted.incrementAndGet();
                } else {
                    totalConnectionsCreated.incrementAndGet();
                }
                lastConnectionTime = System.currentTimeMillis();
                
                System.out.println("[DB] Conexao estabelecida (total: " + totalConnectionsCreated.get() + ")");
                return connection;
                
            } catch (SQLException e) {
                lastException = e;
                System.err.println("[DB] Falha na tentativa " + attempt + ": " + e.getMessage());
                
                if (attempt < MAX_RECONNECT_ATTEMPTS) {
                    int waitTime = attempt * 1000;
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new SQLException("Reconexao interrompida", ie);
                    }
                }
            }
        }
        
        throw new SQLException("Falha apos " + MAX_RECONNECT_ATTEMPTS + " tentativas", lastException);
    }
    
    public static void closeConnection() {
        lock.lock();
        try {
            if (connection != null) {
                try {
                    if (!connection.getAutoCommit() && !connection.isReadOnly()) {
                        connection.rollback();
                    }
                    connection.close();
                    System.out.println("[DB] Conexao fechada");
                } catch (SQLException e) {
                    System.err.println("[DB] Erro ao fechar: " + e.getMessage());
                } finally {
                    connection = null;
                    lastConnectionTime = 0;
                }
            }
        } finally {
            lock.unlock();
        }
    }
    
    public static boolean testConnection() {
        System.out.println("\n=== TESTE DE CONEXAO ===");
        System.out.println("URL: " + URL);
        System.out.println("Usuario: " + USER);
        
        try (Connection conn = getConnection()) {
            boolean ok = conn != null && !conn.isClosed();
            if (ok) {
                System.out.println("[DB] CONEXAO OK!");
                System.out.println("  Produto: " + conn.getMetaData().getDatabaseProductName());
                System.out.println("  Versao: " + conn.getMetaData().getDatabaseProductVersion());
            }
            return ok;
        } catch (SQLException e) {
            System.err.println("[DB] ERRO: " + e.getMessage());
            return false;
        }
    }
    
    // ========== MÉTODOS DE TRANSAÇÃO (OPCIONAIS) ==========
    
    public static void beginTransaction() throws SQLException {
        Connection conn = getConnection();
        if (conn.getAutoCommit()) {
            conn.setAutoCommit(false);
            System.out.println("[DB] Transacao iniciada");
        }
    }
    
    public static void commit() throws SQLException {
        Connection conn = getConnection();
        if (!conn.getAutoCommit()) {
            conn.commit();
            conn.setAutoCommit(true);
            System.out.println("[DB] Commit realizado");
        }
    }
    
    public static void rollback() throws SQLException {
        Connection conn = getConnection();
        if (!conn.getAutoCommit()) {
            conn.rollback();
            conn.setAutoCommit(true);
            System.out.println("[DB] Rollback realizado");
        }
    }
    
    public static String getConnectionStats() {
        return String.format(
            "\n=== STATS ===\n" +
            "Conexoes criadas: %d\n" +
            "Reconexoes: %d\n" +
            "Conexao ativa: %s\n" +
            "Tempo desde ultima: %s seg\n" +
            "URL: %s\n" +
            "=================",
            totalConnectionsCreated.get(),
            totalReconnectionsAttempted.get(),
            isConnectionValid(connection) ? "SIM" : "NAO",
            lastConnectionTime == 0 ? "N/A" : (System.currentTimeMillis() - lastConnectionTime) / 1000,
            URL
        );
    }
    
    // Mantido para compatibilidade
    @Deprecated
    public static Connection getConnectionLegacy() throws SQLException {
        return getConnection();
    }
}