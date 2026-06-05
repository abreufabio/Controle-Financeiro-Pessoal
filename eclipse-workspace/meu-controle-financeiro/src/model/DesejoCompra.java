package model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DesejoCompra {

    private int id;
    private String nomeItem;
    private double valor;
    private LocalDate dataCriacao;

    public DesejoCompra(String nomeItem, double valor) {
        this.nomeItem = nomeItem;
        this.valor = valor;
        this.dataCriacao = LocalDate.now();
    }

    public DesejoCompra(int id, String nomeItem, double valor, LocalDate dataCriacao) {
        this.id = id;
        this.nomeItem = nomeItem;
        this.valor = valor;
        this.dataCriacao = dataCriacao;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomeItem() {
        return nomeItem;
    }

    public void setNomeItem(String nomeItem) {
        this.nomeItem = nomeItem;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public LocalDate getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDate dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public boolean isLiberado() {
        return ChronoUnit.DAYS.between(dataCriacao, LocalDate.now()) >= 3;
    }

    public long getDiasRestantes() {
        long diasPassados = ChronoUnit.DAYS.between(dataCriacao, LocalDate.now());
        return Math.max(0, 3 - diasPassados);
    }
}