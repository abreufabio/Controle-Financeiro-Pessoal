// service/PrimeiroEuService.java
package service;  // Pacote da camada de serviço

import dao.HistoricoRendaDAO;      // Importa o DAO de renda
import model.HistoricoRenda;       // Importa o modelo de renda

import java.sql.SQLException;      // Trata exceções de SQL

public class PrimeiroEuService {
    private final HistoricoRendaDAO rendaDAO;  // DAO para operações no banco
    
    // Inicializa o service com uma instância do DAO
    public PrimeiroEuService() {
        this.rendaDAO = new HistoricoRendaDAO();
    }
    
    // Processa uma nova renda: valida, salva e retorna os cálculos financeiros
    public ResultadoRenda processarRenda(double valorTotal) throws SQLException {
        
        // VALIDAÇÃO: Valor da renda deve ser positivo
        if (valorTotal <= 0) {
            throw new IllegalArgumentException("Valor da renda deve ser positivo");
        }
        
        // Cria o objeto modelo (data atual é definida automaticamente)
        HistoricoRenda renda = new HistoricoRenda(valorTotal);
        
        // Salva no banco de dados
        rendaDAO.salvar(renda);
        
        // Retorna um objeto DTO com os resultados formatados
        // getValorPoupar() = 10% do total
        // getValorDisponivel() = 90% do total
        return new ResultadoRenda(valorTotal, renda.getValorPoupar(), renda.getValorDisponivel());
    }
    
    // ========== CLASSE INTERNA DTO (DATA TRANSFER OBJECT) ==========
    // Responsável por agrupar e formatar os resultados para exibição
    public static class ResultadoRenda {
        // Atributos finais (imutáveis após criação)
        private final double valorTotal;       // Renda bruta recebida
        private final double valorPoupar;      // 10% para poupança
        private final double valorDisponivel;  // 90% para gastos
        
        // Construtor: recebe os três valores calculados
        public ResultadoRenda(double valorTotal, double valorPoupar, double valorDisponivel) {
            this.valorTotal = valorTotal;
            this.valorPoupar = valorPoupar;
            this.valorDisponivel = valorDisponivel;
        }
        
        // Getters (apenas leitura - sem setters para manter imutabilidade)
        public double getValorTotal() { 
            return valorTotal; 
        }
        
        public double getValorPoupar() { 
            return valorPoupar; 
        }
        
        public double getValorDisponivel() { 
            return valorDisponivel; 
        }
        
        // MÉTODO DE FORMATAÇÃO: Cria um display bonito para o usuário
        public String formatarResultado() {
            return String.format(
                "═".repeat(50) + "\n" +          // Linha superior (50 caracteres "═")
                "          PLANO DO PRIMEIRO EU (10%%)\n" +  // Título com escape do %
                "═".repeat(50) + "\n" +          // Linha separadora
                "Renda Bruta:      R$ %,10.2f\n" +  // Renda total (%,10.2f formata com separador de milhar)
                "Poupança (10%%):   R$ %,10.2f\n" +  // Valor a poupar
                "Disponível:       R$ %,10.2f\n" +  // Valor disponível
                "═".repeat(50) + "\n" +          // Linha final
                "Ação: Transfira R$ %,10.2f para sua conta de investimentos\n" +
                "   antes de qualquer gasto!",
                valorTotal,        // Primeiro %: Renda Bruta
                valorPoupar,       // Segundo %: Poupança
                valorDisponivel,   // Terceiro %: Disponível
                valorPoupar        // Quarto %: Ação (mesmo valor da poupança)
            );
        }
    }
}