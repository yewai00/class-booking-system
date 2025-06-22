package com.yewai.classBookingSystem.features.pkg.domain.service;

import com.yewai.classBookingSystem.exception.BaseException;
import com.yewai.classBookingSystem.exception.PaymentException;
import com.yewai.classBookingSystem.features.payment.domain.service.PaymentService;
import com.yewai.classBookingSystem.features.pkg.api.request.PurchasePackageRequest;
import com.yewai.classBookingSystem.features.pkg.api.response.PackageResponse;
import com.yewai.classBookingSystem.features.pkg.api.response.UserPackageResponse;
import com.yewai.classBookingSystem.features.pkg.domain.entity.UserPackage;
import com.yewai.classBookingSystem.features.pkg.domain.enums.UserPackageStatus;
import com.yewai.classBookingSystem.features.pkg.domain.repo.PackageRepository;
import com.yewai.classBookingSystem.features.pkg.domain.repo.UserPackageRepository;
import com.yewai.classBookingSystem.features.user.domain.repo.UserRepository;
import com.yewai.classBookingSystem.features.user.domain.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@AllArgsConstructor
public class PackageService {

    private final PackageRepository packageRepository;
    private final UserPackageRepository userPackageRepository;
    private final PaymentService paymentService;
    private final UserService userService;

    public List<PackageResponse> getAvailablePackages(String countryCode) {
        var packages = packageRepository.findByCountryCodeAndIsActiveTrue(countryCode);
        return packages.stream().map(PackageResponse::from).toList();
    }

    @Transactional
    public UserPackageResponse purchasePackage(PurchasePackageRequest request) {
        var user = userService.getLoginUser();
        var pkg = packageRepository.findById(request.packageId())
                .orElseThrow(() -> new NoSuchElementException("Package Not found!"));
        if (!pkg.getIsActive()) {
            throw new BaseException("Selected package is not active.");
        }
        Map<String, String> cardDetailsMock = Map.of(
                "number",  request.cardNumber(),
                "cvv", request.cvv(),
                "expiryDate", request.expiryDate()
        );
        if (!paymentService.addPaymentCard(cardDetailsMock)) {
            throw new PaymentException("Failed to add payment card. Please try again.");
        }

        String paymentReference = UUID.randomUUID().toString();
        if (!paymentService.chargePayment(pkg.getPrice(), paymentReference)) {
            throw new PaymentException("Payment charge failed for package: " + pkg.getName());
        }
        var newUserPackage = new UserPackage();
        newUserPackage.setUser(user);
        newUserPackage.setPackageType(pkg);
        newUserPackage.setTotalCredits(pkg.getCredits());
        newUserPackage.setCurrentCredits(pkg.getCredits());
        newUserPackage.setPurchaseDate(LocalDateTime.now());
        newUserPackage.setExpiryDate(LocalDateTime.now().plusDays(pkg.getValidityDays()));
        newUserPackage.setStatus(UserPackageStatus.ACTIVE);
        newUserPackage.setPaymentRef(paymentReference);
        newUserPackage.setCountryCode(pkg.getCountryCode());
        var savedUserPackage = userPackageRepository.save(newUserPackage);

        return UserPackageResponse.from(savedUserPackage);
    }

    public List<UserPackageResponse> getUserPackages() {
        var user = userService.getLoginUser();
        var userPackages = userPackageRepository.findByUserId(user.getId());
        return userPackages.stream().map(UserPackageResponse::from).toList();
    }


}
