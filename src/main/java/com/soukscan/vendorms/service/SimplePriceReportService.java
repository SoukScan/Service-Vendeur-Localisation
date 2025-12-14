package com.soukscan.vendorms.service;

import com.soukscan.vendorms.client.ProductClient;
import com.soukscan.vendorms.dto.SimpleReportRequestDTO;
import com.soukscan.vendorms.dto.SimpleReportResponseDTO;
import com.soukscan.vendorms.entity.*;
import com.soukscan.vendorms.repository.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SimplePriceReportService {

    private static final Logger log = LoggerFactory.getLogger(SimplePriceReportService.class);
    
    private final VendorRepository vendorRepository;
    private final VendorProductRepository vendorProductRepository;
    private final LocationRepository locationRepository;
    private final PriceReportRepository priceReportRepository;
    private final PriceAvgRepository priceAvgRepository;
    private final ProductClient productClient;
    private final GeometryFactory geometryFactory;
    private final IntelligentPriceAggregationService priceAggregationService;
    private final LocationVerificationService locationVerificationService;

    // Maximum distance allowed for price reporting (in meters)
    private static final double MAX_REPORT_DISTANCE = 50.0;

    @Autowired
    public SimplePriceReportService(VendorRepository vendorRepository, 
                                   VendorProductRepository vendorProductRepository,
                                   LocationRepository locationRepository,
                                   PriceReportRepository priceReportRepository,
                                   PriceAvgRepository priceAvgRepository,
                                   ProductClient productClient,
                                   IntelligentPriceAggregationService priceAggregationService,
                                   LocationVerificationService locationVerificationService) {
        this.vendorRepository = vendorRepository;
        this.vendorProductRepository = vendorProductRepository;
        this.locationRepository = locationRepository;
        this.priceReportRepository = priceReportRepository;
        this.priceAvgRepository = priceAvgRepository;
        this.productClient = productClient;
        this.geometryFactory = new GeometryFactory();
        this.priceAggregationService = priceAggregationService;
        this.locationVerificationService = locationVerificationService;
    }

    /**
     * Main API for price reporting with location validation
     * User must be physically close to the shop location to report prices
     * Updates: vendors, vendor_products, locations, price_reports, price_avg tables
     */
    @Transactional
    public SimpleReportResponseDTO reportPrice(SimpleReportRequestDTO request) {
        log.info("Processing price report - Product: {}, Location: ({}, {}), Price: {}", 
                request.getProductId(), request.getLatitude(), request.getLongitude(), request.getPrice());

        // 1. Validate product exists and is active
        validateProduct(request.getProductId());

        // 2. Check if there's an existing shop at this location
        Optional<Vendor> existingShop = findNearbyShop(request.getLatitude(), request.getLongitude());

        Vendor vendor;
        boolean isNewShop = false;
        boolean isNewProduct = false;
        double distanceFromUser = 0.0;

        if (existingShop.isPresent()) {
            // Use existing shop - validate user is close enough with enhanced verification
            vendor = existingShop.get();
            
            LocationVerificationService.LocationVerificationResult verification = 
                locationVerificationService.verifyUserLocation(
                    request.getLatitude(), request.getLongitude(),
                    vendor.getLatitude(), vendor.getLongitude(),
                    request.getGpsAccuracyMeters()
                );
            
            if (verification.isFailed()) {
                throw new RuntimeException(verification.getMessage());
            }
            
            distanceFromUser = verification.getDistance();
            log.info("Location verified - Using existing shop: {} - {}", 
                    vendor.getShopName(), verification.getMessage());
        } else {
            // Check for suspicious location before creating new shop
            if (locationVerificationService.isSuspiciousLocation(request.getLatitude(), request.getLongitude())) {
                throw new RuntimeException("Cannot create shop at this location - coordinates appear invalid");
            }
            
            // Create new shop at user's location
            vendor = createNewShop(request);
            isNewShop = true;
            distanceFromUser = 0.0; // User is at the exact location
            log.info("Created new shop: {}", vendor.getShopName());
        }

        // 3. Handle vendor product with intelligent price aggregation
        VendorProduct vendorProduct = updateOrCreateVendorProductWithIntelligentPricing(vendor, request);
        isNewProduct = vendorProduct.getId() == null;

        // 4. Create location entry (locations table)
        createOrUpdateLocation(vendor);

        // 5. Create price report entry (price_reports table)
        createPriceReport(vendor, request);

        // 6. Update price average (price_avg table)
        updatePriceAverage(request.getProductId());

        // 7. Add user as a declarant if not already added
        addUserAsDeclarant(vendor, request.getUserId());

        // 8. Build response
        return buildResponse(vendor, vendorProduct, request, isNewShop, isNewProduct, distanceFromUser);
    }

    /**
     * Validate that the product exists and is active
     */
    private void validateProduct(Long productId) {
        try {
            Map<String, Object> product = productClient.getProductById(productId);
            if (product == null) {
                throw new RuntimeException("Product with ID " + productId + " not found");
            }
            
            Boolean isActive = (Boolean) product.get("isActive");
            if (isActive == null || !isActive) {
                throw new RuntimeException("Product with ID " + productId + " is not active");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to validate product: " + e.getMessage());
        }
    }

    /**
     * Find nearby shop within reporting distance using enhanced location service
     */
    private Optional<Vendor> findNearbyShop(Double latitude, Double longitude) {
        return vendorRepository.findAll().stream()
                .filter(vendor -> vendor.getLatitude() != null && vendor.getLongitude() != null)
                .filter(vendor -> {
                    double distance = locationVerificationService.calculateHaversineDistance(
                            latitude, longitude, vendor.getLatitude(), vendor.getLongitude());
                    return distance <= MAX_REPORT_DISTANCE;
                })
                .findFirst();
    }

    /**
     * Create a new shop at the user's location
     */
    private Vendor createNewShop(SimpleReportRequestDTO request) {
        Vendor vendor = new Vendor();
        
        // Use provided shop name or generate one
        String shopName = request.getShopName() != null && !request.getShopName().trim().isEmpty() 
                ? request.getShopName().trim()
                : "Shop at " + String.format("%.4f,%.4f", request.getLatitude(), request.getLongitude());
        
        vendor.setShopName(shopName);
        vendor.setLatitude(request.getLatitude());
        vendor.setLongitude(request.getLongitude());
        vendor.setVendorStatus(VendorStatus.UNVERIFIED);
        vendor.setIsActive(true);
        vendor.setRating(0.0);
        vendor.setTotalReviews(0);
        
        // Initialize declarants list
        List<Long> declarants = new ArrayList<>();
        declarants.add(request.getUserId());
        vendor.setDeclaredByUserIds(declarants);

        return vendorRepository.save(vendor);
    }

    /**
     * Update existing or create new vendor product using intelligent price aggregation
     * This implements Waze-like logic where multiple reports influence the final price
     */
    private VendorProduct updateOrCreateVendorProductWithIntelligentPricing(Vendor vendor, SimpleReportRequestDTO request) {
        Optional<VendorProduct> existingProduct = vendorProductRepository
                .findByVendorIdAndProductId(vendor.getId(), request.getProductId());

        VendorProduct vendorProduct;
        Double intelligentPrice;
        
        if (existingProduct.isPresent()) {
            // Use intelligent price aggregation for existing product
            vendorProduct = existingProduct.get();
            intelligentPrice = priceAggregationService.calculateOptimalPrice(
                vendor.getId(), request.getProductId(), request.getPrice(), request.getUserId());
            
            log.info("Updating existing product price from {} to {} using intelligent aggregation", 
                    vendorProduct.getPrice(), intelligentPrice);
            
            vendorProduct.setPrice(intelligentPrice);
        } else {
            // Create new vendor product with initial price
            vendorProduct = new VendorProduct();
            vendorProduct.setVendorId(vendor.getId());
            vendorProduct.setProductId(request.getProductId());
            vendorProduct.setPrice(request.getPrice()); // First report uses exact price
            vendorProduct.setIsAvailable(true);
            
            log.info("Creating new vendor product with initial price: {}", request.getPrice());
        }

        return vendorProductRepository.save(vendorProduct);
    }

    /**
     * Add user as declarant if not already added
     */
    private void addUserAsDeclarant(Vendor vendor, Long userId) {
        if (!vendor.getDeclaredByUserIds().contains(userId)) {
            vendor.getDeclaredByUserIds().add(userId);
            vendorRepository.save(vendor);
        }
    }

    /**
     * Create or update location entry in locations table
     */
    private void createOrUpdateLocation(Vendor vendor) {
        Optional<Location> existingLocation = locationRepository.findById(vendor.getId().toString());
        
        if (existingLocation.isEmpty()) {
            // Create new location entry
            Location location = new Location();
            location.setVendorId(vendor.getId().toString());
            location.setName(vendor.getShopName());
            location.setAddress(vendor.getShopAddress());
            
            // Create PostGIS Point geometry
            Point point = geometryFactory.createPoint(new Coordinate(vendor.getLongitude(), vendor.getLatitude()));
            point.setSRID(4326); // WGS 84 coordinate system
            location.setGeom(point);
            
            locationRepository.save(location);
            log.info("Created location entry for vendor {}", vendor.getId());
        }
    }

    /**
     * Create price report entry in price_reports table with user tracking
     */
    private void createPriceReport(Vendor vendor, SimpleReportRequestDTO request) {
        try {
            // Check if user already reported today for this product at this vendor
            boolean alreadyReported = priceReportRepository.hasUserReportedTodayForProductAtVendor(
                request.getProductId().toString(), vendor.getId().toString(), request.getUserId());
            
            if (alreadyReported) {
                log.warn("User {} already reported price for product {} at vendor {} today", 
                        request.getUserId(), request.getProductId(), vendor.getId());
                // Don't create duplicate report, but don't fail the operation either
                return;
            }
        } catch (Exception e) {
            log.warn("Could not check for duplicate reports (user tracking may not be fully available): {}", e.getMessage());
            // Continue with creating the report
        }
        
        try {
            PriceReport priceReport = new PriceReport();
            priceReport.setProductId(request.getProductId().toString());
            priceReport.setVendorId(vendor.getId().toString());
            priceReport.setPrice(BigDecimal.valueOf(request.getPrice()));
            priceReport.setReportedAt(OffsetDateTime.now());
            
            // Try to set user tracking fields if available
            try {
                priceReport.setReportedByUserId(request.getUserId());
                priceReport.setReportedDate(java.time.LocalDate.now());
            } catch (Exception userTrackingError) {
                log.info("User tracking fields not available yet, continuing without them: {}", userTrackingError.getMessage());
            }
            
            priceReportRepository.save(priceReport);
            log.info("Created price report for product {} at vendor {} by user {}", 
                    request.getProductId(), vendor.getId(), request.getUserId());
        } catch (Exception e) {
            log.error("Failed to create price report: {}", e.getMessage());
            throw new RuntimeException("Could not save price report: " + e.getMessage());
        }
    }

    /**
     * Update price average in price_avg table
     */
    private void updatePriceAverage(Long productId) {
        try {
            // Calculate new average from all price reports for this product
            List<PriceReport> allReports = priceReportRepository.findByProductId(productId.toString());
            
            if (!allReports.isEmpty()) {
                BigDecimal sum = allReports.stream()
                    .map(PriceReport::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                BigDecimal average = sum.divide(BigDecimal.valueOf(allReports.size()), 2, RoundingMode.HALF_UP);
                
                // Update or create price average entry
                Optional<PriceAvg> existingAvg = priceAvgRepository.findById(productId.toString());
                
                PriceAvg priceAvg;
                if (existingAvg.isPresent()) {
                    priceAvg = existingAvg.get();
                    priceAvg.setAveragePrice(average);
                    priceAvg.setReportCount(allReports.size());
                    priceAvg.setLastUpdated(OffsetDateTime.now());
                } else {
                    priceAvg = new PriceAvg();
                    priceAvg.setProductId(productId.toString());
                    priceAvg.setAveragePrice(average);
                    priceAvg.setReportCount(allReports.size());
                    priceAvg.setLastUpdated(OffsetDateTime.now());
                }
                
                priceAvgRepository.save(priceAvg);
                log.info("Updated price average for product {}: {} (from {} reports)", 
                        productId, average, allReports.size());
            }
        } catch (Exception e) {
            log.warn("Failed to update price average for product {}: {}", productId, e.getMessage());
            // Don't fail the entire operation if average calculation fails
        }
    }

    /**
     * Build the response DTO with intelligent pricing information
     */
    private SimpleReportResponseDTO buildResponse(Vendor vendor, VendorProduct vendorProduct, 
            SimpleReportRequestDTO request, boolean isNewShop, boolean isNewProduct, double distanceFromUser) {
        
        String message;
        if (isNewShop) {
            message = "New shop created and price reported successfully";
        } else if (isNewProduct) {
            message = "Product added to existing shop";
        } else {
            // Get pricing recommendation for existing product
            List<PriceReport> recentReports = priceReportRepository.findByProductIdAndVendorId(
                request.getProductId().toString(), vendor.getId().toString());
            String priceRecommendation = priceAggregationService.getPriceRecommendationMessage(recentReports);
            message = "Price updated using intelligent aggregation - " + priceRecommendation;
        }

        return new SimpleReportResponseDTO(
                vendor.getId(),
                vendor.getShopName(),
                vendor.getLatitude(),
                vendor.getLongitude(),
                request.getProductId(),
                vendorProduct.getPrice(), // This is now the intelligently calculated price
                isNewShop,
                isNewProduct,
                message,
                distanceFromUser
        );
    }

    /**
     * Modify an existing price report
     * Only allows users to modify their own reports within 24 hours
     */
    @Transactional
    public SimpleReportResponseDTO modifyPriceReport(Long reportId, Double newPrice, Long userId) {
        log.info("Modifying price report {} to {} by user {}", reportId, newPrice, userId);

        // Find the price report
        Optional<PriceReport> reportOpt = priceReportRepository.findById(reportId);
        if (reportOpt.isEmpty()) {
            throw new RuntimeException("Price report not found with ID: " + reportId);
        }

        PriceReport report = reportOpt.get();

        // Verify user ownership
        if (!report.getReportedByUserId().equals(userId)) {
            throw new RuntimeException("You can only modify your own price reports");
        }

        // Check if modification is still allowed (24 hour window)
        OffsetDateTime cutoff = OffsetDateTime.now().minusHours(24);
        if (report.getReportedAt().isBefore(cutoff)) {
            throw new RuntimeException("Price reports can only be modified within 24 hours of reporting");
        }

        // Validate new price
        if (newPrice == null || newPrice <= 0) {
            throw new RuntimeException("Price must be greater than 0");
        }

        // Update the price report
        BigDecimal oldPrice = report.getPrice();
        report.setPrice(BigDecimal.valueOf(newPrice));
        priceReportRepository.save(report);

        // Find the vendor and vendor product
        Optional<Vendor> vendorOpt = vendorRepository.findById(Long.valueOf(report.getVendorId()));
        if (vendorOpt.isEmpty()) {
            throw new RuntimeException("Vendor not found for this report");
        }

        Vendor vendor = vendorOpt.get();

        // Recalculate intelligent price for the vendor product
        Optional<VendorProduct> vendorProductOpt = vendorProductRepository
                .findByVendorIdAndProductId(vendor.getId(), Long.valueOf(report.getProductId()));
        
        if (vendorProductOpt.isPresent()) {
            VendorProduct vendorProduct = vendorProductOpt.get();
            
            // Use intelligent price aggregation to recalculate
            Double intelligentPrice = priceAggregationService.calculateOptimalPrice(
                vendor.getId(), Long.valueOf(report.getProductId()), newPrice, userId);
            
            vendorProduct.setPrice(intelligentPrice);
            vendorProductRepository.save(vendorProduct);
            
            log.info("Updated vendor product price from {} to {} after report modification", 
                    oldPrice, intelligentPrice);
        }

        // Update price averages
        updatePriceAverage(Long.valueOf(report.getProductId()));

        // Build response
        String message = String.format("Price report modified from %.2f to %.2f", oldPrice.doubleValue(), newPrice);
        
        return new SimpleReportResponseDTO(
                vendor.getId(),
                vendor.getShopName(),
                vendor.getLatitude(),
                vendor.getLongitude(),
                Long.valueOf(report.getProductId()),
                newPrice,
                false, // not a new shop
                false, // not a new product
                message,
                0.0 // distance not relevant for modifications
        );
    }

    /**
     * Undo (delete) a price report
     * Only allows users to undo their own reports within 24 hours
     */
    @Transactional
    public boolean undoPriceReport(Long reportId, Long userId) {
        log.info("Attempting to undo price report {} by user {}", reportId, userId);

        // Find the price report
        Optional<PriceReport> reportOpt = priceReportRepository.findById(reportId);
        if (reportOpt.isEmpty()) {
            throw new RuntimeException("Price report not found with ID: " + reportId);
        }

        PriceReport report = reportOpt.get();

        // Verify user ownership
        if (!report.getReportedByUserId().equals(userId)) {
            throw new RuntimeException("You can only undo your own price reports");
        }

        // Check if undo is still allowed (24 hour window)
        OffsetDateTime cutoff = OffsetDateTime.now().minusHours(24);
        if (report.getReportedAt().isBefore(cutoff)) {
            throw new RuntimeException("Price reports can only be undone within 24 hours of reporting");
        }

        try {
            // Store info before deletion
            String vendorId = report.getVendorId();
            String productId = report.getProductId();
            
            // Delete the price report
            priceReportRepository.delete(report);
            
            // Recalculate vendor product price without this report
            Optional<Vendor> vendorOpt = vendorRepository.findById(Long.valueOf(vendorId));
            if (vendorOpt.isPresent()) {
                Vendor vendor = vendorOpt.get();
                Optional<VendorProduct> vendorProductOpt = vendorProductRepository
                        .findByVendorIdAndProductId(vendor.getId(), Long.valueOf(productId));
                
                if (vendorProductOpt.isPresent()) {
                    VendorProduct vendorProduct = vendorProductOpt.get();
                    
                    // Get remaining reports for this vendor-product
                    List<PriceReport> remainingReports = priceReportRepository
                            .findByProductIdAndVendorId(productId, vendorId);
                    
                    if (!remainingReports.isEmpty()) {
                        // Recalculate price based on remaining reports
                        Double newPrice = remainingReports.stream()
                                .mapToDouble(pr -> pr.getPrice().doubleValue())
                                .average()
                                .orElse(vendorProduct.getPrice());
                        
                        vendorProduct.setPrice(newPrice);
                        vendorProductRepository.save(vendorProduct);
                        
                        log.info("Updated vendor product price to {} after report deletion", newPrice);
                    } else {
                        // No more reports, consider removing the product or keeping last price
                        log.info("No remaining reports for product {} at vendor {}", productId, vendorId);
                        // Keep the product but could mark as needing price verification
                    }
                }
            }

            // Update price averages
            updatePriceAverage(Long.valueOf(productId));
            
            log.info("Successfully undone price report {} by user {}", reportId, userId);
            return true;
            
        } catch (Exception e) {
            log.error("Failed to undo price report {}: {}", reportId, e.getMessage());
            throw new RuntimeException("Failed to undo price report: " + e.getMessage());
        }
    }

    /**
     * Get user's price report history
     */
    public List<SimpleReportResponseDTO> getUserReports(Long userId, Integer limit) {
        log.info("Fetching reports for user {} with limit {}", userId, limit);
        
        try {
            OffsetDateTime thirtyDaysAgo = OffsetDateTime.now().minusDays(30);
            List<PriceReport> reports = priceReportRepository.findRecentReportsByUser(userId, thirtyDaysAgo);
            
            // Sort by most recent first and apply limit
            List<PriceReport> limitedReports = reports.stream()
                    .sorted((r1, r2) -> r2.getReportedAt().compareTo(r1.getReportedAt()))
                    .limit(limit)
                    .toList();
            
            List<SimpleReportResponseDTO> responses = new ArrayList<>();
            
            for (PriceReport report : limitedReports) {
                try {
                    Optional<Vendor> vendorOpt = vendorRepository.findById(Long.valueOf(report.getVendorId()));
                    if (vendorOpt.isPresent()) {
                        Vendor vendor = vendorOpt.get();
                        
                        SimpleReportResponseDTO response = new SimpleReportResponseDTO(
                                vendor.getId(),
                                vendor.getShopName(),
                                vendor.getLatitude(),
                                vendor.getLongitude(),
                                Long.valueOf(report.getProductId()),
                                report.getPrice().doubleValue(),
                                false, // historical data
                                false, // historical data
                                "Reported on " + report.getReportedAt().toLocalDate(),
                                0.0
                        );
                        responses.add(response);
                    }
                } catch (Exception e) {
                    log.warn("Could not process report {}: {}", report.getReportId(), e.getMessage());
                    // Continue with other reports
                }
            }
            
            log.info("Returning {} reports for user {}", responses.size(), userId);
            return responses;
            
        } catch (Exception e) {
            log.error("Error fetching user reports: {}", e.getMessage());
            throw new RuntimeException("Could not fetch user reports: " + e.getMessage());
        }
    }

    /**
     * Check if user can modify/undo a specific report
     */
    public boolean canUserModifyReport(Long reportId, Long userId) {
        try {
            Optional<PriceReport> reportOpt = priceReportRepository.findById(reportId);
            if (reportOpt.isEmpty()) {
                return false;
            }

            PriceReport report = reportOpt.get();

            // Check ownership
            if (!report.getReportedByUserId().equals(userId)) {
                return false;
            }

            // Check time window (24 hours)
            OffsetDateTime cutoff = OffsetDateTime.now().minusHours(24);
            return report.getReportedAt().isAfter(cutoff);

        } catch (Exception e) {
            log.warn("Error checking modification rights for report {}: {}", reportId, e.getMessage());
            return false;
        }
    }

}