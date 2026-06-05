package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ChecklistDiario {

    private int id;
    private LocalDate dataVerificacao;
    private boolean anotouGastos;

    public ChecklistDiario(boolean anotouGastos) {
        this.dataVerificacao = LocalDate.now();
        this.anotouGastos = anotouGastos;
    }

    public ChecklistDiario(int id, LocalDate dataVerificacao, boolean anotouGastos) {
        this.id = id;
        this.dataVerificacao = dataVerificacao;
        this.anotouGastos = anotouGastos;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public LocalDate getDataVerificacao() { return dataVerificacao; }
    public void setDataVerificacao(LocalDate dataVerificacao) { this.dataVerificacao = dataVerificacao; }
    public boolean isAnotouGastos() { return anotouGastos; }
    public void setAnotouGastos(boolean anotouGastos) { this.anotouGastos = anotouGastos; }

    public String getStatusTexto() {
        return anotouGastos ? "✓ Registrado" : "✗ Pendente";
    }

    // NOVO MÉTODO: Verifica se é do dia atual
    public boolean isHoje() {
        return dataVerificacao.equals(LocalDate.now());
    }

    // NOVO MÉTODO: Verifica se é do mês atual
    public boolean isMesAtual() {
        LocalDate hoje = LocalDate.now();
        return dataVerificacao.getYear() == hoje.getYear() && 
               dataVerificacao.getMonth() == hoje.getMonth();
    }

    // NOVO MÉTODO: Retorna data formatada
    public String getDataFormatada() {
        return dataVerificacao.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    // NOVO MÉTODO: Retorna cor sugerida para o status (para a View)
    public String getCorSugerida() {
        if (!isHoje()) return "SECONDARY";
        return anotouGastos ? "SUCCESS" : "WARNING";
    }

    @Override
    public String toString() {
        return String.format("Checklist %s: %s", getDataFormatada(), getStatusTexto());
    }
}