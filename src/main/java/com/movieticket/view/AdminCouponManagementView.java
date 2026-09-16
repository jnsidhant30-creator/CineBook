package com.movieticket.view;

import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;

public class AdminCouponManagementView extends JPanel {

    private JTable couponTable;
    private DefaultTableModel tableModel;
    
    private JTextField codeField;
    private JTextField descriptionField;
    private JComboBox<String> discountTypeCombo;
    private JTextField discountValueField;
    private JTextField maxDiscountField;
    private JTextField minBookingField;
    private JTextField usageLimitField;
    private JTextField perUserLimitField;
    private JTextField startDateField;
    private JTextField expiryDateField;
    private JCheckBox activeCheckbox;
    private JCheckBox allowCinePointsCheckbox;
    
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton clearButton;
    private JButton refreshButton;
    
    private JTextField idField; // Hidden field for updates

    public AdminCouponManagementView() {
        setLayout(new BorderLayout(15, 15));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        initComponents();
    }

    private void initComponents() {
        // Title
        JLabel titleLabel = new JLabel("Coupon Management");
        titleLabel.setFont(ThemeManager.getPageTitleFont());
        titleLabel.setForeground(CineBookTheme.TEXT_PRIMARY);
        add(titleLabel, BorderLayout.NORTH);

        // Center Content Split
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setDividerSize(0);

        // Left Side: Table
        JPanel tablePanel = new JPanel(new BorderLayout(0, 10));
        tablePanel.setOpaque(false);

        String[] cols = {"ID", "Code", "Type", "Value", "Expiry", "Used", "Active", "Comb. CinePoints"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        couponTable = new JTable(tableModel);
        ThemeManager.styleTable(couponTable);
        
        JScrollPane scrollPane = new JScrollPane(couponTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel tblActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        tblActions.setOpaque(false);
        refreshButton = new JButton("Refresh");
        ThemeManager.styleSecondaryButton(refreshButton);
        tblActions.add(refreshButton);
        tablePanel.add(tblActions, BorderLayout.NORTH);

        splitPane.setLeftComponent(tablePanel);

        // Right Side: Form
        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.setOpaque(false);
        formContainer.setBorder(new EmptyBorder(0, 20, 0, 0));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;
        
        idField = new JTextField(); // hidden
        
        int row = 0;
        gbc.gridy = row++; gbc.gridwidth = 2;
        JLabel formTitle = new JLabel("Coupon Details");
        formTitle.setFont(ThemeManager.getSectionHeaderFont());
        formTitle.setForeground(CineBookTheme.TEXT_PRIMARY);
        formPanel.add(formTitle, gbc);

        gbc.gridwidth = 1;

        gbc.gridy = row++; gbc.gridx = 0; formPanel.add(createLabel("Code (Unique):"), gbc);
        gbc.gridx = 1; codeField = new JTextField(); ThemeManager.styleTextField(codeField); formPanel.add(codeField, gbc);

        gbc.gridy = row++; gbc.gridx = 0; formPanel.add(createLabel("Description:"), gbc);
        gbc.gridx = 1; descriptionField = new JTextField(); ThemeManager.styleTextField(descriptionField); formPanel.add(descriptionField, gbc);

        gbc.gridy = row++; gbc.gridx = 0; formPanel.add(createLabel("Discount Type:"), gbc);
        gbc.gridx = 1; discountTypeCombo = new JComboBox<>(new String[]{"PERCENTAGE", "FIXED"}); ThemeManager.styleComboBox(discountTypeCombo); formPanel.add(discountTypeCombo, gbc);

        gbc.gridy = row++; gbc.gridx = 0; formPanel.add(createLabel("Discount Value:"), gbc);
        gbc.gridx = 1; discountValueField = new JTextField(); ThemeManager.styleTextField(discountValueField); formPanel.add(discountValueField, gbc);

        gbc.gridy = row++; gbc.gridx = 0; formPanel.add(createLabel("Max Discount (opt):"), gbc);
        gbc.gridx = 1; maxDiscountField = new JTextField(); ThemeManager.styleTextField(maxDiscountField); formPanel.add(maxDiscountField, gbc);

        gbc.gridy = row++; gbc.gridx = 0; formPanel.add(createLabel("Min Booking Amt:"), gbc);
        gbc.gridx = 1; minBookingField = new JTextField("0"); ThemeManager.styleTextField(minBookingField); formPanel.add(minBookingField, gbc);

        gbc.gridy = row++; gbc.gridx = 0; formPanel.add(createLabel("Global Usage Limit (opt):"), gbc);
        gbc.gridx = 1; usageLimitField = new JTextField(); ThemeManager.styleTextField(usageLimitField); formPanel.add(usageLimitField, gbc);

        gbc.gridy = row++; gbc.gridx = 0; formPanel.add(createLabel("Per-User Limit (opt):"), gbc);
        gbc.gridx = 1; perUserLimitField = new JTextField(); ThemeManager.styleTextField(perUserLimitField); formPanel.add(perUserLimitField, gbc);

        String now = LocalDateTime.now().withNano(0).toString().replace("T", " ");
        String nextMonth = LocalDateTime.now().plusMonths(1).withNano(0).toString().replace("T", " ");
        
        gbc.gridy = row++; gbc.gridx = 0; formPanel.add(createLabel("Start Date (yyyy-MM-dd HH:mm:ss):"), gbc);
        gbc.gridx = 1; startDateField = new JTextField(now); ThemeManager.styleTextField(startDateField); formPanel.add(startDateField, gbc);

        gbc.gridy = row++; gbc.gridx = 0; formPanel.add(createLabel("Expiry Date:"), gbc);
        gbc.gridx = 1; expiryDateField = new JTextField(nextMonth); ThemeManager.styleTextField(expiryDateField); formPanel.add(expiryDateField, gbc);

        gbc.gridy = row++; gbc.gridx = 0; gbc.gridwidth = 2;
        JPanel cbPanel = new JPanel(new GridLayout(1, 2));
        cbPanel.setOpaque(false);
        activeCheckbox = new JCheckBox("Active", true);
        activeCheckbox.setOpaque(false);
        activeCheckbox.setForeground(CineBookTheme.TEXT_PRIMARY);
        allowCinePointsCheckbox = new JCheckBox("Combine with CinePoints", true);
        allowCinePointsCheckbox.setOpaque(false);
        allowCinePointsCheckbox.setForeground(CineBookTheme.TEXT_PRIMARY);
        cbPanel.add(activeCheckbox);
        cbPanel.add(allowCinePointsCheckbox);
        formPanel.add(cbPanel, gbc);

        // Actions
        gbc.gridy = row++; gbc.gridx = 0; gbc.gridwidth = 2; gbc.insets = new Insets(20, 5, 5, 5);
        JPanel actionsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        actionsPanel.setOpaque(false);
        
        addButton = new JButton("Create");
        ThemeManager.styleSuccessButton(addButton);
        updateButton = new JButton("Update");
        ThemeManager.styleSecondaryButton(updateButton);
        deleteButton = new JButton("Delete");
        ThemeManager.styleDangerButton(deleteButton);
        clearButton = new JButton("Clear");
        ThemeManager.styleSecondaryButton(clearButton);
        
        actionsPanel.add(addButton);
        actionsPanel.add(updateButton);
        actionsPanel.add(deleteButton);
        actionsPanel.add(clearButton);
        
        formPanel.add(actionsPanel, gbc);
        
        // Push form to top
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(formPanel, BorderLayout.NORTH);
        
        JScrollPane formScroll = new JScrollPane(wrap);
        formScroll.setBorder(null);
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);
        formContainer.add(formScroll, BorderLayout.CENTER);

        splitPane.setRightComponent(formContainer);
        splitPane.setResizeWeight(0.65);
        
        add(splitPane, BorderLayout.CENTER);
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(CineBookTheme.TEXT_MUTED);
        l.setFont(ThemeManager.getLabelFont());
        return l;
    }

    public JTable getCouponTable() { return couponTable; }
    public DefaultTableModel getTableModel() { return tableModel; }
    
    public JTextField getIdField() { return idField; }
    public JTextField getCodeField() { return codeField; }
    public JTextField getDescriptionField() { return descriptionField; }
    public JComboBox<String> getDiscountTypeCombo() { return discountTypeCombo; }
    public JTextField getDiscountValueField() { return discountValueField; }
    public JTextField getMaxDiscountField() { return maxDiscountField; }
    public JTextField getMinBookingField() { return minBookingField; }
    public JTextField getUsageLimitField() { return usageLimitField; }
    public JTextField getPerUserLimitField() { return perUserLimitField; }
    public JTextField getStartDateField() { return startDateField; }
    public JTextField getExpiryDateField() { return expiryDateField; }
    public JCheckBox getActiveCheckbox() { return activeCheckbox; }
    public JCheckBox getAllowCinePointsCheckbox() { return allowCinePointsCheckbox; }
    
    public JButton getAddButton() { return addButton; }
    public JButton getUpdateButton() { return updateButton; }
    public JButton getDeleteButton() { return deleteButton; }
    public JButton getClearButton() { return clearButton; }
    public JButton getRefreshButton() { return refreshButton; }
}
