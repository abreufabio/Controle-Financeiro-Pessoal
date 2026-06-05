package model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DesejoCompra {

    // Constante para o número de dias de bloqueio (facilita manutenção)
    public static final int DIAS_BLOQUEIO = 3;

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

    // Getters e Setters...
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNomeItem() { return nomeItem; }
    public void setNomeItem(String nomeItem) { this.nomeItem = nomeItem; }
    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }
    public LocalDate getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDate dataCriacao) { this.dataCriacao = dataCriacao; }

    public boolean isLiberado() {
        return ChronoUnit.DAYS.between(dataCriacao, LocalDate.now()) >= DIAS_BLOQUEIO;
    }

    public long getDiasRestantes() {
        long diasPassados = ChronoUnit.DAYS.between(dataCriacao, LocalDate.now());
        return Math.max(0, DIAS_BLOQUEIO - diasPassados);
    }

    // NOVO MÉTODO: Retorna status formatado para exibição
    public String getStatusFormatado() {
        return isLiberado() ? "LIBERADO" : "BLOQUEADO";
    }

    // NOVO MÉTODO: Retorna dias restantes formatado
    public String getDiasRestantesFormatado() {
        long dias = getDiasRestantes();
        return dias > 0 ? dias + " dias" : "Liberado";
    }

    // NOVO MÉTODO: Verifica se o desejo é antigo (útil para limpeza)
    public boolean isAntigo() {
        return ChronoUnit.DAYS.between(dataCriacao, LocalDate.now()) > 30;
    }

    @Override
    public String toString() {
        return String.format("Desejo: %s - R$ %.2f (%s)", nomeItem, valor, dataCriacao);
    }
}