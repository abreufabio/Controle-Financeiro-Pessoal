package model;

import java.time.LocalDate;

public class HistoricoRenda {

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public LocalDate getDataRegistro() {
        return dataRegistro;
    }

    public void setDataRegistro(LocalDate dataRegistro) {
        this.dataRegistro = dataRegistro;
    }

    public double getValorPoupar() {
        return valorTotal * 0.10;
    }

    public double getValorDisponivel() {
        return valorTotal - getValorPoupar();
    }

    @Override
    public String toString() {
        return String.format("Renda: R$ %.2f em %s", valorTotal, dataRegistro);
    }
}