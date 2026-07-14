package dao;

import model.HistoricoRenda;
import repository.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HistoricoRendaDAO {

    public void salvar(HistoricoRenda renda) throws SQLException {
        String sql = "INSERT INTO historico_rendas (valor_total, descricao, categoria, data_registro) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setDouble(1, renda.getValorTotal());
            pstmt.setString(2, renda.getDescricao());
            pstmt.setString(3, renda.getCategoriaString());
            pstmt.setString(4, renda.getDataRegistro().toString());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    renda.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public List<HistoricoRenda> listarTodos() throws SQLException {
        List<HistoricoRenda> lista = new ArrayList<>();
        String sql = "SELECT * FROM historico_rendas ORDER BY data_registro DESC";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                HistoricoRenda renda = new HistoricoRenda(
                    rs.getInt("id"),
                    rs.getDouble("valor_total"),
                    rs.getString("descricao"),
                    rs.getString("categoria"),
                    LocalDate.parse(rs.getString("data_registro"))
                );
                lista.add(renda);
            }
        }
        return lista;
    }

    public HistoricoRenda buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM historico_rendas WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new HistoricoRenda(
                        rs.getInt("id"),
                        rs.getDouble("valor_total"),
                        rs.getString("descricao"),
                        rs.getString("categoria"),
                        LocalDate.parse(rs.getString("data_registro"))
                    );
                }
            }
        }
        return null;
    }

    public boolean remover(int id) throws SQLException {
        String sql = "DELETE FROM historico_rendas WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public double getTotalRendaMesAtual() throws SQLException {
        String sql = "SELECT SUM(valor_total) AS total FROM historico_rendas " +
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

    public double getTotalRendaPorMes(int ano, int mes) throws SQLException {
        String sql = "SELECT SUM(valor_total) AS total FROM historico_rendas " +
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
    
    public double getTotalRendaFixoMesAtual() throws SQLException {
        String sql = "SELECT SUM(valor_total) AS total FROM historico_rendas " +
                     "WHERE YEAR(data_registro) = YEAR(CURDATE()) AND " +
                     "MONTH(data_registro) = MONTH(CURDATE()) AND " +
                     "categoria = 'FIXO'";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble("total");
            }
        }
        return 0.0;
    }
    
    public double getTotalRendaExtraMesAtual() throws SQLException {
        String sql = "SELECT SUM(valor_total) AS total FROM historico_rendas " +
                     "WHERE YEAR(data_registro) = YEAR(CURDATE()) AND " +
                     "MONTH(data_registro) = MONTH(CURDATE()) AND " +
                     "categoria = 'EXTRA'";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble("total");
            }
        }
        return 0.0;
    }
}