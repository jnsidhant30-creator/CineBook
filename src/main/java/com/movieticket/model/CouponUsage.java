package com.movieticket.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CouponUsage {
    private int id;
    private int couponId;
    private int userId;
    private int bookingId;
    private BigDecimal discountAmount;
    private LocalDateTime usedAt;

    public CouponUsage() {}

    public CouponUsage(int id, int couponId, int userId, int bookingId, BigDecimal discountAmount, LocalDateTime usedAt) {
        this.id = id;
        this.couponId = couponId;
        this.userId = userId;
        this.bookingId = bookingId;
        this.discountAmount = discountAmount;
        this.usedAt = usedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCouponId() { return couponId; }
    public void setCouponId(int couponId) { this.couponId = couponId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
}
