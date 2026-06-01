// service/ChecklistService.java
package service;  // Pacote da camada de serviço

import dao.ChecklistDiarioDAO;    // Importa o DAO do checklist
import model.ChecklistDiario;     // Importa o modelo do checklist

import java.sql.SQLException;     // Trata exceções de SQL
import java.time.LocalDate;       // Para trabalhar com datas

public class ChecklistService {
    private final ChecklistDiarioDAO checklistDAO;  // DAO para operações no banco
    
    // Inicializa o service com uma instância do DAO
    public ChecklistService() {
        this.checklistDAO = new ChecklistDiarioDAO();
    }
        
    // Registra o checklist do dia (se já existir para hoje, atualiza)
    // Parâmetro: anotouGastos - true se o usuário anotou seus gastos hoje
    public void registrarChecklist(boolean anotouGastos) throws SQLException {
        // Cria um novo objeto ChecklistDiario com a data atual
        ChecklistDiario checklist = new ChecklistDiario(anotouGastos);
        
        // Salva ou atualiza no banco (UPSERT)
        // Se já existe registro para hoje, atualiza; senão, insere novo
        checklistDAO.salvarOuAtualizar(checklist);
    }
    
    // Verifica se o usuário já registrou o checklist hoje
    // Útil para evitar registros duplicados ou mostrar mensagem adequada
    public boolean jaRegistrouHoje() throws SQLException {
        // Delega para o DAO que verifica se existe registro com data atual
        return checklistDAO.hasRegistroHoje();
    }
    
    // Obtém o checklist completo do dia de hoje
    // Retorna o objeto com status (anotouGastos) e ID
    public ChecklistDiario getChecklistHoje() throws SQLException {
        // Busca no banco o registro com data igual a hoje
        return checklistDAO.buscarPorData(LocalDate.now());
    }
    
    // Obtém a taxa de sucesso geral do usuário
    // Porcentagem de dias que ele anotou os gastos desde que começou
    public double getTaxaSucesso() throws SQLException {
        // Delega para o DAO que calcula a média
        return checklistDAO.getTaxaSucesso();
    }
}