package com.dmb.product.product;

import com.dmb.product.exception.ProductPurchaseException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;
    private final ProductMapper mapper;

    public Integer createProduct(
            ProductRequest request
    ) {
        var product = mapper.toProduct(request);
        return repository.save(product).getId();
    }

    public ProductResponse findById(Integer id) {
        return repository.findById(id)
                .map(mapper::toProductResponse)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID:: " + id));
    }

    public List<ProductResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toProductResponse)
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = ProductPurchaseException.class)
    public List<ProductPurchaseResponse> purchaseProducts(
            List<ProductPurchaseRequest> request
    ) {
        var productIds = request
                .stream()
                .map(ProductPurchaseRequest::productId)
                .toList();

        // Verrouillage pessimiste pour éviter les races conditions
        var storedProducts = repository.findAllByIdInOrderById(productIds);

        if (productIds.size() != storedProducts.size()) {
            throw new ProductPurchaseException("One or more products does not exist");
        }

        // Map pour un appariement fiable, sans dépendre de l'ordre du tri
        var productById = storedProducts.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        var purchasedProducts = new ArrayList<ProductPurchaseResponse>();

        for (ProductPurchaseRequest productRequest : request) {
            var product = productById.get(productRequest.productId());

            if (product.getAvailableQuantity() < productRequest.quantity()) {
                throw new ProductPurchaseException(
                        "Insufficient stock quantity for product with ID:: " + productRequest.productId()
                );
            }

            var newAvailableQuantity = product.getAvailableQuantity() - productRequest.quantity();
            product.setAvailableQuantity(newAvailableQuantity);

            purchasedProducts.add(mapper.toproductPurchaseResponse(product, productRequest.quantity()));
        }

        // Sauvegarde groupée en une seule requête (batch) à la fin
        repository.saveAll(productById.values());

        return purchasedProducts;
    }

}
