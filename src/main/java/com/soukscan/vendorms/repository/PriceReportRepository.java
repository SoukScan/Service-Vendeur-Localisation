package com.soukscan.vendorms.repository;

import com.soukscan.vendorms.entity.PriceReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Repository for PriceReport entities.
 */
@Repository
public interface PriceReportRepository extends JpaRepository<PriceReport, Long> {

    /**
     * Find all price reports for a specific product.
     */
    List<PriceReport> findByProductId(String productId);

    /**
     * Find all price reports from a specific vendor.
     */
    List<PriceReport> findByVendorId(String vendorId);

    /**
     * Find all price reports by a specific user.
     * Safe fallback: returns empty list if user tracking not available
     */
    @Query(value = "SELECT pr.* FROM price_reports pr WHERE " +
           "CASE WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_name='price_reports' AND column_name='reported_by_user_id') " +
           "THEN pr.reported_by_user_id = :userId ELSE FALSE END", nativeQuery = true)
    List<PriceReport> findByReportedByUserId(@Param("userId") Long userId);

    /**
     * Find all price reports for a product from a specific vendor.
     */
    List<PriceReport> findByProductIdAndVendorId(String productId, String vendorId);

    /**
     * Find price reports by user for a specific product and vendor.
     * Safe fallback: returns empty list if user tracking not available
     */
    @Query(value = "SELECT pr.* FROM price_reports pr WHERE pr.product_id = :productId AND pr.vendor_id = :vendorId AND " +
           "CASE WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_name='price_reports' AND column_name='reported_by_user_id') " +
           "THEN pr.reported_by_user_id = :userId ELSE FALSE END", nativeQuery = true)
    List<PriceReport> findByProductIdAndVendorIdAndReportedByUserId(@Param("productId") String productId, @Param("vendorId") String vendorId, @Param("userId") Long userId);

    /**
     * Check if user already reported a price for this product at this vendor today.
     * Safe fallback: returns false if user tracking columns don't exist yet
     */
    @Query(value = "SELECT CASE WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_name='price_reports' AND column_name='reported_by_user_id') " +
           "AND EXISTS(SELECT 1 FROM information_schema.columns WHERE table_name='price_reports' AND column_name='reported_date') " +
           "THEN (SELECT COUNT(*) > 0 FROM price_reports WHERE product_id = :productId AND vendor_id = :vendorId AND reported_by_user_id = :userId AND reported_date = CURRENT_DATE) " +
           "ELSE FALSE END", nativeQuery = true)
    boolean hasUserReportedTodayForProductAtVendor(@Param("productId") String productId, @Param("vendorId") String vendorId, @Param("userId") Long userId);

    /**
     * Get user's recent price reports (last 30 days).
     * Safe fallback: returns empty list if user tracking not available
     */
    @Query(value = "SELECT pr.* FROM price_reports pr WHERE " +
           "CASE WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_name='price_reports' AND column_name='reported_by_user_id') " +
           "THEN pr.reported_by_user_id = :userId ELSE FALSE END " +
           "AND pr.reported_at >= :since ORDER BY pr.reported_at DESC", nativeQuery = true)
    List<PriceReport> findRecentReportsByUser(@Param("userId") Long userId, @Param("since") OffsetDateTime since);

    /**
     * Count total reports by user.
     * Safe fallback: returns 0 if user tracking not available
     */
    @Query(value = "SELECT CASE WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_name='price_reports' AND column_name='reported_by_user_id') " +
           "THEN (SELECT COUNT(*) FROM price_reports WHERE reported_by_user_id = :userId) " +
           "ELSE 0 END", nativeQuery = true)
    Integer countReportsByUser(@Param("userId") Long userId);

    /**
     * Find recent price reports for a product (within last N days).
     */
    @Query("SELECT pr FROM PriceReport pr WHERE pr.productId = :productId AND pr.reportedAt >= :since ORDER BY pr.reportedAt DESC")
    List<PriceReport> findRecentReportsByProduct(
        @Param("productId") String productId,
        @Param("since") OffsetDateTime since
    );

    /**
     * Calculate average price for a product.
     */
    @Query("SELECT AVG(pr.price) FROM PriceReport pr WHERE pr.productId = :productId")
    BigDecimal calculateAveragePrice(@Param("productId") String productId);

    /**
     * Calculate average price for a product from recent reports.
     */
    @Query("SELECT AVG(pr.price) FROM PriceReport pr WHERE pr.productId = :productId AND pr.reportedAt >= :since")
    BigDecimal calculateRecentAveragePrice(
        @Param("productId") String productId,
        @Param("since") OffsetDateTime since
    );

    /**
     * Count reports for a product.
     */
    @Query("SELECT COUNT(pr) FROM PriceReport pr WHERE pr.productId = :productId")
    Integer countReportsByProduct(@Param("productId") String productId);

    /**
     * Find price reports for a product within a geographic area.
     * Joins with locations table to filter by distance.
     */
    @Query(value = """
        SELECT pr.* FROM price_reports pr
        INNER JOIN locations l ON pr.vendor_id = l.vendor_id
        WHERE pr.product_id = :productId
        AND ST_DWithin(
            l.geom::geography,
            ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography,
            :distanceMeters
        )
        ORDER BY pr.reported_at DESC
        """, nativeQuery = true)
    List<PriceReport> findPriceReportsNearLocation(
        @Param("productId") String productId,
        @Param("latitude") double latitude,
        @Param("longitude") double longitude,
        @Param("distanceMeters") double distanceMeters
    );
}

