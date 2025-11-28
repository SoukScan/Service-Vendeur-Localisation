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
     * Find all price reports for a product from a specific vendor.
     */
    List<PriceReport> findByProductIdAndVendorId(String productId, String vendorId);

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

