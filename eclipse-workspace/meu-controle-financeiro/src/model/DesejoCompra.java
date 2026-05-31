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
	
}
