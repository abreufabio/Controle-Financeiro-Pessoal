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

public class MainDesktopApp extends JFrame {

    // ===== CORES TEMA LINUX MINT MATE =====
    private static final Color BG_DARK = new Color(47, 52, 55);
    private static final Color PANEL_DARK = new Color(60, 65, 68);
    private static final Color NAV_DARK = new Color(42, 46, 50);
    private static final Color BORDER = new Color(95, 100, 103);  // ← CONSTANTE ADICIONADA
    private static final Color TEXT_PRIMARY = new Color(238, 238, 236);
    private static final Color TEXT_SECONDARY = new Color(186, 189, 182);
    private static final Color MINT = new Color(134, 179, 0);
    private static final Color SUCCESS = new Color(134, 179, 0);
    private static final Color WARNING = new Color(245, 166, 35);
    private static final Color DANGER = new Color(204, 76, 76);
    private static final Color ACCENT = new Color(102, 187, 106);
    private static final Color ACCENT_LIGHT = new Color(165, 214, 167);

    // ===== COMPONENTES PRINCIPAIS =====
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private DefaultTableModel tableModelDesejos;

    // ===== SERVICES =====
    private final RegraTresDiasService regraTresDiasService;
    private final PrimeiroEuService primeiroEuService;
    private final ChecklistService checklistService;

    // ===== CONSTRUTOR =====
    public MainDesktopApp() {
        this.regraTresDiasService = new RegraTresDiasService();
        this.primeiroEuService = new PrimeiroEuService();
        this.checklistService = new ChecklistService();

        setTitle("Meu controle financeiro");
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        verificarConexaoBanco();
        configurarLayout();
    }

    // ===== 1. METODOS DE CONFIGURACAO INICIAL =====
    
    private void verificarConexaoBanco() {
        if (!DatabaseManager.testConnection()) {
            JOptionPane.showMessageDialog(this,
                "Nao foi possivel conectar ao MySQL!\n\n" +
                "Verifique se:\n" +
                "1. O MySQL esta rodando\n" +
                "2. O banco 'controle_financeiro' existe\n" +
                "3. Usuario/senha estao corretos em DatabaseManager.java",
                "Erro de Conexao", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void configurarLayout() {
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        cardPanel.add(criarPainelMenu(), "Menu");
        cardPanel.add(criarPainelRegraTresDias(), "Regra3Dias");
        cardPanel.add(criarPainelPrimeiroEu(), "PrimeiroEu");
        cardPanel.add(criarPainelChecklist(), "Checklist");
        cardPanel.add(criarPainelDashboard(), "Dashboard");

        add(cardPanel, BorderLayout.CENTER);
        add(criarBarraNavegacao(), BorderLayout.SOUTH);
        
        cardLayout.show(cardPanel, "Menu");
    }

    // ===== 2. METODOS DA BARRA DE NAVEGACAO =====
    
    private JPanel criarBarraNavegacao() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panel.setBackground(NAV_DARK);

        String[] botoes = { "Menu", "3 Dias", "Primeiro Eu", "Checklist", "Dashboard" };
        String[] telas = { "Menu", "Regra3Dias", "PrimeiroEu", "Checklist", "Dashboard" };

        for (int i = 0; i < botoes.length; i++) {
            final String tela = telas[i];
            JButton btn = criarBotaoArredondado(botoes[i], PANEL_DARK, Color.WHITE);
            btn.addActionListener(e -> navegarPara(tela));
            panel.add(btn);
        }
        return panel;
    }

    private void navegarPara(String tela) {
        if (tela.equals("Regra3Dias")) {
            atualizarTabelaDesejos();
        }
        if (tela.equals("Dashboard")) {
            mostrarDashboard();
        }
        cardLayout.show(cardPanel, tela);
    }

    // ===== 3. METODOS DO PAINEL MENU =====
    
    private JPanel criarPainelMenu() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        panel.add(criarTituloMenu(), gbc);
        panel.add(criarSubtituloMenu(), gbc);
        panel.add(Box.createVerticalStrut(30), gbc);
        panel.add(criarCardsMenu(), gbc);
        
        return panel;
    }

    private JLabel criarTituloMenu() {
        JLabel titulo = new JLabel("Sistema de controle financeiro", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 22));
        titulo.setForeground(TEXT_PRIMARY);
        return titulo;
    }

    private JLabel criarSubtituloMenu() {
        JLabel subtitulo = new JLabel("Arquitetura POO no padrao MVC - Banco de dados MySQL", SwingConstants.CENTER);
        subtitulo.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitulo.setForeground(TEXT_SECONDARY);
        return subtitulo;
    }

    private JPanel criarCardsMenu() {
        JPanel cardsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        cardsPanel.setOpaque(false);
        cardsPanel.add(criarCard("Regra dos 3 dias",
                "Evite comprar por impulso!\nTodo desejo fica bloqueado por tres dias antes da liberacao",
                ACCENT_LIGHT));
        cardsPanel.add(criarCard("Regra do Primeiro Eu",
                "Poupe 10% da sua renda antes de qualquer gasto.\nInvista no seu futuro primeiro!",
                ACCENT_LIGHT));
        cardsPanel.add(criarCard("Checklist Diario",
                "Registre seus gastos diariamente.\nMantenha o controle financeiro em dia!", 
                ACCENT_LIGHT));
        cardsPanel.add(criarCard("Dashboard",
                "Visualize metricas e indicadores.\nAcompanhe seu progresso financeiro!",
                ACCENT_LIGHT));
        return cardsPanel;
    }

    private JPanel criarCard(String titulo, String descricao, Color cor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(PANEL_DARK);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(cor, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitulo.setForeground(cor);

        JTextArea txtDesc = new JTextArea(descricao);
        txtDesc.setFont(new Font("Arial", Font.PLAIN, 14));
        txtDesc.setForeground(new Color(236, 239, 241));
        txtDesc.setEditable(false);
        txtDesc.setBackground(PANEL_DARK);
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);

        card.add(lblTitulo, BorderLayout.NORTH);
        card.add(txtDesc, BorderLayout.CENTER);
        return card;
    }

    // ===== 4. METODOS DO PAINEL REGRA DOS 3 DIAS =====
    
    private JPanel criarPainelRegraTresDias() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        panel.add(criarTituloRegraTresDias(), BorderLayout.NORTH);
        panel.add(criarFormularioRegraTresDias(), BorderLayout.CENTER);
        panel.add(criarBotaoLimparRegraTresDias(), BorderLayout.SOUTH);
        
        return panel;
    }

    private JLabel criarTituloRegraTresDias() {
        JLabel titulo = new JLabel("Regra dos 3 Dias - Sistema Anti-Impulso", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        titulo.setForeground(MINT);
        return titulo;
    }

    private JPanel criarFormularioRegraTresDias() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(PANEL_DARK);
        formPanel.setBorder(BorderFactory.createTitledBorder("Novo Desejo de Compra"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JTextField txtItem = new JTextField(20);
        JTextField txtValor = new JTextField(10);
        
        // Estiliza os text fields
        txtItem.setBackground(PANEL_DARK);
        txtItem.setForeground(TEXT_PRIMARY);
        txtItem.setCaretColor(TEXT_PRIMARY);
        txtItem.setBorder(BorderFactory.createLineBorder(BORDER));
        
        txtValor.setBackground(PANEL_DARK);
        txtValor.setForeground(TEXT_PRIMARY);
        txtValor.setCaretColor(TEXT_PRIMARY);
        txtValor.setBorder(BorderFactory.createLineBorder(BORDER));
        
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
        gbc.gridwidth = 2;
        JButton btnAdicionar = criarBotaoAdicionarDesejo(txtItem, txtValor);
        formPanel.add(btnAdicionar, gbc);
        
        String[] colunas = {"Item", "Valor", "Data Registro", "Status", "Dias Restantes"};
        tableModelDesejos = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(tableModelDesejos);
        table.setRowHeight(25);
        table.setBackground(PANEL_DARK);
        table.setForeground(TEXT_PRIMARY);
        table.setGridColor(BORDER);
        table.setSelectionBackground(MINT);
        table.setSelectionForeground(Color.BLACK);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Lista de Desejos"));
        scrollPane.getViewport().setBackground(PANEL_DARK);
        
        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JButton criarBotaoAdicionarDesejo(JTextField txtItem, JTextField txtValor) {
        JButton btn = criarBotaoArredondado("Bloquear Compra por 3 Dias", MINT, Color.WHITE);
        
        btn.addActionListener(e -> {
            try {
                String nome = txtItem.getText().trim();
                double valor = Double.parseDouble(txtValor.getText().trim());
                regraTresDiasService.adicionarDesejo(nome, valor);
                txtItem.setText("");
                txtValor.setText("");
                atualizarTabelaDesejos();
                JOptionPane.showMessageDialog(this, 
                    "Item bloqueado por 3 dias!\nVoce so podera comprar apos o periodo de reflexao.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Insira um valor numerico valido!");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao salvar: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        
        return btn;
    }

    private JPanel criarBotaoLimparRegraTresDias() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(BG_DARK);
        
        JButton btnLimpar = criarBotaoArredondado("Remover Desejos Liberados", DANGER, Color.WHITE);
        btnLimpar.addActionListener(e -> {
            try {
                regraTresDiasService.limparLiberados();
                atualizarTabelaDesejos();
                JOptionPane.showMessageDialog(this, "Desejos liberados removidos com sucesso!");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao remover: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        
        panel.add(btnLimpar);
        return panel;
    }

    private void atualizarTabelaDesejos() {
        if (tableModelDesejos == null) return;
        
        tableModelDesejos.setRowCount(0);
        try {
            var desejos = regraTresDiasService.listarDesejos();
            LocalDate hoje = LocalDate.now();
            
            for (DesejoCompra d : desejos) {
                long diasRestantes = d.getDiasRestantes();
                String status = diasRestantes > 0 ? "BLOQUEADO" : "LIBERADO";
                String diasTexto = diasRestantes > 0 ? diasRestantes + " dias" : "Liberado";
                
                tableModelDesejos.addRow(new Object[]{
                    d.getNomeItem(),
                    String.format("R$ %.2f", d.getValor()),
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

    // ===== 5. METODOS DO PAINEL PRIMEIRO EU =====
    
    private JPanel criarPainelPrimeiroEu() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        panel.add(criarTituloPrimeiroEu(), BorderLayout.NORTH);
        panel.add(criarFormularioPrimeiroEu(), BorderLayout.CENTER);
        
        return panel;
    }

    private JLabel criarTituloPrimeiroEu() {
        JLabel titulo = new JLabel("Regra do Primeiro Eu - Poupe 10% da Renda", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        titulo.setForeground(MINT);
        return titulo;
    }

    private JPanel criarFormularioPrimeiroEu() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        inputPanel.setBackground(PANEL_DARK);
        inputPanel.setBorder(BorderFactory.createTitledBorder("Registrar Renda"));
        
        JTextField txtRenda = new JTextField(12);
        txtRenda.setFont(new Font("Arial", Font.PLAIN, 14));
        txtRenda.setBackground(PANEL_DARK);
        txtRenda.setForeground(TEXT_PRIMARY);
        txtRenda.setCaretColor(TEXT_PRIMARY);
        txtRenda.setBorder(BorderFactory.createLineBorder(BORDER));
        
        JButton btnCalcular = criarBotaoArredondado("Processar Renda", MINT, Color.WHITE);
        
        JLabel lblRenda = new JLabel("Renda bruta: R$ ");
        lblRenda.setForeground(TEXT_PRIMARY);
        
        inputPanel.add(lblRenda);
        inputPanel.add(txtRenda);
        inputPanel.add(btnCalcular);
        
        JTextArea txtResultado = new JTextArea(10, 40);
        txtResultado.setEditable(false);
        txtResultado.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtResultado.setBackground(PANEL_DARK);
        txtResultado.setForeground(TEXT_PRIMARY);
        txtResultado.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(MINT),
            "Plano de Alocacao"
        ));
        
        JScrollPane scrollResultado = new JScrollPane(txtResultado);
        scrollResultado.getViewport().setBackground(PANEL_DARK);
        
        btnCalcular.addActionListener(e -> {
            try {
                double renda = Double.parseDouble(txtRenda.getText());
                var resultado = primeiroEuService.processarRenda(renda);
                txtResultado.setText(resultado.formatarResultado());
                txtRenda.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Insira um valor numerico valido!");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao salvar: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        
        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(scrollResultado, BorderLayout.CENTER);
        
        return panel;
    }

    // ===== 6. METODOS DO PAINEL CHECKLIST =====
    
    private JPanel criarPainelChecklist() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        panel.add(criarTituloChecklist(), BorderLayout.NORTH);
        panel.add(criarConteudoChecklist(), BorderLayout.CENTER);
        
        return panel;
    }

    private JLabel criarTituloChecklist() {
        JLabel titulo = new JLabel("Checklist Diario de Gastos", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        titulo.setForeground(MINT);
        return titulo;
    }

    private JPanel criarConteudoChecklist() {
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(PANEL_DARK);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JLabel pergunta = new JLabel("Voce registou minuciosamente todos os seus gastos hoje?");
        pergunta.setFont(new Font("Arial", Font.BOLD, 14));
        pergunta.setForeground(TEXT_PRIMARY);
        centerPanel.add(pergunta, gbc);
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setBackground(PANEL_DARK);
        
        JButton btnSim = criarBotaoArredondado("SIM, registrei meus gastos", SUCCESS, Color.WHITE);
        JButton btnNao = criarBotaoArredondado("NAO, ainda nao registrei", DANGER, Color.WHITE);
        
        btnSim.addActionListener(e -> registrarChecklist(true));
        btnNao.addActionListener(e -> registrarChecklist(false));
        
        btnPanel.add(btnSim);
        btnPanel.add(btnNao);
        centerPanel.add(btnPanel, gbc);
        
        JLabel lblStatus = criarLabelStatusChecklist();
        centerPanel.add(lblStatus, gbc);
        
        return centerPanel;
    }

    private JLabel criarLabelStatusChecklist() {
        JLabel lblStatus = new JLabel();
        lblStatus.setFont(new Font("Arial", Font.PLAIN, 12));
        
        try {
            if (checklistService.jaRegistrouHoje()) {
                var hoje = checklistService.getChecklistHoje();
                String status = hoje.isAnotouGastos() ? 
                    "Hoje voce ja registrou seus gastos. Parabens!" : 
                    "Hoje voce marcou que nao registrou gastos. Que tal registrar agora?";
                lblStatus.setText(status);
                lblStatus.setForeground(hoje.isAnotouGastos() ? SUCCESS : WARNING);
            } else {
                lblStatus.setText("Voce ainda nao registrou nada hoje. Responda acima!");
                lblStatus.setForeground(TEXT_SECONDARY);
            }
        } catch (SQLException e) {
            lblStatus.setText("Erro ao verificar status: " + e.getMessage());
            lblStatus.setForeground(DANGER);
            e.printStackTrace();
        }
        
        return lblStatus;
    }

    private void registrarChecklist(boolean anotou) {
        try {
            System.out.println("Registrando checklist: anotou = " + anotou);
            checklistService.registrarChecklist(anotou);
            String mensagem = anotou ? 
                "Excelente! Continue mantendo o controle dos seus gastos!" :
                "Registro salvo. Nao se esqueca de registrar seus gastos amanha!";
            JOptionPane.showMessageDialog(this, mensagem);
            
            // Recarrega o painel para atualizar o status
            recarregarPainelChecklist();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao registrar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void recarregarPainelChecklist() {
        // Remove o painel antigo e adiciona um novo
        Component[] components = cardPanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            Component comp = components[i];
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                // Verifica se é o painel do checklist pela borda vazada
                if (panel.getBorder() instanceof EmptyBorder) {
                    JPanel novoPainel = criarPainelChecklist();
                    cardPanel.remove(panel);
                    cardPanel.add(novoPainel, "Checklist", i);
                    cardPanel.revalidate();
                    cardPanel.repaint();
                    break;
                }
            }
        }
    }

    // ===== 7. METODOS DO PAINEL DASHBOARD =====
    
    private JPanel criarPainelDashboard() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel titulo = new JLabel("Dashboard - Metricas e Indicadores", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        titulo.setForeground(ACCENT_LIGHT);
        panel.add(titulo, BorderLayout.NORTH);
        
        JTextArea dashboardArea = new JTextArea();
        dashboardArea.setEditable(false);
        dashboardArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        dashboardArea.setBackground(PANEL_DARK);
        dashboardArea.setForeground(TEXT_PRIMARY);
        
        JScrollPane scrollPane = new JScrollPane(dashboardArea);
        scrollPane.getViewport().setBackground(PANEL_DARK);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JButton btnAtualizar = criarBotaoArredondado("Atualizar Dashboard", ACCENT, Color.WHITE);
        btnAtualizar.addActionListener(e -> mostrarDashboard(dashboardArea));
        
        panel.add(btnAtualizar, BorderLayout.SOUTH);
        
        mostrarDashboard(dashboardArea);
        
        return panel;
    }

    private void mostrarDashboard() {
        JTextArea tempArea = new JTextArea();
        tempArea.setBackground(PANEL_DARK);
        tempArea.setForeground(TEXT_PRIMARY);
        mostrarDashboard(tempArea);
        
        JScrollPane scrollPane = new JScrollPane(tempArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        scrollPane.getViewport().setBackground(PANEL_DARK);
        
        JOptionPane.showMessageDialog(this, scrollPane, "Dashboard Financeiro", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarDashboard(JTextArea dashboardArea) {
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(60)).append("\n");
        sb.append("              DASHBOARD FINANCEIRO\n");
        sb.append("=".repeat(60)).append("\n\n");
        
        adicionarInfoDesejosDashboard(sb);
        adicionarInfoRendasDashboard(sb);
        adicionarInfoChecklistDashboard(sb);
        
        sb.append("\n\n=".repeat(60)).append("\n");
        sb.append("     Continue assim! Controle financeiro e habito!\n");
        sb.append("=".repeat(60));
        
        dashboardArea.setText(sb.toString());
    }

    private void adicionarInfoDesejosDashboard(StringBuilder sb) {
        sb.append("REGRA DOS 3 DIAS\n");
        sb.append("-".repeat(40)).append("\n");
        
        try {
            var desejos = regraTresDiasService.listarDesejos();
            long bloqueados = desejos.stream().filter(d -> d.getDiasRestantes() > 0).count();
            long liberados = desejos.stream().filter(d -> d.getDiasRestantes() == 0).count();
            double totalBloqueado = desejos.stream()
                .filter(d -> d.getDiasRestantes() > 0)
                .mapToDouble(DesejoCompra::getValor)
                .sum();
            
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

    private void adicionarInfoRendasDashboard(StringBuilder sb) {
        sb.append("REGRA DO PRIMEIRO EU\n");
        sb.append("-".repeat(40)).append("\n");
        
        try {
            HistoricoRendaDAO rendaDAO = new HistoricoRendaDAO();
            double totalRendaMes = rendaDAO.getTotalRendaMesAtual();
            double totalPoupadoMes = totalRendaMes * 0.10;
            
            sb.append(String.format("Renda total do mes:   R$ %,10.2f\n", totalRendaMes));
            sb.append(String.format("Total poupado (10%%): R$ %,10.2f\n", totalPoupadoMes));
            sb.append(String.format("Meta anual:           R$ %,10.2f\n", totalPoupadoMes * 12));
        } catch (SQLException e) {
            sb.append("Erro ao carregar dados das rendas: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
        sb.append("\n");
    }

    private void adicionarInfoChecklistDashboard(StringBuilder sb) {
        sb.append("CHECKLIST DIARIO\n");
        sb.append("-".repeat(40)).append("\n");
        
        try {
            double taxaSucesso = checklistService.getTaxaSucesso();
            sb.append(String.format("Taxa de sucesso:      %.1f%%\n", taxaSucesso));
            
            if (checklistService.jaRegistrouHoje()) {
                var hoje = checklistService.getChecklistHoje();
                sb.append(String.format("Status hoje:          %s\n", 
                    hoje.isAnotouGastos() ? "Registrado" : "Pendente"));
            } else {
                sb.append("Status hoje:          Nao registrado\n");
            }
            
            String recomendacao = taxaSucesso >= 80 ? 
                "Excelente! Mantenha o foco!" :
                (taxaSucesso >= 50 ? "Bom progresso! Continue melhorando!" : 
                "Voce precisa registrar seus gastos com mais frequencia!");
            sb.append("\n").append(recomendacao);
        } catch (SQLException e) {
            sb.append("Erro ao carregar dados do checklist: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    // ===== 8. METODO AUXILIAR PARA BOTOES ARREDONDADOS =====
    
    private JButton criarBotaoArredondado(String texto, Color bgColor, Color fgColor) {
        JButton botao = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bgColor.brighter());
                } else {
                    g2.setColor(bgColor);
                }
                
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
                
                super.paintComponent(g);
            }
        };
        
        botao.setForeground(fgColor);
        botao.setBackground(bgColor);
        botao.setFocusPainted(false);
        botao.setBorderPainted(false);
        botao.setContentAreaFilled(false);
        botao.setOpaque(false);
        botao.setFont(new Font("Arial", Font.BOLD, 12));
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return botao;
    }

    // ===== 9. METODO MAIN =====
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new MainDesktopApp().setVisible(true);
        });
    }
}