package model;

import java.time.LocalDate;

public class HistoricoRenda {
    private int id;
    private double valorTotal;
    private LocalDate dataRegisto;
    
    // Construtor para nova renda
    public HistoricoRenda(double valorTotal) {
        this.valorTotal = valorTotal;
        this.dataRegisto = LocalDate.now();
    }
    
    // Construtor para recuperar do banco
    public HistoricoRenda(int id, double valorTotal, LocalDate dataRegisto) {
        this.id = id;
        this.valorTotal = valorTotal;
        this.dataRegisto = dataRegisto;
    }
    
    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public double getValorTotal() { return valorTotal; }
    public void setValorTotal(double valorTotal) { this.valorTotal = valorTotal; }
    
    public LocalDate getDataRegisto() { return dataRegisto; }
    public void setDataRegisto(LocalDate dataRegisto) { this.dataRegisto = dataRegisto; }
    
    // Calcula valor a poupar (10%)
    public double getValorPoupar() {
        return valorTotal * 0.10;
    }
    
    // Calcula valor disponível para gastos
    public double getValorDisponivel() {
        return valorTotal - getValorPoupar();
    }
    
    @Override
    public String toString() {
        return String.format("Renda: R$ %.2f em %s", valorTotal, dataRegisto);
    }
    
}