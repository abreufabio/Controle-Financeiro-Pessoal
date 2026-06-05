package service;

import dao.ChecklistDiarioDAO;
import model.ChecklistDiario;

import java.sql.SQLException;
import java.time.LocalDate;

public class ChecklistService {

    private final ChecklistDiarioDAO checklistDAO;

    public ChecklistService() {
        this.checklistDAO = new ChecklistDiarioDAO();
    }

    public void registrarChecklist(boolean anotouGastos) throws SQLException {
        ChecklistDiario checklist = new ChecklistDiario(anotouGastos);
        checklistDAO.salvarOuAtualizar(checklist);
    }

    public boolean jaRegistrouHoje() throws SQLException {
        return checklistDAO.hasRegistroHoje();
    }

    public ChecklistDiario getChecklistHoje() throws SQLException {
        return checklistDAO.buscarPorData(LocalDate.now());
    }

    public double getTaxaSucesso() throws SQLException {
        return checklistDAO.getTaxaSucesso();
    }
}