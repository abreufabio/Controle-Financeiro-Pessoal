package service;

import dao.DesejoCompraDAO;
import model.DesejoCompra;

import java.sql.SQLException;
import java.util.List;

public class RegraTresDiasService {

    private final DesejoCompraDAO desejoDAO;

    public RegraTresDiasService() {
        this.desejoDAO = new DesejoCompraDAO();
    }

    public void adicionarDesejo(String nome, double valor) throws SQLException {

        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do item não pode estar vazio");
        }

        if (valor <= 0) {
            throw new IllegalArgumentException("O valor deve ser positivo");
        }

        DesejoCompra desejo = new DesejoCompra(nome.trim(), valor);

        desejoDAO.salvar(desejo);
    }

    public List<DesejoCompra> listarDesejos() throws SQLException {
        return desejoDAO.listarTodos();
    }

    public void removerDesejo(int id) throws SQLException {
        desejoDAO.remover(id);
    }

    public void limparLiberados() throws SQLException {
        desejoDAO.removerLiberados();
    }
}