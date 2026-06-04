package view;

import dao.HistoricoRendaDAO;
import model.DesejoCompra;
import service.ChecklistService;
import service.PrimeiroEuService;
import service.RegraTresDiasService;
import repository.DatabaseManager;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * CLASSE PRINCIPAL - INTERFACE GRÁFICA DO SISTEMA FINANCEIRO
 * 
 * Funcionalidades principais:
 * 1. Regra dos 3 Dias: Sistema anti-impulso para compras
 * 2. Regra do Primeiro Eu: Sistema de poupança automática (10% da renda)
 * 3. Checklist Diário: Controle de registro de gastos
 * 4. Dashboard: Visualização de métricas e indicadores financeiros
 * 
 * Arquitetura: MVC (Model-View-Controller)
 * - Model: classes do package model (DesejoCompra, etc.)
 * - View: esta classe (interface gráfica)
 * - Controller: services (RegraTresDiasService, etc.)
 */
public class MainDesktopApp extends JFrame {

    // ===== 1. CONSTANTES DE ESTILO =====
    // Tema escuro inspirado no Linux Mint Mate
    private static final Color BG_DARK = new Color(47, 52, 55);        // Fundo principal
    private static final Color PANEL_DARK = new Color(60, 65, 68);     // Fundo de painéis
    private static final Color NAV_DARK = new Color(42, 46, 50);       // Fundo da navegação
    private static final Color BORDER = new Color(95, 100, 103);       // Cor das bordas
    private static final Color TEXT_PRIMARY = new Color(238, 238, 236); // Texto principal
    private static final Color TEXT_SECONDARY = new Color(186, 189, 182); // Texto secundário
    private static final Color MINT = new Color(134, 179, 0);          // Cor de destaque (verde menta)
    private static final Color SUCCESS = new Color(134, 179, 0);       // Verde para ações positivas
    private static final Color WARNING = new Color(245, 166, 35);      // Laranja para alertas
    private static final Color DANGER = new Color(204, 76, 76);        // Vermelho para perigo/ações negativas
    private static final Color ACCENT = new Color(102, 187, 106);      // Verde claro para acentos
    private static final Color ACCENT_LIGHT = new Color(165, 214, 167); // Verde mais claro

    // ===== 2. COMPONENTES DA INTERFACE =====
    private JPanel cardPanel;              // Painel que contém todas as telas (CardLayout)
    private CardLayout cardLayout;         // Gerenciador de layout que permite trocar entre telas
    private DefaultTableModel tableModelDesejos; // Modelo de dados para a tabela de desejos

    // ===== 3. SERVICES (CAMADA DE NEGÓCIO) =====
    private final RegraTresDiasService regraTresDiasService; // Gerencia a lógica da Regra dos 3 Dias
    private final PrimeiroEuService primeiroEuService;       // Gerencia a lógica do Primeiro Eu
    private final ChecklistService checklistService;         // Gerencia a lógica do Checklist

    // ===== 4. CONSTRUTOR =====
    /**
     * Construtor principal - Inicializa a aplicação
     * Configura a janela, verifica conexão com banco e monta o layout
     */
    public MainDesktopApp() {
        // Inicializa os serviços (camada de negócio)
        this.regraTresDiasService = new RegraTresDiasService();
        this.primeiroEuService = new PrimeiroEuService();
        this.checklistService = new ChecklistService();

        // Configurações básicas da janela
        setTitle("Meu controle financeiro");  // Título da janela
        setSize(850, 600);                    // Dimensões: 850x600 pixels
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Fecha a aplicação ao clicar no X
        setLocationRelativeTo(null);          // Centraliza a janela na tela

        // Verifica se o banco de dados está acessível
        verificarConexaoBanco();
        
        // Monta toda a interface gráfica
        configurarLayout();
    }

    // ===== 5. MÉTODOS DE CONFIGURAÇÃO INICIAL =====
    
    /**
     * Verifica a conexão com o banco de dados MySQL
     * Se falhar, exibe mensagem de erro com instruções
     */
    private void verificarConexaoBanco() {
        if (!DatabaseManager.testConnection()) {
            JOptionPane.showMessageDialog(this,
                "Não foi possível conectar ao MySQL!\n\n" +
                "Verifique se:\n" +
                "1. O MySQL está rodando\n" +
                "2. O banco 'controle_financeiro' existe\n" +
                "3. Usuário/senha estão corretos em DatabaseManager.java",
                "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Configura o layout principal da aplicação
     * Cria um CardLayout para permitir navegação entre diferentes telas
     * Adiciona cada painel (tela) ao cardPanel com um identificador único
     * Adiciona a barra de navegação na parte inferior
     */
    private void configurarLayout() {
        // CardLayout permite trocar entre telas como cartas em um baralho
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Adiciona cada tela ao cardPanel com um nome identificador
        cardPanel.add(criarPainelMenu(), "Menu");           // Tela inicial
        cardPanel.add(criarPainelRegraTresDias(), "Regra3Dias"); // Tela da Regra dos 3 Dias
        cardPanel.add(criarPainelPrimeiroEu(), "PrimeiroEu");    // Tela do Primeiro Eu
        cardPanel.add(criarPainelChecklist(), "Checklist");      // Tela do Checklist
        cardPanel.add(criarPainelDashboard(), "Dashboard");      // Tela do Dashboard

        // Adiciona os componentes à janela
        add(cardPanel, BorderLayout.CENTER);  // Painel de telas no centro
        add(criarBarraNavegacao(), BorderLayout.SOUTH); // Barra de navegação na parte inferior
        
        // Mostra a tela inicial (Menu)
        cardLayout.show(cardPanel, "Menu");
    }

    // ===== 6. MÉTODOS DA BARRA DE NAVEGAÇÃO =====
    
    /**
     * Cria a barra de navegação com botões para todas as telas do sistema
     * @return JPanel contendo os botões estilizados
     */
    private JPanel criarBarraNavegacao() {
        // Painel com layout FlowLayout (centralizado, espaçamento 10px horizontal, 5px vertical)
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panel.setBackground(NAV_DARK); // Cor escura da barra

        // Define os textos dos botões e os identificadores das telas correspondentes
        String[] botoes = { "Menu", "3 Dias", "Primeiro Eu", "Checklist", "Dashboard" };
        String[] telas = { "Menu", "Regra3Dias", "PrimeiroEu", "Checklist", "Dashboard" };

        // Cria cada botão dinamicamente
        for (int i = 0; i < botoes.length; i++) {
            final String tela = telas[i]; // Identificador da tela (precisa ser final para uso no ActionListener)
            JButton btn = criarBotaoArredondado(botoes[i], PANEL_DARK, Color.WHITE);
            
            // Define ação do botão - navega para a tela correspondente
            btn.addActionListener(e -> navegarPara(tela));
            panel.add(btn);
        }
        return panel;
    }

    /**
     * Navega entre as telas do sistema
     * Atualiza dados específicos antes de mostrar a tela, se necessário
     * @param tela Identificador da tela para onde navegar
     */
    private void navegarPara(String tela) {
        // Se for para a tela da Regra dos 3 Dias, atualiza a tabela de desejos
        if (tela.equals("Regra3Dias")) {
            atualizarTabelaDesejos(); // Carrega dados atualizados do banco
        }
        
        // Se for para o Dashboard, mostra as métricas
        if (tela.equals("Dashboard")) {
            mostrarDashboard(); // Exibe o dashboard com dados atualizados
        }
        
        // Troca para a tela solicitada usando o CardLayout
        cardLayout.show(cardPanel, tela);
    }

    // ===== 7. MÉTODOS DO PAINEL MENU (TELA INICIAL) =====
    
    /**
     * Cria o painel do menu principal com cards informativos
     * @return JPanel estilizado com GridBagLayout
     */
    private JPanel criarPainelMenu() {
        // GridBagLayout permite controle fino sobre o posicionamento dos componentes
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50)); // Margens: cima, esq, baixo, dir

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER; // Ocupa toda a linha
        gbc.fill = GridBagConstraints.HORIZONTAL;     // Expande horizontalmente
        gbc.insets = new Insets(10, 10, 10, 10);      // Espaçamento entre componentes

        // Adiciona componentes em ordem vertical
        panel.add(criarTituloMenu(), gbc);
        panel.add(criarSubtituloMenu(), gbc);
        panel.add(Box.createVerticalStrut(30), gbc); // Espaçador vertical de 30px
        panel.add(criarCardsMenu(), gbc);
        
        return panel;
    }

    /**
     * Cria o título principal do menu
     * @return JLabel estilizada com o título
     */
    private JLabel criarTituloMenu() {
        JLabel titulo = new JLabel("Sistema de controle financeiro", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 22));
        titulo.setForeground(TEXT_PRIMARY);
        return titulo;
    }

    /**
     * Cria o subtítulo com informações sobre a arquitetura do sistema
     * @return JLabel estilizada com o subtítulo
     */
    private JLabel criarSubtituloMenu() {
        JLabel subtitulo = new JLabel("Arquitetura POO no padrão MVC - Banco de dados MySQL", SwingConstants.CENTER);
        subtitulo.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitulo.setForeground(TEXT_SECONDARY);
        return subtitulo;
    }

    /**
     * Cria os cards informativos do menu (2x2 grid)
     * Cada card descreve uma funcionalidade do sistema
     * @return JPanel com grid de 2 linhas e 2 colunas
     */
    private JPanel criarCardsMenu() {
        JPanel cardsPanel = new JPanel(new GridLayout(2, 2, 20, 20)); // 2 linhas, 2 colunas, gap 20px
        cardsPanel.setOpaque(false); // Fundo transparente para herdar o BG_DARK
        
        // Adiciona os 4 cards com suas respectivas descrições
        cardsPanel.add(criarCard("Regra dos 3 dias",
                "Evite comprar por impulso!\nTodo desejo fica bloqueado por três dias antes da liberação",
                ACCENT_LIGHT));
        cardsPanel.add(criarCard("Regra do Primeiro Eu",
                "Poupe 10% da sua renda antes de qualquer gasto.\nInvista no seu futuro primeiro!",
                ACCENT_LIGHT));
        cardsPanel.add(criarCard("Checklist Diário",
                "Registre seus gastos diariamente.\nMantenha o controle financeiro em dia!", 
                ACCENT_LIGHT));
        cardsPanel.add(criarCard("Dashboard",
                "Visualize métricas e indicadores.\nAcompanhe seu progresso financeiro!",
                ACCENT_LIGHT));
        return cardsPanel;
    }

    /**
     * Cria um card individual para o menu
     * @param titulo Título do card
     * @param descricao Descrição em texto da funcionalidade
     * @param cor Cor de destaque do card (borda e título)
     * @return JPanel estilizado como card
     */
    private JPanel criarCard(String titulo, String descricao, Color cor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(PANEL_DARK);
        
        // Borda composta: linha colorida + espaçamento interno
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(cor, 2),    // Borda externa colorida
            BorderFactory.createEmptyBorder(15, 15, 15, 15))); // Espaçamento interno

        // Título do card
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitulo.setForeground(cor);

        // Área de texto para a descrição (permite múltiplas linhas)
        JTextArea txtDesc = new JTextArea(descricao);
        txtDesc.setFont(new Font("Arial", Font.PLAIN, 14));
        txtDesc.setForeground(new Color(236, 239, 241));
        txtDesc.setEditable(false);        // Não permite edição
        txtDesc.setBackground(PANEL_DARK); // Fundo transparente
        txtDesc.setLineWrap(true);         // Quebra linha automaticamente
        txtDesc.setWrapStyleWord(true);    // Quebra por palavras, não por caracteres

        card.add(lblTitulo, BorderLayout.NORTH);
        card.add(txtDesc, BorderLayout.CENTER);
        return card;
    }

    // ===== 8. MÉTODOS DO PAINEL REGRA DOS 3 DIAS =====
    
    /**
     * Cria o painel da Regra dos 3 Dias
     * Permite adicionar desejos de compra que ficam bloqueados por 3 dias
     * @return JPanel completo com formulário e tabela
     */
    private JPanel criarPainelRegraTresDias() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Adiciona os três componentes principais: título, formulário e botão de limpeza
        panel.add(criarTituloRegraTresDias(), BorderLayout.NORTH);
        panel.add(criarFormularioRegraTresDias(), BorderLayout.CENTER);
        panel.add(criarBotaoLimparRegraTresDias(), BorderLayout.SOUTH);
        
        return panel;
    }

    /**
     * Cria o título do painel Regra dos 3 Dias
     * @return JLabel com título estilizado
     */
    private JLabel criarTituloRegraTresDias() {
        JLabel titulo = new JLabel("Regra dos 3 Dias - Sistema Anti-Impulso", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        titulo.setForeground(MINT);
        return titulo;
    }

    /**
     * Cria o formulário completo da Regra dos 3 Dias
     * Inclui campos de entrada e tabela de desejos
     * @return JPanel com formulário (norte) + tabela (centro)
     */
    private JPanel criarFormularioRegraTresDias() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        
        // Painel do formulário de entrada (campos + botão)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(PANEL_DARK);
        formPanel.setBorder(BorderFactory.createTitledBorder("Novo Desejo de Compra"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Espaçamento entre componentes
        
        // Campos de entrada
        JTextField txtItem = new JTextField(20);
        JTextField txtValor = new JTextField(10);
        
        // Estilização dos campos de texto
        txtItem.setBackground(PANEL_DARK);
        txtItem.setForeground(TEXT_PRIMARY);
        txtItem.setCaretColor(TEXT_PRIMARY); // Cor do cursor
        txtItem.setBorder(BorderFactory.createLineBorder(BORDER));
        
        txtValor.setBackground(PANEL_DARK);
        txtValor.setForeground(TEXT_PRIMARY);
        txtValor.setCaretColor(TEXT_PRIMARY);
        txtValor.setBorder(BorderFactory.createLineBorder(BORDER));
        
        // Layout dos componentes no formPanel
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblItem = new JLabel("Item desejado:");
        lblItem.setForeground(TEXT_PRIMARY);
        formPanel.add(lblItem, gbc);
        gbc.gridx = 1;
        formPanel.add(txtItem, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblValor = new JLabel("Valor (R$):");
        lblValor.setForeground(TEXT_PRIMARY);
        formPanel.add(lblValor, gbc);
        gbc.gridx = 1;
        formPanel.add(txtValor, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2; // Botão ocupará 2 colunas
        JButton btnAdicionar = criarBotaoAdicionarDesejo(txtItem, txtValor);
        formPanel.add(btnAdicionar, gbc);
        
        // Configuração da tabela de desejos
        String[] colunas = {"Item", "Valor", "Data Registro", "Status", "Dias Restantes"};
        tableModelDesejos = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabela não editável pelo usuário
            }
        };
        
        JTable table = new JTable(tableModelDesejos);
        table.setRowHeight(25);                 // Altura da linha
        table.setBackground(PANEL_DARK);
        table.setForeground(TEXT_PRIMARY);
        table.setGridColor(BORDER);
        table.setSelectionBackground(MINT);     // Cor de seleção
        table.setSelectionForeground(Color.BLACK);
        
        // Painel de rolagem para a tabela
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Lista de Desejos"));
        scrollPane.getViewport().setBackground(PANEL_DARK);
        
        // Monta o painel final
        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    /**
     * Cria o botão para adicionar um novo desejo à lista
     * @param txtItem Campo de texto para o nome do item
     * @param txtValor Campo de texto para o valor
     * @return JButton configurado com ação de adicionar ao banco
     */
    private JButton criarBotaoAdicionarDesejo(JTextField txtItem, JTextField txtValor) {
        JButton btn = criarBotaoArredondado("Bloquear Compra por 3 Dias", MINT, Color.WHITE);
        
        // ActionListener: executa quando o botão é clicado
        btn.addActionListener(e -> {
            try {
                // Captura e valida os dados do formulário
                String nome = txtItem.getText().trim();
                double valor = Double.parseDouble(txtValor.getText().trim());
                
                // Chama o serviço para salvar no banco
                regraTresDiasService.adicionarDesejo(nome, valor);
                
                // Limpa os campos
                txtItem.setText("");
                txtValor.setText("");
                
                // Atualiza a tabela para mostrar o novo item
                atualizarTabelaDesejos();
                
                // Feedback ao usuário
                JOptionPane.showMessageDialog(this, 
                    "Item bloqueado por 3 dias!\nVocê só poderá comprar após o período de reflexão.");
                    
            } catch (NumberFormatException ex) {
                // Erro: valor não é um número válido
                JOptionPane.showMessageDialog(this, "Insira um valor numérico válido!");
            } catch (IllegalArgumentException ex) {
                // Erro: validação de negócio (ex: valor negativo)
                JOptionPane.showMessageDialog(this, ex.getMessage());
            } catch (SQLException ex) {
                // Erro: problema no banco de dados
                JOptionPane.showMessageDialog(this, "Erro ao salvar: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        
        return btn;
    }

    /**
     * Cria o botão para remover desejos que já foram liberados (após 3 dias)
     * @return JPanel contendo o botão centralizado
     */
    private JPanel criarBotaoLimparRegraTresDias() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(BG_DARK);
        
        JButton btnLimpar = criarBotaoArredondado("Remover Desejos Liberados", DANGER, Color.WHITE);
        btnLimpar.addActionListener(e -> {
            try {
                // Remove todos os desejos que já estão liberados (dias restantes = 0)
                regraTresDiasService.limparLiberados();
                atualizarTabelaDesejos(); // Recarrega a tabela
                JOptionPane.showMessageDialog(this, "Desejos liberados removidos com sucesso!");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao remover: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        
        panel.add(btnLimpar);
        return panel;
    }

    /**
     * Atualiza a tabela de desejos com dados atuais do banco de dados
     * Calcula dias restantes e status de cada desejo
     */
    private void atualizarTabelaDesejos() {
        if (tableModelDesejos == null) return; // Segurança: tabela ainda não foi criada
        
        // Limpa todas as linhas atuais
        tableModelDesejos.setRowCount(0);
        
        try {
            // Busca todos os desejos do banco de dados
            var desejos = regraTresDiasService.listarDesejos();
            LocalDate hoje = LocalDate.now();
            
            // Para cada desejo, calcula o status e adiciona na tabela
            for (DesejoCompra d : desejos) {
                long diasRestantes = d.getDiasRestantes(); // Calcula dias entre data registro e hoje
                String status = diasRestantes > 0 ? "BLOQUEADO" : "LIBERADO";
                String diasTexto = diasRestantes > 0 ? diasRestantes + " dias" : "Liberado";
                
                tableModelDesejos.addRow(new Object[]{
                    d.getNomeItem(),
                    String.format("R$ %.2f", d.getValor()), // Formata valor com 2 casas decimais
                    d.getDataCriacao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    status,
                    diasTexto
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao carregar desejos: " + e.getMessage());
        }
    }

    // ===== 9. MÉTODOS DO PAINEL PRIMEIRO EU =====
    
    /**
     * Cria o painel da Regra do Primeiro Eu
     * Calcula e exibe plano de alocação de renda (10% poupança, 90% gastos)
     * @return JPanel completo com formulário e área de resultados
     */
    private JPanel criarPainelPrimeiroEu() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        panel.add(criarTituloPrimeiroEu(), BorderLayout.NORTH);
        panel.add(criarFormularioPrimeiroEu(), BorderLayout.CENTER);
        
        return panel;
    }

    /**
     * Cria o título do painel Primeiro Eu
     * @return JLabel com título estilizado
     */
    private JLabel criarTituloPrimeiroEu() {
        JLabel titulo = new JLabel("Regra do Primeiro Eu - Poupe 10% da Renda", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        titulo.setForeground(MINT);
        return titulo;
    }

    /**
     * Cria o formulário do Primeiro Eu
     * Permite inserir renda e processa mostrando plano de alocação
     * @return JPanel com campos de entrada e área de resultado
     */
    private JPanel criarFormularioPrimeiroEu() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        
        // Painel de entrada (renda + botão)
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        inputPanel.setBackground(PANEL_DARK);
        inputPanel.setBorder(BorderFactory.createTitledBorder("Registrar Renda"));
        
        // Campo para valor da renda
        JTextField txtRenda = new JTextField(12);
        txtRenda.setFont(new Font("Arial", Font.PLAIN, 14));
        txtRenda.setBackground(PANEL_DARK);
        txtRenda.setForeground(TEXT_PRIMARY);
        txtRenda.setCaretColor(TEXT_PRIMARY);
        txtRenda.setBorder(BorderFactory.createLineBorder(BORDER));
        
        // Botão para processar
        JButton btnCalcular = criarBotaoArredondado("Processar Renda", MINT, Color.WHITE);
        
        // Labels
        JLabel lblRenda = new JLabel("Renda bruta: R$ ");
        lblRenda.setForeground(TEXT_PRIMARY);
        
        // Adiciona componentes ao painel de entrada
        inputPanel.add(lblRenda);
        inputPanel.add(txtRenda);
        inputPanel.add(btnCalcular);
        
        // Área de texto para exibir o plano de alocação
        JTextArea txtResultado = new JTextArea(10, 40);
        txtResultado.setEditable(false);
        txtResultado.setFont(new Font("Monospaced", Font.PLAIN, 12)); // Fonte monoespaçada para alinhamento
        txtResultado.setBackground(PANEL_DARK);
        txtResultado.setForeground(TEXT_PRIMARY);
        txtResultado.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(MINT),
            "Plano de Alocação"
        ));
        
        // Painel de rolagem para a área de texto
        JScrollPane scrollResultado = new JScrollPane(txtResultado);
        scrollResultado.getViewport().setBackground(PANEL_DARK);
        
        // Ação do botão Calcular
        btnCalcular.addActionListener(e -> {
            try {
                double renda = Double.parseDouble(txtRenda.getText());
                // Processa a renda: calcula 10% poupança, 90% gastos, salva no banco
                var resultado = primeiroEuService.processarRenda(renda);
                // Exibe o resultado formatado
                txtResultado.setText(resultado.formatarResultado());
                txtRenda.setText(""); // Limpa o campo
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Insira um valor numérico válido!");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao salvar: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        
        // Monta o painel final
        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(scrollResultado, BorderLayout.CENTER);
        
        return panel;
    }

    // ===== 10. MÉTODOS DO PAINEL CHECKLIST =====
    
    /**
     * Cria o painel do Checklist Diário
     * Permite registrar se o usuário anotou os gastos do dia
     * @return JPanel com pergunta e botões de resposta
     */
    private JPanel criarPainelChecklist() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        panel.add(criarTituloChecklist(), BorderLayout.NORTH);
        panel.add(criarConteudoChecklist(), BorderLayout.CENTER);
        
        return panel;
    }

    /**
     * Cria o título do painel Checklist
     * @return JLabel com título estilizado
     */
    private JLabel criarTituloChecklist() {
        JLabel titulo = new JLabel("Checklist Diário de Gastos", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        titulo.setForeground(MINT);
        return titulo;
    }

    /**
     * Cria o conteúdo interativo do Checklist
     * Inclui pergunta, botões SIM/NAO e status atual
     * @return JPanel com todos os componentes
     */
    private JPanel criarConteudoChecklist() {
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(PANEL_DARK);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Pergunta principal
        JLabel pergunta = new JLabel("Você registrou minuciosamente todos os seus gastos hoje?");
        pergunta.setFont(new Font("Arial", Font.BOLD, 14));
        pergunta.setForeground(TEXT_PRIMARY);
        centerPanel.add(pergunta, gbc);
        
        // Painel de botões
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setBackground(PANEL_DARK);
        
        JButton btnSim = criarBotaoArredondado("SIM, registrei meus gastos", SUCCESS, Color.WHITE);
        JButton btnNao = criarBotaoArredondado("NÃO, ainda não registrei", DANGER, Color.WHITE);
        
        // Ações dos botões
        btnSim.addActionListener(e -> registrarChecklist(true));
        btnNao.addActionListener(e -> registrarChecklist(false));
        
        btnPanel.add(btnSim);
        btnPanel.add(btnNao);
        centerPanel.add(btnPanel, gbc);
        
        // Label de status (mostra se já registrou hoje)
        JLabel lblStatus = criarLabelStatusChecklist();
        centerPanel.add(lblStatus, gbc);
        
        return centerPanel;
    }

    /**
     * Cria label que mostra o status atual do checklist para o dia de hoje
     * @return JLabel com texto e cor apropriados conforme o status
     */
    private JLabel criarLabelStatusChecklist() {
        JLabel lblStatus = new JLabel();
        lblStatus.setFont(new Font("Arial", Font.PLAIN, 12));
        
        try {
            if (checklistService.jaRegistrouHoje()) {
                var hoje = checklistService.getChecklistHoje();
                String status = hoje.isAnotouGastos() ? 
                    "Hoje você já registrou seus gastos. Parabéns!" : 
                    "Hoje você marcou que não registrou gastos. Que tal registrar agora?";
                lblStatus.setText(status);
                lblStatus.setForeground(hoje.isAnotouGastos() ? SUCCESS : WARNING);
            } else {
                lblStatus.setText("Você ainda não registrou nada hoje. Responda acima!");
                lblStatus.setForeground(TEXT_SECONDARY);
            }
        } catch (SQLException e) {
            lblStatus.setText("Erro ao verificar status: " + e.getMessage());
            lblStatus.setForeground(DANGER);
            e.printStackTrace();
        }
        
        return lblStatus;
    }

    /**
     * Registra a resposta do checklist no banco de dados
     * @param anotou true se registrou gastos, false caso contrário
     */
    private void registrarChecklist(boolean anotou) {
        try {
            System.out.println("Registrando checklist: anotou = " + anotou);
            // Salva no banco
            checklistService.registrarChecklist(anotou);
            
            // Feedback ao usuário
            String mensagem = anotou ? 
                "Excelente! Continue mantendo o controle dos seus gastos!" :
                "Registro salvo. Não se esqueça de registrar seus gastos amanhã!";
            JOptionPane.showMessageDialog(this, mensagem);
            
            // Recarrega o painel para atualizar o status
            recarregarPainelChecklist();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao registrar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Recarrega o painel do checklist para mostrar o status atualizado
     * Remove o painel antigo e adiciona um novo com dados frescos
     */
    private void recarregarPainelChecklist() {
        // Itera sobre os componentes do cardPanel para encontrar o painel do Checklist
        Component[] components = cardPanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            Component comp = components[i];
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                // Verifica se é o painel do checklist pela borda vazada (EmptyBorder)
                if (panel.getBorder() instanceof EmptyBorder) {
                    JPanel novoPainel = criarPainelChecklist(); // Cria novo painel atualizado
                    cardPanel.remove(panel);      // Remove o antigo
                    cardPanel.add(novoPainel, "Checklist", i); // Adiciona o novo na mesma posição
                    cardPanel.revalidate();       // Revalida o layout
                    cardPanel.repaint();          // Redesenha
                    break;
                }
            }
        }
    }

    // ===== 11. MÉTODOS DO PAINEL DASHBOARD =====
    
    /**
     * Cria o painel do Dashboard
     * Mostra métricas e indicadores financeiros
     * @return JPanel com dashboard
     */
    private JPanel criarPainelDashboard() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Título
        JLabel titulo = new JLabel("Dashboard - Métricas e Indicadores", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        titulo.setForeground(ACCENT_LIGHT);
        panel.add(titulo, BorderLayout.NORTH);
        
        // Área de texto para exibir os dados (fonte monoespaçada para alinhamento)
        JTextArea dashboardArea = new JTextArea();
        dashboardArea.setEditable(false);
        dashboardArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        dashboardArea.setBackground(PANEL_DARK);
        dashboardArea.setForeground(TEXT_PRIMARY);
        
        // Scroll para a área de texto
        JScrollPane scrollPane = new JScrollPane(dashboardArea);
        scrollPane.getViewport().setBackground(PANEL_DARK);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Botão para atualizar o dashboard
        JButton btnAtualizar = criarBotaoArredondado("Atualizar Dashboard", ACCENT, Color.WHITE);
        btnAtualizar.addActionListener(e -> mostrarDashboard(dashboardArea));
        
        panel.add(btnAtualizar, BorderLayout.SOUTH);
        
        // Carrega os dados inicialmente
        mostrarDashboard(dashboardArea);
        
        return panel;
    }

    /**
     * Mostra o dashboard em um diálogo modal (pop-up)
     * Usado quando o dashboard está em uma tela separada
     */
    private void mostrarDashboard() {
        // Cria uma área de texto temporária
        JTextArea tempArea = new JTextArea();
        tempArea.setBackground(PANEL_DARK);
        tempArea.setForeground(TEXT_PRIMARY);
        mostrarDashboard(tempArea);
        
        // Coloca em um scroll pane
        JScrollPane scrollPane = new JScrollPane(tempArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        scrollPane.getViewport().setBackground(PANEL_DARK);
        
        // Exibe em um diálogo
        JOptionPane.showMessageDialog(this, scrollPane, "Dashboard Financeiro", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Mostra o dashboard na área de texto fornecida
     * Coleta dados de todos os serviços e formata em texto
     * @param dashboardArea JTextArea onde exibir o dashboard
     */
    private void mostrarDashboard(JTextArea dashboardArea) {
        StringBuilder sb = new StringBuilder();
        
        // Cabeçalho
        sb.append("=".repeat(60)).append("\n");
        sb.append("              DASHBOARD FINANCEIRO\n");
        sb.append("=".repeat(60)).append("\n\n");
        
        // Adiciona informações de cada módulo
        adicionarInfoDesejosDashboard(sb);    // Regra dos 3 Dias
        adicionarInfoRendasDashboard(sb);     // Primeiro Eu
        adicionarInfoChecklistDashboard(sb);  // Checklist
        
        // Rodapé motivacional
        sb.append("\n\n=".repeat(60)).append("\n");
        sb.append("     Continue assim! Controle financeiro é hábito!\n");
        sb.append("=".repeat(60));
        
        dashboardArea.setText(sb.toString());
    }

    /**
     * Adiciona informações da Regra dos 3 Dias ao dashboard
     * @param sb StringBuilder para construir o texto
     */
    private void adicionarInfoDesejosDashboard(StringBuilder sb) {
        sb.append("REGRA DOS 3 DIAS\n");
        sb.append("-".repeat(40)).append("\n");
        
        try {
            var desejos = regraTresDiasService.listarDesejos();
            
            // Conta quantos estão bloqueados vs liberados
            long bloqueados = desejos.stream().filter(d -> d.getDiasRestantes() > 0).count();
            long liberados = desejos.stream().filter(d -> d.getDiasRestantes() == 0).count();
            
            // Soma dos valores bloqueados
            double totalBloqueado = desejos.stream()
                .filter(d -> d.getDiasRestantes() > 0)
                .mapToDouble(DesejoCompra::getValor)
                .sum();
            
            // Exibe métricas
            sb.append(String.format("Desejos bloqueados:   %d\n", bloqueados));
            sb.append(String.format("Desejos liberados:    %d\n", liberados));
            sb.append(String.format("Total bloqueado:      R$ %,10.2f\n", totalBloqueado));
            sb.append(String.format("Economia potencial:   R$ %,10.2f\n", totalBloqueado));
        } catch (SQLException e) {
            sb.append("Erro ao carregar dados dos desejos: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
        sb.append("\n");
    }

    /**
     * Adiciona informações do Primeiro Eu ao dashboard
     * @param sb StringBuilder para construir o texto
     */
    private void adicionarInfoRendasDashboard(StringBuilder sb) {
        sb.append("REGRA DO PRIMEIRO EU\n");
        sb.append("-".repeat(40)).append("\n");
        
        try {
            HistoricoRendaDAO rendaDAO = new HistoricoRendaDAO();
            double totalRendaMes = rendaDAO.getTotalRendaMesAtual();   // Soma de todas as rendas do mês
            double totalPoupadoMes = totalRendaMes * 0.10;             // 10% vai para poupança
            
            // Exibe métricas
            sb.append(String.format("Renda total do mês:   R$ %,10.2f\n", totalRendaMes));
            sb.append(String.format("Total poupado (10%%): R$ %,10.2f\n", totalPoupadoMes));
            sb.append(String.format("Meta anual:           R$ %,10.2f\n", totalPoupadoMes * 12));
        } catch (SQLException e) {
            sb.append("Erro ao carregar dados das rendas: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
        sb.append("\n");
    }

    /**
     * Adiciona informações do Checklist ao dashboard
     * @param sb StringBuilder para construir o texto
     */
    private void adicionarInfoChecklistDashboard(StringBuilder sb) {
        sb.append("CHECKLIST DIÁRIO\n");
        sb.append("-".repeat(40)).append("\n");
        
        try {
            double taxaSucesso = checklistService.getTaxaSucesso(); // Percentual de dias que registrou gastos
            sb.append(String.format("Taxa de sucesso:      %.1f%%\n", taxaSucesso));
            
            // Verifica status de hoje
            if (checklistService.jaRegistrouHoje()) {
                var hoje = checklistService.getChecklistHoje();
                sb.append(String.format("Status hoje:          %s\n", 
                    hoje.isAnotouGastos() ? "Registrado" : "Pendente"));
            } else {
                sb.append("Status hoje:          Não registrado\n");
            }
            
            // Recomendação baseada na taxa de sucesso
            String recomendacao = taxaSucesso >= 80 ? 
                "Excelente! Mantenha o foco!" :
                (taxaSucesso >= 50 ? "Bom progresso! Continue melhorando!" : 
                "Você precisa registrar seus gastos com mais frequência!");
            sb.append("\n").append(recomendacao);
        } catch (SQLException e) {
            sb.append("Erro ao carregar dados do checklist: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    // ===== 12. MÉTODO AUXILIAR PARA BOTÕES ARREDONDADOS =====
    
    /**
     * Cria um botão com cantos arredondados e efeitos hover/press
     * Sobrescreve o método paintComponent para desenhar um botão customizado
     * @param texto Texto do botão
     * @param bgColor Cor de fundo
     * @param fgColor Cor do texto
     * @return JButton estilizado
     */
    private JButton criarBotaoArredondado(String texto, Color bgColor, Color fgColor) {
        JButton botao = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                // Ativa anti-aliasing para bordas suaves
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Muda cor conforme estado do botão (pressionado, hover, normal)
                if (getModel().isPressed()) {
                    g2.setColor(bgColor.darker());     // Mais escuro quando pressionado
                } else if (getModel().isRollover()) {
                    g2.setColor(bgColor.brighter());   // Mais claro quando mouse em cima
                } else {
                    g2.setColor(bgColor);              // Cor normal
                }
                
                // Desenha retângulo arredondado (15px de raio nos cantos)
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
                
                // Desenha o texto do botão
                super.paintComponent(g);
            }
        };
        
        // Configurações básicas
        botao.setForeground(fgColor);
        botao.setBackground(bgColor);
        botao.setFocusPainted(false);     // Remove borda de foco
        botao.setBorderPainted(false);    // Remove borda padrão
        botao.setContentAreaFilled(false); // Não preenche área padrão (usamos nosso paintComponent)
        botao.setOpaque(false);            // Fundo transparente
        botao.setFont(new Font("Arial", Font.BOLD, 12));
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Cursor de mão ao passar mouse
        
        return botao;
    }

    // ===== 13. MÉTODO MAIN =====
    
    /**
     * Ponto de entrada da aplicação
     * Configura o Look and Feel (aparência) do sistema e inicia a interface
     * @param args Argumentos de linha de comando (não utilizados)
     */
    public static void main(String[] args) {
        // Define o Look and Feel padrão do sistema operacional
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace(); // Se falhar, usa o padrão do Java
        }
        
        // Executa a criação da interface na thread de eventos do Swing
        SwingUtilities.invokeLater(() -> {
            new MainDesktopApp().setVisible(true); // Cria e mostra a janela
        });
    }
}

/*
 * RESUMO DAS FUNCIONALIDADES IMPLEMENTADAS:
 * 
 * 1. REGRA DOS 3 DIAS (Anti-impulso)
 *    - Adiciona desejos de compra com data de registro
 *    - Calcula automaticamente dias restantes baseado na data atual
 *    - Bloqueia compras por 3 dias (período de reflexão)
 *    - Permite remover desejos já liberados
 *    - Tabela mostra status (BLOQUEADO/LIBERADO) e dias restantes
 * 
 * 2. REGRA DO PRIMEIRO EU (Poupança automática)
 *    - Processa renda informada pelo usuário
 *    - Calcula 10% para poupança e 90% para gastos
 *    - Registra cada renda no histórico do banco
 *    - Mantém total do mês atual
 * 
 * 3. CHECKLIST DIÁRIO (Controle de gastos)
 *    - Registra se o usuário anotou os gastos do dia
 *    - Permite apenas um registro por dia
 *    - Mantém histórico de registros
 *    - Calcula taxa de sucesso (percentual de dias com registro)
 * 
 * 4. DASHBOARD (Métricas)
 *    - Mostra resumo de desejos (bloqueados/liberados)
 *    - Mostra total economizado (valores bloqueados)
 *    - Mostra total de renda do mês e poupança acumulada
 *    - Mostra taxa de sucesso do checklist
 *    - Inclui recomendações baseadas nos dados
 * 
 * 5. INTERFACE GRÁFICA
 *    - Tema escuro inspirado no Linux Mint Mate
 *    - Navegação por abas (CardLayout)
 *    - Botões com efeitos hover/press e cantos arredondados
 *    - Layout responsivo e profissional
 * 
 * 6. ARQUITETURA
 *    - MVC: View (esta classe), Model (classes model), Controller (services)
 *    - Persistência em MySQL via JDBC
 *    - Services encapsulam regras de negócio
 *    - DAOs (Data Access Objects) para acesso ao banco
 */