package model;  // Pacote da camada de modelo

import java.time.LocalDate;  // Para trabalhar com datas (sem horas)

public class HistoricoRenda {
    
    // ATRIBUTOS - cada registro de renda terá esses dados
    private int id;                // Identificador único no banco de dados
    private double valorTotal;     // Valor total da renda (ex: salário, freela, etc)
    private LocalDate dataRegistro; // Data em que a renda foi registrada no sistema
    
    // CONSTRUTOR 1: Usado quando o usuário registra uma NOVA renda (ainda sem ID)
    // Parâmetro: 
    //   valorTotal - quanto dinheiro o usuário recebeu
    public HistoricoRenda(double valorTotal) {
        this.valorTotal = valorTotal;              // Guarda o valor recebido
        this.dataRegistro = LocalDate.now();       // Define a data atual como data do registro
        // Obs: o ID fica como 0 porque ainda será gerado pelo banco de dados
    }
    
    // CONSTRUTOR 2: Usado quando carregamos rendas JÁ EXISTENTES no banco de dados
    // Parâmetros:
    //   id - identificador vindo do banco
    //   valorTotal - valor da renda salvo
    //   dataRegistro - data em que foi registrada originalmente
    public HistoricoRenda(int id, double valorTotal, LocalDate dataRegistro) {
        this.id = id;                // Recupera o ID do banco
        this.valorTotal = valorTotal; // Recupera o valor salvo
        this.dataRegistro = dataRegistro; // Recupera a data original
    }
    // Métodos para acessar e modificar os atributos de forma controlada
    public int getId() {
        return id;  // Retorna o ID atual
    }
    
    public void setId(int id) {
        this.id = id;  // Altera o ID (útil após salvar no banco)
    }
    
    public double getValorTotal() {
        return valorTotal;  // Retorna o valor total da renda
    }
    
    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;  // Permite editar o valor depois
    }
    
    public LocalDate getDataRegistro() {
        return dataRegistro;  // Retorna a data de registro
    }
    
    public void setDataRegistro(LocalDate dataRegistro) {
        this.dataRegistro = dataRegistro;  // Permite modificar a data
    }
    // Calcula quanto o usuário deve POUPAR desta renda
    // Regra financeira: poupar 10% de tudo que receber
    // Retorno: double - 10% do valor total
    public double getValorPoupar() {
        return valorTotal * 0.10;  // Multiplica por 10/100 = 0,10
        // Exemplo: valorTotal = 2000.00 → retorna 200.00
    }
    
    // Calcula quanto o usuário pode GASTAR livremente desta renda
    // Fórmula: valor total - valor que deve ser poupado
    // Retorno: double - o que sobra após separar os 10%
    public double getValorDisponivel() {
        return valorTotal - getValorPoupar();
        // Exemplo: valorTotal = 2000.00
        // getValorPoupar() = 200.00
        // getValorDisponivel() = 2000.00 - 200.00 = 1800.00
    }
    // Sobrescreve o método toString() da classe Object
    // Define como o objeto será exibido quando convertido para String
    // Exemplo de saída: "Renda: R$ 2000.00 em 2026-06-01"
    @Override
    public String toString() {
        // String.format() formata a string com placeholders:
        // %.2f → número decimal com 2 casas decimais (ex: 2000.00)
        // %s → string (no caso, a data)
        return String.format("Renda: R$ %.2f em %s", valorTotal, dataRegistro);
    }
}