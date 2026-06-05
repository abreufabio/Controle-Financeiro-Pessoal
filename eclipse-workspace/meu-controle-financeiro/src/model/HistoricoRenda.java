package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class HistoricoRenda {

    public static final double PERCENTUAL_POUPANCA = 0.10;

    private int id;
    private double valorTotal;
    private LocalDate dataRegistro;

    public HistoricoRenda(double valorTotal) {
        this.valorTotal = valorTotal;
        this.dataRegistro = LocalDate.now();
    }

    public HistoricoRenda(int id, double valorTotal, LocalDate dataRegistro) {
        this.id = id;
        this.valorTotal = valorTotal;
        this.dataRegistro = dataRegistro;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public double getValorTotal() { return valorTotal; }
    public void setValorTotal(double valorTotal) { this.valorTotal = valorTotal; }
    public LocalDate getDataRegistro() { return dataRegistro; }
    public void setDataRegistro(LocalDate dataRegistro) { this.dataRegistro = dataRegistro; }

    public double getValorPoupar() {
        return valorTotal * PERCENTUAL_POUPANCA;
    }

    public double getValorDisponivel() {
        return valorTotal - getValorPoupar();
    }

    // NOVO MÉTODO: Retorna o percentual formatado para exibição
    public String getPercentualFormatado() {
        return String.format("%.0f%%", PERCENTUAL_POUPANCA * 100);
    }

    // NOVO MÉTODO: Verifica se a renda é do mês atual
    public boolean isRendaMesAtual() {
        LocalDate hoje = LocalDate.now();
        return dataRegistro.getYear() == hoje.getYear() && 
               dataRegistro.getMonth() == hoje.getMonth();
    }

    // NOVO MÉTODO: Retorna resumo formatado
    public String getResumoFormatado() {
        return String.format("R$ %,10.2f em %s", valorTotal, 
               dataRegistro.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    @Override
    public String toString() {
        return String.format("Renda: R$ %.2f em %s", valorTotal, dataRegistro);
    }
}