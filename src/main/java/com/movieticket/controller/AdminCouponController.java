package com.movieticket.controller;

import com.movieticket.dao.CouponDAO;
import com.movieticket.model.Coupon;
import com.movieticket.util.ThemeManager;
import com.movieticket.view.AdminCouponManagementView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

public class AdminCouponController {

    private final AdminCouponManagementView view;
    private final CouponDAO couponDAO;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AdminCouponController(AdminCouponManagementView view, CouponDAO couponDAO) {
        this.view = view;
        this.couponDAO = couponDAO;
        initBindings();
        refreshTable();
    }

    private void initBindings() {
        view.getRefreshButton().addActionListener(e -> refreshTable());
        view.getClearButton().addActionListener(e -> clearForm());

        view.getCouponTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateFormFromSelection();
            }
        });

        view.getAddButton().addActionListener(e -> handleAdd());
        view.getUpdateButton().addActionListener(e -> handleUpdate());
        view.getDeleteButton().addActionListener(e -> handleDelete());
    }

    private void refreshTable() {
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);
        
        List<Coupon> coupons = couponDAO.getAllCoupons();
        for (Coupon c : coupons) {
            model.addRow(new Object[]{
                c.getId(),
                c.getCode(),
                c.getDiscountType(),
                c.getDiscountValue(),
                c.getExpiryDate().format(formatter),
                c.getUsedCount() + (c.getUsageLimit() != null ? "/" + c.getUsageLimit() : ""),
                c.isActive() ? "Yes" : "No",
                c.isAllowCinepoints() ? "Yes" : "No"
            });
        }
    }

    private void clearForm() {
        view.getIdField().setText("");
        view.getCodeField().setText("");
        view.getDescriptionField().setText("");
        view.getDiscountTypeCombo().setSelectedIndex(0);
        view.getDiscountValueField().setText("");
        view.getMaxDiscountField().setText("");
        view.getMinBookingField().setText("0");
        view.getUsageLimitField().setText("");
        view.getPerUserLimitField().setText("");
        
        String now = LocalDateTime.now().withNano(0).format(formatter);
        String nextMonth = LocalDateTime.now().plusMonths(1).withNano(0).format(formatter);
        view.getStartDateField().setText(now);
        view.getExpiryDateField().setText(nextMonth);
        
        view.getActiveCheckbox().setSelected(true);
        view.getAllowCinePointsCheckbox().setSelected(true);
        
        view.getCouponTable().clearSelection();
    }

    private void populateFormFromSelection() {
        int row = view.getCouponTable().getSelectedRow();
        if (row < 0) return;

        int id = (int) view.getCouponTable().getValueAt(row, 0);
        String code = (String) view.getCouponTable().getValueAt(row, 1);
        
        Optional<Coupon> opt = couponDAO.findByCode(code);
        if (opt.isPresent()) {
            Coupon c = opt.get();
            view.getIdField().setText(String.valueOf(c.getId()));
            view.getCodeField().setText(c.getCode());
            view.getDescriptionField().setText(c.getDescription() != null ? c.getDescription() : "");
            view.getDiscountTypeCombo().setSelectedItem(c.getDiscountType());
            view.getDiscountValueField().setText(c.getDiscountValue().toString());
            view.getMaxDiscountField().setText(c.getMaxDiscount() != null ? c.getMaxDiscount().toString() : "");
            view.getMinBookingField().setText(c.getMinBookingAmount() != null ? c.getMinBookingAmount().toString() : "0");
            view.getUsageLimitField().setText(c.getUsageLimit() != null ? c.getUsageLimit().toString() : "");
            view.getPerUserLimitField().setText(c.getPerUserLimit() != null ? c.getPerUserLimit().toString() : "");
            view.getStartDateField().setText(c.getStartDate().format(formatter));
            view.getExpiryDateField().setText(c.getExpiryDate().format(formatter));
            view.getActiveCheckbox().setSelected(c.isActive());
            view.getAllowCinePointsCheckbox().setSelected(c.isAllowCinepoints());
        }
    }

    private void handleAdd() {
        Coupon c = buildCouponFromForm(false);
        if (c == null) return;
        
        if (couponDAO.findByCode(c.getCode()).isPresent()) {
            ThemeManager.showWarning(view, "Coupon code '" + c.getCode() + "' already exists.", "Duplicate Code");
            return;
        }

        int id = couponDAO.createCoupon(c);
        if (id > 0) {
            ThemeManager.showInfo(view, "Coupon created successfully.", "Success");
            clearForm();
            refreshTable();
        } else {
            ThemeManager.showError(view, "Failed to create coupon.", "Database Error");
        }
    }

    private void handleUpdate() {
        String idStr = view.getIdField().getText();
        if (idStr.isEmpty()) {
            ThemeManager.showWarning(view, "Please select a coupon to update.", "Selection Required");
            return;
        }
        
        Coupon c = buildCouponFromForm(true);
        if (c == null) return;
        
        Optional<Coupon> existing = couponDAO.findByCode(c.getCode());
        if (existing.isPresent() && existing.get().getId() != c.getId()) {
            ThemeManager.showWarning(view, "Coupon code '" + c.getCode() + "' is already in use by another coupon.", "Duplicate Code");
            return;
        }

        if (couponDAO.updateCoupon(c)) {
            ThemeManager.showInfo(view, "Coupon updated successfully.", "Success");
            clearForm();
            refreshTable();
        } else {
            ThemeManager.showError(view, "Failed to update coupon.", "Database Error");
        }
    }

    private void handleDelete() {
        String idStr = view.getIdField().getText();
        if (idStr.isEmpty()) {
            ThemeManager.showWarning(view, "Please select a coupon to delete.", "Selection Required");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(view, "Are you sure you want to delete this coupon? This action cannot be undone.", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (couponDAO.deleteCoupon(Integer.parseInt(idStr))) {
                ThemeManager.showInfo(view, "Coupon deleted successfully.", "Success");
                clearForm();
                refreshTable();
            } else {
                ThemeManager.showError(view, "Failed to delete coupon.", "Database Error");
            }
        }
    }

    private Coupon buildCouponFromForm(boolean isUpdate) {
        try {
            Coupon c = new Coupon();
            
            if (isUpdate) {
                c.setId(Integer.parseInt(view.getIdField().getText()));
            }
            
            String code = view.getCodeField().getText().trim().toUpperCase();
            if (code.isEmpty()) throw new IllegalArgumentException("Code is required.");
            c.setCode(code);
            
            c.setDescription(view.getDescriptionField().getText().trim());
            c.setDiscountType((String) view.getDiscountTypeCombo().getSelectedItem());
            
            c.setDiscountValue(new BigDecimal(view.getDiscountValueField().getText().trim()));
            if (c.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Discount value must be greater than 0.");
            }
            
            String maxD = view.getMaxDiscountField().getText().trim();
            if (!maxD.isEmpty()) c.setMaxDiscount(new BigDecimal(maxD));
            
            String minB = view.getMinBookingField().getText().trim();
            if (!minB.isEmpty()) c.setMinBookingAmount(new BigDecimal(minB));
            else c.setMinBookingAmount(BigDecimal.ZERO);
            
            String uLim = view.getUsageLimitField().getText().trim();
            if (!uLim.isEmpty()) c.setUsageLimit(Integer.parseInt(uLim));
            
            String pLim = view.getPerUserLimitField().getText().trim();
            if (!pLim.isEmpty()) c.setPerUserLimit(Integer.parseInt(pLim));
            
            c.setStartDate(LocalDateTime.parse(view.getStartDateField().getText().trim(), formatter));
            c.setExpiryDate(LocalDateTime.parse(view.getExpiryDateField().getText().trim(), formatter));
            
            if (c.getExpiryDate().isBefore(c.getStartDate())) {
                throw new IllegalArgumentException("Expiry date must be after start date.");
            }
            
            c.setActive(view.getActiveCheckbox().isSelected());
            c.setAllowCinepoints(view.getAllowCinePointsCheckbox().isSelected());
            
            return c;
        } catch (DateTimeParseException ex) {
            ThemeManager.showWarning(view, "Invalid date format. Please use yyyy-MM-dd HH:mm:ss", "Validation Error");
        } catch (NumberFormatException ex) {
            ThemeManager.showWarning(view, "Invalid number format in one of the numeric fields.", "Validation Error");
        } catch (IllegalArgumentException ex) {
            ThemeManager.showWarning(view, ex.getMessage(), "Validation Error");
        }
        return null;
    }
}
