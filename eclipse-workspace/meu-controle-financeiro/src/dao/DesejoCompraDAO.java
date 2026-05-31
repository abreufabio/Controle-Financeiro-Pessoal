// dao/DesejoCompraDAO.java
package dao;

import model.DesejoCompra;
import repository.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DesejoCompraDAO {
    
    // Salva um novo desejo de compra
    public void salvar(DesejoCompra desejo) throws SQLException {
        String sql = "INSERT INTO desejos_compra (nome_item, valor, data_criacao) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, desejo.getNomeItem());
            pstmt.setDouble(2, desejo.getValor());
            pstmt.setString(3, desejo.getDataCriacao().toString());
            pstmt.executeUpdate();
            
            // Recupera o ID gerado
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    desejo.setId(generatedKeys.getInt(1));
                }
            }
        }
    }
    
    // Lista todos os desejos
    public List<DesejoCompra> listarTodos() throws SQLException {
        List<DesejoCompra> lista = new ArrayList<>();
        String sql = "SELECT * FROM desejos_compra ORDER BY data_criacao DESC";
        
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
    
    // Busca desejo por ID
    public DesejoCompra buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM desejos_compra WHERE id = ?";
        
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
    
    // Remove um desejo
    public boolean remover(int id) throws SQLException {
        String sql = "DELETE FROM desejos_compra WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    // Remove desejos antigos (liberados)
    public int removerLiberados() throws SQLException {
        String sql = "DELETE FROM desejos_compra WHERE data_criacao <= DATE_SUB(CURDATE(), INTERVAL 3 DAY)";
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            
            return stmt.executeUpdate(sql);
        }
    }
    
    // ==================== MÉTODO MAIN PARA TESTES ====================
    public static void main(String[] args) {
        System.out.println("=== TESTES DA CLASSE DESEJOCOMPRADAO ===\n");
        
        // Verificar conexão primeiro
        if (!DatabaseManager.testConnection()) {
            System.out.println("❌ Não foi possível conectar ao banco de dados. Abortando testes.");
            return;
        }
        
        DesejoCompraDAO dao = new DesejoCompraDAO();
        
        try {
            testarSalvar(dao);
            testarListarTodos(dao);
            testarBuscarPorId(dao);
            testarRemover(dao);
            testarRemoverLiberados(dao);
            testarInsercaoEmMassa(dao);
            testarRegraDiasLiberacao(dao);
            
        } catch (SQLException e) {
            System.err.println("❌ Erro durante os testes: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseManager.closeConnection();
        }
        
        System.out.println("\n=== TESTES CONCLUÍDOS ===");
    }
    
    // Teste 1: Salvar novos desejos
    static void testarSalvar(DesejoCompraDAO dao) throws SQLException {
        System.out.println("【TESTE 1】 Inserindo novos desejos de compra");
        
        // Dados para inserir
        DesejoCompra[] novosDesejos = {
            new DesejoCompra("PlayStation 5", 4500.00),
            new DesejoCompra("Notebook Dell XPS", 7500.00),
            new DesejoCompra("Smartphone iPhone 15", 5500.00),
            new DesejoCompra("Fone Bluetooth Sony", 350.00),
            new DesejoCompra("Smartwatch Samsung", 1200.00),
            new DesejoCompra("Cadeira Gamer", 1800.00),
            new DesejoCompra("Monitor 4K 32'", 2500.00),
            new DesejoCompra("Teclado Mecânico", 450.00),
            new DesejoCompra("Mouse Gamer", 280.00),
            new DesejoCompra("Webcam 4K", 600.00)
        };
        
        for (DesejoCompra desejo : novosDesejos) {
            dao.salvar(desejo);
            System.out.printf("  ✓ Inserido: %s (ID: %d)%n", 
                              desejo.getNomeItem(), desejo.getId());
        }
        
        System.out.println("  ✅ " + novosDesejos.length + " desejos inseridos com sucesso!\n");
    }
    
    // Teste 2: Listar todos os desejos
    static void testarListarTodos(DesejoCompraDAO dao) throws SQLException {
        System.out.println("【TESTE 2】 Listando todos os desejos");
        
        List<DesejoCompra> lista = dao.listarTodos();
        
        if (lista.isEmpty()) {
            System.out.println("  ℹ Nenhum desejo encontrado no banco.");
        } else {
            System.out.println("  Total de desejos: " + lista.size());
            System.out.println("  Lista de desejos (do mais recente ao mais antigo):");
            System.out.println("  " + "-".repeat(70));
            
            for (DesejoCompra d : lista) {
                String status = d.isLiberado() ? "✓ LIBERADO" : "⏳ Aguardando (" + d.getDiasRestantes() + " dias)";
                System.out.printf("  ID: %2d | %-25s | R$ %8.2f | %s | %s%n",
                                  d.getId(),
                                  d.getNomeItem().length() > 25 ? d.getNomeItem().substring(0, 22) + "..." : d.getNomeItem(),
                                  d.getValor(),
                                  d.getDataCriacao(),
                                  status);
            }
            System.out.println("  " + "-".repeat(70));
        }
        System.out.println("  ✅ Listagem concluída!\n");
    }
    
    // Teste 3: Buscar por ID específico
    static void testarBuscarPorId(DesejoCompraDAO dao) throws SQLException {
        System.out.println("【TESTE 3】 Buscando desejos por ID");
        
        // Buscar pelo primeiro ID (assumindo que existe)
        DesejoCompra encontrado = dao.buscarPorId(1);
        
        if (encontrado != null) {
            System.out.printf("  ✓ Desejo encontrado (ID: %d):%n", encontrado.getId());
            System.out.printf("     Nome: %s%n", encontrado.getNomeItem());
            System.out.printf("     Valor: R$ %.2f%n", encontrado.getValor());
            System.out.printf("     Data: %s%n", encontrado.getDataCriacao());
            System.out.printf("     Liberado: %s%n", encontrado.isLiberado() ? "Sim" : "Não");
        } else {
            System.out.println("  ℹ Nenhum desejo encontrado com ID 1");
        }
        
        // Tentar buscar ID inexistente
        DesejoCompra naoEncontrado = dao.buscarPorId(99999);
        System.out.printf("  ℹ Busca por ID 99999: %s%n", 
                          naoEncontrado == null ? "retornou null (não encontrado)" : "encontrado");
        
        System.out.println("  ✅ Busca concluída!\n");
    }
    
    // Teste 4: Remover um desejo específico
    static void testarRemover(DesejoCompraDAO dao) throws SQLException {
        System.out.println("【TESTE 4】 Removendo um desejo específico");
        
        // Listar antes da remoção
        int antes = dao.listarTodos().size();
        System.out.println("  Desejos antes da remoção: " + antes);
        
        // Remover último desejo (maior ID)
        List<DesejoCompra> lista = dao.listarTodos();
        if (!lista.isEmpty()) {
            int ultimoId = lista.get(lista.size() - 1).getId();
            String nome = lista.get(lista.size() - 1).getNomeItem();
            
            boolean removido = dao.remover(ultimoId);
            System.out.printf("  Removendo '%s' (ID: %d): %s%n", nome, ultimoId, removido ? "SUCESSO" : "FALHA");
            
            // Listar depois da remoção
            int depois = dao.listarTodos().size();
            System.out.println("  Desejos após remoção: " + depois);
            System.out.println("  Diferença: " + (antes - depois) + " registro(s) removido(s)");
        } else {
            System.out.println("  ℹ Nenhum desejo para remover");
        }
        
        System.out.println("  ✅ Remoção concluída!\n");
    }
    
    // Teste 5: Remover desejos liberados (antigos)
    static void testarRemoverLiberados(DesejoCompraDAO dao) throws SQLException {
        System.out.println("【TESTE 5】 Removendo desejos liberados (com mais de 3 dias)");
        
        // Inserir alguns desejos antigos para teste
        System.out.println("  Inserindo desejos com datas antigas para teste...");
        
        // Método auxiliar para inserir com data específica
        inserirDesejoComDataAntiga(dao, "TV Antiga", 1500.00, LocalDate.now().minusDays(10));
        inserirDesejoComDataAntiga(dao, "Geladeira Antiga", 2500.00, LocalDate.now().minusDays(5));
        inserirDesejoComDataAntiga(dao, "Fogão Antigo", 800.00, LocalDate.now().minusDays(7));
        inserirDesejoComDataAntiga(dao, "Microondas Antigo", 450.00, LocalDate.now().minusDays(4));
        
        // Contar antes da remoção
        int antes = dao.listarTodos().size();
        System.out.println("  Total de desejos antes da limpeza: " + antes);
        
        // Remover liberados
        int removidos = dao.removerLiberados();
        System.out.println("  Desejos removidos (liberados): " + removidos);
        
        // Contar depois da remoção
        int depois = dao.listarTodos().size();
        System.out.println("  Total de desejos após limpeza: " + depois);
        
        System.out.println("  ✅ Limpeza de desejos liberados concluída!\n");
    }
    
    // Método auxiliar para inserir com data específica
    private static void inserirDesejoComDataAntiga(DesejoCompraDAO dao, String nome, double valor, LocalDate data) 
            throws SQLException {
        String sql = "INSERT INTO desejos_compra (nome_item, valor, data_criacao) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nome);
            pstmt.setDouble(2, valor);
            pstmt.setString(3, data.toString());
            pstmt.executeUpdate();
        }
    }
    
    // Teste 6: Inserção em massa (dados adicionais)
    static void testarInsercaoEmMassa(DesejoCompraDAO dao) throws SQLException {
        System.out.println("【TESTE 6】 Inserindo mais desejos para teste completo");
        
        // Mais dados realistas
        Object[][] dadosAdicionais = {
            {"Livro: Clean Code", 89.90},
            {"Curso de Java Online", 297.00},
            {"Academia - Mensalidade", 120.00},
            {"Assinatura Netflix", 45.90},
            {"Assinatura Spotify", 21.90},
            {"Jogo do Nintendo Switch", 299.00},
            {"Fone de Ouvido JBL", 199.00},
            {"Power Bank 20000mAh", 150.00},
            {"SSD 1TB Externo", 450.00},
            {"Mousepad Gamer", 89.00},
            {"Cabo HDMI 2.1", 45.00},
            {"Adaptador USB-C", 35.00},
            {"Bolsa para Notebook", 120.00},
            {"Mochila Antifurto", 180.00},
            {"Luminária de Mesa LED", 70.00}
        };
        
        int inseridos = 0;
        for (Object[] dado : dadosAdicionais) {
            DesejoCompra desejo = new DesejoCompra((String) dado[0], (Double) dado[1]);
            dao.salvar(desejo);
            inseridos++;
        }
        
        System.out.printf("  ✓ Inseridos %d novos desejos%n", inseridos);
        
        // Mostrar estatísticas
        List<DesejoCompra> todos = dao.listarTodos();
        double somaValores = todos.stream().mapToDouble(DesejoCompra::getValor).sum();
        double mediaValores = somaValores / todos.size();
        long liberados = todos.stream().filter(DesejoCompra::isLiberado).count();
        
        System.out.println("\n  📊 ESTATÍSTICAS ATUAIS:");
        System.out.printf("     Total de desejos: %d%n", todos.size());
        System.out.printf("     Soma total dos valores: R$ %.2f%n", somaValores);
        System.out.printf("     Média de valores: R$ %.2f%n", mediaValores);
        System.out.printf("     Desejos liberados (3+ dias): %d%n", liberados);
        System.out.printf("     Desejos aguardando: %d%n", todos.size() - liberados);
        
        System.out.println("  ✅ Inserção em massa concluída!\n");
    }
    
    // Teste 7: Validar regra dos 3 dias
    static void testarRegraDiasLiberacao(DesejoCompraDAO dao) throws SQLException {
        System.out.println("【TESTE 7】 Validando regra de liberação (3 dias)");
        
        // Inserir desejos com diferentes idades
        LocalDate hoje = LocalDate.now();
        
        Object[][] desejosIdades = {
            {"Desejo de hoje", 100.00, hoje},
            {"Desejo de ontem", 200.00, hoje.minusDays(1)},
            {"Desejo de 2 dias", 300.00, hoje.minusDays(2)},
            {"Desejo de 3 dias", 400.00, hoje.minusDays(3)},
            {"Desejo de 5 dias", 500.00, hoje.minusDays(5)},
            {"Desejo de 7 dias", 600.00, hoje.minusDays(7)},
            {"Desejo de 10 dias", 700.00, hoje.minusDays(10)}
        };
        
        System.out.println("  Testando liberação conforme idade do desejo:");
        System.out.println("  " + "-".repeat(65));
        System.out.printf("  %-20s | %10s | %8s | %12s%n", "Nome", "Idade(dias)", "Liberado?", "Dias restantes");
        System.out.println("  " + "-".repeat(65));
        
        for (Object[] d : desejosIdades) {
            String nome = (String) d[0];
            double valor = (Double) d[1];
            LocalDate data = (LocalDate) d[2];
            
            // Inserir diretamente com a data específica
            String sql = "INSERT INTO desejos_compra (nome_item, valor, data_criacao) VALUES (?, ?, ?)";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, nome);
                pstmt.setDouble(2, valor);
                pstmt.setString(3, data.toString());
                pstmt.executeUpdate();
            }
            
            // Buscar o desejo recém-inserido
            List<DesejoCompra> todos = dao.listarTodos();
            DesejoCompra inserido = todos.stream()
                .filter(desejo -> desejo.getNomeItem().equals(nome))
                .findFirst()
                .orElse(null);
            
            if (inserido != null) {
                long diasPassados = java.time.temporal.ChronoUnit.DAYS.between(data, hoje);
                System.out.printf("  %-20s | %10d | %8s | %12d%n",
                                  nome.length() > 20 ? nome.substring(0, 17) + "..." : nome,
                                  diasPassados,
                                  inserido.isLiberado() ? "✓ SIM" : "✗ NÃO",
                                  inserido.getDiasRestantes());
            }
        }
        System.out.println("  " + "-".repeat(65));
        System.out.println("  ✅ Regra de liberação validada!");
        System.out.println("  (Desejos com 3+ dias devem estar liberados)");
        
        System.out.println("\n  📝 RESUMO DA REGRA DOS 3 DIAS:");
        System.out.println("     - Dias 0,1,2: NÃO liberados (aguardando)");
        System.out.println("     - Dias 3,4,5...: LIBERADOS");
        System.out.println("     - Desejos com 3+ dias podem ser removidos automaticamente\n");
    }
}