package service;

import dao.DesejoCompraDAO;
import model.DesejoCompra;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

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
        // Limita o nome para evitar problemas no banco
        String nomeLimpo = nome.trim().substring(0, Math.min(nome.trim().length(), 200));
        DesejoCompra desejo = new DesejoCompra(nomeLimpo, valor);
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

    // NOVO MÉTODO: Lista apenas desejos bloqueados
    public List<DesejoCompra> listarBloqueados() throws SQLException {
        return listarDesejos().stream()
                .filter(d -> d.getDiasRestantes() > 0)
                .collect(Collectors.toList());
    }

    // NOVO MÉTODO: Lista apenas desejos liberados
    public List<DesejoCompra> listarLiberados() throws SQLException {
        return listarDesejos().stream()
                .filter(d -> d.getDiasRestantes() == 0)
                .collect(Collectors.toList());
    }

    // NOVO MÉTODO: Conta quantos desejos estão bloqueados
    public long countBloqueados() throws SQLException {
        return listarDesejos().stream()
                .filter(d -> d.getDiasRestantes() > 0)
                .count();
    }

    // NOVO MÉTODO: Soma total dos valores bloqueados
    public double getTotalBloqueado() throws SQLException {
        return listarDesejos().stream()
                .filter(d -> d.getDiasRestantes() > 0)
                .mapToDouble(DesejoCompra::getValor)
                .sum();
    }

    // NOVO MÉTODO: Resumo dos desejos para o Dashboard
    public ResumoDesejos getResumoDesejos() throws SQLException {
        var desejos = listarDesejos();
        long bloqueados = desejos.stream().filter(d -> d.getDiasRestantes() > 0).count();
        long liberados = desejos.stream().filter(d -> d.getDiasRestantes() == 0).count();
        double totalBloqueado = desejos.stream()
                .filter(d -> d.getDiasRestantes() > 0)
                .mapToDouble(DesejoCompra::getValor)
                .sum();
        
        return new ResumoDesejos(bloqueados, liberados, totalBloqueado);
    }

    // NOVO MÉTODO: Verifica se existe algum desejo liberado
    public boolean existeDesejoLiberado() throws SQLException {
        return listarDesejos().stream().anyMatch(d -> d.getDiasRestantes() == 0);
    }

    // CLASSE AUXILIAR: Resumo para Dashboard
    public static class ResumoDesejos {
        private final long bloqueados;
        private final long liberados;
        private final double totalBloqueado;

        public ResumoDesejos(long bloqueados, long liberados, double totalBloqueado) {
            this.bloqueados = bloqueados;
            this.liberados = liberados;
            this.totalBloqueado = totalBloqueado;
        }

        public long getBloqueados() { return bloqueados; }
        public long getLiberados() { return liberados; }
        public double getTotalBloqueado() { return totalBloqueado; }
        public double getEconomiaPotencial() { return totalBloqueado; }
    }
}