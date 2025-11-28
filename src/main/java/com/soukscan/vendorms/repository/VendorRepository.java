package com.soukscan.vendorms.repository;

import com.soukscan.vendorms.entity.Vendor;
import com.soukscan.vendorms.entity.VendorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {

    Optional<Vendor> findByEmail(String email);
    Optional<Vendor> findByUserId(Long userId);
    List<Vendor> findByIsActive(Boolean isActive);
    List<Vendor> findByCity(String city);
    List<Vendor> findByCountry(String country);
    List<Vendor> findByShopNameContainingIgnoreCase(String shopName);
    List<Vendor> findByVendorStatus(VendorStatus vendorStatus);

    @Query("SELECT v FROM Vendor v JOIN v.declaredByUserIds d WHERE d = :userId")
    List<Vendor> findVendorsDeclaredByUser(@Param("userId") Long userId);

    @Query("SELECT v FROM Vendor v WHERE LOWER(v.shopName) = LOWER(:shopName) " +
           "AND v.latitude BETWEEN :lat - 0.001 AND :lat + 0.001 " +
           "AND v.longitude BETWEEN :lng - 0.001 AND :lng + 0.001")
    List<Vendor> findSimilarShops(@Param("shopName") String shopName,
                                   @Param("lat") Double latitude,
                                   @Param("lng") Double longitude);

    boolean existsByEmail(String email);
    boolean existsByUserId(Long userId);
}

