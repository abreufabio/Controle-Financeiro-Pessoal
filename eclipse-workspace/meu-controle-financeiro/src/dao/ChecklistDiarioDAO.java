package dao;

import model.ChecklistDiario;        // Importa a classe modelo
import repository.DatabaseManager;    // Gerencia conexões com o banco

import java.sql.*;                    // Classes para conexão e SQL
import java.time.LocalDate;           // Para trabalhar com datas
import java.util.ArrayList;           // Para criar listas dinâmicas
import java.util.List;                // Interface List

public class ChecklistDiarioDAO {
    // CREATE/UPDATE: Salva um novo checklist ou atualiza se já existe para a data
    // Usa estratégia "UPSERT" (INSERT OR UPDATE)
    public void salvarOuAtualizar(ChecklistDiario checklist) throws SQLException {
        // SQL com ON DUPLICATE KEY UPDATE:
        // Se a data já existir (chave única), atualiza o campo anotou_gastos
        // Se não existir, insere um novo registro
        String sql = "insert into checklist_diario (data_verificacao, anotou_gastos) values (?, ?) " +
                     "on duplicate key update anotou_gastos = values(anotou_gastos)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Converte LocalDate para String (formato yyyy-MM-dd)
            pstmt.setString(1, checklist.getDataVerificacao().toString());
            
            // Converte boolean para inteiro (true = 1, false = 0) para salvar no banco
            pstmt.setInt(2, checklist.isAnotouGastos() ? 1 : 0);
            
            pstmt.executeUpdate();  // Executa o INSERT ou UPDATE
        }
    }
    
    // READ: Busca um checklist específico por data
    public ChecklistDiario buscarPorData(LocalDate data) throws SQLException {
        String sql = "select * from checklist_diario where data_verificacao = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, data.toString());  // Converte data para String
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {  // Se encontrou algum registro
                    return new ChecklistDiario(
                        rs.getInt("id"),                          // ID do banco
                        LocalDate.parse(rs.getString("data_verificacao")), // Converte String para LocalDate
                        rs.getInt("anotou_gastos") == 1           // Converte 1/0 para boolean
                    );
                }
            }
        }
        return null;  // Retorna null se não encontrar checklist para esta data
    }
    
    // READ: Lista todos os checklists (do mais recente para o mais antigo)
    public List<ChecklistDiario> listarTodos() throws SQLException {
        List<ChecklistDiario> lista = new ArrayList<>();
        String sql = "select * from checklist_diario order by data_verificacao desc";
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            // Percorre todas as linhas do resultado
            while (rs.next()) {
                ChecklistDiario checklist = new ChecklistDiario(
                    rs.getInt("id"),
                    LocalDate.parse(rs.getString("data_verificacao")),
                    rs.getInt("anotou_gastos") == 1
                );
                lista.add(checklist);  // Adiciona à lista
            }
        }
        return lista;
    }
    // Verifica se o usuário já registrou o checklist hoje
    // Útil para evitar registros duplicados no mesmo dia
    public boolean hasRegistroHoje() throws SQLException {
        // Busca por data atual; se encontrar algum, retorna true
        return buscarPorData(LocalDate.now()) != null;
    }
    
    // Calcula a porcentagem de dias que o usuário anotou os gastos
    // Métrica de consistência/hábito do usuário
    public double getTaxaSucesso() throws SQLException {
        // SQL que calcula:
        // - total: quantidade total de dias registrados
        // - sucessos: soma dos anotou_gastos (1 para sucesso, 0 para falha)
        String sql = "select count(*) as total, sum(anotou_gastos) as sucessos from checklist_diario";
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                int total = rs.getInt("total");        // Total de dias
                int sucessos = rs.getInt("sucessos");  // Dias que anotou gastos
                
                if (total > 0) {  // Evita divisão por zero
                    // Calcula porcentagem: (sucessos / total) * 100
                    return (double) sucessos / total * 100;
                }
            }
        }
        return 0.0;  // Retorna 0% se não houver registros
    }
}