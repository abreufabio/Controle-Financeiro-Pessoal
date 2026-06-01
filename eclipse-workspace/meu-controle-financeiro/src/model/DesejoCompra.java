package model;  // Pacote onde a classe está localizada (camada de modelo)


import java.time.LocalDate;// Importa a classe para trabalhar com datas (sem hora)
import java.time.temporal.ChronoUnit;// Importa classe para cálculos entre datas (dias, meses, anos)

public class DesejoCompra {
    
    // ATRIBUTOS (variáveis de instância)
    private int id;                 // Identificador único no banco de dados
    private String nomeItem;        // Nome do produto que o usuário deseja comprar
    private double valor;           // Preço do item em reais (ou outra moeda)
    private LocalDate dataCriacao;  // Data em que o desejo foi registrado no sistema
    
    // CONSTRUTOR 1: Para criar um novo desejo (sem ID, pois ainda não foi salvo no BD)
    // Parâmetros:
    public DesejoCompra(String nomeItem, double valor) {
        this.nomeItem = nomeItem;        // Atribui o nome recebido
        this.valor = valor;              // Atribui o valor recebido
        this.dataCriacao = LocalDate.now(); // Define a data atual como data de criação
        // OBS: id fica vazio (0) pois ainda será gerado pelo banco de dados
    }
    
    // CONSTRUTOR 2: Para recuperar desejos já existentes no banco de dados
    public DesejoCompra(int id, String nomeItem, double valor, LocalDate dataCriacao) {
        this.id = id;                    // Recupera o ID do banco
        this.nomeItem = nomeItem;        // Recupera o nome salvo
        this.valor = valor;              // Recupera o valor salvo
        this.dataCriacao = dataCriacao;  // Recupera a data salva
    }
    // GETTERS E SETTERS (métodos de acesso)
    // GETTER: Retorna o ID do desejo
    // Retorno: int - valor do identificador
    public int getId() {
        return id;  // Apenas retorna o valor do atributo
    }
    
    // SETTER: Altera o ID do desejo
    // Parâmetro: id - novo valor a ser atribuído
    public void setId(int id) {
        this.id = id;  // Atribui o valor recebido ao atributo da classe
    }
    
    // GETTER: Retorna o nome do item desejado
    // Retorno: String - nome do produto
    public String getNomeItem() {
        return nomeItem;  // Retorna o valor do atributo
    }
    
    // SETTER: Altera o nome do item desejado
    // Parâmetro: nomeItem - novo nome a ser atribuído
    public void setNomeItem(String nomeItem) {
        this.nomeItem = nomeItem;  // Atribui o novo nome
    }
    
    // GETTER: Retorna o valor do item
    // Retorno: double - preço do produto
    public double getValor() {
        return valor;  // Retorna o valor atual
    }
    
    // SETTER: Altera o valor do item
    // Parâmetro: valor - novo preço a ser atribuído
    public void setValor(double valor) {
        this.valor = valor;  // Atribui o novo valor
    }
    
    // GETTER: Retorna a data de criação do desejo
    // Retorno: LocalDate - data em que foi registrado
    public LocalDate getDataCriacao() {
        return dataCriacao;  // Retorna a data atual
    }
    
    // SETTER: Altera a data de criação
    // Parâmetro: dataCriacao - nova data a ser atribuída
    public void setDataCriacao(LocalDate dataCriacao) {
        this.dataCriacao = dataCriacao;  // Atribui a nova data
    }
    
    // MÉTODO PARA VERIFICAR SE O DESEJO PODE SER LIBERADO
    // Regra de negócio: desejo só pode ser realizado após 3 dias de espera
    // Retorno: boolean - true se pode comprar, false se ainda não
    // ChronoUnit.DAYS.between() calcula a diferença em dias entre duas datas
    public boolean isLiberado() {
        // Calcula quantos dias se passaram desde a criação até hoje
        // Se for maior ou igual a 3, retorna true (liberado)
        return ChronoUnit.DAYS.between(dataCriacao, LocalDate.now()) >= 3;
        // Exemplo: dataCriacao = 01/06/2026, hoje = 04/06/2026
        // Dias entre = 3 → retorna true (liberado)
        // Exemplo: hoje = 02/06/2026 → dias entre = 1 → retorna false
    }
    
    // MÉTODO PARA CALCULAR DIAS RESTANTES ATÉ A LIBERAÇÃO
    // Retorno: long - número de dias que faltam (nunca negativo)
    // Usa Math.max() para garantir que não retorne número negativo
    public long getDiasRestantes() {
        // Calcula quantos dias já se passaram desde a criação
        long diasPassados = ChronoUnit.DAYS.between(dataCriacao, LocalDate.now());
        // Fórmula: 3 (dias necessários) - dias já passados
        // Math.max(0, resultado) garante que se já passou dos 3 dias, retorna 0
        return Math.max(0, 3 - diasPassados);
    }
}