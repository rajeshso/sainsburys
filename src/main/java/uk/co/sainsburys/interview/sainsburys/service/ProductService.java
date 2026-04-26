package uk.co.sainsburys.interview.sainsburys.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.sainsburys.interview.sainsburys.client.ProductApiClient;
import uk.co.sainsburys.interview.sainsburys.model.Product;
import uk.co.sainsburys.interview.sainsburys.model.ProductPriceResponse;
import uk.co.sainsburys.interview.sainsburys.model.ProductResponse;
import uk.co.sainsburys.interview.sainsburys.repository.ProductRepository;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductService {

    private final ProductApiClient productApiClient;

    private final ProductRepository productRepository;

    public ProductService(ProductApiClient productApiClient, ProductRepository productRepository) {
        this.productApiClient = productApiClient;
        this.productRepository = productRepository;
    }

    //fetch both → merge by product_uid → upsert
    @Transactional
    public void ingestProducts() throws MalformedURLException {
        log.info("Ingestion started");

        List<ProductResponse> products = productApiClient.fetchProducts();
        Map<String, Double> priceByUid = productApiClient.fetchPrices().stream()
                .collect(Collectors.toMap(ProductPriceResponse::product_uid, ProductPriceResponse::unit_price, (a, b) -> b));

        log.info("Fetched {} products and {} prices", products.size(), priceByUid.size());

        List<Product> toSave = products.stream()
                .map(p -> toEntity(p, priceByUid.get(p.product_uid())))
                .toList();

        productRepository.saveAll(toSave);
        log.info("Ingestion complete, persisted {} records", toSave.size());
    }

    private Product toEntity(ProductResponse p, Double unitPrice) {
        return Product.builder()
                .productUid(p.product_uid())
                .name(p.name())
                .fullUrl(p.fullUrl())
                .unitPrice(unitPrice) // null if no price — handled per spec
                .productType(p.product_type())
                .build();
    }

}
