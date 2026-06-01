// dao/DesejoCompraDAO.java
package dao;  // Pacote de acesso a dados (Data Access Object)

import model.DesejoCompra;           // Importa a classe modelo
import repository.DatabaseManager;    // Gerencia conexões com o banco

import java.sql.*;                    // Classes para conexão e SQL
import java.time.LocalDate;           // Para trabalhar com datas
import java.util.ArrayList;           // Para criar listas dinâmicas
import java.util.List;                // Interface List

public class DesejoCompraDAO {
    
    // CREATE: Salva um novo desejo de compra no banco
    public void salvar(DesejoCompra desejo) throws SQLException {
        // SQL de inserção (os ? são placeholders para valores)
        String sql = "insert into desejos_compra (nome_item, valor, data_criacao) values (?, ?, ?)";
        
        // try-with-resources: fecha conexão automaticamente
        // RETURN_GENERATED_KEYS: solicita que o banco retorne o ID gerado
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Substitui os placeholders pelos valores do objeto
            pstmt.setString(1, desejo.getNomeItem());     // Primeiro ? → nome
            pstmt.setDouble(2, desejo.getValor());        // Segundo ? → valor
            pstmt.setString(3, desejo.getDataCriacao().toString()); // Terceiro ? → data
            pstmt.executeUpdate();  // Executa o INSERT
            
            // Recupera o ID que o banco gerou automaticamente
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    desejo.setId(generatedKeys.getInt(1)); // Seta o ID no objeto
                }
            }
        }
    }
    
    // READ: Lista todos os desejos (do mais novo para o mais antigo)
    public List<DesejoCompra> listarTodos() throws SQLException {
        List<DesejoCompra> lista = new ArrayList<>();  // Lista vazia para armazenar resultados
        String sql = "select * from desejos_compra order by data_criacao DESC"; // DESC = mais recente primeiro
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();   // Para consultas sem parâmetros
             ResultSet rs = stmt.executeQuery(sql)) {   // Executa a consulta
            
            // Percorre cada linha do resultado
            while (rs.next()) {
                // Para cada linha, cria um objeto DesejoCompra com os dados
                DesejoCompra desejo = new DesejoCompra(
                    rs.getInt("id"),                    // Pega o ID pelo nome da coluna
                    rs.getString("nome_item"),          // Pega o nome
                    rs.getDouble("valor"),              // Pega o valor
                    LocalDate.parse(rs.getString("data_criacao")) // Converte String para LocalDate
                );
                lista.add(desejo);  // Adiciona à lista
            }
        }
        return lista;  // Retorna a lista com todos os desejos
    }
    
    // READ: Busca um desejo específico pelo ID
    public DesejoCompra buscarPorId(int id) throws SQLException {
        String sql = "select * from desejos_compra where id = ?"; // Filtra pelo ID
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);  // Substitui o ? pelo ID recebido
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {  // Se encontrou algum resultado
                    return new DesejoCompra(
                        rs.getInt("id"),
                        rs.getString("nome_item"),
                        rs.getDouble("valor"),
                        LocalDate.parse(rs.getString("data_criacao"))
                    );
                }
            }
        }
        return null;  // Retorna null se não encontrar nenhum desejo com esse ID
    }
    
    // DELETE: Remove um desejo pelo ID
    public boolean remover(int id) throws SQLException {
        String sql = "delete from desejos_compra where id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            // executeUpdate() retorna número de linhas afetadas
            // Se removeu 1 ou mais linhas, retorna true
            return pstmt.executeUpdate() > 0;
        }
    }
    
    // DELETE EM MASSA: Remove todos os desejos que já estão liberados (com 3+ dias)
    public int removerLiberados() throws SQLException {
        // SQL: delete onde a data_criacao for menor ou igual a hoje - 3 dias
        // date_sub(curdate(), interval 3 day) = data atual menos 3 dias
        String sql = "delete from desejos_compra where data_criacao <= date_sub(curdate(), interval 3 day)";
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Retorna quantos registros foram removidos
            return stmt.executeUpdate(sql);
        }
    }
}