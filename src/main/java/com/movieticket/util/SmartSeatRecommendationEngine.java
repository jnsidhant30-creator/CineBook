package com.movieticket.util;

import com.movieticket.model.Seat;

import java.util.*;
import java.util.stream.Collectors;

/**
 * SmartSeatRecommendationEngine.java
 * Algorithmic seat scoring engine for SIH Smart Seat Recommendation & Group Booking.
 * Scores candidate seat blocks based on:
 * 1. Contiguous adjacency in single row (+50 pts)
 * 2. Center alignment distance (+40 pts)
 * 3. Row viewing distance (+30 pts)
 * 4. Available status (Mandatory)
 */
public class SmartSeatRecommendationEngine {

    public static class RecommendationResult {
        private final List<Seat> recommendedSeats;
        private final String rationale;
        private final boolean isSingleRowContiguous;
        private final double score;

        public RecommendationResult(List<Seat> recommendedSeats, String rationale, boolean isSingleRowContiguous, double score) {
            this.recommendedSeats = recommendedSeats;
            this.rationale = rationale;
            this.isSingleRowContiguous = isSingleRowContiguous;
            this.score = score;
        }

        public List<Seat> getRecommendedSeats() { return recommendedSeats; }
        public String getRationale() { return rationale; }
        public boolean isSingleRowContiguous() { return isSingleRowContiguous; }
        public double getScore() { return score; }

        public String getFormattedSeatNumbers() {
            if (recommendedSeats == null || recommendedSeats.isEmpty()) return "None";
            return recommendedSeats.stream()
                    .map(Seat::getSeatNumber)
                    .collect(Collectors.joining("  "));
        }
    }

    /**
     * Finds the optimal N seats from availableSeats for a show.
     */
    public static RecommendationResult recommendSeats(List<Seat> availableSeats, Set<Integer> bookedSeatIds, int count) {
        if (count <= 0 || availableSeats == null || availableSeats.isEmpty()) {
            return new RecommendationResult(Collections.emptyList(), "No seats available.", false, 0);
        }

        // Filter truly available seats
        List<Seat> freeSeats = availableSeats.stream()
                .filter(s -> !bookedSeatIds.contains(s.getSeatId()))
                .filter(s -> "AVAILABLE".equalsIgnoreCase(s.getStatus()))
                .collect(Collectors.toList());

        if (freeSeats.size() < count) {
            return new RecommendationResult(Collections.emptyList(), "Not enough available seats (" + freeSeats.size() + " left for " + count + " requested).", false, 0);
        }

        // Group free seats by Row prefix (e.g. 'A', 'B', 'C', 'D')
        Map<String, List<Seat>> seatsByRow = new TreeMap<>();
        for (Seat s : freeSeats) {
            String row = parseRow(s.getSeatNumber());
            seatsByRow.computeIfAbsent(row, k -> new ArrayList<>()).add(s);
        }

        // Sort seats in each row by column number
        for (List<Seat> rowList : seatsByRow.values()) {
            rowList.sort(Comparator.comparingInt(s -> parseCol(s.getSeatNumber())));
        }

        // Step 1: Look for contiguous block of length 'count' in the SAME row
        RecommendationResult bestSingleRow = findBestContiguousBlockInRows(seatsByRow, count);
        if (bestSingleRow != null) {
            return bestSingleRow;
        }

        // Step 2: Fallback — find best multi-row combination
        return findBestMultiRowCombination(freeSeats, count);
    }

    private static RecommendationResult findBestContiguousBlockInRows(Map<String, List<Seat>> seatsByRow, int count) {
        RecommendationResult best = null;
        double maxScore = -1.0;

        for (Map.Entry<String, List<Seat>> entry : seatsByRow.entrySet()) {
            String rowName = entry.getKey();
            List<Seat> rowSeats = entry.getValue();

            if (rowSeats.size() < count) continue;

            for (int i = 0; i <= rowSeats.size() - count; i++) {
                List<Seat> candidate = rowSeats.subList(i, i + count);

                // Check if candidate seats have consecutive column numbers
                if (isContiguous(candidate)) {
                    double score = calculateScore(candidate, true);
                    if (score > maxScore) {
                        maxScore = score;
                        String rationale = String.format("✓ %d contiguous seats in Row %s\n✓ Optimal viewing angle & center alignment\n✓ All seats available", count, rowName);
                        best = new RecommendationResult(new ArrayList<>(candidate), rationale, true, score);
                    }
                }
            }
        }
        return best;
    }

    private static RecommendationResult findBestMultiRowCombination(List<Seat> freeSeats, int count) {
        // Sort free seats by overall score individual priority
        freeSeats.sort((s1, s2) -> Double.compare(scoreSingleSeat(s2), scoreSingleSeat(s1)));

        List<Seat> chosen = freeSeats.stream().limit(count).collect(Collectors.toList());
        chosen.sort(Comparator.comparing((Seat s) -> parseRow(s.getSeatNumber())).thenComparingInt(s -> parseCol(s.getSeatNumber())));

        String rationale = String.format("⚠ Single contiguous block of %d seats unavailable.\n✓ Recommended best available combination across rows\n✓ High viewing quality seats selected", count);
        return new RecommendationResult(chosen, rationale, false, 40.0);
    }

    private static boolean isContiguous(List<Seat> candidate) {
        for (int i = 0; i < candidate.size() - 1; i++) {
            int col1 = parseCol(candidate.get(i).getSeatNumber());
            int col2 = parseCol(candidate.get(i + 1).getSeatNumber());
            if (col2 != col1 + 1) {
                return false;
            }
        }
        return true;
    }

    private static double calculateScore(List<Seat> candidate, boolean isContiguous) {
        double score = isContiguous ? 50.0 : 10.0;

        for (Seat s : candidate) {
            score += scoreSingleSeat(s);
        }

        return score / candidate.size();
    }

    private static double scoreSingleSeat(Seat s) {
        double score = 0.0;
        String row = parseRow(s.getSeatNumber());
        int col = parseCol(s.getSeatNumber());

        // Row Viewing Preference (Rows B and C preferred, Row D second, Row A front)
        if ("B".equalsIgnoreCase(row) || "C".equalsIgnoreCase(row)) {
            score += 30.0;
        } else if ("D".equalsIgnoreCase(row)) {
            score += 20.0;
        } else {
            score += 10.0;
        }

        // Center Column Alignment (Assuming 8 columns: cols 4, 5 closest to center)
        double distFromCenter = Math.abs(col - 4.5);
        score += Math.max(0, 40.0 - (distFromCenter * 8.0));

        // Category Bonus
        String type = s.getSeatType() != null ? s.getSeatType().toUpperCase() : "REGULAR";
        if ("VIP".equals(type)) score += 15.0;
        else if ("PREMIUM".equals(type)) score += 10.0;

        return score;
    }

    public static String parseRow(String seatNumber) {
        if (seatNumber == null || seatNumber.isEmpty()) return "A";
        return seatNumber.replaceAll("[^A-Za-z]", "").toUpperCase();
    }

    public static int parseCol(String seatNumber) {
        if (seatNumber == null || seatNumber.isEmpty()) return 1;
        try {
            return Integer.parseInt(seatNumber.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
