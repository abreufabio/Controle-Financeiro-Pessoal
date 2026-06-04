package service;  // Pacote da camada de serviço (regras de negócio)

import dao.DesejoCompraDAO;      // Importa o DAO para acesso ao banco
import model.DesejoCompra;       // Importa o modelo de dados

import java.sql.SQLException;    // Trata exceções de SQL
import java.util.List;           // Para listar os desejos

public class RegraTresDiasService {
    private final DesejoCompraDAO desejoDAO;  // DAO para operações no banco (final = não pode ser trocado)

    // Inicializa o service criando uma instância do DAO
    public RegraTresDiasService() {
        this.desejoDAO = new DesejoCompraDAO();  // Cria o DAO para ser usado pelos métodos
    }
    
    // Adiciona um novo desejo de compra (com validações)
    public void adicionarDesejo(String nome, double valor) throws SQLException {
        
        // VALIDAÇÃO 1: Nome não pode ser nulo ou vazio
        // trim() remove espaços no início e fim
        // isEmpty() verifica se string está vazia
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do item não pode estar vazio");
        }
        
        // VALIDAÇÃO 2: Valor deve ser positivo (maior que zero)
        if (valor <= 0) {
            throw new IllegalArgumentException("O valor deve ser positivo");
        }
        
        // Se passou nas validações, cria o objeto modelo
        // trim() no nome para remover espaços extras antes de salvar
        DesejoCompra desejo = new DesejoCompra(nome.trim(), valor);
        
        // Salva no banco de dados através do DAO
        desejoDAO.salvar(desejo);
    }
    
    // Lista todos os desejos com seus status (liberado ou não)
    public List<DesejoCompra> listarDesejos() throws SQLException {
        // Delega a responsabilidade para o DAO
        return desejoDAO.listarTodos();
    }
    
    // Remove um desejo específico pelo seu ID
    public void removerDesejo(int id) throws SQLException {
        // Delega a remoção para o DAO
        desejoDAO.remover(id);
    }
    
    // Remove todos os desejos que já estão liberados (com mais de 3 dias)
    public void limparLiberados() throws SQLException {
        // Delega a limpeza para o DAO
        desejoDAO.removerLiberados();
    }
}