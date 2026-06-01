package dao;

import model.ChecklistDiario;
import repository.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ChecklistDiarioDAO {
    
    // Salva ou atualiza o checklist do dia
    public void salvarOuAtualizar(ChecklistDiario checklist) throws SQLException {
        String sql = "insert into checklist_diario (data_verificacao, anotou_gastos) values (?, ?) " +
                     "on duplicate key update anotou_gastos = values(anotou_gastos)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, checklist.getDataVerificacao().toString());
            pstmt.setInt(2, checklist.isAnotouGastos() ? 1 : 0);
            pstmt.executeUpdate();
        }
    }
    
    // Busca checklist por data
    public ChecklistDiario buscarPorData(LocalDate data) throws SQLException {
        String sql = "select * from checklist_diario where data_verificacao = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, data.toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new ChecklistDiario(
                        rs.getInt("id"),
                        LocalDate.parse(rs.getString("data_verificacao")),
                        rs.getInt("anotou_gastos") == 1
                    );
                }
            }
        }
        return null;
    }
    
    // Lista todos os checklists
    public List<ChecklistDiario> listarTodos() throws SQLException {
        List<ChecklistDiario> lista = new ArrayList<>();
        String sql = "select * from checklist_diario order by data_verificacao desc";
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                ChecklistDiario checklist = new ChecklistDiario(
                    rs.getInt("id"),
                    LocalDate.parse(rs.getString("data_verificacao")),
                    rs.getInt("anotou_gastos") == 1
                );
                lista.add(checklist);
            }
        }
        return lista;
    }
    
    // Verifica se já registrou hoje
    public boolean hasRegistroHoje() throws SQLException {
        return buscarPorData(LocalDate.now()) != null;
    }
    
    // Obtém taxa de sucesso (dias que registrou gastos)
    public double getTaxaSucesso() throws SQLException {
        String sql = "select count(*) as total, sum(anotou_gastos) as sucessos from checklist_diario";
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                int total = rs.getInt("total");
                int sucessos = rs.getInt("sucessos");
                if (total > 0) {
                    return (double) sucessos / total * 100;
                }
            }
        }
        return 0.0;
    }
}