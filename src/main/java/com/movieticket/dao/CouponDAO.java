package com.movieticket.dao;

import com.movieticket.model.Coupon;
import com.movieticket.model.CouponUsage;
import com.movieticket.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CouponDAO {

    public Optional<Coupon> findByCode(String code) {
        String sql = "SELECT * FROM coupons WHERE code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCoupon(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<Coupon> getAllCoupons() {
        List<Coupon> coupons = new ArrayList<>();
        String sql = "SELECT * FROM coupons ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                coupons.add(mapResultSetToCoupon(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return coupons;
    }

    public int createCoupon(Coupon coupon) {
        String sql = "INSERT INTO coupons (code, description, discount_type, discount_value, max_discount, " +
                "min_booking_amount, usage_limit, per_user_limit, start_date, expiry_date, active, allow_cinepoints) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, coupon.getCode());
            pstmt.setString(2, coupon.getDescription());
            pstmt.setString(3, coupon.getDiscountType());
            pstmt.setBigDecimal(4, coupon.getDiscountValue());
            pstmt.setBigDecimal(5, coupon.getMaxDiscount());
            pstmt.setBigDecimal(6, coupon.getMinBookingAmount());
            if (coupon.getUsageLimit() != null) {
                pstmt.setInt(7, coupon.getUsageLimit());
            } else {
                pstmt.setNull(7, Types.INTEGER);
            }
            if (coupon.getPerUserLimit() != null) {
                pstmt.setInt(8, coupon.getPerUserLimit());
            } else {
                pstmt.setNull(8, Types.INTEGER);
            }
            pstmt.setTimestamp(9, Timestamp.valueOf(coupon.getStartDate()));
            pstmt.setTimestamp(10, Timestamp.valueOf(coupon.getExpiryDate()));
            pstmt.setBoolean(11, coupon.isActive());
            pstmt.setBoolean(12, coupon.isAllowCinepoints());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean updateCoupon(Coupon coupon) {
        String sql = "UPDATE coupons SET code = ?, description = ?, discount_type = ?, discount_value = ?, " +
                "max_discount = ?, min_booking_amount = ?, usage_limit = ?, per_user_limit = ?, " +
                "start_date = ?, expiry_date = ?, active = ?, allow_cinepoints = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, coupon.getCode());
            pstmt.setString(2, coupon.getDescription());
            pstmt.setString(3, coupon.getDiscountType());
            pstmt.setBigDecimal(4, coupon.getDiscountValue());
            pstmt.setBigDecimal(5, coupon.getMaxDiscount());
            pstmt.setBigDecimal(6, coupon.getMinBookingAmount());
            if (coupon.getUsageLimit() != null) {
                pstmt.setInt(7, coupon.getUsageLimit());
            } else {
                pstmt.setNull(7, Types.INTEGER);
            }
            if (coupon.getPerUserLimit() != null) {
                pstmt.setInt(8, coupon.getPerUserLimit());
            } else {
                pstmt.setNull(8, Types.INTEGER);
            }
            pstmt.setTimestamp(9, Timestamp.valueOf(coupon.getStartDate()));
            pstmt.setTimestamp(10, Timestamp.valueOf(coupon.getExpiryDate()));
            pstmt.setBoolean(11, coupon.isActive());
            pstmt.setBoolean(12, coupon.isAllowCinepoints());
            pstmt.setInt(13, coupon.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getUserCouponUsageCount(int couponId, int userId) {
        String sql = "SELECT COUNT(*) FROM coupon_usage WHERE coupon_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, couponId);
            pstmt.setInt(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean recordCouponUsage(CouponUsage usage) {
        String sql = "INSERT INTO coupon_usage (coupon_id, user_id, booking_id, discount_amount) VALUES (?, ?, ?, ?)";
        String updateUsageSql = "UPDATE coupons SET used_count = used_count + 1 WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement updateStmt = conn.prepareStatement(updateUsageSql)) {
                
                pstmt.setInt(1, usage.getCouponId());
                pstmt.setInt(2, usage.getUserId());
                pstmt.setInt(3, usage.getBookingId());
                pstmt.setBigDecimal(4, usage.getDiscountAmount());
                int rows = pstmt.executeUpdate();
                
                if (rows > 0) {
                    updateStmt.setInt(1, usage.getCouponId());
                    updateStmt.executeUpdate();
                    conn.commit();
                    return true;
                }
                conn.rollback();
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean deleteCoupon(int id) {
        String sql = "DELETE FROM coupons WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Coupon mapResultSetToCoupon(ResultSet rs) throws SQLException {
        Coupon c = new Coupon();
        c.setId(rs.getInt("id"));
        c.setCode(rs.getString("code"));
        c.setDescription(rs.getString("description"));
        c.setDiscountType(rs.getString("discount_type"));
        c.setDiscountValue(rs.getBigDecimal("discount_value"));
        c.setMaxDiscount(rs.getBigDecimal("max_discount"));
        c.setMinBookingAmount(rs.getBigDecimal("min_booking_amount"));
        
        c.setUsageLimit(rs.getObject("usage_limit") != null ? rs.getInt("usage_limit") : null);
        c.setPerUserLimit(rs.getObject("per_user_limit") != null ? rs.getInt("per_user_limit") : null);
        
        c.setUsedCount(rs.getInt("used_count"));
        c.setStartDate(rs.getTimestamp("start_date").toLocalDateTime());
        c.setExpiryDate(rs.getTimestamp("expiry_date").toLocalDateTime());
        c.setActive(rs.getBoolean("active"));
        c.setAllowCinepoints(rs.getBoolean("allow_cinepoints"));
        
        if (rs.getTimestamp("created_at") != null) {
            c.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        return c;
    }
}
