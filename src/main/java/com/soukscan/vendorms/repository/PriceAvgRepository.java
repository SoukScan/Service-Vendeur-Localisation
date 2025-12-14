package com.soukscan.vendorms.repository;

import com.soukscan.vendorms.entity.PriceAvg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for PriceAvg entities.
 */
@Repository
public interface PriceAvgRepository extends JpaRepository<PriceAvg, String> {

    /**
     * Find average price for a specific product.
     */
    Optional<PriceAvg> findByProductId(String productId);

    /**
     * Find all products with average price within a range.
     */
    @Query("SELECT pa FROM PriceAvg pa WHERE pa.averagePrice BETWEEN :minPrice AND :maxPrice ORDER BY pa.averagePrice")
    List<PriceAvg> findByPriceRange(
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice
    );

    /**
     * Find top N products by lowest average price.
     */
    @Query("SELECT pa FROM PriceAvg pa ORDER BY pa.averagePrice ASC LIMIT :limit")
    List<PriceAvg> findCheapestProducts(@Param("limit") int limit);

    /**
     * Find products updated after a certain date.
     */
    @Query("SELECT pa FROM PriceAvg pa WHERE pa.lastUpdated >= :since ORDER BY pa.lastUpdated DESC")
    List<PriceAvg> findRecentlyUpdated(@Param("since") java.time.OffsetDateTime since);
}

