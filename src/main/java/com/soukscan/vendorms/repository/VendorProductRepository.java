package com.soukscan.vendorms.repository;

import com.soukscan.vendorms.entity.VendorProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorProductRepository extends JpaRepository<VendorProduct, Long> {
    List<VendorProduct> findByVendorId(Long vendorId);
    List<VendorProduct> findByVendorIdAndIsAvailableTrue(Long vendorId);
    Optional<VendorProduct> findByVendorIdAndProductId(Long vendorId, Long productId);
    boolean existsByVendorIdAndProductId(Long vendorId, Long productId);
}

