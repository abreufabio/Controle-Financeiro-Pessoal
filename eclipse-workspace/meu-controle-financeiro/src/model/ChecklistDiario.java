package model;

import java.time.LocalDate;

public class ChecklistDiario {
	private int id;
	private LocalDate dataVerificacao;
	private boolean anotouGastos;

	// Construtor para novo checklist (hoje)
	public ChecklistDiario(boolean anotouGastos) {
		this.dataVerificacao = LocalDate.now();
		this.anotouGastos = anotouGastos;
	}

	// Construtor para recuperar do banco
	public ChecklistDiario(int id, LocalDate dataVerificacao, boolean anotouGastos) {
		this.id = id;
		this.dataVerificacao = dataVerificacao;
		this.anotouGastos = anotouGastos;
	}

	// Getters e Setters
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public LocalDate getDataVerificacao() {
		return dataVerificacao;
	}

	public void setDataVerificacao(LocalDate dataVerificacao) {
		this.dataVerificacao = dataVerificacao;
	}

	public boolean isAnotouGastos() {
		return anotouGastos;
	}

	public void setAnotouGastos(boolean anotouGastos) {
		this.anotouGastos = anotouGastos;
	}

	// Retorna status do checklist para exibição
	public String getStatusTexto() {
		return anotouGastos ? "✓ Registrado" : "✗ Pendente";
	}

	@Override
	public String toString() {
		return String.format("Checklist %s: %s", dataVerificacao, getStatusTexto());
	}
}
