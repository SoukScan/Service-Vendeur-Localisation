package com.soukscan.vendorms.service;

import com.soukscan.vendorms.entity.PriceReport;
import com.soukscan.vendorms.entity.VendorProduct;
import com.soukscan.vendorms.repository.PriceReportRepository;
import com.soukscan.vendorms.repository.VendorProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Intelligent Price Aggregation Service - Like Waze, handles multiple price reports intelligently
 */
@Service
public class IntelligentPriceAggregationService {

    private static final Logger log = LoggerFactory.getLogger(IntelligentPriceAggregationService.class);
    
    private final PriceReportRepository priceReportRepository;
    private final VendorProductRepository vendorProductRepository;
    
    // Configuration constants
    private static final int MIN_REPORTS_FOR_CONSENSUS = 3;
    private static final int RECENT_REPORTS_DAYS = 30;
    private static final double PRICE_TOLERANCE_PERCENTAGE = 10.0; // 10% tolerance for similar prices
    
    @Autowired
    public IntelligentPriceAggregationService(PriceReportRepository priceReportRepository,
                                            VendorProductRepository vendorProductRepository) {
        this.priceReportRepository = priceReportRepository;
        this.vendorProductRepository = vendorProductRepository;
    }

    /**
     * Calculate the best price for a vendor's product based on multiple reports
     * Uses Waze-like logic: consensus, recency, and outlier detection
     */
    public Double calculateOptimalPrice(Long vendorId, Long productId, Double newPrice, Long userId) {
        // Get recent price reports for this vendor-product combination
        List<PriceReport> recentReports = getRecentReports(vendorId.toString(), productId.toString());
        
        log.info("Processing price aggregation for vendor {} product {} - Found {} recent reports", 
                vendorId, productId, recentReports.size());

        // Add the new price to consideration
        recentReports.add(createTempReport(vendorId.toString(), productId.toString(), newPrice, userId));
        
        if (recentReports.size() < MIN_REPORTS_FOR_CONSENSUS) {
            // Not enough data for consensus, use simple average
            return calculateSimpleAverage(recentReports);
        } else {
            // Use intelligent consensus-based pricing
            return calculateConsensusPrice(recentReports);
        }
    }

    /**
     * Get recent price reports for a vendor-product combination
     */
    private List<PriceReport> getRecentReports(String vendorId, String productId) {
        OffsetDateTime cutoffDate = OffsetDateTime.now().minusDays(RECENT_REPORTS_DAYS);
        return priceReportRepository.findByProductIdAndVendorId(productId, vendorId)
                .stream()
                .filter(report -> report.getReportedAt().isAfter(cutoffDate))
                .collect(Collectors.toList());
    }

    /**
     * Calculate simple average when insufficient data
     */
    private Double calculateSimpleAverage(List<PriceReport> reports) {
        double average = reports.stream()
                .mapToDouble(report -> report.getPrice().doubleValue())
                .average()
                .orElse(0.0);
        
        log.info("Using simple average: {} from {} reports", average, reports.size());
        return average;
    }

    /**
     * Calculate consensus-based price using Waze-like logic
     */
    private Double calculateConsensusPrice(List<PriceReport> reports) {
        // Group similar prices together (within tolerance)
        Map<Double, Long> priceGroups = reports.stream()
                .collect(Collectors.groupingBy(
                    report -> roundToNearestGroup(report.getPrice().doubleValue()),
                    Collectors.counting()
                ));

        // Find the most common price group
        Map.Entry<Double, Long> consensusGroup = priceGroups.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

        if (consensusGroup != null && consensusGroup.getValue() >= 2) {
            // Consensus found - use average of prices in the consensus group
            double consensusPrice = reports.stream()
                    .filter(report -> isInSameGroup(report.getPrice().doubleValue(), consensusGroup.getKey()))
                    .mapToDouble(report -> report.getPrice().doubleValue())
                    .average()
                    .orElse(consensusGroup.getKey());
            
            log.info("Consensus price found: {} (supported by {} reports)", 
                    consensusPrice, consensusGroup.getValue());
            return consensusPrice;
        } else {
            // No clear consensus - use weighted average favoring recent reports
            return calculateWeightedAverage(reports);
        }
    }

    /**
     * Round price to group similar prices together
     */
    private Double roundToNearestGroup(double price) {
        // Group prices within 10% tolerance
        double groupSize = price * (PRICE_TOLERANCE_PERCENTAGE / 100.0);
        return Math.round(price / groupSize) * groupSize;
    }

    /**
     * Check if two prices belong to the same group
     */
    private boolean isInSameGroup(double price1, double price2) {
        double tolerance = Math.max(price1, price2) * (PRICE_TOLERANCE_PERCENTAGE / 100.0);
        return Math.abs(price1 - price2) <= tolerance;
    }

    /**
     * Calculate weighted average considering user credibility and recency
     */
    private Double calculateWeightedAverage(List<PriceReport> reports) {
        OffsetDateTime now = OffsetDateTime.now();
        
        double weightedSum = 0.0;
        double totalWeight = 0.0;
        
        for (PriceReport report : reports) {
            // Calculate base weight based on recency
            long daysOld = java.time.Duration.between(report.getReportedAt(), now).toDays();
            double recencyWeight = Math.max(1.0, RECENT_REPORTS_DAYS - daysOld) / RECENT_REPORTS_DAYS;
            
            // Calculate user credibility weight based on their reporting history
            double userCredibilityWeight = calculateUserCredibilityWeight(report.getReportedByUserId());
            
            // Combine weights
            double totalReportWeight = recencyWeight * userCredibilityWeight;
            
            weightedSum += report.getPrice().doubleValue() * totalReportWeight;
            totalWeight += totalReportWeight;
        }
        
        double weightedAverage = totalWeight > 0 ? weightedSum / totalWeight : 0.0;
        log.info("Using credibility-weighted average: {} from {} reports", weightedAverage, reports.size());
        return weightedAverage;
    }

    /**
     * Calculate user credibility weight based on their reporting history
     */
    private double calculateUserCredibilityWeight(Long userId) {
        if (userId == null) return 1.0;
        
        try {
            // Get user's total reports and recent activity
            Integer totalReports = priceReportRepository.countReportsByUser(userId);
            if (totalReports == null || totalReports == 0) {
                return 1.0; // Default weight for new users or when tracking is unavailable
            }
            
            OffsetDateTime thirtyDaysAgo = OffsetDateTime.now().minusDays(30);
            List<PriceReport> recentReports = priceReportRepository.findRecentReportsByUser(userId, thirtyDaysAgo);
            
            // Base credibility on reporting volume and consistency
            double volumeWeight = Math.min(2.0, 1.0 + (totalReports * 0.1)); // Max 2x weight for active reporters
            double activityWeight = recentReports != null && recentReports.size() > 0 ? 1.2 : 0.9; // Slight boost for recent activity
            
            return Math.min(3.0, volumeWeight * activityWeight); // Cap at 3x weight
        } catch (Exception e) {
            log.warn("Could not calculate user credibility for user {} (user tracking may not be available): {}", userId, e.getMessage());
            return 1.0; // Default weight when user tracking is not available
        }
    }

    /**
     * Create a temporary price report for calculation purposes
     */
    private PriceReport createTempReport(String vendorId, String productId, Double price, Long userId) {
        PriceReport tempReport = new PriceReport();
        tempReport.setVendorId(vendorId);
        tempReport.setProductId(productId);
        tempReport.setPrice(BigDecimal.valueOf(price));
        tempReport.setReportedByUserId(userId);
        tempReport.setReportedAt(OffsetDateTime.now());
        tempReport.setReportedDate(java.time.LocalDate.now());
        return tempReport;
    }

    /**
     * Get pricing recommendation for display
     */
    public String getPriceRecommendationMessage(List<PriceReport> reports) {
        if (reports.size() < MIN_REPORTS_FOR_CONSENSUS) {
            return String.format("Price based on %d report(s) - more reports needed for accuracy", reports.size());
        }
        
        Map<Double, Long> priceGroups = reports.stream()
                .collect(Collectors.groupingBy(
                    report -> roundToNearestGroup(report.getPrice().doubleValue()),
                    Collectors.counting()
                ));
        
        Map.Entry<Double, Long> consensusGroup = priceGroups.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);
        
        if (consensusGroup != null && consensusGroup.getValue() >= 2) {
            return String.format("Price confirmed by %d users", consensusGroup.getValue());
        } else {
            return String.format("Average from %d reports - price may vary", reports.size());
        }
    }
}