package dao;

import model.HistoricoRenda;        // Importa a classe modelo
import repository.DatabaseManager;   // Gerencia conexões com o banco

import java.sql.*;                   // Classes para conexão e SQL
import java.time.LocalDate;          // Para trabalhar com datas
import java.util.ArrayList;          // Para criar listas dinâmicas
import java.util.List;               // Interface List

public class HistoricoRendaDAO {
    // CREATE: Salva um novo registro de renda no banco
    public void salvar(HistoricoRenda renda) throws SQLException {
        String sql = "insert into historico_renda (valor_total, data_registro) values (?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Substitui os placeholders pelos valores do objeto
            pstmt.setDouble(1, renda.getValorTotal());           // Primeiro ? → valor
            pstmt.setString(2, renda.getDataRegistro().toString()); // Segundo ? → data
            pstmt.executeUpdate();  // Executa o INSERT
            
            // Recupera o ID gerado automaticamente pelo banco
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    renda.setId(generatedKeys.getInt(1));  // Atualiza o objeto com o ID
                }
            }
        }
    }
    
    // READ: Lista todos os registros de renda (do mais recente para o mais antigo)
    public List<HistoricoRenda> listarTodos() throws SQLException {
        List<HistoricoRenda> lista = new ArrayList<>();
        String sql = "select * from historico_renda order by data_registro desc";
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            // Percorre todas as linhas do resultado
            while (rs.next()) {
                HistoricoRenda renda = new HistoricoRenda(
                    rs.getInt("id"),                              // ID do banco
                    rs.getDouble("valor_total"),                  // Valor da renda
                    LocalDate.parse(rs.getString("data_registro")) // Converte String para LocalDate
                );
                lista.add(renda);  // Adiciona à lista
            }
        }
        return lista;
    }
    
    // READ: Busca um registro de renda por data específica
    public HistoricoRenda buscarPorData(LocalDate data) throws SQLException {
        String sql = "select * from historico_renda where data_registro = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, data.toString());  // Converte data para String
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {  // Se encontrou algum registro
                    return new HistoricoRenda(
                        rs.getInt("id"),
                        rs.getDouble("valor_total"),
                        LocalDate.parse(rs.getString("data_registro"))
                    );
                }
            }
        }
        return null;  // Retorna null se não encontrar renda para esta data
    }
    
    // DELETE: Remove um registro de renda pelo ID
    public boolean remover(int id) throws SQLException {
        String sql = "DELETE FROM historico_renda WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            // executeUpdate() retorna número de linhas afetadas
            return pstmt.executeUpdate() > 0;  // true se removeu algo
        }
    }
    
    // DELETE EM MASSA: Remove registros de um período específico
    public int removerPorPeriodo(LocalDate dataInicio, LocalDate dataFim) throws SQLException {
        String sql = "DELETE FROM historico_renda WHERE data_registro BETWEEN ? AND ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, dataInicio.toString());  // Data inicial do período
            pstmt.setString(2, dataFim.toString());     // Data final do período
            return pstmt.executeUpdate();  // Retorna quantos registros foram removidos
        }
    }
    
    // ========== MÉTODOS DE CÁLCULO E ESTATÍSTICA ==========
    
    // READ/SUM: Obtém o total de todas as rendas do mês atual
    // Útil para dashboard e relatórios mensais
    public double getTotalRendaMesAtual() throws SQLException {
        // SQL que soma valor_total onde ano e mês correspondem ao mês atual
        String sql = "SELECT SUM(valor_total) AS total FROM historico_renda " +
                     "WHERE YEAR(data_registro) = YEAR(CURDATE()) AND " +
                     "MONTH(data_registro) = MONTH(CURDATE())";
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getDouble("total");  // Retorna a soma (pode ser null se não houver registros)
            }
        }
        return 0.0;  // Retorna 0 se não houver rendas no mês
    }
    // READ/SUM: Obtém o total de rendas de um mês específico (ano e mês)
    // Útil para comparar meses diferentes ou ver evolução
    public double getTotalRendaPorMes(int ano, int mes) throws SQLException {
        String sql = "SELECT SUM(valor_total) AS total FROM historico_renda " +
                     "WHERE YEAR(data_registro) = ? AND MONTH(data_registro) = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, ano);  // Ano desejado
            
            pstmt.setInt(2, mes);  // Mês desejado
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");  // Soma total do mês
                }
            }
        }
        return 0.0;
    }
}