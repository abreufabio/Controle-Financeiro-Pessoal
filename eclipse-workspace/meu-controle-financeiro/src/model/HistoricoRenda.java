package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class HistoricoRenda {

    public static final double PERCENTUAL_POUPANCA = 0.10;

    private int id;
    private double valorTotal;
    private String descricao;
    private CategoriaRenda categoria;
    private LocalDate dataRegistro;

    // Construtor para NOVA renda (sem ID)
    public HistoricoRenda(double valorTotal, String descricao, CategoriaRenda categoria) {
        this.valorTotal = valorTotal;
        this.descricao = descricao;
        this.categoria = categoria;
        this.dataRegistro = LocalDate.now();
    }

    // Construtor para RECUPERAR renda do banco (com ID)
    public HistoricoRenda(int id, double valorTotal, String descricao, String categoria, LocalDate dataRegistro) {
        this.id = id;
        this.valorTotal = valorTotal;
        this.descricao = descricao;
        this.categoria = CategoriaRenda.valueOf(categoria);
        this.dataRegistro = dataRegistro;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public double getValorTotal() { return valorTotal; }
    public void setValorTotal(double valorTotal) { this.valorTotal = valorTotal; }
    
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    
    public CategoriaRenda getCategoria() { return categoria; }
    public void setCategoria(CategoriaRenda categoria) { this.categoria = categoria; }
    
    public LocalDate getDataRegistro() { return dataRegistro; }
    public void setDataRegistro(LocalDate dataRegistro) { this.dataRegistro = dataRegistro; }

    public String getCategoriaString() {
        return categoria.name();
    }

    public double getValorPoupar() {
        return valorTotal * PERCENTUAL_POUPANCA;
    }

    public double getValorDisponivel() {
        return valorTotal - getValorPoupar();
    }

    public boolean isRendaMesAtual() {
        LocalDate hoje = LocalDate.now();
        return dataRegistro.getYear() == hoje.getYear() && 
               dataRegistro.getMonth() == hoje.getMonth();
    }

    public String getDataFormatada() {
        return dataRegistro.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: R$ %.2f em %s", categoria.getDescricao(), descricao, valorTotal, getDataFormatada());
    }
}