package com.movieticket.util;

import com.movieticket.dao.SeatHoldDAO;

import java.time.LocalDateTime;
import java.util.*;

/**
 * SeatHoldService — Coordinates temporary seat reservations.
 *
 * Default hold duration: 10 minutes (configurable via HOLD_DURATION_MINUTES).
 *
 * Flow:
 *   1. User selects seats → tryHoldSeats()
 *   2. Payment succeeds → confirmBooking()
 *   3. Payment fails/cancelled → releaseSeats()
 *   4. Hold expires → auto-released via lazy cleanup in SeatHoldDAO
 */
public class SeatHoldService {

    public static final int HOLD_DURATION_MINUTES = 5; // configurable
    public static final int REFRESH_INTERVAL_SECONDS = 5; // for UI polling

    private final SeatHoldDAO seatHoldDAO;

    public SeatHoldService() {
        this.seatHoldDAO = new SeatHoldDAO();
    }

    /**
     * Attempts to hold the given seats for the user.
     *
     * @return List of seat IDs that could NOT be held (already held/booked by another user).
     *         Empty list means all seats were successfully held.
     */
    public List<Integer> tryHoldSeats(int showId, List<Integer> seatIds, int userId) {
        if (seatIds == null || seatIds.isEmpty()) return Collections.emptyList();
        List<Integer> failed = seatHoldDAO.holdSeats(showId, seatIds, userId, HOLD_DURATION_MINUTES);
        if (failed.isEmpty()) {
            AuditService.log(AuditService.SEAT_HELD, "SHOW", showId,
                "User " + userId + " held " + seatIds.size() + " seat(s) for show " + showId);
        }
        return failed;
    }

    /**
     * Releases holds — called on payment failure or user cancellation.
     */
    public void releaseSeats(int showId, List<Integer> seatIds, int userId) {
        if (seatIds == null || seatIds.isEmpty()) return;
        seatHoldDAO.releaseHold(showId, seatIds, userId);
        AuditService.log(AuditService.SEAT_RELEASED, "SHOW", showId,
            "User " + userId + " released " + seatIds.size() + " held seat(s) for show " + showId);
    }

    /**
     * Converts active holds to CONVERTED status — called after successful booking commit.
     */
    public void confirmBooking(int showId, List<Integer> seatIds, int userId) {
        if (seatIds == null || seatIds.isEmpty()) return;
        seatHoldDAO.convertToBooked(showId, seatIds, userId);
    }

    /**
     * Returns seat IDs held by ANY user for the show (excluding the current user's own holds).
     * Used for UI refresh to mark "temporarily unavailable" seats.
     */
    public Set<Integer> getOtherUsersHeldSeatIds(int showId, int currentUserId) {
        Set<Integer> all = seatHoldDAO.getActiveHeldSeatIds(showId);
        Set<Integer> mine = seatHoldDAO.getUserHeldSeatIds(showId, currentUserId);
        all.removeAll(mine);
        return all;
    }

    /**
     * Returns seat IDs held by the current user (to show as HELD/yellow in the seat map).
     */
    public Set<Integer> getMyHeldSeatIds(int showId, int userId) {
        return seatHoldDAO.getUserHeldSeatIds(showId, userId);
    }

    /**
     * Gets the hold expiry timestamp for the current user.
     */
    public Optional<LocalDateTime> getMyHoldExpiry(int showId, int userId) {
        return seatHoldDAO.getHoldExpiry(showId, userId);
    }

    /**
     * Returns the number of seconds remaining on the hold.
     * Returns 0 if expired or no hold found.
     */
    public long getSecondsRemaining(int showId, int userId) {
        return getMyHoldExpiry(showId, userId)
            .map(expiry -> java.time.Duration.between(LocalDateTime.now(), expiry).toSeconds())
            .map(s -> Math.max(0, s))
            .orElse(0L);
    }
}
