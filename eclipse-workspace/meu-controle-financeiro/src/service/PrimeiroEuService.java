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

    public static class ResultadoRenda {

        private final double valorTotal;
        private final double valorPoupar;
        private final double valorDisponivel;

        public ResultadoRenda(double valorTotal, double valorPoupar, double valorDisponivel) {
            this.valorTotal = valorTotal;
            this.valorPoupar = valorPoupar;
            this.valorDisponivel = valorDisponivel;
        }

        public double getValorTotal() {
            return valorTotal;
        }

        public double getValorPoupar() {
            return valorPoupar;
        }

        public double getValorDisponivel() {
            return valorDisponivel;
        }

        public String formatarResultado() {
            return String.format(
                "═".repeat(50) + "\n" +
                "          PLANO DO PRIMEIRO EU (10%%)\n" +
                "═".repeat(50) + "\n" +
                "Renda Bruta:      R$ %,10.2f\n" +
                "Poupança (10%%):   R$ %,10.2f\n" +
                "Disponível:       R$ %,10.2f\n" +
                "═".repeat(50) + "\n" +
                "Ação: Transfira R$ %,10.2f para sua conta de investimentos\n" +
                "   antes de qualquer gasto!",
                valorTotal,
                valorPoupar,
                valorDisponivel,
                valorPoupar
            );
        }
    }
}