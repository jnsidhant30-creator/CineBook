package com.movieticket.view;

import com.movieticket.util.CineBookTheme;
import com.movieticket.util.ThemeManager;
import com.movieticket.view.components.AnimatedCheckmarkSpinner;
import com.movieticket.view.components.GlassCardPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Professional desktop payment panel.
 * Left: Breakdown / Order Summary
 * Right: Payment Methods (Simulated)
 */
public class PaymentSimulationView extends JPanel {

    private JLabel movieLabel;
    private JLabel theatreLabel;
    private JLabel dateLabel;
    private JLabel timeLabel;
    private JLabel seatsLabel;
    private JCheckBox useCinePointsCheckbox;
    private JLabel cinePointsDiscountLabel;
    
    private JTextField couponCodeField;
    private JButton applyCouponButton;
    private JButton removeCouponButton;
    private JLabel couponDiscountLabel;
    private JLabel couponMessageLabel;
    
    private JLabel totalAmountLabel;

    private JComboBox<String> paymentMethodCombo;
    private JPanel dynamicFormPanel;
    private CardLayout formCardLayout;

    private JTextField upiIdField;
    private JTextField cardNumberField;
    private JTextField cardNameField;
    private JComboBox<String> bankCombo;
    private JComboBox<String> walletCombo;

    private JButton payNowButton;
    private JButton cancelButton;

    private JPanel overlayStatusPanel;
    private AnimatedCheckmarkSpinner animatedSpinner;
    private JLabel statusNoticeLabel;

    public PaymentSimulationView() {
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);
        setBackground(CineBookTheme.BG_PRIMARY);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        initComponents();
    }

    private void initComponents() {
        // 1. Title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 24, 0));

        JLabel titleLabel = new JLabel("Secure Checkout");
        titleLabel.setFont(ThemeManager.getFont(Font.BOLD, 36));
        titleLabel.setForeground(CineBookTheme.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Complete your payment to confirm the booking.");
        subtitleLabel.setFont(ThemeManager.getFont(Font.PLAIN, 16));
        subtitleLabel.setForeground(CineBookTheme.TEXT_MUTED);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Center Container
        JPanel centerContainer = new JPanel(new GridBagLayout());
        centerContainer.setOpaque(false);
        
        GridBagConstraints gbcMain = new GridBagConstraints();
        gbcMain.fill = GridBagConstraints.BOTH;
        gbcMain.weighty = 1.0;

        // --- Left Column: Order Summary ---
        GlassCardPanel summaryCard = new GlassCardPanel(new GridBagLayout());
        summaryCard.setTopAccent(CineBookTheme.ACCENT_PURPLE, 4);
        summaryCard.setBorder(new EmptyBorder(32, 40, 32, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 0, 10, 20);
        gbc.gridx = 0;
        int row = 0;

        gbc.gridy = row++;
        gbc.gridwidth = 2;
        JLabel summaryTitle = new JLabel("Order Summary");
        summaryTitle.setFont(ThemeManager.getFont(Font.BOLD, 24));
        summaryTitle.setForeground(CineBookTheme.TEXT_PRIMARY);
        summaryCard.add(summaryTitle, gbc);

        gbc.gridy = row++;
        JSeparator sep = new JSeparator();
        sep.setForeground(CineBookTheme.BORDER_COLOR);
        summaryCard.add(sep, gbc);

        gbc.gridwidth = 1;

        gbc.gridy = row; gbc.gridx = 0; summaryCard.add(createLabel("Movie"), gbc);
        gbc.gridx = 1; movieLabel = createValueLabel("—"); summaryCard.add(movieLabel, gbc);
        row++;

        gbc.gridy = row; gbc.gridx = 0; summaryCard.add(createLabel("Theatre"), gbc);
        gbc.gridx = 1; theatreLabel = createValueLabel("—"); summaryCard.add(theatreLabel, gbc);
        row++;

        gbc.gridy = row; gbc.gridx = 0; summaryCard.add(createLabel("Show Date"), gbc);
        gbc.gridx = 1; dateLabel = createValueLabel("—"); summaryCard.add(dateLabel, gbc);
        row++;

        gbc.gridy = row; gbc.gridx = 0; summaryCard.add(createLabel("Show Time"), gbc);
        gbc.gridx = 1; timeLabel = createValueLabel("—"); summaryCard.add(timeLabel, gbc);
        row++;

        gbc.gridy = row; gbc.gridx = 0; summaryCard.add(createLabel("Seats"), gbc);
        gbc.gridx = 1; seatsLabel = createValueLabel("—"); seatsLabel.setForeground(CineBookTheme.ACCENT_CYAN); summaryCard.add(seatsLabel, gbc);
        row++;

        gbc.gridy = row++; gbc.gridx = 0; gbc.gridwidth = 2; 
        JSeparator sep2 = new JSeparator(); sep2.setForeground(CineBookTheme.BORDER_COLOR); 
        summaryCard.add(sep2, gbc);

        // Coupon Section
        gbc.gridy = row++; gbc.gridx = 0; gbc.gridwidth = 2;
        JLabel couponTitle = new JLabel("Apply Coupon");
        couponTitle.setFont(ThemeManager.getFont(Font.BOLD, 18));
        couponTitle.setForeground(CineBookTheme.TEXT_PRIMARY);
        summaryCard.add(couponTitle, gbc);

        gbc.gridy = row++; gbc.gridx = 0; gbc.gridwidth = 2;
        JPanel couponPanel = new JPanel(new BorderLayout(10, 0));
        couponPanel.setOpaque(false);
        couponCodeField = new JTextField();
        ThemeManager.styleTextField(couponCodeField);
        couponCodeField.setPreferredSize(new Dimension(200, 45));
        
        applyCouponButton = new JButton("Apply");
        ThemeManager.styleSuccessButton(applyCouponButton);
        applyCouponButton.setPreferredSize(new Dimension(100, 45));
        
        removeCouponButton = new JButton("Remove");
        ThemeManager.styleDangerButton(removeCouponButton);
        removeCouponButton.setPreferredSize(new Dimension(100, 45));
        removeCouponButton.setVisible(false);
        
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(applyCouponButton);
        btnPanel.add(removeCouponButton);
        
        couponPanel.add(couponCodeField, BorderLayout.CENTER);
        couponPanel.add(btnPanel, BorderLayout.EAST);
        summaryCard.add(couponPanel, gbc);

        gbc.gridy = row++; gbc.gridx = 0; gbc.gridwidth = 2;
        couponMessageLabel = new JLabel(" ");
        couponMessageLabel.setFont(ThemeManager.getFont(Font.PLAIN, 14));
        summaryCard.add(couponMessageLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = row; gbc.gridx = 0; summaryCard.add(createLabel("Coupon Discount"), gbc);
        gbc.gridx = 1; couponDiscountLabel = createValueLabel("-₹0.00"); couponDiscountLabel.setForeground(CineBookTheme.SUCCESS_COLOR); summaryCard.add(couponDiscountLabel, gbc);
        row++;

        gbc.gridy = row++; gbc.gridx = 0; gbc.gridwidth = 2;
        useCinePointsCheckbox = new JCheckBox("Use CinePoints for additional discount");
        useCinePointsCheckbox.setFont(ThemeManager.getFont(Font.PLAIN, 16));
        useCinePointsCheckbox.setForeground(CineBookTheme.ACCENT_PURPLE);
        useCinePointsCheckbox.setOpaque(false);
        summaryCard.add(useCinePointsCheckbox, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = row; gbc.gridx = 0; summaryCard.add(createLabel("CinePoints Discount"), gbc);
        gbc.gridx = 1; cinePointsDiscountLabel = createValueLabel("-₹0.00"); cinePointsDiscountLabel.setForeground(CineBookTheme.SUCCESS_COLOR); summaryCard.add(cinePointsDiscountLabel, gbc);
        row++;

        gbc.gridy = row++; gbc.gridx = 0; gbc.gridwidth = 2; 
        JSeparator sep3 = new JSeparator(); sep3.setForeground(CineBookTheme.BORDER_COLOR); 
        summaryCard.add(sep3, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = row; gbc.gridx = 0; 
        JLabel totalText = new JLabel("Total Payable");
        totalText.setFont(ThemeManager.getFont(Font.BOLD, 20));
        totalText.setForeground(CineBookTheme.TEXT_MUTED);
        summaryCard.add(totalText, gbc);
        
        gbc.gridx = 1; 
        totalAmountLabel = new JLabel("₹0.00"); 
        totalAmountLabel.setFont(ThemeManager.getFont(Font.BOLD, 32)); 
        totalAmountLabel.setForeground(CineBookTheme.SUCCESS_COLOR); 
        summaryCard.add(totalAmountLabel, gbc);
        row++;

        // Spacer
        gbc.gridy = row++; gbc.gridx = 0; gbc.gridwidth = 2; gbc.weighty = 1.0;
        summaryCard.add(Box.createVerticalGlue(), gbc);

        gbcMain.gridx = 0;
        gbcMain.weightx = 0.55;
        gbcMain.insets = new Insets(0, 0, 0, 20);
        centerContainer.add(summaryCard, gbcMain);

        // --- Right Column: Payment Methods ---
        GlassCardPanel paymentCard = new GlassCardPanel(new BorderLayout(0, 24));
        paymentCard.setTopAccent(CineBookTheme.ACCENT_BLUE, 4);
        paymentCard.setBorder(new EmptyBorder(32, 40, 32, 40));

        JLabel payTitle = new JLabel("Payment Method");
        payTitle.setFont(ThemeManager.getFont(Font.BOLD, 24));
        payTitle.setForeground(CineBookTheme.TEXT_PRIMARY);
        paymentCard.add(payTitle, BorderLayout.NORTH);

        JPanel methodPanel = new JPanel(new BorderLayout(0, 24));
        methodPanel.setOpaque(false);

        String[] methods = {"UPI", "Debit / Credit Card", "Net Banking", "Wallet"};
        paymentMethodCombo = new JComboBox<>(methods);
        ThemeManager.styleComboBox(paymentMethodCombo);
        paymentMethodCombo.setPreferredSize(new Dimension(Integer.MAX_VALUE, 48));
        paymentMethodCombo.setFont(ThemeManager.getFont(Font.BOLD, 16));
        methodPanel.add(paymentMethodCombo, BorderLayout.NORTH);

        formCardLayout = new CardLayout();
        dynamicFormPanel = new JPanel(formCardLayout);
        dynamicFormPanel.setOpaque(false);

        // UPI Form
        JPanel upiForm = new JPanel(new GridBagLayout());
        upiForm.setOpaque(false);
        GridBagConstraints upiGbc = new GridBagConstraints();
        upiGbc.fill = GridBagConstraints.HORIZONTAL;
        upiGbc.weightx = 1.0;
        upiGbc.insets = new Insets(0, 0, 10, 0);
        upiGbc.gridx = 0; upiGbc.gridy = 0;
        upiForm.add(createLabel("Enter UPI ID"), upiGbc);
        upiIdField = new JTextField();
        ThemeManager.styleTextField(upiIdField);
        upiIdField.setPreferredSize(new Dimension(Integer.MAX_VALUE, 48));
        upiGbc.gridy = 1;
        upiForm.add(upiIdField, upiGbc);
        dynamicFormPanel.add(upiForm, "UPI");

        // Card Form
        JPanel cardForm = new JPanel(new GridBagLayout());
        cardForm.setOpaque(false);
        GridBagConstraints cardGbc = new GridBagConstraints();
        cardGbc.fill = GridBagConstraints.HORIZONTAL;
        cardGbc.weightx = 1.0;
        cardGbc.insets = new Insets(0, 0, 10, 0);
        cardGbc.gridx = 0;
        
        cardGbc.gridy = 0; cardForm.add(createLabel("Card Number"), cardGbc);
        cardNumberField = new JTextField();
        ThemeManager.styleTextField(cardNumberField);
        cardNumberField.setPreferredSize(new Dimension(Integer.MAX_VALUE, 48));
        cardGbc.gridy = 1; cardForm.add(cardNumberField, cardGbc);
        
        cardGbc.gridy = 2; cardForm.add(createLabel("Card Holder Name"), cardGbc);
        cardNameField = new JTextField();
        ThemeManager.styleTextField(cardNameField);
        cardNameField.setPreferredSize(new Dimension(Integer.MAX_VALUE, 48));
        cardGbc.gridy = 3; cardForm.add(cardNameField, cardGbc);
        
        JPanel expCvvPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        expCvvPanel.setOpaque(false);
        JTextField expField = new JTextField("MM/YY");
        JTextField cvvField = new JTextField("CVV");
        ThemeManager.styleTextField(expField);
        ThemeManager.styleTextField(cvvField);
        expField.setPreferredSize(new Dimension(Integer.MAX_VALUE, 48));
        cvvField.setPreferredSize(new Dimension(Integer.MAX_VALUE, 48));
        expCvvPanel.add(expField);
        expCvvPanel.add(cvvField);
        
        cardGbc.gridy = 4;
        cardGbc.insets = new Insets(10, 0, 0, 0);
        cardForm.add(expCvvPanel, cardGbc);
        
        dynamicFormPanel.add(cardForm, "Debit / Credit Card");

        // Net Banking
        JPanel netBankForm = new JPanel(new GridBagLayout());
        netBankForm.setOpaque(false);
        GridBagConstraints nbGbc = new GridBagConstraints();
        nbGbc.fill = GridBagConstraints.HORIZONTAL;
        nbGbc.weightx = 1.0;
        nbGbc.insets = new Insets(0, 0, 10, 0);
        nbGbc.gridx = 0; nbGbc.gridy = 0;
        netBankForm.add(createLabel("Select Bank"), nbGbc);
        String[] banks = {"State Bank of India", "HDFC Bank", "ICICI Bank", "Axis Bank", "Punjab National Bank"};
        bankCombo = new JComboBox<>(banks);
        ThemeManager.styleComboBox(bankCombo);
        bankCombo.setPreferredSize(new Dimension(Integer.MAX_VALUE, 48));
        nbGbc.gridy = 1;
        netBankForm.add(bankCombo, nbGbc);
        dynamicFormPanel.add(netBankForm, "Net Banking");

        // Wallet
        JPanel walletForm = new JPanel(new GridBagLayout());
        walletForm.setOpaque(false);
        GridBagConstraints wGbc = new GridBagConstraints();
        wGbc.fill = GridBagConstraints.HORIZONTAL;
        wGbc.weightx = 1.0;
        wGbc.insets = new Insets(0, 0, 10, 0);
        wGbc.gridx = 0; wGbc.gridy = 0;
        walletForm.add(createLabel("Select Wallet"), wGbc);
        String[] wallets = {"Paytm", "PhonePe", "Amazon Pay", "CineBook Wallet"};
        walletCombo = new JComboBox<>(wallets);
        ThemeManager.styleComboBox(walletCombo);
        walletCombo.setPreferredSize(new Dimension(Integer.MAX_VALUE, 48));
        wGbc.gridy = 1;
        walletForm.add(walletCombo, wGbc);
        dynamicFormPanel.add(walletForm, "Wallet");

        methodPanel.add(dynamicFormPanel, BorderLayout.CENTER);

        JLabel simLabel = new JLabel("Simulation Mode. No real money will be charged.", SwingConstants.CENTER);
        simLabel.setFont(ThemeManager.getFont(Font.PLAIN, 14));
        simLabel.setForeground(CineBookTheme.TEXT_MUTED);
        methodPanel.add(simLabel, BorderLayout.SOUTH);

        paymentCard.add(methodPanel, BorderLayout.CENTER);

        // Overlay status panel (hidden by default)
        overlayStatusPanel = new JPanel();
        overlayStatusPanel.setLayout(new BoxLayout(overlayStatusPanel, BoxLayout.Y_AXIS));
        overlayStatusPanel.setOpaque(false);
        overlayStatusPanel.setVisible(false);

        animatedSpinner = new AnimatedCheckmarkSpinner();
        animatedSpinner.setAlignmentX(Component.CENTER_ALIGNMENT);

        statusNoticeLabel = new JLabel("Processing Payment...", SwingConstants.CENTER);
        statusNoticeLabel.setFont(ThemeManager.getFont(Font.BOLD, 22));
        statusNoticeLabel.setForeground(CineBookTheme.TEXT_PRIMARY);
        statusNoticeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        overlayStatusPanel.add(Box.createVerticalGlue());
        overlayStatusPanel.add(animatedSpinner);
        overlayStatusPanel.add(Box.createVerticalStrut(20));
        overlayStatusPanel.add(statusNoticeLabel);
        overlayStatusPanel.add(Box.createVerticalGlue());

        JPanel rightContainer = new JPanel(new CardLayout());
        rightContainer.setOpaque(false);
        rightContainer.add(paymentCard, "Form");
        rightContainer.add(overlayStatusPanel, "Processing");

        gbcMain.gridx = 1;
        gbcMain.weightx = 0.45;
        gbcMain.insets = new Insets(0, 20, 0, 0);
        centerContainer.add(rightContainer, gbcMain);
                JScrollPane scrollPane = new JScrollPane(centerContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        // 3. Actions Panel
        JPanel actions = new com.movieticket.view.components.ResponsiveGridPanel(new com.movieticket.view.components.WrapLayout(FlowLayout.RIGHT, 16, 0));
        actions.setOpaque(false);
        actions.setBorder(new EmptyBorder(24, 0, 0, 0));

        cancelButton = new JButton("Cancel Payment");
        ThemeManager.styleDangerButton(cancelButton);
        cancelButton.setPreferredSize(new Dimension(180, 48));
        cancelButton.setFont(ThemeManager.getFont(Font.BOLD, 14));

        payNowButton = new JButton("Pay Now & Confirm");
        ThemeManager.styleSuccessButton(payNowButton);
        payNowButton.setPreferredSize(new Dimension(240, 48));
        payNowButton.setFont(ThemeManager.getFont(Font.BOLD, 14));

        actions.add(cancelButton);
        actions.add(payNowButton);
        add(actions, BorderLayout.SOUTH);

        // Logic
        paymentMethodCombo.addActionListener(e -> {
            String selected = (String) paymentMethodCombo.getSelectedItem();
            if (selected != null) {
                formCardLayout.show(dynamicFormPanel, selected);
            }
        });
    }

    public void showProcessingState() {
        payNowButton.setEnabled(false);
        cancelButton.setEnabled(false);
        paymentMethodCombo.setEnabled(false);
        Container rightContainer = overlayStatusPanel.getParent();
        if (rightContainer.getLayout() instanceof CardLayout cl) {
            cl.show(rightContainer, "Processing");
        }
        animatedSpinner.setMode(AnimatedCheckmarkSpinner.Mode.SPINNER);
        statusNoticeLabel.setText("Processing Payment...");
        statusNoticeLabel.setForeground(CineBookTheme.TEXT_PRIMARY);
    }

    public void showSuccessState(Runnable onComplete) {
        animatedSpinner.setMode(AnimatedCheckmarkSpinner.Mode.CHECKMARK);
        statusNoticeLabel.setText("Payment Successful!");
        statusNoticeLabel.setForeground(CineBookTheme.SUCCESS_COLOR);
        
        Timer t = new Timer(1500, e -> {
            if (onComplete != null) onComplete.run();
        });
        t.setRepeats(false);
        t.start();
    }

    public void showFailedState(Runnable onComplete) {
        animatedSpinner.setMode(AnimatedCheckmarkSpinner.Mode.CHECKMARK);
        statusNoticeLabel.setText("Payment Failed!");
        statusNoticeLabel.setForeground(CineBookTheme.DANGER_COLOR);
        
        Timer t = new Timer(1500, e -> {
            resetState();
            if (onComplete != null) onComplete.run();
        });
        t.setRepeats(false);
        t.start();
    }

    public void resetState() {
        payNowButton.setEnabled(true);
        cancelButton.setEnabled(true);
        paymentMethodCombo.setEnabled(true);
        Container rightContainer = overlayStatusPanel.getParent();
        if (rightContainer.getLayout() instanceof CardLayout cl) {
            cl.show(rightContainer, "Form");
        }
        upiIdField.setText("");
        cardNumberField.setText("");
        cardNameField.setText("");
        
        couponCodeField.setText("");
        couponCodeField.setEnabled(true);
        applyCouponButton.setVisible(true);
        removeCouponButton.setVisible(false);
        couponDiscountLabel.setText("-₹0.00");
        couponMessageLabel.setText(" ");
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(ThemeManager.getFont(Font.PLAIN, 16));
        l.setForeground(CineBookTheme.TEXT_MUTED);
        return l;
    }

    private JLabel createValueLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(ThemeManager.getFont(Font.BOLD, 16));
        l.setForeground(CineBookTheme.TEXT_PRIMARY);
        return l;
    }

    // Getters
    public JLabel getMovieLabel() { return movieLabel; }
    public JLabel getTheatreLabel() { return theatreLabel; }
    public JLabel getDateLabel() { return dateLabel; }
    public JLabel getTimeLabel() { return timeLabel; }
    public JLabel getSeatsLabel() { return seatsLabel; }
    public JCheckBox getUseCinePointsCheckbox() { return useCinePointsCheckbox; }
    public JLabel getCinePointsDiscountLabel() { return cinePointsDiscountLabel; }
    public JLabel getTotalAmountLabel() { return totalAmountLabel; }
    public JButton getPayNowButton() { return payNowButton; }
    public JButton getCancelButton() { return cancelButton; }
    public JComboBox<String> getPaymentMethodCombo() { return paymentMethodCombo; }
    public JTextField getUpiIdField() { return upiIdField; }

    public JTextField getCouponCodeField() { return couponCodeField; }
    public JButton getApplyCouponButton() { return applyCouponButton; }
    public JButton getRemoveCouponButton() { return removeCouponButton; }
    public JLabel getCouponDiscountLabel() { return couponDiscountLabel; }
    public JLabel getCouponMessageLabel() { return couponMessageLabel; }
}
