package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ChecklistDiario {

    private int id;
    private LocalDate dataVerificacao;
    private boolean anotouGastos;
    private double totalGastoDia;  // ← NOVO CAMPO

    public ChecklistDiario(boolean anotouGastos) {
        this.dataVerificacao = LocalDate.now();
        this.anotouGastos = anotouGastos;
        this.totalGastoDia = 0.0;  // ← INICIALIZA COM ZERO
    }

    public ChecklistDiario(int id, LocalDate dataVerificacao, boolean anotouGastos, double totalGastoDia) {
        this.id = id;
        this.dataVerificacao = dataVerificacao;
        this.anotouGastos = anotouGastos;
        this.totalGastoDia = totalGastoDia;  // ← ADICIONADO
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public LocalDate getDataVerificacao() { return dataVerificacao; }
    public void setDataVerificacao(LocalDate dataVerificacao) { this.dataVerificacao = dataVerificacao; }
    
    public boolean isAnotouGastos() { return anotouGastos; }
    public void setAnotouGastos(boolean anotouGastos) { this.anotouGastos = anotouGastos; }
    
    public double getTotalGastoDia() { return totalGastoDia; }  // ← NOVO
    public void setTotalGastoDia(double totalGastoDia) { this.totalGastoDia = totalGastoDia; }  // ← NOVO

    public String getStatusTexto() {
        return anotouGastos ? "✓ Registrado" : "✗ Pendente";
    }

    public String getDataFormatada() {
        return dataVerificacao.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    @Override
    public String toString() {
        return String.format("Checklist %s: %s - Total gasto: R$ %.2f", 
               getDataFormatada(), getStatusTexto(), totalGastoDia);
    }
}