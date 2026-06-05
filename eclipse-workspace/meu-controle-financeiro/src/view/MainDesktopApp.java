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
import java.time.format.DateTimeFormatter;

/**
 * Classe principal da aplicação de Controle Financeiro Pessoal.
 * Responsável pela interface gráfica e navegação entre os módulos.
 * 
 * Arquitetura: MVC com padrão DAO e Services.
 * Tema visual: Linux Mint MATE (cores escuras com destaque verde).
 */
public class MainDesktopApp extends JFrame {

    // ============================================================
    // 1. CONSTANTES DE COR (TEMA LINUX MINT MATE)
    // ============================================================
    
    private static final Color BG_DARK = new Color(47, 52, 55);
    private static final Color PANEL_DARK = new Color(60, 65, 68);
    private static final Color NAV_DARK = new Color(42, 46, 50);
    private static final Color BORDER = new Color(95, 100, 103);
    private static final Color TEXT_PRIMARY = new Color(238, 238, 236);
    private static final Color TEXT_SECONDARY = new Color(186, 189, 182);
    private static final Color MINT = new Color(134, 179, 0);
    private static final Color SUCCESS = new Color(134, 179, 0);
    private static final Color WARNING = new Color(245, 166, 35);
    private static final Color DANGER = new Color(204, 76, 76);
    private static final Color ACCENT = new Color(102, 187, 106);

    // ============================================================
    // 2. COMPONENTES PRINCIPAIS
    // ============================================================
    
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private DefaultTableModel tableModelDesejos;

    // ============================================================
    // 3. SERVICES
    // ============================================================
    
    private final RegraTresDiasService regraTresDiasService;
    private final PrimeiroEuService primeiroEuService;
    private final ChecklistService checklistService;

    // ============================================================
    // 4. CONSTRUTOR
    // ============================================================
    
    public MainDesktopApp() {
        this.regraTresDiasService = new RegraTresDiasService();
        this.primeiroEuService = new PrimeiroEuService();
        this.checklistService = new ChecklistService();

        setTitle("Controle Financeiro Pessoal");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        getContentPane().setBackground(BG_DARK);
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        
        configurarLookAndFeel();
        verificarConexaoBanco();
        configurarLayout();
    }

    // ============================================================
    // 5. CONFIGURAÇÃO GLOBAL
    // ============================================================
    
    private void verificarConexaoBanco() {
        try {
            if (!DatabaseManager.testConnection()) {
                mostrarAviso("Banco de dados nao disponivel. Algumas funcionalidades podem nao funcionar.");
            }
        } catch (Exception e) {
            mostrarAviso("Erro ao conectar ao banco de dados: " + e.getMessage());
        }
    }

    private void configurarLookAndFeel() {
        UIManager.put("Table.gridColor", BORDER);
        UIManager.put("TableHeader.background", NAV_DARK);
        UIManager.put("TableHeader.foreground", TEXT_PRIMARY);
        UIManager.put("TextField.background", PANEL_DARK);
        UIManager.put("TextField.foreground", TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground", TEXT_PRIMARY);
        UIManager.put("TextArea.background", PANEL_DARK);
        UIManager.put("TextArea.foreground", TEXT_PRIMARY);
        UIManager.put("Panel.background", BG_DARK);
        UIManager.put("ScrollPane.background", BG_DARK);
        UIManager.put("Viewport.background", BG_DARK);
        UIManager.put("TitledBorder.titleColor", MINT);
        UIManager.put("TitledBorder.border", BorderFactory.createLineBorder(BORDER));
    }

    private void configurarLayout() {
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(BG_DARK);

        cardPanel.add(criarPainelMenu(), "Menu");
        cardPanel.add(criarPainelRegraTresDias(), "Regra3Dias");
        cardPanel.add(criarPainelPrimeiroEu(), "PrimeiroEu");
        cardPanel.add(criarPainelChecklist(), "Checklist");
        cardPanel.add(criarPainelDashboard(), "Dashboard");

        add(cardPanel, BorderLayout.CENTER);
        add(criarBarraNavegacao(), BorderLayout.SOUTH);
        cardLayout.show(cardPanel, "Menu");
    }

    // ============================================================
    // 6. BARRA DE NAVEGAÇÃO
    // ============================================================
    
    private JPanel criarBarraNavegacao() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        panel.setBackground(NAV_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        String[] botoes = { "Menu", "3 Dias", "Primeiro Eu", "Checklist", "Dashboard" };
        String[] telas = { "Menu", "Regra3Dias", "PrimeiroEu", "Checklist", "Dashboard" };

        for (int i = 0; i < botoes.length; i++) {
            final String tela = telas[i];
            JButton btn = criarBotaoRedondo(botoes[i], PANEL_DARK, PANEL_DARK.brighter());
            btn.setForeground(TEXT_PRIMARY);
            btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            btn.addActionListener(e -> navegarPara(tela));
            panel.add(btn);
        }
        return panel;
    }

    private void navegarPara(String tela) {
        try {
            if (tela.equals("Regra3Dias")) atualizarTabelaDesejos();
            if (tela.equals("Dashboard")) mostrarDashboard();
            cardLayout.show(cardPanel, tela);
        } catch (Exception e) {
            mostrarErro("Erro ao navegar para " + tela, e);
        }
    }

    // ============================================================
    // 7. PAINEL MENU
    // ============================================================
    
    private JPanel criarPainelMenu() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        panel.add(criarTituloMenu(), gbc);
        panel.add(criarSubtituloMenu(), gbc);
        panel.add(Box.createVerticalStrut(40), gbc);
        panel.add(criarCardsMenu(), gbc);

        return panel;
    }

    private JLabel criarTituloMenu() {
        JLabel titulo = new JLabel("Controle Financeiro Pessoal", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 24));
        titulo.setForeground(MINT);
        return titulo;
    }

    private JLabel criarSubtituloMenu() {
        JLabel subtitulo = new JLabel("Arquitetura POO no padrao MVC - Banco de dados MySQL", SwingConstants.CENTER);
        subtitulo.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitulo.setForeground(TEXT_SECONDARY);
        return subtitulo;
    }

    private JPanel criarCardsMenu() {
        JPanel cardsPanel = new JPanel(new GridLayout(2, 2, 25, 25));
        cardsPanel.setOpaque(false);
        cardsPanel.add(criarCard("Regra dos 3 Dias",
                "Evite compras por impulso!\nTodo desejo fica bloqueado por tres dias\nantes de ser liberado para compra.", MINT));
        cardsPanel.add(criarCard("Regra do Primeiro Eu",
                "Poupe 10% da sua renda antes de qualquer gasto.\nInvista no seu futuro primeiro!", SUCCESS));
        cardsPanel.add(criarCard("Checklist Diario",
                "Registre seus gastos diariamente.\nMantenha o controle financeiro em dia!", ACCENT));
        cardsPanel.add(criarCard("Dashboard",
                "Visualize metricas e indicadores.\nAcompanhe seu progresso financeiro!", WARNING));
        return cardsPanel;
    }

    private JPanel criarCard(String titulo, String descricao, Color cor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(PANEL_DARK);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(cor, 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setForeground(cor);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);

        JTextArea txtDesc = new JTextArea(descricao);
        txtDesc.setFont(new Font("Arial", Font.PLAIN, 13));
        txtDesc.setForeground(TEXT_PRIMARY);
        txtDesc.setEditable(false);
        txtDesc.setBackground(PANEL_DARK);
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);

        card.add(lblTitulo, BorderLayout.NORTH);
        card.add(txtDesc, BorderLayout.CENTER);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(cor.brighter(), 2),
                    BorderFactory.createEmptyBorder(20, 20, 20, 20)));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(cor, 2),
                    BorderFactory.createEmptyBorder(20, 20, 20, 20)));
            }
        });

        return card;
    }

    // ============================================================
    // 8. PAINEL REGRA DOS 3 DIAS
    // ============================================================
    
    private JPanel criarPainelRegraTresDias() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(criarTituloRegraTresDias(), BorderLayout.NORTH);
        panel.add(criarFormularioRegraTresDias(), BorderLayout.CENTER);
        return panel;
    }

    private JLabel criarTituloRegraTresDias() {
        JLabel titulo = new JLabel("Regra dos 3 Dias - Sistema Anti-Impulso", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        titulo.setForeground(MINT);
        return titulo;
    }

    private JPanel criarFormularioRegraTresDias() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(PANEL_DARK);
        
        TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(MINT), "Novo Desejo de Compra");
        border.setTitleColor(MINT);
        formPanel.setBorder(border);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        JTextField txtItem = criarTextField(20);
        JTextField txtValor = criarTextField(10);

        JLabel lblItem = new JLabel("Item desejado:");
        lblItem.setForeground(TEXT_PRIMARY);
        JLabel lblValor = new JLabel("Valor (R$):");
        lblValor.setForeground(TEXT_PRIMARY);

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(lblItem, gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(txtItem, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(lblValor, gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(txtValor, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(criarBotaoAdicionarDesejo(txtItem, txtValor), gbc);

        String[] colunas = { "Item", "Valor", "Data Registro", "Status", "Dias Restantes" };
        tableModelDesejos = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable table = new JTable(tableModelDesejos);
        table.setRowHeight(28);
        table.setBackground(PANEL_DARK);
        table.setForeground(TEXT_PRIMARY);
        table.setGridColor(BORDER);
        table.setSelectionBackground(MINT);
        table.setSelectionForeground(Color.BLACK);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        
        table.getTableHeader().setBackground(NAV_DARK);
        table.getTableHeader().setForeground(TEXT_PRIMARY);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBorder(BorderFactory.createLineBorder(NAV_DARK));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(MINT), "Lista de Desejos Bloqueados"));
        scrollPane.getViewport().setBackground(PANEL_DARK);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(BG_DARK);
        JButton btnLimpar = criarBotaoRedondo("Remover Desejos Liberados", DANGER, DANGER.brighter());
        btnLimpar.setForeground(Color.WHITE);
        btnLimpar.addActionListener(e -> limparDesejosLiberados());
        buttonPanel.add(btnLimpar);
        
        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JTextField criarTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setBackground(PANEL_DARK);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setSelectedTextColor(Color.BLACK);
        field.setSelectionColor(MINT);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        return field;
    }

    private JButton criarBotaoAdicionarDesejo(JTextField txtItem, JTextField txtValor) {
        JButton btn = criarBotaoRedondo("Bloquear Compra por 3 Dias", MINT, MINT.brighter());
        btn.setForeground(Color.WHITE);
        btn.addActionListener(e -> {
            try {
                String nome = txtItem.getText().trim();
                String valorStr = txtValor.getText().trim();

                if (nome.isEmpty()) {
                    mostrarAviso("Digite o nome do item desejado.");
                    txtItem.requestFocus();
                    return;
                }
                if (valorStr.isEmpty()) {
                    mostrarAviso("Digite o valor do item.");
                    txtValor.requestFocus();
                    return;
                }

                double valor = Double.parseDouble(valorStr);
                if (valor <= 0) {
                    mostrarAviso("O valor deve ser maior que zero.");
                    return;
                }

                regraTresDiasService.adicionarDesejo(nome, valor);
                txtItem.setText("");
                txtValor.setText("");
                atualizarTabelaDesejos();
                mostrarSucesso("Item bloqueado por 3 dias!");

            } catch (NumberFormatException ex) {
                mostrarErro("Valor invalido! Use ponto como separador decimal. Ex: 99.90", ex);
            } catch (IllegalArgumentException ex) {
                mostrarAviso(ex.getMessage());
            } catch (SQLException ex) {
                mostrarErro("Erro ao salvar: " + ex.getMessage(), ex);
            }
        });
        return btn;
    }

    private void limparDesejosLiberados() {
        try {
            var desejos = regraTresDiasService.listarDesejos();
            long liberadosCount = desejos.stream().filter(d -> d.getDiasRestantes() == 0).count();

            if (liberadosCount == 0) {
                mostrarAviso("Nao ha desejos liberados para remover.\nOs desejos so sao liberados apos 3 dias.");
                return;
            }

            String msg = (liberadosCount == 1) ? "Existe 1 desejo liberado. Deseja remove-lo?" 
                : "Existem " + liberadosCount + " desejos liberados. Deseja remove-los?";

            if (mostrarConfirmacaoEscura(msg, "Confirmar Remocao")) {
                regraTresDiasService.limparLiberados();
                atualizarTabelaDesejos();
                String msgSucesso = (liberadosCount == 1) ? "1 desejo liberado removido!" 
                    : liberadosCount + " desejos liberados removidos!";
                mostrarSucesso(msgSucesso);
            }
        } catch (SQLException ex) {
            mostrarErro("Erro ao remover desejos: " + ex.getMessage(), ex);
        }
    }

    private void atualizarTabelaDesejos() {
        if (tableModelDesejos == null) return;
        tableModelDesejos.setRowCount(0);
        
        try {
            var desejos = regraTresDiasService.listarDesejos();
            if (desejos.isEmpty()) {
                tableModelDesejos.addRow(new Object[] { "Nenhum item cadastrado", "", "", "", "" });
                return;
            }

            for (DesejoCompra d : desejos) {
                long dias = d.getDiasRestantes();
                tableModelDesejos.addRow(new Object[] {
                    d.getNomeItem(),
                    String.format("R$ %.2f", d.getValor()),
                    d.getDataCriacao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    dias > 0 ? "BLOQUEADO" : "LIBERADO",
                    dias > 0 ? dias + " dias" : "Liberado"
                });
            }
        } catch (SQLException e) {
            mostrarErro("Erro ao carregar desejos", e);
        }
    }

    // ============================================================
    // 9. PAINEL PRIMEIRO EU
    // ============================================================
    
    private JPanel criarPainelPrimeiroEu() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        panel.add(criarTituloPrimeiroEu(), BorderLayout.NORTH);
        panel.add(criarFormularioPrimeiroEu(), BorderLayout.CENTER);
        return panel;
    }

    private JLabel criarTituloPrimeiroEu() {
        JLabel titulo = new JLabel("Regra do Primeiro Eu - Poupe 10% da Renda", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        titulo.setForeground(SUCCESS);
        return titulo;
    }

    private JPanel criarFormularioPrimeiroEu() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_DARK);

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
        inputPanel.setBackground(PANEL_DARK);
        
        TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(SUCCESS), "Registrar Nova Renda");
        border.setTitleColor(SUCCESS);
        inputPanel.setBorder(border);

        JLabel lblRenda = new JLabel("Renda bruta: R$ ");
        lblRenda.setForeground(TEXT_PRIMARY);
        lblRenda.setFont(new Font("Arial", Font.BOLD, 14));

        JTextField txtRenda = criarTextField(12);
        txtRenda.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton btnCalcular = criarBotaoRedondo("Processar Renda", SUCCESS, SUCCESS.brighter());
        btnCalcular.setForeground(Color.WHITE);

        inputPanel.add(lblRenda);
        inputPanel.add(txtRenda);
        inputPanel.add(btnCalcular);

        JTextArea txtResultado = new JTextArea(12, 45);
        txtResultado.setEditable(false);
        txtResultado.setFont(new Font("Monospaced", Font.PLAIN, 13));
        txtResultado.setBackground(PANEL_DARK);
        txtResultado.setForeground(TEXT_PRIMARY);
        
        TitledBorder resultBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(SUCCESS), "Plano de Alocacao");
        resultBorder.setTitleColor(SUCCESS);
        txtResultado.setBorder(resultBorder);

        JScrollPane scrollResultado = new JScrollPane(txtResultado);
        scrollResultado.getViewport().setBackground(PANEL_DARK);
        
        btnCalcular.addActionListener(e -> {
            try {
                String rendaStr = txtRenda.getText().trim();
                if (rendaStr.isEmpty()) {
                    mostrarAviso("Digite o valor da renda.");
                    txtRenda.requestFocus();
                    return;
                }
                double renda = Double.parseDouble(rendaStr);
                if (renda <= 0) {
                    mostrarAviso("Valor da renda deve ser maior que zero.");
                    return;
                }
                var resultado = primeiroEuService.processarRenda(renda);
                txtResultado.setText(resultado.formatarResultado());
                txtRenda.setText("");
                mostrarSucesso("Renda registrada!");
            } catch (NumberFormatException ex) {
                mostrarErro("Valor invalido! Use ponto como separador decimal. Ex: 2500.00", ex);
            } catch (IllegalArgumentException | SQLException ex) {
                mostrarErro(ex.getMessage(), ex);
            }
        });

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(scrollResultado, BorderLayout.CENTER);
        return panel;
    }

    // ============================================================
    // 10. PAINEL CHECKLIST
    // ============================================================
    
    private JPanel criarPainelChecklist() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
        panel.add(criarTituloChecklist(), BorderLayout.NORTH);
        panel.add(criarConteudoChecklist(), BorderLayout.CENTER);
        return panel;
    }

    private JLabel criarTituloChecklist() {
        JLabel titulo = new JLabel("Checklist Diario de Gastos", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        titulo.setForeground(ACCENT);
        return titulo;
    }

    private JPanel criarConteudoChecklist() {
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(PANEL_DARK);
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT, 1),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 15, 15, 15);

        JLabel pergunta = new JLabel("Voce registou todos os seus gastos hoje?");
        pergunta.setFont(new Font("Arial", Font.BOLD, 16));
        pergunta.setForeground(TEXT_PRIMARY);
        centerPanel.add(pergunta, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 10));
        btnPanel.setBackground(PANEL_DARK);

        JButton btnSim = criarBotaoRedondo("SIM, registrei meus gastos", SUCCESS, SUCCESS.brighter());
        btnSim.setForeground(Color.WHITE);
        JButton btnNao = criarBotaoRedondo("NAO, ainda nao registrei", DANGER, DANGER.brighter());
        btnNao.setForeground(Color.WHITE);

        btnSim.addActionListener(e -> registrarChecklist(true));
        btnNao.addActionListener(e -> registrarChecklist(false));

        btnPanel.add(btnSim);
        btnPanel.add(btnNao);
        centerPanel.add(btnPanel, gbc);

        centerPanel.add(criarLabelStatusChecklist(), gbc);
        return centerPanel;
    }

    private JLabel criarLabelStatusChecklist() {
        JLabel lblStatus = new JLabel();
        lblStatus.setFont(new Font("Arial", Font.PLAIN, 13));
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        atualizarLabelStatus(lblStatus);
        return lblStatus;
    }

    private void atualizarLabelStatus(JLabel lblStatus) {
        try {
            if (checklistService.jaRegistrouHoje()) {
                var hoje = checklistService.getChecklistHoje();
                lblStatus.setText(hoje.isAnotouGastos() ? "Hoje voce ja registrou seus gastos. Parabens!" 
                    : "Hoje voce marcou que nao registrou gastos. Que tal registrar agora?");
                lblStatus.setForeground(hoje.isAnotouGastos() ? SUCCESS : WARNING);
            } else {
                lblStatus.setText("Voce ainda nao registrou nada hoje. Responda acima!");
                lblStatus.setForeground(TEXT_SECONDARY);
            }
        } catch (SQLException e) {
            lblStatus.setText("Erro ao verificar status");
            lblStatus.setForeground(DANGER);
        }
    }

    private void registrarChecklist(boolean anotou) {
        try {
            checklistService.registrarChecklist(anotou);
            mostrarSucesso(anotou ? "Excelente! Continue assim!" : "Registro salvo. Nao esqueca amanha!");
            
            for (Component comp : cardPanel.getComponents()) {
                if (comp instanceof JPanel && ((JPanel) comp).getBorder() instanceof EmptyBorder) {
                    atualizarLabelStatusNaTela(comp);
                    break;
                }
            }
        } catch (SQLException e) {
            mostrarErro("Erro ao registrar: " + e.getMessage(), e);
        }
    }

    private void atualizarLabelStatusNaTela(Component comp) {
        if (comp instanceof JPanel) {
            for (Component c : ((JPanel) comp).getComponents()) {
                atualizarLabelStatusNaTela(c);
            }
        } else if (comp instanceof JLabel) {
            String texto = ((JLabel) comp).getText();
            if (texto != null && (texto.contains("Hoje") || texto.contains("Voce"))) {
                atualizarLabelStatus((JLabel) comp);
            }
        }
    }

    // ============================================================
    // 11. PAINEL DASHBOARD
    // ============================================================
    
    private JPanel criarPainelDashboard() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titulo = new JLabel("Dashboard - Metricas e Indicadores", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        titulo.setForeground(WARNING);
        panel.add(titulo, BorderLayout.NORTH);

        JTextArea dashboardArea = new JTextArea();
        dashboardArea.setEditable(false);
        dashboardArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        dashboardArea.setBackground(PANEL_DARK);
        dashboardArea.setForeground(TEXT_PRIMARY);
        dashboardArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(dashboardArea);
        scrollPane.getViewport().setBackground(PANEL_DARK);
        scrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(WARNING), "Resumo Financeiro"));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(BG_DARK);
        JButton btnAtualizar = criarBotaoRedondo("Atualizar Dashboard", WARNING, WARNING.brighter());
        btnAtualizar.setForeground(Color.BLACK);
        btnAtualizar.addActionListener(e -> mostrarDashboard(dashboardArea));
        buttonPanel.add(btnAtualizar);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        mostrarDashboard(dashboardArea);
        return panel;
    }

    private void mostrarDashboard() {
        mostrarModalDashboard(gerarConteudoDashboard());
    }

    private void mostrarDashboard(JTextArea area) {
        area.setText(gerarConteudoDashboard());
    }

    private String gerarConteudoDashboard() {
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(60)).append("\n");
        sb.append("              DASHBOARD FINANCEIRO\n");
        sb.append("=".repeat(60)).append("\n\n");

        sb.append("REGRA DOS 3 DIAS\n").append("-".repeat(40)).append("\n");
        try {
            var desejos = regraTresDiasService.listarDesejos();
            long bloqueados = desejos.stream().filter(d -> d.getDiasRestantes() > 0).count();
            long liberados = desejos.stream().filter(d -> d.getDiasRestantes() == 0).count();
            double totalBloqueado = desejos.stream().filter(d -> d.getDiasRestantes() > 0).mapToDouble(DesejoCompra::getValor).sum();
            sb.append(String.format("Desejos bloqueados:   %d\n", bloqueados));
            sb.append(String.format("Desejos liberados:    %d\n", liberados));
            sb.append(String.format("Total bloqueado:      R$ %,10.2f\n", totalBloqueado));
            sb.append(String.format("Economia potencial:   R$ %,10.2f\n\n", totalBloqueado));
        } catch (SQLException e) { sb.append("Erro ao carregar desejos\n\n"); }

        sb.append("REGRA DO PRIMEIRO EU\n").append("-".repeat(40)).append("\n");
        try {
            HistoricoRendaDAO rendaDAO = new HistoricoRendaDAO();
            double totalRendaMes = rendaDAO.getTotalRendaMesAtual();
            double totalPoupadoMes = totalRendaMes * 0.10;
            sb.append(String.format("Renda total do mes:   R$ %,10.2f\n", totalRendaMes));
            sb.append(String.format("Total poupado (10%%): R$ %,10.2f\n", totalPoupadoMes));
            sb.append(String.format("Meta anual:           R$ %,10.2f\n\n", totalPoupadoMes * 12));
        } catch (SQLException e) { sb.append("Erro ao carregar rendas\n\n"); }

        sb.append("CHECKLIST DIARIO\n").append("-".repeat(40)).append("\n");
        try {
            double taxa = checklistService.getTaxaSucesso();
            sb.append(String.format("Taxa de sucesso:      %.1f%%\n", taxa));
            if (checklistService.jaRegistrouHoje()) {
                var hoje = checklistService.getChecklistHoje();
                sb.append("Status hoje:          " + (hoje.isAnotouGastos() ? "Registrado" : "Pendente") + "\n");
            } else { sb.append("Status hoje:          Nao registrado\n"); }
            sb.append("\n").append(taxa >= 80 ? "Excelente! Continue assim!" : (taxa >= 50 ? "Bom progresso!" : "Registre seus gastos mais vezes!"));
        } catch (SQLException e) { sb.append("Erro ao carregar checklist\n"); }

        sb.append("\n\n").append("=".repeat(60)).append("\n");
        sb.append("     Continue assim! Controle financeiro e habito!\n");
        sb.append("=".repeat(60));
        
        return sb.toString();
    }

    private void mostrarModalDashboard(String conteudo) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(PANEL_DARK);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(WARNING, 2),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)));
        
        JTextArea textArea = new JTextArea(conteudo);
        textArea.setEditable(false);
        textArea.setBackground(PANEL_DARK);
        textArea.setForeground(TEXT_PRIMARY);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBackground(PANEL_DARK);
        scrollPane.getViewport().setBackground(PANEL_DARK);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
        scrollPane.setPreferredSize(new Dimension(500, 400));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        btnPanel.setBackground(PANEL_DARK);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        JButton btnOk = criarBotaoRedondo("OK", NAV_DARK, WARNING);
        btnOk.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));
        btnOk.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(btnOk);
            if (window != null) window.dispose();
        });
        
        btnPanel.add(btnOk);
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        JDialog dialog = new JDialog(this, "Dashboard Financeiro", true);
        dialog.setBackground(PANEL_DARK);
        dialog.getContentPane().setBackground(PANEL_DARK);
        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // ============================================================
    // 12. MÉTODO ÚNICO PARA BOTÕES REDONDOS
    // ============================================================
    
    private JButton criarBotaoRedondo(String texto, Color corNormal, Color corHover) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g2.setColor(corHover.darker());
                else if (getModel().isRollover()) g2.setColor(corHover);
                else g2.setColor(corNormal);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(TEXT_PRIMARY);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 25, 8, 25));
        return btn;
    }

    // ============================================================
    // 13. MÉTODOS DE MODAL (UNIFICADOS)
    // ============================================================
    
    private void mostrarErro(String mensagem, Exception e) {
        mostrarModalComIcone(mensagem, "Erro", DANGER, JOptionPane.ERROR_MESSAGE);
        if (e != null) e.printStackTrace();
    }

    private void mostrarSucesso(String mensagem) {
        mostrarModalComIcone(mensagem, "Sucesso", SUCCESS, JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarAviso(String mensagem) {
        mostrarModalComIcone(mensagem, "Atencao", WARNING, JOptionPane.WARNING_MESSAGE);
    }
    
    private void mostrarModalComIcone(String mensagem, String titulo, Color cor, int tipoMensagem) {
        Icon icon = UIManager.getIcon("OptionPane." + getIconName(tipoMensagem) + "Icon");
        
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(PANEL_DARK);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(cor, 2),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)));
        
        if (icon != null) {
            JLabel lblIcon = new JLabel(icon);
            lblIcon.setVerticalAlignment(SwingConstants.TOP);
            panel.add(lblIcon, BorderLayout.WEST);
        }
        
        JLabel lblMensagem = new JLabel("<html><div style='text-align: left; width: 260px;'>" + mensagem + "</div></html>");
        lblMensagem.setForeground(TEXT_PRIMARY);
        lblMensagem.setFont(new Font("Arial", Font.PLAIN, 13));
        lblMensagem.setVerticalAlignment(SwingConstants.CENTER);
        panel.add(lblMensagem, BorderLayout.CENTER);
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        btnPanel.setBackground(PANEL_DARK);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        JButton btnOk = criarBotaoRedondo("OK", NAV_DARK, cor);
        btnOk.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));
        btnOk.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(btnOk);
            if (window != null) window.dispose();
        });
        
        btnPanel.add(btnOk);
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        JDialog dialog = new JDialog(this, titulo, true);
        dialog.setBackground(PANEL_DARK);
        dialog.getContentPane().setBackground(PANEL_DARK);
        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private String getIconName(int tipo) {
        switch (tipo) {
            case JOptionPane.ERROR_MESSAGE: return "Error";
            case JOptionPane.WARNING_MESSAGE: return "Warning";
            case JOptionPane.QUESTION_MESSAGE: return "Question";
            default: return "Info";
        }
    }

    private boolean mostrarConfirmacaoEscura(String mensagem, String titulo) {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(PANEL_DARK);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MINT, 2),
            BorderFactory.createEmptyBorder(25, 35, 25, 35)));
        
        JLabel lbl = new JLabel("<html><div style='text-align: center; width: 280px;'>" + mensagem + "</div></html>");
        lbl.setForeground(TEXT_PRIMARY);
        lbl.setFont(new Font("Arial", Font.PLAIN, 14));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lbl, BorderLayout.CENTER);
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setBackground(PANEL_DARK);
        
        JButton btnSim = criarBotaoRedondo("Sim", NAV_DARK, SUCCESS);
        JButton btnNao = criarBotaoRedondo("Nao", NAV_DARK, DANGER);
        
        btnPanel.add(btnSim);
        btnPanel.add(btnNao);
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        JDialog dialog = new JDialog(this, titulo, true);
        dialog.setBackground(PANEL_DARK);
        dialog.getContentPane().setBackground(PANEL_DARK);
        dialog.getContentPane().add(panel);
        
        final boolean[] escolha = {false};
        btnSim.addActionListener(e -> { escolha[0] = true; dialog.dispose(); });
        btnNao.addActionListener(e -> { escolha[0] = false; dialog.dispose(); });
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        return escolha[0];
    }
    
    // ============================================================
    // 14. MAIN
    // ============================================================
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Erro ao aplicar look and feel");
        }
        SwingUtilities.invokeLater(() -> new MainDesktopApp().setVisible(true));
    }
}