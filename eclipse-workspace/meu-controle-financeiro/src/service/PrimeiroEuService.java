package service;

import dao.HistoricoRendaDAO;
import model.HistoricoRenda;

import java.sql.SQLException;

public class PrimeiroEuService {

    private final HistoricoRendaDAO rendaDAO;

    public PrimeiroEuService() {
        this.rendaDAO = new HistoricoRendaDAO();
    }

    public ResultadoRenda processarRenda(double valorTotal) throws SQLException {

        if (valorTotal <= 0) {
            throw new IllegalArgumentException("Valor da renda deve ser positivo");
        }

        HistoricoRenda renda = new HistoricoRenda(valorTotal);
        rendaDAO.salvar(renda);

        return new ResultadoRenda(
            valorTotal,
            renda.getValorPoupar(),
            renda.getValorDisponivel()
        );
    }

    // NOVO MÉTODO: Busca total de rendas do mês atual (para o Dashboard)
    public double getTotalRendaMesAtual() throws SQLException {
        return rendaDAO.getTotalRendaMesAtual();
    }

    // NOVO MÉTODO: Busca total de rendas de um mês específico
    public double getTotalRendaPorMes(int ano, int mes) throws SQLException {
        return rendaDAO.getTotalRendaPorMes(ano, mes);
    }

    // NOVO MÉTODO: Busca última renda registrada
    public HistoricoRenda getUltimaRenda() throws SQLException {
        var todas = rendaDAO.listarTodos();
        if (todas.isEmpty()) return null;
        return todas.get(0); // Já vem ordenado por data decrescente
    }

    // NOVO MÉTODO: Resumo completo para o Dashboard (evita múltiplas chamadas)
    public ResumoFinanceiro getResumoFinanceiro() throws SQLException {
        double totalMes = rendaDAO.getTotalRendaMesAtual();
        double totalPoupadoMes = totalMes * 0.10;
        double metaAnual = totalPoupadoMes * 12;
        
        return new ResumoFinanceiro(totalMes, totalPoupadoMes, metaAnual);
    }

    public static class ResultadoRenda {
        private final double valorTotal;
        private final double valorPoupar;
        private final double valorDisponivel;

        public ResultadoRenda(double valorTotal, double valorPoupar, double valorDisponivel) {
            this.valorTotal = valorTotal;
            this.valorPoupar = valorPoupar;
            this.valorDisponivel = valorDisponivel;
        }

        public double getValorTotal() { return valorTotal; }
        public double getValorPoupar() { return valorPoupar; }
        public double getValorDisponivel() { return valorDisponivel; }

        public String formatarResultado() {
            // Usa StringBuilder para melhor performance
            StringBuilder sb = new StringBuilder();
            String linha = "═".repeat(50);
            sb.append(linha).append("\n");
            sb.append("          PLANO DO PRIMEIRO EU (10%)\n");
            sb.append(linha).append("\n");
            sb.append(String.format("Renda Bruta:      R$ %,10.2f\n", valorTotal));
            sb.append(String.format("Poupança (10%%):   R$ %,10.2f\n", valorPoupar));
            sb.append(String.format("Disponível:       R$ %,10.2f\n", valorDisponivel));
            sb.append(linha).append("\n");
            sb.append(String.format("Ação: Transfira R$ %,10.2f para sua conta de investimentos\n", valorPoupar));
            sb.append("   antes de qualquer gasto!");
            return sb.toString();
        }
    }

    // NOVA CLASSE: Resumo financeiro para o Dashboard
    public static class ResumoFinanceiro {
        private final double totalRendaMes;
        private final double totalPoupadoMes;
        private final double metaAnual;

        public ResumoFinanceiro(double totalRendaMes, double totalPoupadoMes, double metaAnual) {
            this.totalRendaMes = totalRendaMes;
            this.totalPoupadoMes = totalPoupadoMes;
            this.metaAnual = metaAnual;
        }

        public double getTotalRendaMes() { return totalRendaMes; }
        public double getTotalPoupadoMes() { return totalPoupadoMes; }
        public double getMetaAnual() { return metaAnual; }
    }
}