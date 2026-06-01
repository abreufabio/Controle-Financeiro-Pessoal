package dao;

import model.HistoricoRenda;
import repository.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HistoricoRendaDAO {

    // Salva um historico de renda
    public void salvar(HistoricoRenda renda) throws SQLException {
        String sql = "insert into historico_renda (valor_total, data_registro) values (?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setDouble(1, renda.getValorTotal());
            pstmt.setString(2, renda.getDataRegistro().toString());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    renda.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    // Listar todos os registros
    public List<HistoricoRenda> listarTodos() throws SQLException {
        List<HistoricoRenda> lista = new ArrayList<>();
        String sql = "select * from historico_renda order by data_registro desc";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                HistoricoRenda renda = new HistoricoRenda(
                        rs.getInt("id"),
                        rs.getDouble("valor_total"),
                        LocalDate.parse(rs.getString("data_registro"))
                );
                lista.add(renda);
            }
        }
        return lista;
    }

    // Busca renda por data
    public HistoricoRenda buscarPorData(LocalDate data) throws SQLException {
        String sql = "select * from historico_renda where data_registro = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, data.toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new HistoricoRenda(
                            rs.getInt("id"),
                            rs.getDouble("valor_total"),
                            LocalDate.parse(rs.getString("data_registro"))
                    );
                }
            }
        }
        return null;
    }

    // Obtém soma total de rendas do mês atual
    public double getTotalRendaMesAtual() throws SQLException {
        String sql = "SELECT SUM(valor_total) AS total FROM historico_renda " +
                     "WHERE YEAR(data_registro) = YEAR(CURDATE()) AND " +
                     "MONTH(data_registro) = MONTH(CURDATE())";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble("total");
            }
        }
        return 0.0;
    }

    // Obtém soma total de rendas de um mês específico
    public double getTotalRendaPorMes(int ano, int mes) throws SQLException {
        String sql = "SELECT SUM(valor_total) AS total FROM historico_renda " +
                     "WHERE YEAR(data_registro) = ? AND MONTH(data_registro) = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, ano);
            pstmt.setInt(2, mes);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }
        return 0.0;
    }

    // Remove um registro de renda por ID
    public boolean remover(int id) throws SQLException {
        String sql = "DELETE FROM historico_renda WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    // Remove registros de um período específico
    public int removerPorPeriodo(LocalDate dataInicio, LocalDate dataFim) throws SQLException {
        String sql = "DELETE FROM historico_renda WHERE data_registro BETWEEN ? AND ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, dataInicio.toString());
            pstmt.setString(2, dataFim.toString());
            return pstmt.executeUpdate();
        }
    }
}