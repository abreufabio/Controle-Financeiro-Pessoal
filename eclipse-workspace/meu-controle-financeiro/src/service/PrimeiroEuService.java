package service;

import dao.HistoricoRendaDAO;
import model.CategoriaRenda;
import model.HistoricoRenda;

import java.sql.SQLException;
import java.util.List;

public class PrimeiroEuService {

    private final HistoricoRendaDAO rendaDAO;

    public PrimeiroEuService() {
        this.rendaDAO = new HistoricoRendaDAO();
    }

    public void adicionarRenda(double valorTotal, String descricao, CategoriaRenda categoria) throws SQLException {
        if (valorTotal <= 0) {
            throw new IllegalArgumentException("Valor da renda deve ser positivo");
        }
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição não pode estar vazia");
        }
        if (categoria == null) {
            throw new IllegalArgumentException("Categoria não pode estar vazia");
        }

        HistoricoRenda renda = new HistoricoRenda(valorTotal, descricao.trim(), categoria);
        rendaDAO.salvar(renda);
    }

    public List<HistoricoRenda> listarRendas() throws SQLException {
        return rendaDAO.listarTodos();
    }

    public void removerRenda(int id) throws SQLException {
        rendaDAO.remover(id);
    }

    public double getTotalRendaMesAtual() throws SQLException {
        return rendaDAO.getTotalRendaMesAtual();
    }
    
    public double getTotalRendaFixoMesAtual() throws SQLException {
        return rendaDAO.getTotalRendaFixoMesAtual();
    }
    
    public double getTotalRendaExtraMesAtual() throws SQLException {
        return rendaDAO.getTotalRendaExtraMesAtual();
    }

    public ResumoRendas getResumoRendas() throws SQLException {
        double totalMes = rendaDAO.getTotalRendaMesAtual();
        double totalFixo = rendaDAO.getTotalRendaFixoMesAtual();
        double totalExtra = rendaDAO.getTotalRendaExtraMesAtual();
        double totalPoupado = totalMes * 0.10;
        
        return new ResumoRendas(totalMes, totalFixo, totalExtra, totalPoupado);
    }

    public static class ResumoRendas {
        private final double totalMes;
        private final double totalFixo;
        private final double totalExtra;
        private final double totalPoupado;

        public ResumoRendas(double totalMes, double totalFixo, double totalExtra, double totalPoupado) {
            this.totalMes = totalMes;
            this.totalFixo = totalFixo;
            this.totalExtra = totalExtra;
            this.totalPoupado = totalPoupado;
        }

        public double getTotalMes() { return totalMes; }
        public double getTotalFixo() { return totalFixo; }
        public double getTotalExtra() { return totalExtra; }
        public double getTotalPoupado() { return totalPoupado; }
        public double getTotalDisponivel() { return totalMes - totalPoupado; }
        
        public String formatar() {
            return String.format(
                "Renda total do mês:   R$ %,10.2f\n" +
                "  • Fixo:             R$ %,10.2f\n" +
                "  • Extra:            R$ %,10.2f\n" +
                "Poupança (10%%):       R$ %,10.2f\n" +
                "Disponível:           R$ %,10.2f",
                totalMes, totalFixo, totalExtra, totalPoupado, getTotalDisponivel()
            );
        }
    }
}