package com.movieticket.util;

import com.movieticket.dao.CinePointsDAO;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CinePointsService {

    private static final int POINTS_PER_100_INR = 10;
    private static final int DISCOUNT_PER_100_POINTS = 50;

    private final CinePointsDAO cinePointsDAO;

    public CinePointsService() {
        this.cinePointsDAO = new CinePointsDAO();
    }

    public CinePointsService(CinePointsDAO cinePointsDAO) {
        this.cinePointsDAO = cinePointsDAO;
    }

    public int getBalance(int userId) {
        cinePointsDAO.reconcilePendingPoints(userId);
        return cinePointsDAO.getBalance(userId);
    }

    public java.util.List<com.movieticket.model.CinePointsTransaction> getTransactionHistory(int userId) {
        cinePointsDAO.reconcilePendingPoints(userId);
        return cinePointsDAO.getTransactionHistory(userId);
    }

    public int calculatePointsEarned(BigDecimal finalBookingAmount) {
        if (finalBookingAmount == null || finalBookingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        // e.g. 500 / 100 = 5 * 10 = 50 points
        BigDecimal hundreds = finalBookingAmount.divide(new BigDecimal("100"), 0, RoundingMode.DOWN);
        return hundreds.intValue() * POINTS_PER_100_INR;
    }

    public BigDecimal calculateDiscount(int pointsToRedeem) {
        if (pointsToRedeem <= 0) return BigDecimal.ZERO;
        
        // e.g. 100 points = 50 INR discount
        int hundreds = pointsToRedeem / 100;
        return new BigDecimal(hundreds * DISCOUNT_PER_100_POINTS);
    }

    public int calculateMaxRedeemablePoints(int availablePoints, BigDecimal payableAmount) {
        // Can only redeem in multiples of 100 points
        int redeemableBlocks = availablePoints / 100;
        int maxBlocksNeeded = payableAmount.divide(new BigDecimal(DISCOUNT_PER_100_POINTS), 0, RoundingMode.UP).intValue();
        
        int blocksToUse = Math.min(redeemableBlocks, maxBlocksNeeded);
        return blocksToUse * 100;
    }

    public boolean awardPoints(int userId, int bookingId, int showId, java.time.LocalDateTime showStartTime, BigDecimal amount) {
        if (cinePointsDAO.checkBookingRewarded(bookingId)) {
            // Already rewarded
            return false;
        }

        int pointsToAward = calculatePointsEarned(amount);
        if (pointsToAward > 0) {
            String desc = "Earned points for booking ID " + bookingId;
            return cinePointsDAO.addPoints(userId, pointsToAward, bookingId, showId, showStartTime, "PENDING", desc);
        }
        return true; // No points to award, but not a failure
    }
    
    public void cancelPendingPoints(int bookingId) {
        cinePointsDAO.cancelPendingPoints(bookingId);
    }

    public boolean redeemPoints(int userId, int pointsToRedeem, int bookingId) {
        if (pointsToRedeem <= 0) return true;
        
        String desc = "Redeemed points for booking ID " + bookingId;
        return cinePointsDAO.redeemPoints(userId, pointsToRedeem, bookingId, desc);
    }
}
