package com.yewai.classBookingSystem.features.pkg.api;

import com.yewai.classBookingSystem.common.dto.ApiResponse;
import com.yewai.classBookingSystem.features.pkg.api.request.PurchasePackageRequest;
import com.yewai.classBookingSystem.features.pkg.api.response.PackageResponse;
import com.yewai.classBookingSystem.features.pkg.api.response.UserPackageResponse;
import com.yewai.classBookingSystem.features.pkg.domain.service.PackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@Tag(name = "Package", description = "Package Api")
@RequestMapping("${api.version}")
public class PackageApiController {

    private final PackageService packageService;

    @GetMapping("/packages/available")
    @Operation(summary = "get available packages Api")
    public ResponseEntity<ApiResponse<List<PackageResponse>>> getAvailablePackages(@RequestParam("country") String country) {
        var packages = packageService.getAvailablePackages(country);
        return ResponseEntity.ok(ApiResponse.success(packages));
    }

    @PostMapping("/packages/purchase")
    @Operation(summary = "Purchase Package Api")
    public ResponseEntity<ApiResponse<UserPackageResponse>> purchasePackage(@Valid @RequestBody PurchasePackageRequest request) {
        var purchasePackage = packageService.purchasePackage(request);
        return ResponseEntity.ok(ApiResponse.success(purchasePackage));
    }

    @GetMapping("/packages/my-packages")
    @Operation(summary = "Purchase Packages Api")
    public ResponseEntity<ApiResponse<List<UserPackageResponse>>> getPurchasePackages() {
        var packages = packageService.getUserPackages();
        return ResponseEntity.ok(ApiResponse.success(packages));
    }

}
