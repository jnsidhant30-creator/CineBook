package com.movieticket.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Coupon {
    private int id;
    private String code;
    private String description;
    private String discountType; // "PERCENTAGE" or "FIXED"
    private BigDecimal discountValue;
    private BigDecimal maxDiscount;
    private BigDecimal minBookingAmount;
    private Integer usageLimit;
    private Integer perUserLimit;
    private int usedCount;
    private LocalDateTime startDate;
    private LocalDateTime expiryDate;
    private boolean active;
    private boolean allowCinepoints;
    private LocalDateTime createdAt;

    public Coupon() {}

    public Coupon(int id, String code, String description, String discountType, BigDecimal discountValue,
                  BigDecimal maxDiscount, BigDecimal minBookingAmount, Integer usageLimit, Integer perUserLimit,
                  int usedCount, LocalDateTime startDate, LocalDateTime expiryDate, boolean active,
                  boolean allowCinepoints, LocalDateTime createdAt) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.maxDiscount = maxDiscount;
        this.minBookingAmount = minBookingAmount;
        this.usageLimit = usageLimit;
        this.perUserLimit = perUserLimit;
        this.usedCount = usedCount;
        this.startDate = startDate;
        this.expiryDate = expiryDate;
        this.active = active;
        this.allowCinepoints = allowCinepoints;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }

    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }

    public BigDecimal getMaxDiscount() { return maxDiscount; }
    public void setMaxDiscount(BigDecimal maxDiscount) { this.maxDiscount = maxDiscount; }

    public BigDecimal getMinBookingAmount() { return minBookingAmount; }
    public void setMinBookingAmount(BigDecimal minBookingAmount) { this.minBookingAmount = minBookingAmount; }

    public Integer getUsageLimit() { return usageLimit; }
    public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }

    public Integer getPerUserLimit() { return perUserLimit; }
    public void setPerUserLimit(Integer perUserLimit) { this.perUserLimit = perUserLimit; }

    public int getUsedCount() { return usedCount; }
    public void setUsedCount(int usedCount) { this.usedCount = usedCount; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isAllowCinepoints() { return allowCinepoints; }
    public void setAllowCinepoints(boolean allowCinepoints) { this.allowCinepoints = allowCinepoints; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
