/*Classe para a indicar desejos de compra que deseja
 * mas so pode liberar apos 3 dias, caso o desejo de 
 * comprar ainda persista*/

package model;

import java.time.LocalDate; //Pegar a data localmente
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DesejoCompra {
	
	private int id;					//identificador no banco de dados
	private String nomeItem;		//Nome do item
	private double valor;			//Valor do item
	private LocalDate dataCriacao;  //Data do "desejo" foi registrado
	
	//Contrutor do desjo sem o ID
	public DesejoCompra(String nomeItem, double valor) {
		this.nomeItem = nomeItem;
		this.valor = valor;
		this.dataCriacao = LocalDate.now();
	}
	
	//Construtor para recuperar os dados direto BD com o ID
	public DesejoCompra(int id, String nomeItem, double valor, LocalDate dataCriacao) {
		this.id = id;
		this.nomeItem = nomeItem;
		this.valor = valor;
		this.dataCriacao = dataCriacao;
	}
	
	//Metodos GEt e SET
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
	
	//Verifica se o desejo pode ser liberado (após três dia)
	public boolean isLiberado() {
		return ChronoUnit.DAYS.between(dataCriacao, LocalDate.now()) >= 3;
	}
	
	//Retorna dias retantes para liberação
	public long getDiasRestantes() {
		long diasPassados = ChronoUnit.DAYS.between(dataCriacao, LocalDate.now());
		return Math.max(0, 3 -diasPassados);
	}
	
	public static void main(String[] args) {
	    // Teste 1: Criar novo desejo
	    System.out.println("=== TESTE 1: Criando novo desejo ===");
	    DesejoCompra desejo1 = new DesejoCompra("PlayStation 5", 4500.00);
	    System.out.println(desejo1);
	    System.out.println("Liberado? " + desejo1.isLiberado());
	    System.out.println("Dias restantes: " + desejo1.getDiasRestantes());
	    
	    // Teste 2: Criar desejo com data antiga (já liberado)
	    System.out.println("\n=== TESTE 2: Desejo com data antiga ===");
	    LocalDate dataAntiga = LocalDate.now().minusDays(5);
	    DesejoCompra desejo2 = new DesejoCompra(1, "TV 4K", 3200.00, dataAntiga);
	    System.out.println(desejo2);
	    System.out.println("Data criação: " + desejo2.getDataCriacao());
	    System.out.println("Liberado? " + desejo2.isLiberado());
	    System.out.println("Dias restantes: " + desejo2.getDiasRestantes());
	    
	    // Teste 3: Desejo com 2 dias (ainda não liberado)
	    System.out.println("\n=== TESTE 3: Desejo com 2 dias ===");
	    LocalDate data2Dias = LocalDate.now().minusDays(2);
	    DesejoCompra desejo3 = new DesejoCompra(2, "Fone Bluetooth", 250.00, data2Dias);
	    System.out.println(desejo3);
	    System.out.println("Liberado? " + desejo3.isLiberado());
	    System.out.println("Dias restantes: " + desejo3.getDiasRestantes());
	}
}
