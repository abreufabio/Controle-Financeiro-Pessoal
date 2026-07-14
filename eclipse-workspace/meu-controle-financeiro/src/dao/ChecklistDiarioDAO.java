package dao;

import model.ChecklistDiario;
import repository.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ChecklistDiarioDAO {

    public void salvarOuAtualizar(ChecklistDiario checklist) throws SQLException {
        String sql = "INSERT INTO checklist_diario (data_verificacao, anotou_gastos, total_gasto_dia) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE anotou_gastos = VALUES(anotou_gastos), total_gasto_dia = VALUES(total_gasto_dia)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, checklist.getDataVerificacao().toString());
            pstmt.setInt(2, checklist.isAnotouGastos() ? 1 : 0);
            pstmt.setDouble(3, checklist.getTotalGastoDia());

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    checklist.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public ChecklistDiario buscarPorData(LocalDate data) throws SQLException {
        String sql = "SELECT * FROM checklist_diario WHERE data_verificacao = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, data.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new ChecklistDiario(
                        rs.getInt("id"),
                        LocalDate.parse(rs.getString("data_verificacao")),
                        rs.getInt("anotou_gastos") == 1,
                        rs.getDouble("total_gasto_dia")
                    );
                }
            }
        }
        return null;
    }

    public List<ChecklistDiario> listarTodos() throws SQLException {
        List<ChecklistDiario> lista = new ArrayList<>();
        String sql = "SELECT * FROM checklist_diario ORDER BY data_verificacao DESC";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ChecklistDiario checklist = new ChecklistDiario(
                    rs.getInt("id"),
                    LocalDate.parse(rs.getString("data_verificacao")),
                    rs.getInt("anotou_gastos") == 1,
                    rs.getDouble("total_gasto_dia")
                );
                lista.add(checklist);
            }
        }
        return lista;
    }

    public boolean hasRegistroHoje() throws SQLException {
        return buscarPorData(LocalDate.now()) != null;
    }

    public void atualizarTotalGasto(int checklistId, double novoTotal) throws SQLException {
        String sql = "UPDATE checklist_diario SET total_gasto_dia = ? WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, novoTotal);
            pstmt.setInt(2, checklistId);
            pstmt.executeUpdate();
        }
    }

    public double getTaxaSucesso() throws SQLException {
        String sql = "SELECT COUNT(*) AS total, SUM(anotou_gastos) AS sucessos FROM checklist_diario";

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