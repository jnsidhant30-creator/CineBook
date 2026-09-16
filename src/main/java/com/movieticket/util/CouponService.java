package com.movieticket.util;

import com.movieticket.dao.CouponDAO;
import com.movieticket.model.Coupon;
import com.movieticket.model.CouponUsage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

public class CouponService {

    private final CouponDAO couponDAO;

    public CouponService() {
        this.couponDAO = new CouponDAO();
    }

    public static class CouponValidationResult {
        private final boolean valid;
        private final String message;
        private final Coupon coupon;

        public CouponValidationResult(boolean valid, String message, Coupon coupon) {
            this.valid = valid;
            this.message = message;
            this.coupon = coupon;
        }

        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public Coupon getCoupon() { return coupon; }
    }

    public CouponValidationResult validateCoupon(String code, int userId, BigDecimal bookingAmount) {
        if (code == null || code.trim().isEmpty()) {
            return new CouponValidationResult(false, "Please enter a valid coupon code.", null);
        }

        Optional<Coupon> optCoupon = couponDAO.findByCode(code.trim().toUpperCase());
        if (optCoupon.isEmpty()) {
            return new CouponValidationResult(false, "Coupon not found.", null);
        }

        Coupon coupon = optCoupon.get();

        if (!coupon.isActive()) {
            return new CouponValidationResult(false, "This coupon is currently inactive.", null);
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartDate())) {
            return new CouponValidationResult(false, "This coupon is not yet valid.", null);
        }

        if (now.isAfter(coupon.getExpiryDate())) {
            return new CouponValidationResult(false, "Coupon has expired.", null);
        }

        if (coupon.getMinBookingAmount() != null && bookingAmount.compareTo(coupon.getMinBookingAmount()) < 0) {
            return new CouponValidationResult(false, "Minimum booking amount for this coupon is ₹" + coupon.getMinBookingAmount(), null);
        }

        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            return new CouponValidationResult(false, "Coupon usage limit reached.", null);
        }

        if (coupon.getPerUserLimit() != null) {
            int userUsage = couponDAO.getUserCouponUsageCount(coupon.getId(), userId);
            if (userUsage >= coupon.getPerUserLimit()) {
                return new CouponValidationResult(false, "You have already used this coupon the maximum allowed times.", null);
            }
        }

        return new CouponValidationResult(true, "Coupon applied successfully!", coupon);
    }

    public BigDecimal calculateDiscount(Coupon coupon, BigDecimal bookingAmount) {
        if (coupon == null || bookingAmount == null || bookingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = BigDecimal.ZERO;

        if ("FIXED".equalsIgnoreCase(coupon.getDiscountType())) {
            discount = coupon.getDiscountValue();
        } else if ("PERCENTAGE".equalsIgnoreCase(coupon.getDiscountType())) {
            BigDecimal percentage = coupon.getDiscountValue().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            discount = bookingAmount.multiply(percentage);
        }

        if (coupon.getMaxDiscount() != null && coupon.getMaxDiscount().compareTo(BigDecimal.ZERO) > 0) {
            if (discount.compareTo(coupon.getMaxDiscount()) > 0) {
                discount = coupon.getMaxDiscount();
            }
        }
        
        // Cannot discount more than the booking amount
        if (discount.compareTo(bookingAmount) > 0) {
            discount = bookingAmount;
        }

        return discount.setScale(2, RoundingMode.HALF_UP);
    }
    
    public boolean recordUsage(int couponId, int userId, int bookingId, BigDecimal discountAmount) {
        CouponUsage usage = new CouponUsage(0, couponId, userId, bookingId, discountAmount, LocalDateTime.now());
        return couponDAO.recordCouponUsage(usage);
    }
}
