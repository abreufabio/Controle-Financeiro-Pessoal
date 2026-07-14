package view;

import model.CategoriaGasto;
import model.CategoriaRenda;
import model.ChecklistDiario;
import model.DesejoCompra;
import model.GastoDiario;
import model.HistoricoRenda;
import service.ChecklistService;
import service.GastoService;
import service.PrimeiroEuService;
import service.RegraTresDiasService;
import repository.DatabaseManager;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class MainDesktopApp extends JFrame {

    // ============================================================
    // CONSTANTES
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
    // COMPONENTES
    // ============================================================
    
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private DefaultTableModel tableModelDesejos;

    // ============================================================
    // SERVICES
    // ============================================================
    
    private final RegraTresDiasService regraTresDiasService;
    private final PrimeiroEuService primeiroEuService;
    private final ChecklistService checklistService;
    private final GastoService gastoService;

    // ============================================================
    // CONSTRUTOR
    // ============================================================
    
    public MainDesktopApp() {
        this.regraTresDiasService = new RegraTresDiasService();
        this.primeiroEuService = new PrimeiroEuService();
        this.checklistService = new ChecklistService();
        this.gastoService = new GastoService();

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
    // CONFIGURAÇÃO GLOBAL
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
    // BARRA DE NAVEGAÇÃO
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
    // PAINEL MENU
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
        cardsPanel.add(criarCard("Regra dos 3 Dias", "Evite compras por impulso!\nTodo desejo fica bloqueado por tres dias\nantes de ser liberado para compra.", MINT));
        cardsPanel.add(criarCard("Regra do Primeiro Eu", "Poupe 10% da sua renda antes de qualquer gasto.\nInvista no seu futuro primeiro!", SUCCESS));
        cardsPanel.add(criarCard("Checklist Diario", "Registre seus gastos diariamente.\nMantenha o controle financeiro em dia!", ACCENT));
        cardsPanel.add(criarCard("Dashboard", "Visualize metricas e indicadores.\nAcompanhe seu progresso financeiro!", WARNING));
        return cardsPanel;
    }

    private JPanel criarCard(String titulo, String descricao, Color cor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(PANEL_DARK);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(cor, 2), 
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
                card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(cor.brighter(), 2), 
                		BorderFactory.createEmptyBorder(20, 20, 20, 20)));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(cor, 2), 
                		BorderFactory.createEmptyBorder(20, 20, 20, 20)));
            }
        });

        return card;
    }

    // ============================================================
    // PAINEL REGRA DOS 3 DIAS
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

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST; formPanel.add(lblItem, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; formPanel.add(txtItem, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST; formPanel.add(lblValor, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; formPanel.add(txtValor, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
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
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER, 1), BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        return field;
    }

    private JButton criarBotaoAdicionarDesejo(JTextField txtItem, JTextField txtValor) {
        JButton btn = criarBotaoRedondo("Bloquear Compra por 3 Dias", MINT, MINT.brighter());
        btn.setForeground(Color.WHITE);
        btn.addActionListener(e -> {
            try {
                String nome = txtItem.getText().trim();
                String valorStr = txtValor.getText().trim();

                if (nome.isEmpty()) { mostrarAviso("Digite o nome do item desejado."); txtItem.requestFocus(); return; }
                if (valorStr.isEmpty()) { mostrarAviso("Digite o valor do item."); txtValor.requestFocus(); return; }

                double valor = Double.parseDouble(valorStr);
                if (valor <= 0) { mostrarAviso("O valor deve ser maior que zero."); return; }

                regraTresDiasService.adicionarDesejo(nome, valor);
                txtItem.setText(""); txtValor.setText("");
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
            if (!regraTresDiasService.existeDesejoLiberado()) {
                mostrarAviso("Nao ha desejos liberados para remover.\nOs desejos so sao liberados apos 3 dias.");
                return;
            }
            var resumo = regraTresDiasService.getResumoDesejos();
            long liberadosCount = resumo.getLiberados();
            String msg = (liberadosCount == 1) ? "Existe 1 desejo liberado. Deseja remove-lo?" : "Existem " 
            		+ liberadosCount + " desejos liberados. Deseja remove-los?";
            if (mostrarConfirmacaoEscura(msg, "Confirmar Remocao")) {
                regraTresDiasService.limparLiberados();
                atualizarTabelaDesejos();
                String msgSucesso = (liberadosCount == 1) ? "1 desejo liberado removido!" : liberadosCount + 
                		" desejos liberados removidos!";
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
            if (desejos.isEmpty()) { tableModelDesejos.addRow(new Object[] { "Nenhum item cadastrado", "", "", "", "" }); return; }
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
    // PAINEL PRIMEIRO EU
    // ============================================================
    
    private JPanel criarPainelPrimeiroEu() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titulo = new JLabel("Registro de Entradas (Rendas)", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        titulo.setForeground(SUCCESS);
        panel.add(titulo, BorderLayout.NORTH);
        
        // Formulário
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(PANEL_DARK);
        formPanel.setBorder(BorderFactory.createTitledBorder("Adicionar Entrada"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        
        JTextField txtDescricao = criarTextField(20);
        JTextField txtValor = criarTextField(10);
        JComboBox<String> cmbCategoria = new JComboBox<>(new String[]{ "Fixo", "Extra" });
        cmbCategoria.setBackground(PANEL_DARK);
        cmbCategoria.setForeground(TEXT_PRIMARY);
        cmbCategoria.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JLabel lblDescricao = new JLabel("Descrição:");
        lblDescricao.setForeground(TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(lblDescricao, gbc);
        gbc.gridx = 1; formPanel.add(txtDescricao, gbc);
        
        JLabel lblValor = new JLabel("Valor (R$):");
        lblValor.setForeground(TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(lblValor, gbc);
        gbc.gridx = 1; formPanel.add(txtValor, gbc);
        
        JLabel lblCategoria = new JLabel("Tipo:");
        lblCategoria.setForeground(TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(lblCategoria, gbc);
        gbc.gridx = 1; formPanel.add(cmbCategoria, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JButton btnAdicionar = criarBotaoRedondo("Adicionar Entrada", SUCCESS, SUCCESS.brighter());
        btnAdicionar.setForeground(Color.WHITE);
        formPanel.add(btnAdicionar, gbc);
        
        // Tabela
        String[] colunas = {"Data", "Valor", "Descrição", "Tipo"};
        DefaultTableModel tableModelRendas = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable tableRendas = new JTable(tableModelRendas);
        tableRendas.setRowHeight(28);
        tableRendas.setBackground(PANEL_DARK);
        tableRendas.setForeground(TEXT_PRIMARY);
        tableRendas.setGridColor(BORDER);
        tableRendas.getTableHeader().setBackground(NAV_DARK);
        tableRendas.getTableHeader().setForeground(TEXT_PRIMARY);
        tableRendas.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        JScrollPane scrollPane = new JScrollPane(tableRendas);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Histórico de Entradas"));
        scrollPane.getViewport().setBackground(PANEL_DARK);
        
        // Bottom
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(BG_DARK);
        
        JLabel lblTotalMes = new JLabel("Total do mês: R$ 0,00");
        lblTotalMes.setFont(new Font("Arial", Font.BOLD, 14));
        lblTotalMes.setForeground(WARNING);
        bottomPanel.add(lblTotalMes, BorderLayout.WEST);
        
        JButton btnVerDashboard = criarBotaoRedondo("Ver Dashboard", MINT, MINT.brighter());
        btnVerDashboard.setForeground(Color.WHITE);
        btnVerDashboard.addActionListener(e -> cardLayout.show(cardPanel, "Dashboard"));
        bottomPanel.add(btnVerDashboard, BorderLayout.EAST);
        
        carregarRendas(tableModelRendas, lblTotalMes);
        
        btnAdicionar.addActionListener(e -> {
            try {
                String descricao = txtDescricao.getText().trim();
                String valorStr = txtValor.getText().trim();
                String categoriaStr = (String) cmbCategoria.getSelectedItem();
                
                if (descricao.isEmpty()) { mostrarAviso("Digite a descrição."); txtDescricao.requestFocus(); return; }
                if (valorStr.isEmpty()) { mostrarAviso("Digite o valor."); txtValor.requestFocus(); return; }
                
                double valor = Double.parseDouble(valorStr);
                if (valor <= 0) { mostrarAviso("Valor deve ser maior que zero."); return; }
                
                CategoriaRenda categoria = categoriaStr.equals("Fixo") ? CategoriaRenda.FIXO : CategoriaRenda.EXTRA;
                
                primeiroEuService.adicionarRenda(valor, descricao, categoria);
                txtDescricao.setText(""); txtValor.setText("");
                carregarRendas(tableModelRendas, lblTotalMes);
                mostrarSucesso("Entrada registrada!");
                
            } catch (NumberFormatException ex) {
                mostrarErro("Valor inválido! Use ponto como separador decimal.", ex);
            } catch (IllegalArgumentException | SQLException ex) {
                mostrarErro(ex.getMessage(), ex);
            }
        });
        
        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private void carregarRendas(DefaultTableModel tableModel, JLabel lblTotal) {
        tableModel.setRowCount(0);
        try {
            var rendas = primeiroEuService.listarRendas();
            double total = 0;
            
            for (HistoricoRenda r : rendas) {
                total += r.getValorTotal();
                tableModel.addRow(new Object[]{
                    r.getDataFormatada(),
                    String.format("R$ %.2f", r.getValorTotal()),
                    r.getDescricao(),
                    r.getCategoria().getDescricao()
                });
            }
            lblTotal.setText(String.format("Total do mês: R$ %,10.2f", total));
        } catch (SQLException e) {
            mostrarErro("Erro ao carregar rendas", e);
        }
    }

    // ============================================================
    // PAINEL CHECKLIST
    // ============================================================
    
    private JPanel criarPainelChecklist() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titulo = new JLabel("Registro de Gastos", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        titulo.setForeground(ACCENT);
        panel.add(titulo, BorderLayout.NORTH);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(PANEL_DARK);
        formPanel.setBorder(BorderFactory.createTitledBorder("Adicionar Gasto"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        
        JTextField txtDescricao = criarTextField(20);
        JTextField txtValor = criarTextField(10);
        JComboBox<String> cmbCategoria = new JComboBox<>(new String[]{ "Fixo Mensal", "Diário" });
        cmbCategoria.setBackground(PANEL_DARK);
        cmbCategoria.setForeground(TEXT_PRIMARY);
        cmbCategoria.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JLabel lblDescricao = new JLabel("Descrição:");
        lblDescricao.setForeground(TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(lblDescricao, gbc);
        gbc.gridx = 1; formPanel.add(txtDescricao, gbc);
        
        JLabel lblValor = new JLabel("Valor (R$):");
        lblValor.setForeground(TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(lblValor, gbc);
        gbc.gridx = 1; formPanel.add(txtValor, gbc);
        
        JLabel lblCategoria = new JLabel("Categoria:");
        lblCategoria.setForeground(TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(lblCategoria, gbc);
        gbc.gridx = 1; formPanel.add(cmbCategoria, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JButton btnAdicionar = criarBotaoRedondo("Adicionar Gasto", SUCCESS, SUCCESS.brighter());
        btnAdicionar.setForeground(Color.WHITE);
        formPanel.add(btnAdicionar, gbc);
        
        String[] colunas = {"Data", "Valor", "Categoria", "Descrição"};
        DefaultTableModel tableModelGastos = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable tableGastos = new JTable(tableModelGastos);
        tableGastos.setRowHeight(28);
        tableGastos.setBackground(PANEL_DARK);
        tableGastos.setForeground(TEXT_PRIMARY);
        tableGastos.setGridColor(BORDER);
        tableGastos.getTableHeader().setBackground(NAV_DARK);
        tableGastos.getTableHeader().setForeground(TEXT_PRIMARY);
        tableGastos.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        JScrollPane scrollPane = new JScrollPane(tableGastos);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Gastos Registrados"));
        scrollPane.getViewport().setBackground(PANEL_DARK);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(BG_DARK);
        JLabel lblTotal = new JLabel("Total: R$ 0,00");
        lblTotal.setFont(new Font("Arial", Font.BOLD, 16));
        lblTotal.setForeground(WARNING);
        bottomPanel.add(lblTotal, BorderLayout.WEST);
        
        JButton btnVerDashboard = criarBotaoRedondo("Ver Dashboard", MINT, MINT.brighter());
        btnVerDashboard.setForeground(Color.WHITE);
        btnVerDashboard.addActionListener(e -> cardLayout.show(cardPanel, "Dashboard"));
        bottomPanel.add(btnVerDashboard, BorderLayout.EAST);
        
        carregarGastosDoDia(tableModelGastos, lblTotal);
        
        btnAdicionar.addActionListener(e -> {
            try {
                String descricao = txtDescricao.getText().trim();
                String valorStr = txtValor.getText().trim();
                String categoriaStr = (String) cmbCategoria.getSelectedItem();
                
                if (descricao.isEmpty()) { mostrarAviso("Digite a descrição do gasto."); txtDescricao.requestFocus(); return; }
                if (valorStr.isEmpty()) { mostrarAviso("Digite o valor do gasto."); txtValor.requestFocus(); return; }
                
                double valor = Double.parseDouble(valorStr);
                if (valor <= 0) { mostrarAviso("Valor deve ser maior que zero."); return; }
                
                CategoriaGasto categoria = categoriaStr.equals("Fixo Mensal") ? CategoriaGasto.FIXO_MENSAL : CategoriaGasto.DIARIO;
                
                ChecklistDiario checklist = checklistService.getChecklistHoje();
                if (checklist == null) {
                    checklist = new ChecklistDiario(true);
                    checklistService.registrarChecklist(true);
                    checklist = checklistService.getChecklistHoje();
                }
                
                gastoService.adicionarGasto(checklist.getId(), descricao, valor, categoria);
                txtDescricao.setText(""); txtValor.setText("");
                carregarGastosDoDia(tableModelGastos, lblTotal);
                mostrarSucesso("Gasto adicionado!");
                
            } catch (NumberFormatException ex) {
                mostrarErro("Valor inválido! Use ponto como separador decimal.", ex);
            } catch (SQLException | IllegalArgumentException ex) {
                mostrarErro(ex.getMessage(), ex);
            }
        });
        
        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private void carregarGastosDoDia(DefaultTableModel tableModel, JLabel lblTotal) {
        tableModel.setRowCount(0);
        try {
            ChecklistDiario checklist = checklistService.getChecklistHoje();
            if (checklist == null) { lblTotal.setText("Total: R$ 0,00"); return; }
            
            var gastos = gastoService.listarGastosDoDia(checklist.getId());
            double total = 0;
            
            for (GastoDiario g : gastos) {
                total += g.getValor();
                String dataFormatada = g.getDataCriacao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                tableModel.addRow(new Object[]{
                    dataFormatada,
                    String.format("R$ %.2f", g.getValor()),
                    g.getCategoria().getDescricao(),
                    g.getDescricao()
                });
            }
            lblTotal.setText(String.format("Total: R$ %,10.2f", total));
        } catch (SQLException e) {
            mostrarErro("Erro ao carregar gastos", e);
        }
    }

    // ============================================================
    // PAINEL DASHBOARD
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

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(BG_DARK);
        
        JButton btnAtualizar = criarBotaoRedondo("Atualizar", WARNING, WARNING.brighter());
        btnAtualizar.setForeground(Color.BLACK);
        btnAtualizar.addActionListener(e -> mostrarDashboard(dashboardArea));
        buttonPanel.add(btnAtualizar);
        
        // NOVO BOTÃO: Limpar Dashboard
        JButton btnLimparDashboard = criarBotaoRedondo("Limpar Dashboard", DANGER, DANGER.brighter());
        btnLimparDashboard.setForeground(Color.WHITE);
        btnLimparDashboard.addActionListener(e -> {
            dashboardArea.setText("");
            mostrarSucesso("Dashboard limpo!");
        });
        buttonPanel.add(btnLimparDashboard);
        
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
            var resumo = regraTresDiasService.getResumoDesejos();
            sb.append(String.format("Desejos bloqueados:   %d\n", resumo.getBloqueados()));
            sb.append(String.format("Desejos liberados:    %d\n", resumo.getLiberados()));
            sb.append(String.format("Total bloqueado:      R$ %,10.2f\n", resumo.getTotalBloqueado()));
            sb.append(String.format("Economia potencial:   R$ %,10.2f\n\n", resumo.getEconomiaPotencial()));
        } catch (SQLException e) { sb.append("Erro ao carregar desejos\n\n"); }

        sb.append("REGRA DO PRIMEIRO EU\n").append("-".repeat(40)).append("\n");
        try {
            var resumo = primeiroEuService.getResumoRendas();
            sb.append(resumo.formatar()).append("\n");
        } catch (SQLException e) { 
            sb.append("Erro ao carregar rendas\n\n"); 
        }

        sb.append("CHECKLIST DIARIO\n").append("-".repeat(40)).append("\n");
        try {
            var resumo = checklistService.getResumoChecklist();
            sb.append(String.format("Taxa de sucesso:      %.1f%%\n", resumo.getTaxaSucesso()));
            sb.append("Status hoje:          " + resumo.getStatusHojeTexto() + "\n");
            sb.append("\n").append(resumo.getRecomendacao());
        } catch (SQLException e) { sb.append("Erro ao carregar checklist\n"); }

        sb.append("\nRESUMO DE GASTOS DO MES\n").append("-".repeat(40)).append("\n");
        try {
            var resumoGastos = gastoService.getResumoGastosMensal();
            sb.append(resumoGastos.formatar()).append("\n");
            
            double totalRendaMes = primeiroEuService.getTotalRendaMesAtual();
            double limiteRecomendado = totalRendaMes * 0.90;
            sb.append(String.format("\nLimite recomendado (90%% da renda): R$ %,10.2f\n", limiteRecomendado));
            
            if (resumoGastos.getTotalGastosMes() > limiteRecomendado) {
                double excedente = resumoGastos.getTotalGastosMes() - limiteRecomendado;
                sb.append(String.format("⚠️ ATENÇÃO: Você gastou R$ %,10.2f acima do recomendado!\n", excedente));
            } else {
                double economia = limiteRecomendado - resumoGastos.getTotalGastosMes();
                sb.append(String.format("✅ Bom trabalho! Você economizou R$ %,10.2f dentro do seu orçamento.\n", economia));
            }
        } catch (SQLException e) { sb.append("Erro ao carregar gastos\n\n"); }

        sb.append("\n\n").append("=".repeat(60)).append("\n");
        sb.append("     Continue assim! Controle financeiro e habito!\n");
        sb.append("=".repeat(60));
        
        return sb.toString();
    }

    private void mostrarModalDashboard(String conteudo) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(PANEL_DARK);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(WARNING, 2), 
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
    // BOTÕES REDONDOS
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
    // MODAIS
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
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(cor, 2), 
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
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(MINT, 2), 
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
    // MAIN
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