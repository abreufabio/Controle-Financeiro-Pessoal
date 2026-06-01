package model;  // Pacote onde a classe está localizada (camada de modelo)

import java.time.LocalDate;  // Importa a classe para trabalhar com datas (sem hora)

public class ChecklistDiario {
    // ATRIBUTOS (variáveis de instância)
    private int id;                    // Identificador único no banco de dados
    private LocalDate dataVerificacao; // Data em que o checklist foi realizado
    private boolean anotouGastos;      // Flag que indica se o usuário registrou gastos

    // CONSTRUTOR 1: Para criar um novo checklist do dia atual
    // Parâmetro: anotouGastos - estado inicial do registro de gastos
    public ChecklistDiario(boolean anotouGastos) {
        // Atribui a data atual do sistema à data de verificação
        this.dataVerificacao = LocalDate.now();
        // Atribui o valor recebido ao campo anotouGastos
        this.anotouGastos = anotouGastos;
        // OBS: id fica vazio (0) pois ainda não foi salvo no banco
    }

    // CONSTRUTOR 2: Para recuperar um checklist já existente no banco de dados
    // Parâmetros: 
    public ChecklistDiario(int id, LocalDate dataVerificacao, boolean anotouGastos) {
        this.id = id;                       // Recupera o ID do banco
        this.dataVerificacao = dataVerificacao; // Recupera a data salva
        this.anotouGastos = anotouGastos;   // Recupera o status salvo
    }

    // GETTERS E SETTERS (métodos de acesso)
    
    // GETTER: Retorna o ID do checklist
    // Retorno: int - valor do identificador
    public int getId() {
        return id;  // Apenas retorna o valor do atributo
    }

    // SETTER: Altera o ID do checklist
    // Parâmetro: id - novo valor a ser atribuído
    public void setId(int id) {
        this.id = id;  // Atribui o valor recebido ao atributo da classe
    }

    // GETTER: Retorna a data de verificação
    // Retorno: LocalDate - objeto com ano, mês e dia
    public LocalDate getDataVerificacao() {
        return dataVerificacao;  // Retorna o valor do atributo
    }

    // SETTER: Altera a data de verificação
    // Parâmetro: dataVerificacao - nova data a ser atribuída
    public void setDataVerificacao(LocalDate dataVerificacao) {
        this.dataVerificacao = dataVerificacao;  // Atribui nova data
    }

    // GETTER: Verifica se os gastos foram anotados
    // Retorno: boolean - true se anotou, false se não
    public boolean isAnotouGastos() {
        return anotouGastos;  // Padrão 'is' para variável booleana
    }

    // SETTER: Altera o status de anotação de gastos
    // Parâmetro: anotouGastos - novo status (true/false)
    public void setAnotouGastos(boolean anotouGastos) {
        this.anotouGastos = anotouGastos;  // Atribui novo status
    }

    // MÉTODO PARA EXIBIÇÃO DO STATUS EM TEXTO
    // Retorno: String - "Registrado" se true, "Pendente" se false
    // Operador ternário: condição ? valor_se_verdadeiro : valor_se_falso
    public String getStatusTexto() {
        return anotouGastos ? "Registrado" : "Pendente";
        // Se anotouGastos == true → retorna "Registrado"
        // Se anotouGastos == false → retorna "Pendente"
    }

    // MÉTODO toString: Representação textual do objeto
    // Retorno: String - formato "Checklist 2024-01-15: Registrado"
    // String.format() formata a string com placeholders %s
    // %s é substituído pelo primeiro argumento (dataVerificacao)
    // %s é substituído pelo segundo argumento (resultado de getStatusTexto())
    @Override
    public String toString() {
        return String.format("Checklist %s: %s", dataVerificacao, getStatusTexto());
    }
    	//Observações importantes:
    	//this é usado para diferenciar parâmetros de atributos
        //LocalDate.now() pega a data atual do sistema
        //O operador ternário (? :) substitui um if-else simples
        //@Override indica que estamos sobrescrevendo o método da classe Object
}