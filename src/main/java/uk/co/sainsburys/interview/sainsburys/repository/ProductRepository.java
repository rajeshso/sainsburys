package uk.co.sainsburys.interview.sainsburys.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.co.sainsburys.interview.sainsburys.model.Product;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByProductUid(String product_uid);
}
