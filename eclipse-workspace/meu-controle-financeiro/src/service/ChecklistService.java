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

    // NOVO MÉTODO: Retorna status do dia com texto e cor (para a View)
    public StatusChecklist getStatusHoje() throws SQLException {
        boolean jaRegistrou = jaRegistrouHoje();
        
        if (!jaRegistrou) {
            return new StatusChecklist("Você ainda não registrou nada hoje. Responda acima!", "SECONDARY", false);
        }
        
        ChecklistDiario hoje = getChecklistHoje();
        if (hoje.isAnotouGastos()) {
            return new StatusChecklist("Hoje você já registrou seus gastos. Parabéns!", "SUCCESS", true);
        } else {
            return new StatusChecklist("Hoje você marcou que não registrou gastos. Que tal registrar agora?", "WARNING", false);
        }
    }

    // NOVO MÉTODO: Resumo do checklist para Dashboard
    public ResumoChecklist getResumoChecklist() throws SQLException {
        double taxa = getTaxaSucesso();
        boolean registrouHoje = jaRegistrouHoje();
        boolean anotouHoje = registrouHoje && getChecklistHoje().isAnotouGastos();
        
        String recomendacao;
        if (taxa >= 80) {
            recomendacao = "Excelente! Continue com o bom trabalho!";
        } else if (taxa >= 50) {
            recomendacao = "Bom progresso! Continue melhorando!";
        } else {
            recomendacao = "Você precisa registrar seus gastos com mais frequência!";
        }
        
        return new ResumoChecklist(taxa, registrouHoje, anotouHoje, recomendacao);
    }

    // NOVO MÉTODO: Cache para evitar múltiplas consultas no mesmo dia (opcional)
    private transient StatusChecklist statusCache;
    private transient LocalDate dataCache;
    
    public StatusChecklist getStatusHojeComCache() throws SQLException {
        LocalDate hoje = LocalDate.now();
        if (statusCache != null && hoje.equals(dataCache)) {
            return statusCache;
        }
        statusCache = getStatusHoje();
        dataCache = hoje;
        return statusCache;
    }

    // CLASSE AUXILIAR: Status do checklist
    public static class StatusChecklist {
        private final String texto;
        private final String corReferencia; // "SUCCESS", "WARNING", "SECONDARY"
        private final boolean anotou;

        public StatusChecklist(String texto, String corReferencia, boolean anotou) {
            this.texto = texto;
            this.corReferencia = corReferencia;
            this.anotou = anotou;
        }

        public String getTexto() { return texto; }
        public String getCorReferencia() { return corReferencia; }
        public boolean isAnotou() { return anotou; }
    }

    // CLASSE AUXILIAR: Resumo para Dashboard
    public static class ResumoChecklist {
        private final double taxaSucesso;
        private final boolean registrouHoje;
        private final boolean anotouHoje;
        private final String recomendacao;

        public ResumoChecklist(double taxaSucesso, boolean registrouHoje, boolean anotouHoje, String recomendacao) {
            this.taxaSucesso = taxaSucesso;
            this.registrouHoje = registrouHoje;
            this.anotouHoje = anotouHoje;
            this.recomendacao = recomendacao;
        }

        public double getTaxaSucesso() { return taxaSucesso; }
        public boolean isRegistrouHoje() { return registrouHoje; }
        public boolean isAnotouHoje() { return anotouHoje; }
        public String getRecomendacao() { return recomendacao; }
        
        public String getStatusHojeTexto() {
            if (!registrouHoje) return "Não registrado";
            return anotouHoje ? "Registrado" : "Pendente";
        }
    }
}