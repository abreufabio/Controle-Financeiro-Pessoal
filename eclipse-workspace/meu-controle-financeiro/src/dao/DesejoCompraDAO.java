package dao;

import model.DesejoCompra;
import repository.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DesejoCompraDAO {

    public void salvar(DesejoCompra desejo) throws SQLException {
        String sql = "insert into desejos_compra (nome_item, valor, data_criacao) values (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, desejo.getNomeItem());
            pstmt.setDouble(2, desejo.getValor());
            pstmt.setString(3, desejo.getDataCriacao().toString());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    desejo.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public List<DesejoCompra> listarTodos() throws SQLException {
        List<DesejoCompra> lista = new ArrayList<>();
        String sql = "select * from desejos_compra order by data_criacao DESC";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                DesejoCompra desejo = new DesejoCompra(
                    rs.getInt("id"),
                    rs.getString("nome_item"),
                    rs.getDouble("valor"),
                    LocalDate.parse(rs.getString("data_criacao"))
                );
                lista.add(desejo);
            }
        }
        return lista;
    }

    public DesejoCompra buscarPorId(int id) throws SQLException {
        String sql = "select * from desejos_compra where id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new DesejoCompra(
                        rs.getInt("id"),
                        rs.getString("nome_item"),
                        rs.getDouble("valor"),
                        LocalDate.parse(rs.getString("data_criacao"))
                    );
                }
            }
        }
        return null;
    }

    public boolean remover(int id) throws SQLException {
        String sql = "delete from desejos_compra where id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public int removerLiberados() throws SQLException {
        String sql = "delete from desejos_compra where data_criacao <= date_sub(curdate(), interval 3 day)";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            return stmt.executeUpdate(sql);
        }
    }
}