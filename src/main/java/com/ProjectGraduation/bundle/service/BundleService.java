package com.ProjectGraduation.bundle.service;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.bundle.dto.BundleProductDTO;
import com.ProjectGraduation.bundle.dto.BundleRequest;
import com.ProjectGraduation.bundle.dto.BundleResponse;
import com.ProjectGraduation.bundle.entity.Bundle;
import com.ProjectGraduation.bundle.entity.BundleProduct;
import com.ProjectGraduation.bundle.repository.BundleProductRepository;
import com.ProjectGraduation.bundle.repository.BundleRepository;
import com.ProjectGraduation.product.entity.Product;
import com.ProjectGraduation.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BundleService {

    private final BundleRepository bundleRepository;
    private final BundleProductRepository bundleProductRepository;
    private final ProductService productService;

    @Transactional
    public BundleResponse createBundle(BundleRequest request, User merchant) {
        Set<Long> productIds = new HashSet<>();
        for (var item : request.getProducts()) {
            if (!productIds.add(item.getProductId())) {
                throw new RuntimeException("Duplicate product in bundle");
            }
        }
        Bundle bundle = new Bundle();
        bundle.setName(request.getName());
        bundle.setDescription(request.getDescription());
        bundle.setBundlePrice(request.getBundlePrice());
        bundle.setMerchant(merchant);
        bundle.setActive(true);

        Bundle savedBundle = bundleRepository.save(bundle);

        List<BundleProduct> bundleProducts = request.getProducts().stream().map(item -> {
            Product product = productService.getById(item.getProductId());
            return new BundleProduct(savedBundle, product, item.getQuantity());
        }).collect(Collectors.toList());

        bundleProductRepository.saveAll(bundleProducts);
        savedBundle.setProducts(bundleProducts);

        return mapToResponse(savedBundle);
    }
    public Bundle getById(Long id) {
        return bundleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bundle not found"));
    }
    public List<BundleResponse> getAllBundles() {
        return bundleRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<BundleResponse> getBundlesForMerchant(String username) {
        return bundleRepository.findByMerchant_Username(username).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public BundleResponse getBundleById(Long id) {
        Bundle bundle = bundleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bundle not found"));
        return mapToResponse(bundle);
    }

    @Transactional
    public void toggleBundleStatus(Long id, boolean isActive) {
        Bundle bundle = bundleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bundle not found"));
        bundle.setActive(isActive);
        bundleRepository.save(bundle);
    }

    @Transactional
    public void deleteBundle(Long id, User merchant) {
        Bundle bundle = bundleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bundle not found"));
        if (!bundle.getMerchant().getId().equals(merchant.getId())) {
            throw new RuntimeException("You are not authorized to delete this bundle");
        }
        bundleRepository.delete(bundle);
    }

    private BundleResponse mapToResponse(Bundle bundle) {
        List<BundleProductDTO> productDTOs = bundle.getProducts().stream().map(bp ->
                BundleProductDTO.builder()
                        .productId(bp.getProduct().getId())
                        .productName(bp.getProduct().getName())
                        .quantity(bp.getQuantity())
                        .build()
        ).collect(Collectors.toList());

        return BundleResponse.builder()
                .id(bundle.getId())
                .name(bundle.getName())
                .description(bundle.getDescription())
                .bundlePrice(bundle.getBundlePrice())
                .active(bundle.isActive())
                .createdAt(bundle.getCreatedAt())
                .products(productDTOs)
                .build();
    }
}
